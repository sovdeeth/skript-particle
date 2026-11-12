package com.sovdee.skriptparticles.skript.shaders.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.SpatialAxis;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.modifiers.WaveModifier;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Wave Modifier Constructor")
@Description({
        "Creates a modifier that moves a shape's points along an axis by amplitude × sin(360° × frequency × input + phase), " +
                "so a frequency of 1 is one full wave over the input.",
        "The input can be the x, y or z axis, the draw order (default), the radius (distance from the shape's y axis), " +
                "the spherical distance (distance from its center), or the angle around its y axis. " +
                "By default, it moves points along the y axis.",
        "This can take a section to set the rest of the modifier's properties, like its phase or easing. " +
                "Inside it, 'the new modifier' is the modifier being created, and modifier properties use it by default."
})
@Example("add a wave with amplitude 0.5 and frequency 2 to modifiers of {_shape}")
@Example("add a wave with amplitude 0.3 and frequency 4 along the x axis displacing the y axis to modifiers of {_shape}")
@Example("""
        add a wave with amplitude 0.5 and frequency 1 along the angle to modifiers of {_shape}:
            set wave phase to 90
        """)
@Since("2.0.0")
public class ExprWaveModifier extends ModifierConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprWaveModifier.class, PointModifier.class)
                .supplier(ExprWaveModifier::new)
                .addPattern("[a] wave with amplitude %number% [and] frequency %number% [along [the] " + ModifierInputs.ALL_INPUTS + "]"
                        + " [displacing [the] (out_x:x|out_y:y|out_z:z)[-| ]axis]")
                .build());
    }

    private Expression<Number> amplitude, frequency;
    private StandardInput input;
    private SpatialAxis outputAxis = SpatialAxis.Y;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        amplitude = (Expression<Number>) expressions[0];
        frequency = (Expression<Number>) expressions[1];
        StandardInput chosen = ModifierInputs.fromTags(result);
        input = chosen == null ? StandardInput.T : chosen;
        for (String tag : result.tags) {
            if (tag.startsWith("out_"))
                outputAxis = SpatialAxis.valueOf(tag.substring(4).toUpperCase());
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        Number amp = amplitude.getSingle(event), freq = frequency.getSingle(event);
        if (amp == null || freq == null)
            return null;
        return new WaveModifier(amp.doubleValue(), freq.doubleValue(), 0.0, input, outputAxis);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a wave with amplitude " + amplitude.toString(event, debug) + " and frequency " + frequency.toString(event, debug)
                + " along the " + ModifierInputs.toString(input) + " displacing the " + outputAxis.name().toLowerCase() + " axis";
    }
}
