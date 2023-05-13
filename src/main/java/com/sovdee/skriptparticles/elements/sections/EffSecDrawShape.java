package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Name("Draw Shape")
@Description({
        "Draws the given shapes at the given locations. The shapes will be drawn in the order they are given.",
        "The code inside the draw shape section will be executed before drawing begins. You can use `event-shape` or `drawn shape` to get the shape being drawn. " +
        "Modifying this shape affects the end result, but it does not modify the original shape! This means you can set particle data, " +
        "or change the shape's location, rotation, or scale, without affecting the shape the next time it's drawn.",
        "**Note that this means the section is run once for each shape!** This is the same way the spawn section works in Skript. This is subject to change if people find it cumbersome.",
        "",
        "By default, this effect will run asynchronously, meaning it will do all the calculation and drawing on a separate thread, instead of blocking your server's main thread. " +
        "This is much better if you want to draw a lot of shapes at once, or if you want to draw large and complex shapes.",
        "Be aware that this changes the behavior of the section slightly. All the section code will first be executed synchronously, " +
        "and then the drawing will be done asynchronously. This means that the time the shape appears may be slightly delayed compared to the completion of the section code.",
        "Additionally, the code immediately after the draw shape section will be executed immediately, often before the drawing is finished. If you stumble across issues with this, " +
        "please report them on the Skript-Particles GitHub page and use the synchronous option instead.",
        "",
        "Drawing a shape for a duration is async only."
})
@Examples({
        "draw a sphere with radius 1 at player's location",
        "draw (a sphere with radius 1 and a cube with radius 1) at player's location for (all players in radius 10 of player)",
        "synchronously draw a sphere with radius 1 at player's location",
        "",
        "draw {_shape} at player's location:",
            "\tset event-shape's particle to dust using dustOption(red, 1)",
        "",
        "synchronously draw (a sphere with radius 1 and a cube with radius 1) at player's location:",
            "\tset event-shape's radius to 2",
})
@Since("1.0.0")
public class EffSecDrawShape extends EffectSection {
    public static class DrawEvent extends Event {
        private final Shape shape;
        public DrawEvent(Shape shape) {
            this.shape = shape;
        }

        public Shape getShape() {
            return shape;
        }

        @Override
        @NotNull
        public HandlerList getHandlers() {
            throw new IllegalStateException();
        }
    }


    static {
        Skript.registerSection(EffSecDrawShape.class,
                "[sync:sync[hronously]] draw [shape[s]] %shapes% [%-directions% %-locations/entities%] [to %-players%]",
                "draw [shape[s]] %shapes% [%-directions% %-locations/entities%] [to %-players%] (duration:for) [duration] %timespan% [with (delay|refresh [rate]) [of] %-timespan%]"
        );
        EventValues.registerEventValue(EffSecDrawShape.DrawEvent.class, Shape.class, new Getter<>() {
            @Override
            public Shape get(EffSecDrawShape.DrawEvent event) {
                return event.getShape();
            }
        }, EventValues.TIME_NOW);
    }

    private Expression<?> locations;
    private Expression<Direction> directions;
    private Expression<Shape> shapes;
    private Expression<Player> players;
    private Expression<Timespan> duration;
    private Expression<Timespan> delay;

    public static final Timespan ONE_TICK = Timespan.fromTicks_i(1);

    @Nullable
    private Trigger trigger;
    private boolean sync;
    private boolean useShapeLocation;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        shapes = (Expression<Shape>) expressions[0];

        if (expressions[2] != null) {
            if (expressions[1] != null)
                directions = (Expression<Direction>) expressions[1];
            locations = expressions[2];
        } else {
            useShapeLocation = true;
        }
        players = (Expression<Player>) expressions[3];

        if (sectionNode != null) {
            AtomicBoolean delayed = new AtomicBoolean(false);
            Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
            trigger = loadCode(sectionNode, "draw", afterLoading, EffSecDrawShape.DrawEvent.class);
            if (delayed.get()) {
                Skript.error("Delays can't be used within a Draw Shape Effect Section");
                return false;
            }
        }

        if (parseResult.hasTag("duration")) {
            duration = (Expression<Timespan>) expressions[4];
            delay = (Expression<Timespan>) expressions[5];
        }

        sync = parseResult.hasTag("sync");

        return true;
    }
    @Override
    @Nullable
    protected TriggerItem walk(Event event) {
        debug(event, true);

        Delay.addDelayedEvent(event); // Mark this event as delayed

        Collection<Player> recipients = new ArrayList<>();
        if (players != null) {
            recipients.addAll(List.of(players.getArray(event)));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        Object localVars = Variables.copyLocalVariables(event);

        Consumer<Shape> consumer;
        if (trigger != null) {
            consumer = shape -> {
                DrawEvent drawEvent = new EffSecDrawShape.DrawEvent(shape);
                Variables.setLocalVariables(drawEvent, localVars);
                TriggerItem.walk(trigger, drawEvent);
                Variables.setLocalVariables(event, Variables.copyLocalVariables(drawEvent));
                Variables.removeLocals(drawEvent);
            };
        } else {
            consumer = null;
        }

        // Figure out what locations to draw at, or what entities to follow
        List<DynamicLocation> locations = new ArrayList<>();
        Direction direction = null;
        if (!useShapeLocation) {
            if (directions != null)
                direction = directions.getSingle(event);
            for (Object location : this.locations.getArray(event)) {
                if (location instanceof Entity) {
                    locations.add(new DynamicLocation((Entity) location, direction));
                } else if (location instanceof Location) {
                    locations.add(new DynamicLocation((Location) location, direction));
                }
            }
        } else {
            // blank value means use shape location
            locations.add(new DynamicLocation());
        }

        if (sync) {
            executeSync(event, locations, consumer, recipients);
        } else {
            // Clone shapes and run Consumer before going async
            // We can't guarantee that the consumer will be thread-safe, so we need do this before going async
            List<Shape> preppedShapes = new ArrayList<>();
            Shape preppedShape;
            for (Shape shape : shapes.getArray(event)) {
                preppedShape = shape.clone();
                if (consumer != null)
                    consumer.accept(preppedShape);
                preppedShapes.add(preppedShape);
            }

            if (preppedShapes.isEmpty()) return getNext();

            long period, iterations;
            if (duration == null) {
                period = 1;
                iterations = 1;
            } else {
                Timespan delay = (this.delay == null ? ONE_TICK : this.delay.getSingle(event));
                Timespan duration = this.duration.getSingle(event);
                if (delay == null || duration == null) return getNext();

                period = Math.max(delay.getTicks_i(),1);
                iterations = Math.max(duration.getTicks_i() / period, 1);
            }
            AtomicLong currentIteration = new AtomicLong(0);
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    executeAsync(locations, preppedShapes, recipients);
                    if (currentIteration.incrementAndGet() >= iterations) {
                        this.cancel();
                    }
                }
            };
            runnable.runTaskTimerAsynchronously(Skript.getInstance(), 0, period);

        }
        return getNext();
    }

    private void executeSync(Event event, Collection<DynamicLocation> locations, Consumer<Shape> consumer, Collection<Player> recipients){
        Shape shapeCopy;
        Location location;
        for (DynamicLocation dynamicLocation : locations) {
            location = dynamicLocation.getLocation();
            for (Shape shape : shapes.getArray(event)) {
                if (consumer != null) {
                    // copy the shape so that it can be modified by the consumer without affecting the original
                    shapeCopy = shape.clone();
                    shapeCopy.draw(location, consumer, recipients);
                } else {
                    shape.draw(location, null, null, recipients);
                }
            }
        }
    }

    private void executeAsync(Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        Location location;
        for (DynamicLocation dynamicLocation : locations) {
            location = dynamicLocation.getLocation();
            for (Shape shape : shapes) {
                shape.draw(location, null, null, recipients);
            }
        }
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean b) {
        return "draw shape " + shapes.toString(event, b) + " at " + locations.toString(event, b) + " for " + (players == null ? "all players" : players.toString(event, b));
    }
}
