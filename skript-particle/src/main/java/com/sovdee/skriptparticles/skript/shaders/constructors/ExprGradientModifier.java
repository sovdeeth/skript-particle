package com.sovdee.skriptparticles.skript.shaders.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.skriptparticles.rendering.shaders.AbstractGradientModifier;
import com.sovdee.skriptparticles.rendering.shaders.GradientModifier;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Arrays;

@Name("Gradient Modifier Constructor")
@Description({
        "Creates a modifier that colours a shape's points with a gradient. This only affects particles that can be coloured, like dust.",
        "The colours, either a list or 'from red to blue', are spaced evenly along an input: the x, y (default) or z axis, the draw order, the radius " +
                "(distance from the shape's y axis), the spherical distance (distance from its center), or the angle around its y axis. " +
                "'radial', 'spherical' and 'angular' gradients are short for gradients along those inputs.",
        "Use 'add color stop' for colours at uneven positions.",
        "This can take a section to set the rest of the modifier's properties, like its easing. " +
                "Inside it, 'the new modifier' is the gradient being created, and modifier properties use it by default."
})
@Example("add a gradient from red to blue along the y axis to modifiers of {_shape}")
@Example("add a colour gradient from red, yellow, green and blue along the angle to modifiers of {_shape}")
@Example("add a radial gradient of white and black to modifiers of {_shape}")
@Example("set modifiers of {_shape} to a spherical gradient of yellow and orange")
@Example("""
        set {_gradient} to a gradient of red and blue along the draw order:
            set modifier easing to ease in-out sine
            add color stop at 0.5 colored white to the new modifier
        """)
@Since("2.0.0")
public class ExprGradientModifier extends ModifierConstructorExpression {

    // a list of colours, or the two-colour "from red to blue"
    private static final String COLORS = "((from|of) %-colors%|from %-color% to %-color%)";

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprGradientModifier.class, PointModifier.class)
                .supplier(ExprGradientModifier::new)
                .addPatterns(
                        "[a] [colo[u]r] gradient " + COLORS + " [along [the] " + ModifierInputs.ALL_INPUTS + "]",
                        "[a[n]] (:radial|:angular|:spherical) [colo[u]r] gradient " + COLORS
                )
                .build());
    }

    private @Nullable Expression<Color> colors;
    private @Nullable Expression<Color> from, to;
    private StandardInput input;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        colors = (Expression<Color>) expressions[0];
        from = (Expression<Color>) expressions[1];
        to = (Expression<Color>) expressions[2];
        if (result.hasTag("radial")) {
            input = StandardInput.RADIUS;
        } else if (result.hasTag("angular")) {
            input = StandardInput.ANGLE;
        } else if (result.hasTag("spherical")) {
            input = StandardInput.SPHERICAL;
        } else {
            StandardInput chosen = ModifierInputs.fromTags(result);
            input = chosen == null ? StandardInput.Y : chosen;
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        Color[] values;
        if (colors != null) {
            values = colors.getArray(event);
        } else {
            Color start = from.getSingle(event), end = to.getSingle(event);
            if (start == null || end == null)
                return null;
            values = new Color[]{start, end};
        }
        if (values.length == 0)
            return null;
        return new GradientModifier(AbstractGradientModifier.evenlySpaced(
                Arrays.stream(values).map(Color::asBukkitColor).toList()), input);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        String of = colors != null ? "of " + colors.toString(event, debug)
                : "from " + from.toString(event, debug) + " to " + to.toString(event, debug);
        return "a gradient " + of + " along the " + ModifierInputs.toString(input);
    }
}
