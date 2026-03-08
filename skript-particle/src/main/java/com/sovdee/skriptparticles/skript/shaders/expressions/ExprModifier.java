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
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.SampleAxis;
import com.sovdee.shapes.modifiers.TaperModifier;
import com.sovdee.shapes.modifiers.TwistModifier;
import com.sovdee.shapes.modifiers.WaveModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Point Modifier Constructor")
@Description({
        "Creates a point modifier that transforms shape geometry before drawing.",
        "- Taper: scales the two perpendicular axes by lerp(start, end, normalised position along input axis).",
        "  Default input axis is y. Use 'along the x/z/t axis' to change it.",
        "- Twist: rotates the plane perpendicular to the input axis by angle * normalised position.",
        "  Default input axis is y. Use 'along the x/z/t axis' to change it.",
        "- Wave: displaces an output axis by amplitude * sin(frequency * input axis value).",
        "  Output axis: x, y, or z. Input axis: x, y, z, or t (normalised draw order).",
        "  When the displacing clause is omitted, defaults to displacing y by t.",
})
@Examples({
        "set {_m} to a taper from 1 to 0",
        "add taper from 1 to 0 along the y axis to modifiers of {_shape}",
        "add taper from 1 to 0 along the x axis to modifiers of {_shape}",
        "add a twist of 360 degrees to modifiers of {_shape}",
        "add a twist of 180 degrees along the x axis to modifiers of {_shape}",
        "add a wave with amplitude 0.5 and frequency 2 displacing y by t to modifiers of {_shape}",
        "add a wave with amplitude 0.3 and frequency 4 displacing y by x to modifiers of {_shape}",
        "add a wave with amplitude 0.5 and frequency 1 displacing x by z to modifiers of {_shape}",
})
@Since("2.0.0")
public class ExprModifier extends SimpleExpression<PointModifier> {

    // Taper parse mark:
    //   0=Y (default), 1=X, 2=Z, 3=T

    // Twist parse mark bit layout:
    //   bit 0      → unit:  0=degrees (default), 1=radians
    //   bits 1-2   → axis:  0=Y (default), 1=X, 2=Z, 3=T  (stored as mark>>1)
    //   Combined values: y-deg=0, y-rad=1, x-deg=2, x-rad=3, z-deg=4, z-rad=5, t-deg=6, t-rad=7

    // Wave parse mark bit layout:
    //   bits 0-1 (& 3)  → output axis: 0=Y (default), 1=X, 2=Z
    //   bits 2+  (>> 2) → input axis:  0=T (default), 1=X, 2=Y, 3=Z
    // Default mark=0 → displace Y by T (most useful wave default).

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprModifier.class, PointModifier.class)
                .supplier(ExprModifier::new)
                .addPatterns(
                        "[a] taper from %number% to %number% [along [the] (0¦y|1¦x|2¦z|3¦t) [axis]] [eased with %-easing%]",
                        "[a] twist of %number% (0¦degrees|1¦radians) [along [the] (0¦y|2¦x|4¦z|6¦t) [axis]] [eased with %-easing%]",
                        "[a] wave with amplitude %number% [and] frequency %number% [displacing [the] (1¦x|0¦y|2¦z) [axis] [by [the] (4¦x|8¦y|12¦z|0¦t) [axis]]]"
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
            // taper: exprs[1]=endScale, exprs[2]=optional easing
            this.arg2 = (Expression<Number>) exprs[1];
            this.easing = (Expression<EasingFunction>) exprs[2];
        } else if (matchedPattern == 1) {
            // twist: exprs[1]=optional easing (no arg2)
            this.easing = (Expression<EasingFunction>) exprs[1];
        } else {
            // wave: exprs[1]=frequency
            if (exprs.length > 1) this.arg2 = (Expression<Number>) exprs[1];
        }
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        Number n1 = arg1.getSingle(event);
        if (n1 == null) return null;

        return switch (pattern) {
            case 0 -> { // taper
                Number n2 = arg2.getSingle(event);
                if (n2 == null) yield null;
                SampleAxis axis = axisFromMark(parseMark);
                TaperModifier mod = new TaperModifier(n1.doubleValue(), n2.doubleValue(), axis);
                if (easing != null) { EasingFunction ef = easing.getSingle(event); if (ef != null) mod.setEasing(ef); }
                yield new PointModifier[]{mod};
            }
            case 1 -> { // twist
                double angle = n1.doubleValue();
                if ((parseMark & 1) == 0) angle = Math.toRadians(angle); // degrees
                SampleAxis axis = axisFromMark(parseMark >> 1);
                TwistModifier mod = new TwistModifier(angle, axis);
                if (easing != null) { EasingFunction ef = easing.getSingle(event); if (ef != null) mod.setEasing(ef); }
                yield new PointModifier[]{mod};
            }
            case 2 -> { // wave
                Number n2 = arg2.getSingle(event);
                if (n2 == null) yield null;
                WaveModifier.Axis outputAxis = switch (parseMark & 3) {
                    case 1 -> WaveModifier.Axis.X;
                    case 2 -> WaveModifier.Axis.Z;
                    default -> WaveModifier.Axis.Y;
                };
                SampleAxis inputAxis = switch (parseMark >> 2) {
                    case 1 -> SampleAxis.X;
                    case 2 -> SampleAxis.Y;
                    case 3 -> SampleAxis.Z;
                    default -> SampleAxis.T;
                };
                yield new PointModifier[]{new WaveModifier(n1.doubleValue(), n2.doubleValue(), 0.0, inputAxis, outputAxis)};
            }
            default -> null;
        };
    }

    private static SampleAxis axisFromMark(int mark) {
        return switch (mark) {
            case 1 -> SampleAxis.X;
            case 2 -> SampleAxis.Z;
            case 3 -> SampleAxis.T;
            default -> SampleAxis.Y;
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
            case 0 -> "taper from " + arg1.toString(event, debug) + " to " + arg2.toString(event, debug);
            case 1 -> "twist of " + arg1.toString(event, debug) + ((parseMark & 1) == 0 ? " degrees" : " radians");
            case 2 -> "wave with amplitude " + arg1.toString(event, debug) + " frequency " + arg2.toString(event, debug);
            default -> "modifier";
        };
    }
}
