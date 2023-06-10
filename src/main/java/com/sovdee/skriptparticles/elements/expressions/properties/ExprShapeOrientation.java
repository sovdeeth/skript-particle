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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Name("Shape Orientation")
@Description({
        "The orientation of a shape. This is the rotation that is applied to the shape before drawing.",
        "Changing this will rotate the shape accordingly. Resetting or deleting it will set it back to the default orientation of (0, 0, 0, 1).",
        "The expression returns a quaternion. You can use the quaternion and axis angle functions or the rotation expression to create an orientation."
})
@Examples({
        "set {_shape}'s orientation to quaternion(0, 0, 0, 1)",
        "set {_shape}'s orientation to axisAngle(90, 0, 1, 0)",
        "set {_shape}'s orientation to {_shape2}'s orientation",
        "reset {_shape}'s orientation"
})
@Since("1.0.0")
public class ExprShapeOrientation extends SimplePropertyExpression<Shape, Quaternion> {

    static {
        PropertyExpression.register(ExprShapeOrientation.class, Quaternion.class, "orientation", "shapes");
    }

    @Override
    @Nullable
    public Quaternion convert(Shape shape) {
        return shape.getOrientation();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{Quaternion.class};
            case ADD, REMOVE, REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                for (Shape shape : getExpr().getArray(event)) {
                    shape.setOrientation((Quaternion) delta[0]);
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
    public Class<? extends Quaternion> getReturnType() {
        return Quaternion.class;
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "orientation";
    }

}
