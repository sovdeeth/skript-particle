package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprShapeNormal extends SimplePropertyExpression<Shape, Vector> {

    static {
        register(ExprShapeNormal.class, Vector.class, "normal [vector]", "shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.REMOVE_ALL)
            return new Class[]{Vector.class};
        return super.acceptChange(mode);
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                for (Shape shape : getExpr().getAll(e)) {
                    shape.orientation(Quaternion.rotationToVector((Vector) delta[0]));
                }
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getAll(e)) {
                    shape.resetOrientation();
                }
                break;
            default:
                assert false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "normal vector or relative y-axis";
    }

    @Override
    public Vector convert(Shape shape) {
        return shape.relativeYAxis();
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }
}
