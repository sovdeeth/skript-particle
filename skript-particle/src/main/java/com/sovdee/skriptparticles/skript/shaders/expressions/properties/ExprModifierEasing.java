package com.sovdee.skriptparticles.skript.shaders.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.modifiers.Easable;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.PointModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Modifier Easing")
@Description({
        "The easing of a scale, twist or gradient modifier: the curve that reshapes its input before it is used. " +
                "For example, ease in quadratic makes a taper stay wide for longer before narrowing.",
        "Resetting or deleting it makes the modifier linear again.",
        "Inside a modifier constructor section, this can leave out the modifier and use the new one."
})
@Example("set modifier easing of {_taper} to ease in quadratic")
@Example("""
        set {_twist} to a twist of 360 degrees along the y axis:
            set modifier easing to ease in-out sine
        """)
@Example("reset modifier easing of {_twist}")
@Since("2.0.0")
public class ExprModifierEasing extends SimplePropertyExpression<PointModifier, EasingFunction> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprModifierEasing.class, EasingFunction.class,
                                "modifier easing", "pointmodifiers", true)
                        .supplier(ExprModifierEasing::new)
                        .build());
    }

    @Override
    public @Nullable EasingFunction convert(PointModifier modifier) {
        return modifier instanceof Easable easable ? easable.getEasing() : null;
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{EasingFunction.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        EasingFunction easing = delta == null || delta.length == 0 ? EasingFunction.LINEAR : (EasingFunction) delta[0];
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (modifier instanceof Easable easable)
                easable.setEasing(easing);
        }
    }

    @Override
    public Class<? extends EasingFunction> getReturnType() {
        return EasingFunction.class;
    }

    @Override
    protected String getPropertyName() {
        return "modifier easing";
    }
}
