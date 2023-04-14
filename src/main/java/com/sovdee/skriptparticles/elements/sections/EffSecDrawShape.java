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
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.SkriptParticle;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Draw Shape")
@Description({
        "Draws the given shapes at the given locations. The shapes will be drawn in the order they are given.",
        "The code inside the draw shape section will be executed before drawing begins, and will not affect the original shapes.",
        "This means you can set particle data, or change the shape's location, rotation, or scale, without affecting the shape the next time it's drawn.",
        "",
        "By default, this effect will run synchronously, meaning it will block the main thread until it's finished drawing. For most cases, this is fine. " +
        "However, if you want to draw a lot of shapes at once, or if you want to draw large and complex shapes, you should consider using the async option.",
        "Be aware that this changes the behavior of the section slightly. All the section code will first be executed synchronously, " +
        "and then the drawing will be done asynchronously. This means that the time the shape appears may be slightly delayed compared to the completion of the section code.",
        "Additionally, the code immediately after the draw shape section will be executed immediately, often before the drawing is finished. If you stumble across issues with this, " +
        "please report them on the Skript-Particles GitHub page."
})
@Examples({
        "draw a sphere with radius 1 at player's location",
        "draw (a sphere with radius 1 and a cube with radius 1) at player's location for (all players in radius 10 of player)",
        "asynchronously draw a sphere with radius 1 at player's location",
        "",
        "draw {_shape} at player's location:",
            "\tset event-shape's particle to dust using dustOption(red, 1)",
        "",
        "asynchronously draw (a sphere with radius 1 and a cube with radius 1) at player's location:",
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
                "[async:async[hronously]] draw [shape[s]] %shapes% [%-directions% %-locations%] [for %-players%]"
        );
        EventValues.registerEventValue(EffSecDrawShape.DrawEvent.class, Shape.class, new Getter<>() {
            @Override
            public Shape get(EffSecDrawShape.DrawEvent event) {
                return event.getShape();
            }
        }, EventValues.TIME_NOW);
    }

    private Expression<Location> locations;
    private Expression<Shape> shapes;
    private Expression<Player> players;

    @Nullable
    private Trigger trigger;
    private boolean async;
    private boolean useShapeLocation;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        shapes = (Expression<Shape>) expressions[0];
        if (expressions[1] != null || expressions[2] != null) {
            locations = Direction.combine((Expression<? extends Direction>) expressions[1], (Expression<? extends Location>) expressions[2]);
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

        async = parseResult.hasTag("async");

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

        if (!async) {
            long now = System.nanoTime();
            SkriptParticle.info("Drawing shape synchronously: " + (now / 1000000) + "ms");
            executeSync(event, consumer, recipients);
            SkriptParticle.info("Finished drawing shape synchronously: " + (System.nanoTime() - now) / 1000000.0 + "ms");
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

            Bukkit.getScheduler().runTaskAsynchronously(Skript.getInstance(), () -> {
                long now = System.nanoTime();
                SkriptParticle.info("Drawing shape asynchronously: " + (now / 1000000) + "ms");
                if (useShapeLocation) {
                    executeAsync(new Location[]{null}, preppedShapes, recipients);
                } else {
                    executeAsync(locations.getArray(event), preppedShapes, recipients);
                }
                SkriptParticle.info("Finished drawing shape asynchronously: " + (System.nanoTime() - now) / 1000000.0 + "ms");
            });
        }
        return getNext();
    }

    private void executeSync(Event event, Consumer<Shape> consumer, Collection<Player> recipients){
        Shape shapeCopy;
        Location[] locations = useShapeLocation ? new Location[]{null} : this.locations.getArray(event);
        for (Location location : locations) {
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

    private void executeAsync(Location[] locations, Collection<Shape> shapes, Collection<Player> recipients) {
        for (Location location : locations) {
            for (Shape shape : shapes) {
                SkriptParticle.info("Drawing shape: " + shape);
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
