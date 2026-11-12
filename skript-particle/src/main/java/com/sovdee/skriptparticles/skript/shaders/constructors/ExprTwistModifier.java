package com.sovdee.skriptparticles.skript.shaders.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.RotationPlane;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.modifiers.TwistModifier;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Twist Modifier Constructor")
@Description({
        "Creates a modifier that rotates a shape's points by an angle that grows along an input, " +
                "from no rotation where the input is 0 to the full angle where it is 1.",
        "The angle is in degrees. Skript reads '3.14 radians' as degrees too, so either unit works.",
        "The input can be the x, y (default) or z axis, the draw order, the radius (distance from the shape's y axis), " +
                "the spherical distance (distance from its center), or the angle around its y axis. " +
                "By default, it turns the xz plane, around the y axis.",
        "This can take a section to set the rest of the modifier's properties, like its easing. " +
                "Inside it, 'the new modifier' is the modifier being created, and modifier properties use it by default."
})
@Example("add a twist of 360 degrees to modifiers of {_shape}")
@Example("add a twist of 180 degrees along the radius in the xy plane to modifiers of {_shape}")
@Example("add a twist of 3.14 radians to modifiers of {_shape}")
@Since("2.0.0")
public class ExprTwistModifier extends ModifierConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprTwistModifier.class, PointModifier.class)
                .supplier(ExprTwistModifier::new)
                .addPattern("[a] twist of %number% [along [the] " + ModifierInputs.ALL_INPUTS + "]"
                        + " [in [the] (plane_xz:xz|plane_xy:xy|plane_yz:yz) plane]")
                .build());
    }

    private Expression<Number> angle;
    private StandardInput input;
    private RotationPlane plane = RotationPlane.XZ;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        angle = (Expression<Number>) expressions[0];
        StandardInput chosen = ModifierInputs.fromTags(result);
        input = chosen == null ? StandardInput.Y : chosen;
        for (String tag : result.tags) {
            if (tag.startsWith("plane_"))
                plane = RotationPlane.valueOf(tag.substring(6).toUpperCase());
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        Number degrees = angle.getSingle(event);
        if (degrees == null)
            return null;
        return new TwistModifier(Math.toRadians(degrees.doubleValue()), input, plane);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a twist of " + angle.toString(event, debug) + " along the " + ModifierInputs.toString(input)
                + " in the " + plane.name().toLowerCase() + " plane";
    }
}
