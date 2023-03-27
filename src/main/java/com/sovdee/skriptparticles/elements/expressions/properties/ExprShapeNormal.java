package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

@Name("Normal Vector of Shape")
@Description({
        "Returns the normal vector of a shape. This, by default, is the vector (0, 1, 0) which points directly up." +
        "If the shape is rotated, this will be the vector that is pointing up after the rotation. Treat it as what the shape thinks is \"up\".",
        "Changing this will rotate the shape accordingly. Resetting or deleting it will set it back to the default orientation.",
})
@Examples({
        "set {_shape}'s normal vector to vector(1, 0, 0)",
        "set {_v} to {_shape}'s normal",
        "reset {_shape}'s normal vector"
})
@Since("1.0.0")
public class ExprShapeNormal extends SimplePropertyExpression<Shape, Vector> {

    static {
        PropertyExpression.register(ExprShapeNormal.class, Vector.class, "normal [vector]", "shapes");
    }

    @Override
    public Vector convert(Shape shape) {
        return shape.getRelativeYAxis(false);
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{Vector.class};
            case ADD, REMOVE, REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                for (Shape shape : getExpr().getArray(event)) {
                    shape.setOrientation(Quaternion.rotationToVector((Vector) delta[0]));
                }
                break;
            case RESET:
            case DELETE:
                for (Shape shape : getExpr().getArray(event)) {
                    shape.setOrientation(Quaternion.IDENTITY);
                }
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected String getPropertyName() {
        return "normal vector or relative y-axis";
    }

}
