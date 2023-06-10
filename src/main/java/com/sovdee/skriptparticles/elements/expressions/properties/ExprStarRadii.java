package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Star;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Star Radii")
@Description({
        "Returns the inner or outer radius of a star. The inner radius is the distance from the center of the star to " +
                "the innermost points, and likewise the outer radius is the distance to the tips of the star.",
        "Changing this will change the size of the star. Both radii must be greater than 0."
})
@Examples({
        "set {_star} to a star with 5 points, inner radius 1, and outer radius 2",
        "set {_star}'s inner radius to 2",
        "set {_star}'s outer radius to 3"
})
@Since("1.0.1")
public class ExprStarRadii extends SimplePropertyExpression<Shape, Number> {

    static {
        register(ExprStarRadii.class, Number.class, "(:inner|outer) radius", "shapes");
    }

    private boolean isInner;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        isInner = parseResult.hasTag("inner");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        if (shape instanceof Star star) {
            return (isInner ? star.getInnerRadius() : star.getOuterRadius());
        }
        return null;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        double deltaValue = ((Number) delta[0]).doubleValue();
        Shape[] shapes = getExpr().getArray(event);

        switch (mode) {
            case REMOVE:
                deltaValue = -deltaValue;
            case ADD:
                for (Shape shape : shapes) {
                    if (shape instanceof Star star) {
                        if (isInner) {
                            star.setInnerRadius(Math.max(star.getInnerRadius() + deltaValue, MathUtil.EPSILON));
                        } else {
                            star.setOuterRadius(Math.max(star.getOuterRadius() + deltaValue, MathUtil.EPSILON));
                        }
                    }
                }
                break;
            case SET:
                deltaValue = Math.max(deltaValue, MathUtil.EPSILON);
                for (Shape shape : shapes) {
                    if (shape instanceof Star star) {
                        if (isInner) {
                            star.setInnerRadius(deltaValue);
                        } else {
                            star.setOuterRadius(deltaValue);
                        }
                    }
                }
                break;
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Double.class;
    }

    @Override
    protected String getPropertyName() {
        return (isInner ? "inner" : "outer") + " radius";
    }
}
