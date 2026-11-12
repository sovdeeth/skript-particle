package com.sovdee.skriptparticles.skript.shaders.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.AbstractGradientModifier;
import com.sovdee.skriptparticles.rendering.shaders.ColorStop;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Name("Gradient Modifier Colours")
@Description({
        "The colours of a gradient modifier, in order.",
        "Setting them, or adding to them, spaces every colour evenly along the gradient again, " +
                "replacing any stops added at uneven positions.",
        "Inside a modifier constructor section, this can leave out the modifier and use the new one."
})
@Example("set gradient colours of {_gradient} to red, white and blue")
@Example("add black to {_gradient}'s gradient colors")
@Example("send \"%gradient colours of {_gradient}%\"")
@Since("2.0.0")
public class ExprGradientColors extends PropertyExpression<PointModifier, Color> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprGradientColors.class, Color.class,
                                "gradient colo[u]rs", "pointmodifiers", true)
                        .supplier(ExprGradientColors::new)
                        .build());
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult result) {
        setExpr((Expression<? extends PointModifier>) expressions[0]);
        return true;
    }

    @Override
    protected Color[] get(Event event, PointModifier[] source) {
        List<Color> colors = new ArrayList<>();
        for (PointModifier<?> modifier : source) {
            if (modifier instanceof AbstractGradientModifier gradient) {
                for (ColorStop stop : gradient.getStops())
                    colors.add(ColorRGB.fromBukkitColor(stop.color()));
            }
        }
        return colors.toArray(new Color[0]);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD -> new Class[]{Color[].class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        List<org.bukkit.Color> given = Arrays.stream(delta).map(c -> ((Color) c).asBukkitColor()).toList();
        for (PointModifier<?> modifier : getExpr().getArray(event)) {
            if (!(modifier instanceof AbstractGradientModifier gradient))
                continue;
            List<org.bukkit.Color> colors = new ArrayList<>();
            if (mode == ChangeMode.ADD) {
                for (ColorStop stop : gradient.getStops())
                    colors.add(stop.color());
            }
            colors.addAll(given);
            gradient.setColors(colors);
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Color> getReturnType() {
        return Color.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "gradient colours of " + getExpr().toString(event, debug);
    }
}
