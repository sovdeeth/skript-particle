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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public abstract class DrawShapeEffectSection extends EffectSection {

    public static final Timespan ONE_TICK = Timespan.fromTicks_i(1);

    static {
        EventValues.registerEventValue(DrawEvent.class, Shape.class, new Getter<>() {
            @Override
            public Shape get(DrawEvent event) {
                return event.getShape();
            }
        }, EventValues.TIME_NOW);
    }

    protected Expression<Shape> shapes;
    protected Expression<Direction> directions;
    protected Expression<?> locations;
    protected Expression<Player> players;
    @Nullable
    private Trigger trigger;

    protected boolean useShapeLocation;
    protected boolean sync;

    /**
     * Called just after the constructor. Handles checking for delays in the section body and setting the sync tag, then
     * passes execution to {@link #init(Expression[], int, Kleenean, ParseResult, boolean)}.
     *
     * @param expressions all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out it will still be included in this list
     *            holding the default value of the desired type which usually depends on the event.
     * @param matchedPattern The index of the pattern which matched
     * @param isDelayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
     * @param parseResult Additional information about the match.
     * @param sectionNode The section node that represents this section.
     * @param list A list of {@link TriggerItem}s that belong to this section. This list is modifiable.
     * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause.
     * @see ParserInstance#isCurrentEvent(Class...)
     */
    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        // handle delays in the section body
        if (hasSection()) {
            AtomicBoolean delayed = new AtomicBoolean(false);
            Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
            assert sectionNode != null;
            trigger = loadCode(sectionNode, "draw", afterLoading, DrawEvent.class);
            if (delayed.get()) {
                Skript.error("Delays can't be used within a Draw Shape Effect Section");
                return false;
            }
        }
        return init(expressions, matchedPattern, isDelayed, parseResult, hasSection());
    }

    /**
     * Called just after the constructor. By default, this method does sets the following fields:
     *  - {@link #shapes} to the first expression
     *  - {@link #directions} to the second expression
     *  - {@link #locations} to the third expression
     *  - {@link #players} to the fourth expression
     *  - {@link #sync} to whether the sync tag was present
     *
     * @param expressions all %expr%s included in the matching pattern in the order they appear in the pattern. If an optional value was left out it will still be included in this list
     *            holding the default value of the desired type which usually depends on the event.
     * @param matchedPattern The index of the pattern which matched
     * @param isDelayed Whether this expression is used after a delay or not (i.e. if the event has already passed when this expression will be called)
     * @param parseResult Additional information about the match.
     * @param hasSection Whether this section had a valid section body.
     * @return Whether this expression was initialised successfully. An error should be printed prior to returning false to specify the cause
     */
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, boolean hasSection) {
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
                DrawEvent drawEvent = new DrawEvent(shape);
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

            setupAsync(event, locations, preppedShapes, recipients);
        }
        return getNext();
    }

    /**
     * Sets up the async task to draw the shapes at the given locations for the given recipients.
     * Should be called from {@link #walk(Event)} if {@link #sync} is false, and will return the next trigger item to run.
     * @param locations the locations to draw the shapes at
     * @param shapes the shapes to draw
     * @param recipients the players to draw the shapes for
     */
    protected void setupAsync(Event event, Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                executeAsync(locations, shapes, recipients);
            }
        };
        runnable.runTaskAsynchronously(Skript.getInstance());
    }

    protected void executeSync(Event event, Collection<DynamicLocation> locations, Consumer<Shape> consumer, Collection<Player> recipients) {
        Shape shapeCopy;
        for (DynamicLocation dynamicLocation : locations) {
            for (Shape shape : shapes.getArray(event)) {
                if (consumer != null) {
                    // copy the shape so that it can be modified by the consumer without affecting the original
                    shapeCopy = shape.clone();
                    shapeCopy.draw(dynamicLocation, consumer, recipients);
                } else {
                    shape.draw(dynamicLocation, recipients);
                }
            }
        }
    }

    protected void executeAsync(Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        for (DynamicLocation dynamicLocation : locations) {
            for (Shape shape : shapes) {
                shape.draw(dynamicLocation, recipients);
            }
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
