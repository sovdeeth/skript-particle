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
import com.sovdee.shapes.modifiers.ScalingModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Scale Modifier Start / End")
@Description({
        "The scale a scale modifier uses where its input is lowest (the start) and where it is highest (the end).",
        "Inside a modifier constructor section, these can leave out the modifier and use the new one."
})
@Example("set scale start of {_taper} to 2")
@Example("set {_taper}'s scale end to 0.5")
@Since("2.0.0")
public class ExprScaleModifierRange extends SimplePropertyExpression<PointModifier, Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprScaleModifierRange.class, Number.class,
                                "scale (:start|end)", "pointmodifiers", true)
                        .supplier(ExprScaleModifierRange::new)
                        .build());
    }

    private boolean start;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        start = result.hasTag("start");
        return super.init(expressions, matchedPattern, isDelayed, result);
    }

    @Override
    public @Nullable Number convert(PointModifier modifier) {
        if (!(modifier instanceof ScalingModifier scale))
            return null;
        return start ? scale.getStartScale() : scale.getEndScale();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            case RESET, DELETE -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        double value = delta == null ? 1 : ((Number) delta[0]).doubleValue();
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (!(modifier instanceof ScalingModifier scale))
                continue;
            double current = start ? scale.getStartScale() : scale.getEndScale();
            double result = switch (mode) {
                case ADD -> current + value;
                case REMOVE -> current - value;
                // set, or reset/delete back to 1
                default -> value;
            };
            if (start) {
                scale.setStartScale(result);
            } else {
                scale.setEndScale(result);
            }
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return start ? "scale start" : "scale end";
    }
}
