package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

@Name("Draw Shape Animation")
@Description({
})
@Examples({
})
@Since("1.2.0")
public class EffSecDrawShapeAnimation extends DrawShapeEffectSection {

    static {
        Skript.registerSection(EffSecDrawShapeAnimation.class,
                "draw [an] (animation [of] [the]|animated) shape[s] [of] %shapes% [%-directions% %-locations/entities%] [to %-players%] over %timespan%"
        );
    }

    private Expression<Timespan> duration;

    @Override
    public boolean init(@Nullable Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, boolean hasSection) {
        duration = (Expression<Timespan>) expressions[4];
        return super.init(expressions, matchedPattern, isDelayed, parseResult, hasSection);
    }

    /**
     * This method should not be called for this section.
     */
    @Override
    protected void executeSync(Event event, Collection<DynamicLocation> locations, @Nullable Consumer<Shape> consumer, Collection<Player> recipients) {
        // intentionally empty
    }

    @Override
    protected void setupAsync(Event event, Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        Timespan duration = this.duration.getOptionalSingle(event).orElse(new Timespan(TimePeriod.TICK, 0));
        long milliseconds = duration.getAs(TimePeriod.MILLISECOND);
        for (Shape shape : shapes) {
            shape.setAnimationDuration(milliseconds);
        }
        super.setupAsync(event, locations, shapes, recipients);
    }

    @Override
    @NonNull
    public String toString(@Nullable Event event, boolean debug) {
        return "draw an animation of the shape of " + shapes.toString(event, debug) + " at " + (locations != null ? locations.toString(event, debug) : "shape's location") +
                " for " + (players == null ? "all players" : players.toString(event, debug) + " over " + duration.toString(event, debug));
    }
}
