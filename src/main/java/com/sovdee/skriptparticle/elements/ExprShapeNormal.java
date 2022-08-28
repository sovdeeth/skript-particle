package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprShapeNormal extends SimplePropertyExpression<Shape, Vector> {

    static {
        register(ExprShapeNormal.class, Vector.class, "normal [vector]","shapes");
    }

    @Override
    public Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.REMOVE_ALL)
            return new Class[]{Vector.class};
        return super.acceptChange(mode);
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        switch (mode) {
            case SET:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setNormal((Vector) delta[0]);
                }
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getArray(e)) {
                    shape.setNormal(new Vector(0, 1, 0));
                }
                break;
        }
    }

    @Override
    public @NotNull Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "normal vector of shapes";
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        return shape.getNormal();
    }
}
