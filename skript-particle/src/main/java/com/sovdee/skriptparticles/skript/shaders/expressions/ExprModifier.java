package com.sovdee.skriptparticles.skript.shaders.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.NormalizedInput;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.RotationPlane;
import com.sovdee.shapes.modifiers.ScaleAxes;
import com.sovdee.shapes.modifiers.ScalingModifier;
import com.sovdee.shapes.modifiers.SpatialAxis;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.modifiers.TwistModifier;
import com.sovdee.shapes.modifiers.WaveModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Point Modifier Constructor")
@Description({
        "Creates a point modifier that transforms shape geometry before drawing.",
        "- Scale: scales the specified axes by lerp(start, end, input). Default: scales XZ by Y input.",
        "  Inputs: y (default), x, z, t, radius, spherical, angle.",
        "  Scale axes: xz (default), xy, yz, x, y, z, xyz.",
        "- Twist: rotates the specified plane by angle * input. Default: XZ plane driven by Y input.",
        "  Inputs: y (default), x, z, t, radius, spherical, angle.",
        "  Planes: xz (default), xy, yz.",
        "- Wave: displaces an output axis by amplitude * sin(2π * frequency * input + phase).",
        "  Output axis: x, y (default), or z. Input: x, y, z, t (default), radius, spherical, angle.",
        "  frequency=1 means one full sine cycle over the full input range.",
        "  When the displacing clause is omitted, defaults to displacing y by t.",
})
@Examples({
        "set {_m} to a scale from 1 to 0",
        "add scale from 1 to 0 along the y axis to modifiers of {_shape}",
        "add scale from 1 to 0 driven by radius scaling xyz to modifiers of {_shape}",
        "add a twist of 360 degrees to modifiers of {_shape}",
        "add a twist of 180 degrees in the xz plane driven by radius to modifiers of {_shape}",
        "add a wave with amplitude 0.5 and frequency 2 displacing y by t to modifiers of {_shape}",
        "add a wave with amplitude 0.3 and frequency 4 displacing y by x to modifiers of {_shape}",
        "add a wave with amplitude 0.5 and frequency 1 displacing x by radius to modifiers of {_shape}",
})
@Since("2.0.0")
public class ExprModifier extends SimpleExpression<PointModifier> {

    // Scale parse mark bit layout:
    //   bits 0-2  (& 7)   → input:      0=Y, 1=X, 2=Z, 3=T, 4=RADIUS, 5=SPHERICAL, 6=ANGLE
    //   bits 3-5  (>> 3)  → scale axes: 0=XZ (default), 1=X, 2=Y, 3=Z, 4=XY, 5=YZ, 6=XYZ

    // Twist parse mark bit layout:
    //   bit 0      → unit:  0=degrees (default), 1=radians
    //   bits 1-3   → input: 0=Y (default), 1=X, 2=Z, 3=T, 4=RADIUS, 5=SPHERICAL, 6=ANGLE  (stored as mark>>1 & 7)
    //   bits 4-5   → plane: 0=XZ (default), 1=XY, 2=YZ  (stored as mark>>4)

    // Wave parse mark bit layout:
    //   bits 0-1 (& 3)  → output axis: 0=Y (default), 1=X, 2=Z
    //   bits 2-4 (>> 2) → input:       0=T (default), 1=X, 2=Y, 3=Z, 4=RADIUS, 5=SPHERICAL, 6=ANGLE

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprModifier.class, PointModifier.class)
                .supplier(ExprModifier::new)
                .addPatterns(
                        "[a] scale from %number% to %number% [along [the] (0¦y|1¦x|2¦z|3¦t|4¦radius|5¦spherical|6¦angle) [axis]|driven by (0¦y|1¦x|2¦z|3¦t|4¦radius|5¦spherical|6¦angle)] [scaling (0¦xz|8¦x|16¦y|24¦z|32¦xy|40¦yz|48¦xyz)] [eased with %-easing%]",
                        "[a] twist of %number% (0¦degrees|1¦radians) [in [the] (0¦xz|16¦xy|32¦yz) plane] [driven by (0¦y|2¦x|4¦z|6¦t|8¦radius|10¦spherical|12¦angle)] [eased with %-easing%]",
                        "[a] wave with amplitude %number% [and] frequency %number% [displacing [the] (1¦x|0¦y|2¦z) [axis] [by [the] (4¦x|8¦y|12¦z|0¦t|16¦radius|20¦spherical|24¦angle) [axis]]]"
                )
                .build());
    }

    private int pattern;
    private int parseMark;
    private Expression<Number> arg1, arg2;
    private @Nullable Expression<EasingFunction> easing;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.parseMark = parseResult.mark;
        this.arg1 = (Expression<Number>) exprs[0];
        if (matchedPattern == 0) {
            this.arg2 = (Expression<Number>) exprs[1];
            this.easing = (Expression<EasingFunction>) exprs[2];
        } else if (matchedPattern == 1) {
            this.easing = (Expression<EasingFunction>) exprs[1];
        } else {
            if (exprs.length > 1) this.arg2 = (Expression<Number>) exprs[1];
        }
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        Number n1 = arg1.getSingle(event);
        if (n1 == null) return null;

        return switch (pattern) {
            case 0 -> { // scale
                Number n2 = arg2.getSingle(event);
                if (n2 == null) yield null;
                NormalizedInput input = inputFromMark(parseMark & 7);
                ScaleAxes axes = scaleAxesFromMark((parseMark >> 3) & 7);
                ScalingModifier mod = new ScalingModifier(n1.doubleValue(), n2.doubleValue(), input, axes);
                if (easing != null) { EasingFunction ef = easing.getSingle(event); if (ef != null) mod.setEasing(ef); }
                yield new PointModifier[]{mod};
            }
            case 1 -> { // twist
                double angle = n1.doubleValue();
                if ((parseMark & 1) == 0) angle = Math.toRadians(angle); // degrees
                NormalizedInput input = inputFromMark((parseMark >> 1) & 7);
                RotationPlane plane = rotationPlaneFromMark(parseMark >> 4);
                TwistModifier mod = new TwistModifier(angle, input, plane);
                if (easing != null) { EasingFunction ef = easing.getSingle(event); if (ef != null) mod.setEasing(ef); }
                yield new PointModifier[]{mod};
            }
            case 2 -> { // wave
                Number n2 = arg2.getSingle(event);
                if (n2 == null) yield null;
                SpatialAxis outputAxis = switch (parseMark & 3) {
                    case 1 -> SpatialAxis.X;
                    case 2 -> SpatialAxis.Z;
                    default -> SpatialAxis.Y;
                };
                NormalizedInput input = waveInputFromMark(parseMark >> 2);
                yield new PointModifier[]{new WaveModifier(n1.doubleValue(), n2.doubleValue(), 0.0, input, outputAxis)};
            }
            default -> null;
        };
    }

    private static NormalizedInput inputFromMark(int mark) {
        return switch (mark) {
            case 1 -> StandardInput.X;
            case 2 -> StandardInput.Z;
            case 3 -> StandardInput.T;
            case 4 -> StandardInput.RADIUS;
            case 5 -> StandardInput.SPHERICAL;
            case 6 -> StandardInput.ANGLE;
            default -> StandardInput.Y;
        };
    }

    private static NormalizedInput waveInputFromMark(int mark) {
        return switch (mark) {
            case 1 -> StandardInput.X;
            case 2 -> StandardInput.Y;
            case 3 -> StandardInput.Z;
            case 4 -> StandardInput.RADIUS;
            case 5 -> StandardInput.SPHERICAL;
            case 6 -> StandardInput.ANGLE;
            default -> StandardInput.T;
        };
    }

    private static ScaleAxes scaleAxesFromMark(int mark) {
        return switch (mark) {
            case 1 -> ScaleAxes.X;
            case 2 -> ScaleAxes.Y;
            case 3 -> ScaleAxes.Z;
            case 4 -> ScaleAxes.XY;
            case 5 -> ScaleAxes.YZ;
            case 6 -> ScaleAxes.XYZ;
            default -> ScaleAxes.XZ;
        };
    }

    private static RotationPlane rotationPlaneFromMark(int mark) {
        return switch (mark) {
            case 1 -> RotationPlane.XY;
            case 2 -> RotationPlane.YZ;
            default -> RotationPlane.XZ;
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (pattern) {
            case 0 -> "scale from " + arg1.toString(event, debug) + " to " + arg2.toString(event, debug);
            case 1 -> "twist of " + arg1.toString(event, debug) + ((parseMark & 1) == 0 ? " degrees" : " radians");
            case 2 -> "wave with amplitude " + arg1.toString(event, debug) + " frequency " + arg2.toString(event, debug);
            default -> "modifier";
        };
    }
}
