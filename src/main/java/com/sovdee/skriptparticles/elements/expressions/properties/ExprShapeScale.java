package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;

import org.jetbrains.annotations.Nullable;

@Name("Shape Scale")
@Description({
        "Returns the scale of a shape. This is will scale the shape up or down accordingly, with respect to its center.",
        "This does not affect the number of particles drawn, so a scaled up shape will look more sparse. A negative scale will flip the shape inside-out.",
        "Changing this will scale the shape accordingly. Resetting or deleting it will set it back to the default scale of 1."
})
@Examples({
        "set {_shape}'s scale to 2",
        "set scale of {_shape} to 0.5",
        "set scale of {_shape} to 2 * (scale of {_shape})",
        "reset {_shape}'s scale"
})
@Since("1.0.0")
public class ExprShapeScale extends SimplePropertyExpression<Shape, Number> {

    static {
        PropertyExpression.register(ExprShapeScale.class, Number.class, "scale", "shapes");
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        return shape.getScale();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE, ADD, REMOVE -> new Class[]{Number.class};
            case REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        if ((mode == ChangeMode.ADD || mode == ChangeMode.SET || mode == ChangeMode.REMOVE) && (delta == null || delta.length == 0))
            return;
        Shape[] shapes = getExpr().getArray(event);
        if (shapes.length == 0)
            return;

        double deltaValue = ((Number) delta[0]).doubleValue();
        switch (mode) {
            case REMOVE:
                deltaValue = -deltaValue;
            case ADD:
                for (Shape shape : shapes) {
                    shape.setScale(shape.getScale() + deltaValue);
                }
                break;
            case DELETE:
            case RESET:
                deltaValue = 1;
            case SET:
                for (Shape shape : shapes) {
                    shape.setScale(deltaValue);
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "scale";
    }

}
