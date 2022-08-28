package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprShapeOrientation extends SimplePropertyExpression<Shape, Number> {
    static {
        register(ExprShapeOrientation.class, Number.class, "orientation","shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if ((delta == null || delta.length == 0) && (mode == Changer.ChangeMode.ADD || mode == Changer.ChangeMode.SET))
            return;
        for (Shape shape : getExpr().getArray(e)) {
            shape.setNeedsUpdate(true);
        }
        switch (mode) {
            case ADD:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setRotation(shape.getRotation() + Math.toRadians(((Number) delta[0]).doubleValue()));
                }
                break;
            case REMOVE:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setRotation(shape.getRotation() - Math.toRadians(((Number) delta[0]).doubleValue()));
                }
                break;
            case SET:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setRotation(Math.toRadians(((Number) delta[0]).doubleValue()));
                }
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setRotation(0);
                }
                break;
            default:
                assert false;
        }
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "orientation";
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        return shape.getRotation();
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
