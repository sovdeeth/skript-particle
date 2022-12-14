package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprShapeOrientation extends SimplePropertyExpression<Shape, Quaternion> {

    static {
        register(ExprShapeOrientation.class, Quaternion.class, "orientation","shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                for (Shape shape : getExpr().getArray(event)) {
                    shape.orientation((Quaternion) delta[0]);
                }
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getArray(event)) {
                    shape.resetOrientation();
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
    public @Nullable Quaternion convert(Shape shape) {
        return shape.orientation();
    }

    @Override
    public @NotNull Class<? extends Quaternion> getReturnType() {
        return Quaternion.class;
    }
}
