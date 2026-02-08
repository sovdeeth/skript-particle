package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
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
        "draw the shape (sphere with radius 1) at player's location",
        "draw the shapes of (a sphere with radius 1 and a cube with radius 1) at player's location for (all players in radius 10 of player)",
        "synchronously draw the shape of a sphere with radius 1 at player's location",
        "",
        "draw the shape {_shape} at player's location:",
            "\tset event-shape's particle to red dust",
        "",
        "synchronously draw shape (a sphere with radius 1 and a cube with radius 1) at player's location:",
            "\tset event-shape's radius to 2",
})
@Since("1.0.0")
public class EffSecDrawShape extends DrawShapeEffectSection {

    static {
        Skript.registerSection(EffSecDrawShape.class,
                "[sync:sync[hronously]] draw [the] shape[s] [of] %shapes% [%-directions% %-locations/entities%] [to %-players%]",
                "draw [the] shape[s] [of] %shapes% [%-directions% %-locations/entities%] [to %-players%] (duration:for) [duration] %timespan% [with (delay|refresh [rate]) [of] %-timespan%]"
        );
        EventValues.registerEventValue(EffSecDrawShape.DrawEvent.class, Shape.class, DrawEvent::getShape, EventValues.TIME_NOW);
    }

    @Nullable
    private Expression<Timespan> duration;
    @Nullable
    private Expression<Timespan> delay;

    @Override
    public boolean init(@Nullable Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, boolean hasSection) {
        if (parseResult.hasTag("duration")) {
            duration = (Expression<Timespan>) expressions[4];
            delay = (Expression<Timespan>) expressions[5];
        }
        return super.init(expressions, matchedPattern, isDelayed, parseResult, hasSection);
    }

    @Override
    protected void setupAsync(Event event, Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        long period, iterations;
        if (this.duration == null) {
            period = 1;
            iterations = 1;
        } else {
            @Nullable Timespan delay = (this.delay == null ? ONE_TICK : this.delay.getSingle(event));
            @Nullable Timespan duration = this.duration.getSingle(event);
            if (delay == null || duration == null) return;

            period = Math.max(delay.getAs(TimePeriod.TICK), 1);
            iterations = Math.max(duration.getAs(TimePeriod.TICK) / period, 1);
        }
        AtomicLong currentIteration = new AtomicLong(0);
        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                executeAsync(locations, shapes, recipients);
                if (currentIteration.incrementAndGet() >= iterations) {
                    this.cancel();
                }
            }
        };
        runnable.runTaskTimerAsynchronously(Skript.getInstance(), 0, period);
    }

    @Override
    @NonNull
    public String toString(@Nullable Event event, boolean b) {
        return "draw shape " + shapes.toString(event, b) + (locations != null ? " at " + locations.toString(event, b) : "") + " for " + (players == null ? "all players" : players.toString(event, b));
    }
}
