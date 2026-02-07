package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.Star;
import com.sovdee.skriptparticles.shapes.DrawableShape;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Star Points")
@Description({
        "Returns the number of points on a star. This is the number of points on the star, not the number of particles drawn.",
        "Changing this will change the number of points on the star. The number of points must be at least 2."
})
@Examples({
        "set {_shape}'s star points to 5",
        "set star points of {_shape} to 10",
        "set star points of {_shape} to 2 * (star points of {_shape})"
})
@Since("1.0.1")
public class ExprStarPoints extends SimplePropertyExpression<Shape, Number> {

    static {
        register(ExprStarPoints.class, Number.class, "star points", "shapes");
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        if (shape instanceof DrawableShape ds && ds.getShape() instanceof Star star)
            return star.getStarPoints();
        return null;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        Shape[] shapes = getExpr().getArray(event);

        int deltaValue = ((Number) delta[0]).intValue();
        switch (mode) {
            case REMOVE:
                deltaValue = -deltaValue;
            case ADD:
                for (Shape shape : shapes) {
                    if (shape instanceof DrawableShape ds && ds.getShape() instanceof Star star) {
                        star.setStarPoints(Math.max(star.getStarPoints() + deltaValue, 2));
                    }
                }
                break;
            case SET:
                deltaValue = Math.max(deltaValue, 2);
                for (Shape shape : shapes) {
                    if (shape instanceof DrawableShape ds && ds.getShape() instanceof Star star) {
                        star.setStarPoints(deltaValue);
                    }
                }
                break;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "star points";
    }

}
