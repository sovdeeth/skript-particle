package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.SkriptParticle;
import com.sovdee.skriptparticles.shapes.DrawManager;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class DrawShapeEffectSection extends EffectSection {

    public static final Timespan ONE_TICK = new Timespan(TimePeriod.TICK, 1);

    static {
        EventValues.registerEventValue(DrawEvent.class, Shape.class, DrawEvent::getShape, EventValues.TIME_NOW);
    }

    protected Expression<Shape> shapes;
    @Nullable
    protected Expression<Direction> directions;
    @Nullable
    protected Expression<?> locations;
    @Nullable
    protected Expression<Player> players;
    @Nullable
    private Trigger trigger;

    protected boolean useShapeLocation;
    protected boolean sync;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        if (hasSection()) {
            AtomicBoolean delayed = new AtomicBoolean(false);
            Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
            assert sectionNode != null;
            trigger = loadCode(sectionNode, "draw", null, afterLoading, DrawEvent.class);
            if (delayed.get()) {
                Skript.error("Delays can't be used within a Draw Shape Effect Section");
                return false;
            }
        }
        return init(expressions, matchedPattern, isDelayed, parseResult, hasSection());
    }

    public boolean init(@Nullable Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, boolean hasSection) {
        shapes = (Expression<Shape>) expressions[0];

        if (expressions[2] != null) {
            if (expressions[1] != null)
                directions = (Expression<Direction>) expressions[1];
            locations = expressions[2];
        } else {
            useShapeLocation = true;
        }
        players = (Expression<Player>) expressions[3];

        sync = parseResult.hasTag("sync");

        return true;
    }

    @Override
    @Nullable
    protected TriggerItem walk(Event event) {
        debug(event, true);

        Delay.addDelayedEvent(event);

        Collection<Player> recipients = new ArrayList<>();
        if (players != null) {
            recipients.addAll(List.of(players.getArray(event)));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        @Nullable Object localVars = Variables.copyLocalVariables(event);

        @Nullable Consumer<Shape> consumer;
        if (trigger != null) {
            consumer = shape -> {
                DrawEvent drawEvent = new DrawEvent(shape);
                Variables.withLocalVariables(event, drawEvent, () -> TriggerItem.walk(trigger, drawEvent));
            };
        } else {
            consumer = null;
        }

        List<DynamicLocation> locations = new ArrayList<>();
        @Nullable Direction direction = null;
        if (!useShapeLocation) {
            if (directions != null)
                direction = directions.getSingle(event);
            assert this.locations != null;
            for (Object location : this.locations.getArray(event)) {
                if (location instanceof Entity) {
                    locations.add(new DynamicLocation((Entity) location, direction));
                } else if (location instanceof Location) {
                    locations.add(new DynamicLocation((Location) location, direction));
                }
            }
        } else {
            locations.add(new DynamicLocation());
        }

        if (sync) {
            executeSync(event, locations, consumer, recipients);
        } else {
            List<Shape> preppedShapes = new ArrayList<>();
            Shape preppedShape;
            for (Shape shape : shapes.getArray(event)) {
                preppedShape = shape.clone();
                if (consumer != null)
                    consumer.accept(preppedShape);
                preppedShapes.add(preppedShape);
            }

            if (preppedShapes.isEmpty()) return getNext();

            setupAsync(event, locations, preppedShapes, recipients);
        }
        return getNext();
    }

    protected void setupAsync(Event event, Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                executeAsync(locations, shapes, recipients);
            }
        };
        runnable.runTaskAsynchronously(Skript.getInstance());
    }

    protected void executeSync(Event event, Collection<DynamicLocation> locations, @Nullable Consumer<Shape> consumer, Collection<Player> recipients) {
        try {
            for (DynamicLocation dynamicLocation : locations) {
                for (Shape shape : shapes.getArray(event)) {
                    Shape clone = shape.clone();
                    if (consumer != null) {
                        DrawManager.drawWithConsumer(clone, dynamicLocation, consumer, recipients);
                    } else {
                        DrawManager.draw(clone, dynamicLocation, recipients);
                    }
                }
            }
        } catch (IllegalArgumentException exception) {
            SkriptParticle.severe("Unable to draw shape[s]! Please check that your particles are valid!");
            SkriptParticle.severe("Exception: " + exception.getMessage());
            SkriptParticle.severe("To see the full stack trace, set Skript's verbosity to very high or debug.");
            if (Skript.logVeryHigh())
                exception.printStackTrace();
        }
    }

    protected void executeAsync(Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        try {
            for (DynamicLocation dynamicLocation : locations) {
                for (Shape shape : shapes) {
                    DrawManager.draw(shape.clone(), dynamicLocation, recipients);
                }
            }
        } catch (IllegalArgumentException exception) {
            SkriptParticle.severe("Unable to draw shape[s]! Please check that your particles are valid!");
            SkriptParticle.severe("Exception: " + exception.getMessage());
            SkriptParticle.severe("To see the full stack trace, set Skript's verbosity to very high or debug.");
            if (Skript.logVeryHigh())
                exception.printStackTrace();
        }
    }

    public static class DrawEvent extends Event {
        private final Shape shape;

        public DrawEvent(Shape shape) {
            this.shape = shape;
        }

        public Shape getShape() {
            return shape;
        }

        @Override
        @NonNull
        public HandlerList getHandlers() {
            throw new IllegalStateException();
        }
    }
}
