package com.sovdee.skriptparticles.skript.shaders.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.MotionModifier;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Motion Modifier Velocity")
@Description({
        "The velocity of a motion modifier made with a global or relative velocity. " +
                "Whether it is global or relative stays the same when it is changed.",
        "Motion modifiers made with clockwise, counterclockwise, inwards or outwards have no velocity.",
        "Inside a modifier constructor section, this can leave out the modifier and use the new one."
})
@Example("set motion velocity of {_wind} to vector(1, 0, 0)")
@Example("add vector(0, 0.5, 0) to motion velocity of modifiers of {_shape}")
@Since("2.0.0")
public class ExprMotionVelocity extends SimplePropertyExpression<PointModifier, Vector> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprMotionVelocity.class, Vector.class, "motion velocity", "pointmodifiers", true)
                        .supplier(ExprMotionVelocity::new)
                        .build());
    }

    @Override
    public @Nullable Vector convert(PointModifier modifier) {
        if (modifier instanceof MotionModifier motion && motion.hasVelocity())
            return VectorConversion.toBukkit(motion.getVelocity());
        return null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Vector.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null)
            return;
        Vector given = (Vector) delta[0];
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (!(modifier instanceof MotionModifier motion) || !motion.hasVelocity())
                continue;
            Vector result = switch (mode) {
                case ADD -> VectorConversion.toBukkit(motion.getVelocity()).add(given);
                case REMOVE -> VectorConversion.toBukkit(motion.getVelocity()).subtract(given);
                default -> given;
            };
            motion.setVelocity(VectorConversion.toJOML(result));
        }
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected String getPropertyName() {
        return "motion velocity";
    }
}
