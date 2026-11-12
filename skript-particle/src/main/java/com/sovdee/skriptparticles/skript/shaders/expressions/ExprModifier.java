package com.sovdee.skriptparticles.skript.shaders.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.RotationPlane;
import com.sovdee.shapes.modifiers.ScaleAxes;
import com.sovdee.shapes.modifiers.ScalingModifier;
import com.sovdee.shapes.modifiers.SpatialAxis;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.modifiers.TwistModifier;
import com.sovdee.shapes.modifiers.WaveModifier;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import com.sovdee.skriptparticles.skript.shaders.sections.ModifierConstructorExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Geometry Modifier Constructor")
@Description({
        "Creates a modifier that changes the shape of a shape's points before drawing.",
        "Each modifier changes points by an amount that depends on an input: the x, y or z axis, the draw order, " +
                "the radius (distance from the shape's y axis), the spherical distance (distance from its center), " +
                "or the angle around its y axis. Inputs go from 0 where they are lowest in the shape to 1 where they are highest.",
        "- Scale: scales points from the start scale where the input is 0 to the end scale where it is 1. " +
                "By default, it scales the x and z axes along the y axis, which makes a taper.",
        "- Twist: rotates points by up to the angle, in degrees. Skript reads '3.14 radians' as degrees too, so either unit works. " +
                "By default, it turns the xz plane along the y axis.",
        "- Wave: moves points along an axis by amplitude × sin(360° × frequency × input + phase), " +
                "so a frequency of 1 is one full wave over the input. By default, it moves the y axis along the draw order.",
        "These can take a section to set the rest of the modifier's properties, like its easing or wave phase. " +
                "Inside it, 'the new modifier' is the modifier being created, and modifier properties use it by default."
})
@Example("add a scale from 1 to 0 along the y axis to modifiers of {_shape}")
@Example("add a scale from 1 to 2 along the radius scaling the xyz axes to modifiers of {_shape}")
@Example("add a twist of 360 degrees to modifiers of {_shape}")
@Example("add a twist of 180 degrees along the radius in the xy plane to modifiers of {_shape}")
@Example("add a twist of 3.14 radians to modifiers of {_shape}")
@Example("add a wave with amplitude 0.5 and frequency 2 to modifiers of {_shape}")
@Example("add a wave with amplitude 0.3 and frequency 4 along the x axis displacing the y axis to modifiers of {_shape}")
@Example("""
        set {_taper} to a scale from 1 to 0 along the y axis:
            set modifier easing to ease in quadratic
        add {_taper} to modifiers of {_shape}
        """)
@Since("2.0.0")
public class ExprModifier extends ModifierConstructorExpression {

    private static final String ALONG = "[along [the] " + ModifierInputs.ALL_INPUTS + "]";

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprModifier.class, PointModifier.class)
                .supplier(ExprModifier::new)
                .addPatterns(
                        "[a] scale from %number% to %number% " + ALONG
                                + " [scaling [the] (axes_x:x|axes_y:y|axes_z:z|axes_xy:xy|axes_xz:xz|axes_yz:yz|axes_xyz:xyz) [axis|axes]]",
                        "[a] twist of %number% " + ALONG
                                + " [in [the] (plane_xz:xz|plane_xy:xy|plane_yz:yz) plane]",
                        "[a] wave with amplitude %number% [and] frequency %number% " + ALONG
                                + " [displacing [the] (out_x:x|out_y:y|out_z:z)[-| ]axis]"
                )
                .build());
    }

    private int pattern;
    private Expression<Number> first;
    private @Nullable Expression<Number> second;
    private @Nullable StandardInput input;
    private ScaleAxes axes = ScaleAxes.XZ;
    private RotationPlane plane = RotationPlane.XZ;
    private SpatialAxis outputAxis = SpatialAxis.Y;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        pattern = matchedPattern;
        first = (Expression<Number>) expressions[0];
        if (expressions.length > 1)
            second = (Expression<Number>) expressions[1];
        input = ModifierInputs.fromTags(result);
        for (String tag : result.tags) {
            switch (tag) {
                case "axes_x" -> axes = ScaleAxes.X;
                case "axes_y" -> axes = ScaleAxes.Y;
                case "axes_z" -> axes = ScaleAxes.Z;
                case "axes_xy" -> axes = ScaleAxes.XY;
                case "axes_xz" -> axes = ScaleAxes.XZ;
                case "axes_yz" -> axes = ScaleAxes.YZ;
                case "axes_xyz" -> axes = ScaleAxes.XYZ;
                case "plane_xz" -> plane = RotationPlane.XZ;
                case "plane_xy" -> plane = RotationPlane.XY;
                case "plane_yz" -> plane = RotationPlane.YZ;
                case "out_x" -> outputAxis = SpatialAxis.X;
                case "out_y" -> outputAxis = SpatialAxis.Y;
                case "out_z" -> outputAxis = SpatialAxis.Z;
                default -> {}
            }
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        Number a = first.getSingle(event);
        Number b = second == null ? null : second.getSingle(event);
        if (a == null || (second != null && b == null))
            return null;
        return switch (pattern) {
            case 0 -> new ScalingModifier(a.doubleValue(), b.doubleValue(), input == null ? StandardInput.Y : input, axes);
            case 1 -> new TwistModifier(Math.toRadians(a.doubleValue()),
                    input == null ? StandardInput.Y : input, plane);
            default -> new WaveModifier(a.doubleValue(), b.doubleValue(), 0.0,
                    input == null ? StandardInput.T : input, outputAxis);
        };
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String along = input == null ? "" : " along the " + ModifierInputs.toString(input);
        return switch (pattern) {
            case 0 -> "a scale from " + first.toString(event, debug) + " to " + second.toString(event, debug) + along
                    + " scaling the " + axes.name().toLowerCase() + " axes";
            case 1 -> "a twist of " + first.toString(event, debug) + along
                    + " in the " + plane.name().toLowerCase() + " plane";
            default -> "a wave with amplitude " + first.toString(event, debug) + " and frequency " + second.toString(event, debug)
                    + along + " displacing the " + outputAxis.name().toLowerCase() + " axis";
        };
    }
}
