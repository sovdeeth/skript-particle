package com.sovdee.skriptparticles.skript.drawing.sections;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.Timespan.TimePeriod;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

@Name("Draw Shape Animation")
@Description({
        "Draws the given shapes a few points at a time, spread evenly over the given time span, so the shape appears to be traced out.",
        "Points are drawn in the shape's animation order (see Shape Animation Ordering). Each point is drawn once; " +
                "the shape does not stay visible after the animation unless you draw it again.",
        "As with the draw shape section, the code inside the section runs once for each shape before drawing begins, " +
                "and changes to `drawn shape` only affect this drawing. Animations are always drawn asynchronously, " +
                "so the code after this section runs right away, before the animation is finished."
})
@Example("draw an animation of the shape of a circle with radius 3 at player over 2 seconds")
@Example("""
    draw an animated shape of {_helix} at player to all players over 1 second:
        set particle of drawn shape to red dust particle
    """)
@Since("1.2.0")
public class EffSecDrawShapeAnimation extends DrawShapeEffectSection {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.SECTION, SyntaxInfo.builder(EffSecDrawShapeAnimation.class)
                .supplier(EffSecDrawShapeAnimation::new)
                .addPatterns("draw [an] (animation [of] [the]|animated) shape[s] [of] %shapes% [%-directions% %-locations/entities%] [to %-players%] over %timespan%")
                .build());
    }

    private Expression<Timespan> duration;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(@Nullable Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult, boolean hasSection) {
        duration = (Expression<Timespan>) expressions[4];
        return super.init(expressions, matchedPattern, isDelayed, parseResult, hasSection);
    }

    @Override
    protected void setupAsync(Event event, Collection<DynamicLocation> locations, Collection<Shape> shapes, Collection<Player> recipients) {
        Timespan duration = this.duration.getOptionalSingle(event).orElse(new Timespan(TimePeriod.TICK, 0));
        long milliseconds = duration.getAs(TimePeriod.MILLISECOND);
        for (Shape shape : shapes) {
            DrawData.of(shape).setAnimationDuration(milliseconds);
        }
        super.setupAsync(event, locations, shapes, recipients);
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return "draw an animation of the shape of " + shapes.toString(event, debug) + " at " + (locations != null ? locations.toString(event, debug) : "shape's location") +
                " for " + (players == null ? "all players" : players.toString(event, debug) + " over " + duration.toString(event, debug));
    }
}
