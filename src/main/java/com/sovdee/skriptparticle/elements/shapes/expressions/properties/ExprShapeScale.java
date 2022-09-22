package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeScale extends SimplePropertyExpression<Shape, Number> {

    static {
        register(ExprShapeScale.class, Number.class, "scale", "shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if ((mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.REMOVE) && (delta == null || delta.length == 0))
            return;
        Shape[] shapes = getExpr().getArray(e);
        if (shapes.length == 0)
            return;

        switch (mode) {
            case ADD:
                for (Shape shape : shapes) {
                    shape.scale(shape.scale() + ((Number) delta[0]).doubleValue());
                }
                break;
            case REMOVE:
                for (Shape shape : shapes) {
                    shape.scale(shape.scale() - ((Number) delta[0]).doubleValue());
                }
                break;
            case SET:
                for (Shape shape : shapes) {
                    shape.scale(((Number) delta[0]).doubleValue());
                }
                break;
            case DELETE:
            case REMOVE_ALL:
            case RESET:
                for (Shape shape : shapes) {
                    shape.scale(1);
                }
                break;
        }
    }

    @Override
    protected String getPropertyName() {
        return "scale";
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        return shape.scale();
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
