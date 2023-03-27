package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Name("Shape Offset")
@Description({
        "Returns the offset vector of a shape. This is the vector that is added to the location of the shape before drawing.",
        "Changing this will move the shape accordingly. Resetting or deleting it will set it back to the default offset of (0, 0, 0)."
})
@Examples({
        "set {_shape}'s offset vector to vector(1, 0, 0)",
        "set offset of {_shape} to vector(0, 10, 0)",
        "reset {_shape}'s offset vector"
})
public class ExprShapeOffset extends SimplePropertyExpression<Shape, Vector> {

    static {
        PropertyExpression.register(ExprShapeOffset.class, Vector.class, "offset [vector]","shapes");
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        return shape.getOffset();
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
                if (delta == null || delta.length == 0)
                    return;
                for (Shape shape : getExpr().getArray(event)) {
                    shape.setOffset(((Vector) delta[0]));
                }
                break;
            case RESET:
            case DELETE:
                for (Shape shape : getExpr().getArray(event)) {
                    shape.setOffset(new Vector(0, 0, 0));
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
    public @NotNull Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected @NotNull String getPropertyName() {
        return "offset vector of shapes";
    }

}
