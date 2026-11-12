package com.sovdee.skriptparticles.skript.shaders.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.WaveModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Wave Modifier Properties")
@Description({
        "The amplitude, frequency and phase of a wave modifier.",
        "The amplitude is how far points are moved, in blocks. The frequency is how many full waves fit along the input. " +
                "The phase shifts the wave along, in degrees: 180 flips it. Radians work too, since Skript reads '3.14 radians' as degrees.",
        "Inside a modifier constructor section, these can leave out the modifier and use the new one."
})
@Example("set wave amplitude of {_wave} to 1.5")
@Example("add 1 to {_wave}'s wave frequency")
@Example("""
        set {_wave} to a wave with amplitude 0.5 and frequency 1 along the y axis:
            set wave phase to 90
        """)
@Since("2.0.0")
public class ExprWaveProperty extends SimplePropertyExpression<PointModifier, Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprWaveProperty.class, Number.class,
                                "wave (:amplitude|:frequency|:phase)", "pointmodifiers", true)
                        .supplier(ExprWaveProperty::new)
                        .build());
    }

    private enum Part { AMPLITUDE, FREQUENCY, PHASE }

    private Part part;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        part = Part.valueOf(result.tags.getFirst().toUpperCase());
        return super.init(expressions, matchedPattern, isDelayed, result);
    }

    @Override
    public @Nullable Number convert(PointModifier modifier) {
        if (!(modifier instanceof WaveModifier wave))
            return null;
        return switch (part) {
            case AMPLITUDE -> wave.getAmplitude();
            case FREQUENCY -> wave.getFrequency();
            case PHASE -> Math.toDegrees(wave.getPhase());
        };
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            case RESET, DELETE -> part == Part.PHASE ? new Class[0] : null;
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        double value = delta == null ? 0 : ((Number) delta[0]).doubleValue();
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (!(modifier instanceof WaveModifier wave))
                continue;
            double current = switch (part) {
                case AMPLITUDE -> wave.getAmplitude();
                case FREQUENCY -> wave.getFrequency();
                case PHASE -> Math.toDegrees(wave.getPhase());
            };
            double result = switch (mode) {
                case SET -> value;
                case ADD -> current + value;
                case REMOVE -> current - value;
                default -> 0;
            };
            switch (part) {
                case AMPLITUDE -> wave.setAmplitude(result);
                case FREQUENCY -> wave.setFrequency(result);
                case PHASE -> wave.setPhase(Math.toRadians(result));
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "wave " + part.name().toLowerCase();
    }
}
