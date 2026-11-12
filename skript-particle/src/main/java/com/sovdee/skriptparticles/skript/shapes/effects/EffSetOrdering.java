package com.sovdee.skriptparticles.skript.shapes.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.sampling.PointOrder;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Comparator;

/**
 * Skript effect that sets the order in which a shape's points are produced, and therefore the order in which
 * an animated draw reveals them and the order the draw order input follows.
 */
@Name("Shape Draw Order")
@Description({
        "Sets the order in which a shape's points are drawn. This is the order an animated draw reveals them in, " +
                "and the order the 'draw order' input of modifiers follows.",
        "Points can be ordered along an input: the x, y or z axis, the radius (distance from the shape's y axis), " +
                "the spherical distance (distance from the shape's center), or the angle " +
                "(counterclockwise around the y axis, starting at +x). They go from where the input is lowest to where " +
                "it is highest, unless reversed. The order follows the shape as it is rotated.",
        "lowest-to-highest goes from the -x -y -z corner of the shape to the +x +y +z corner, and highest-to-lowest the other way.",
        "random shuffles the points, but the same way each time, so animations don't flicker.",
        "default draws the points in the order the shape generates them."
})
@Example("set the draw order of {_helix} to y axis")
@Example("set the animation order of {_sphere} to reversed spherical distance")
@Example("set the draw order of {_circle} to angle")
@Example("set the drawing order of {_cube} to random")
@Example("set the animation order of {_cube} to lowest-to-highest")
@Example("set the draw order of {_shape} to default")
@Since("1.3.0")
public class EffSetOrdering extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetOrdering.class)
                .supplier(EffSetOrdering::new)
                .addPatterns("set [the] (animation|draw[ing]) order of %shapes% to (default|:random"
                        + "|lowest:lowest-to-highest|highest:highest-to-lowest|[:reversed] " + ModifierInputs.SPATIAL_INPUTS + ")")
                .build());
    }

    private Expression<Shape> shapes;
    private @Nullable Comparator<Vector3d> ordering;
    private String orderName;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        shapes = (Expression<Shape>) expressions[0];
        StandardInput input = ModifierInputs.fromTags(parseResult);
        if (input != null) {
            boolean reversed = parseResult.hasTag("reversed");
            ordering = reversed ? PointOrder.along(input).reversed() : PointOrder.along(input);
            orderName = (reversed ? "reversed " : "") + ModifierInputs.toString(input);
        } else if (parseResult.hasTag("random")) {
            ordering = PointOrder.RANDOM;
            orderName = "random";
        } else if (parseResult.hasTag("lowest")) {
            ordering = PointOrder.DIAGONAL;
            orderName = "lowest-to-highest";
        } else if (parseResult.hasTag("highest")) {
            ordering = PointOrder.DIAGONAL.reversed();
            orderName = "highest-to-lowest";
        } else {
            ordering = null;
            orderName = "default";
        }
        return true;
    }

    @Override
    protected void execute(Event event) {
        for (Shape shape : shapes.getArray(event)) {
            shape.getPointSampler().setOrdering(ordering);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set the draw order of " + shapes.toString(event, debug) + " to " + orderName;
    }
}
