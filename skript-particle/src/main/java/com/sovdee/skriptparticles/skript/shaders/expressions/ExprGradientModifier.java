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
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.NormalizedInput;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import com.sovdee.skriptparticles.rendering.shaders.AbstractGradientModifier;
import com.sovdee.skriptparticles.rendering.shaders.ColorStop;
import com.sovdee.skriptparticles.rendering.shaders.GradientModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Gradient Modifier Constructor")
@Description({
        "Creates a gradient modifier for coloring shape points.",
        "Two-color form: interpolates from the first color to the second.",
        "Multi-color form: evenly spaces the provided colors across [0, 1].",
        "Axes: x, y (default), z, or t (normalised draw order) for axis-aligned gradients.",
        "Radial: gradient based on XZ distance from center.",
        "Angular: gradient based on angle around the Y axis.",
        "Spherical: gradient based on 3D distance from center.",
})
@Examples({
        "add gradient from red to blue along the y axis to modifiers of {_shape}",
        "add gradient from red, yellow, green, blue along the y axis to modifiers of {_shape}",
        "add radial gradient from white to black to modifiers of {_shape}",
        "add angular gradient from red to blue to modifiers of {_shape}",
        "add spherical gradient from yellow to orange to modifiers of {_shape}",
        "set modifiers of {_shape} to gradient from red to blue along the y axis",
})
@Since("2.0.0")
public class ExprGradientModifier extends SimpleExpression<PointModifier> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprGradientModifier.class, PointModifier.class)
                .supplier(ExprGradientModifier::new)
                .addPatterns(
                        // Pattern 0: two-color axis gradient
                        "[a] [color] gradient from %color% to %color% along [the] (0¦x|1¦y|2¦z|3¦t) [axis] [eased with %-easing%]",
                        // Pattern 1: multi-color axis gradient (evenly spaced)
                        "[a] [color] gradient from %colors% along [the] (0¦x|1¦y|2¦z|3¦t) [axis] [eased with %-easing%]",
                        // Pattern 2: radial gradient (two colors)
                        "[a] radial [color] gradient from %color% to %color% [eased with %-easing%]",
                        // Pattern 3: angular gradient (two colors)
                        "[a] angular [color] gradient from %color% to %color% [eased with %-easing%]",
                        // Pattern 4: spherical gradient (two colors)
                        "[a] spherical [color] gradient from %color% to %color% [eased with %-easing%]"
                )
                .build());
    }

    private int pattern;
    private int parseMark;
    private Expression<Color> color1, color2;
    private Expression<Color> colors; // for multi-color
    private @Nullable Expression<EasingFunction> easing;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.parseMark = parseResult.mark;
        if (matchedPattern == 1) {
            this.colors = (Expression<Color>) exprs[0];
            this.easing = (Expression<EasingFunction>) exprs[1];
        } else {
            this.color1 = (Expression<Color>) exprs[0];
            this.color2 = (Expression<Color>) exprs[1];
            this.easing = (Expression<EasingFunction>) exprs[2];
        }
        return true;
    }

    @Override
    protected PointModifier<ParticleRenderContext> @Nullable [] get(Event event) {
        AbstractGradientModifier mod = switch (pattern) {
            case 0 -> { // two-color axis gradient
                Color c1 = color1.getSingle(event);
                Color c2 = color2.getSingle(event);
                if (c1 == null || c2 == null) yield null;
                yield new GradientModifier(c1.asBukkitColor(), c2.asBukkitColor(), axisFromMark(parseMark));
            }
            case 1 -> { // multi-color axis gradient (evenly spaced)
                Color[] cs = colors.getArray(event);
                if (cs == null || cs.length == 0) yield null;
                yield new GradientModifier(evenlySpaced(cs), axisFromMark(parseMark));
            }
            case 2 -> { // radial
                Color c1 = color1.getSingle(event);
                Color c2 = color2.getSingle(event);
                if (c1 == null || c2 == null) yield null;
                yield new GradientModifier(c1.asBukkitColor(), c2.asBukkitColor(), StandardInput.RADIUS);
            }
            case 3 -> { // angular
                Color c1 = color1.getSingle(event);
                Color c2 = color2.getSingle(event);
                if (c1 == null || c2 == null) yield null;
                yield new GradientModifier(c1.asBukkitColor(), c2.asBukkitColor(), StandardInput.ANGLE);
            }
            case 4 -> { // spherical
                Color c1 = color1.getSingle(event);
                Color c2 = color2.getSingle(event);
                if (c1 == null || c2 == null) yield null;
                yield new GradientModifier(c1.asBukkitColor(), c2.asBukkitColor(), StandardInput.SPHERICAL);
            }
            default -> null;
        };
        if (mod == null) return null;
        if (easing != null) { EasingFunction ef = easing.getSingle(event); if (ef != null) mod.setEasing(ef); }
        return new PointModifier[]{mod};
    }

    private static NormalizedInput axisFromMark(int mark) {
        return switch (mark) {
            case 0 -> StandardInput.X;
            case 2 -> StandardInput.Z;
            case 3 -> StandardInput.T;
            default -> StandardInput.Y;
        };
    }

    private static List<ColorStop> evenlySpaced(Color[] colors) {
        List<ColorStop> stops = new ArrayList<>(colors.length);
        if (colors.length == 1) {
            stops.add(new ColorStop(0.0, colors[0].asBukkitColor()));
            stops.add(new ColorStop(1.0, colors[0].asBukkitColor()));
            return stops;
        }
        for (int i = 0; i < colors.length; i++) {
            stops.add(new ColorStop((double) i / (colors.length - 1), colors[i].asBukkitColor()));
        }
        return stops;
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
            case 0 -> "gradient from " + color1.toString(event, debug) + " to " + color2.toString(event, debug) + " along axis";
            case 1 -> "gradient from colors along axis";
            case 2 -> "radial gradient";
            case 3 -> "angular gradient";
            case 4 -> "spherical gradient";
            default -> "gradient modifier";
        };
    }
}
