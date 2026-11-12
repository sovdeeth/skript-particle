package com.sovdee.skriptparticles.skript.shaders.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.TwistModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Twist Modifier Angle")
@Description({
        "How far a twist modifier turns the points where its input is highest, in degrees. Radians work too, since Skript reads '3.14 radians' as degrees.",
        "Inside a modifier constructor section, this can leave out the modifier and use the new one."
})
@Example("set twist angle of {_twist} to 720")
@Example("add 90 to {_twist}'s twist angle")
@Since("2.0.0")
public class ExprTwistAngle extends SimplePropertyExpression<PointModifier, Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprTwistAngle.class, Number.class, "twist angle", "pointmodifiers", true)
                        .supplier(ExprTwistAngle::new)
                        .build());
    }

    @Override
    public @Nullable Number convert(PointModifier modifier) {
        return modifier instanceof TwistModifier twist ? Math.toDegrees(twist.getTotalAngle()) : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null)
            return;
        double value = Math.toRadians(((Number) delta[0]).doubleValue());
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (!(modifier instanceof TwistModifier twist))
                continue;
            twist.setTotalAngle(switch (mode) {
                case ADD -> twist.getTotalAngle() + value;
                case REMOVE -> twist.getTotalAngle() - value;
                default -> value;
            });
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "twist angle";
    }
}
