package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.RadialShape;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Shape Radius")
@Description({
        "The radius of a shape. For circular or spherical shapes, this is the distance from the center to the edge. " +
        "For shapes like regular polygons, this is the distance from the center to a corner.",
        "Changing this will change the size of the shape. Resetting or deleting it will set it back to the default radius of 1."
})
@Examples({
        "set {_shape}'s radius to 5",
        "set radius of {_shape} to 10",
        "reset {_shape}'s radius"
})
@Since("1.0.0")
public class ExprShapeRadius extends SimplePropertyExpression<Shape, Number> {

    static {
        PropertyExpression.register(ExprShapeRadius.class, Number.class, "radius", "radialshapes");
    }

    @Override
    @Nullable
    public Number convert(Shape shape) {
        if (shape instanceof RadialShape){
            return ((RadialShape) shape).getRadius();
        }
        return null;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
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

        if (delta[0] == null)
            return;

        List<RadialShape> radialShapes = new ArrayList<>();
        for (Shape shape : shapes) {
            if (shape instanceof RadialShape){
                radialShapes.add((RadialShape) shape);
            }
        }

        double deltaValue = ((Number) delta[0]).doubleValue();
        switch (mode) {
            case REMOVE:
                deltaValue = -deltaValue;
            case ADD:
                for (RadialShape shape : radialShapes) {
                    shape.setRadius(Math.max(0.001, shape.getRadius() + deltaValue));
                }
                break;
            case RESET:
            case DELETE:
                deltaValue = 1;
            case SET:
                deltaValue = Math.max(0.001, deltaValue);
                for (RadialShape shape : radialShapes) {
                    shape.setRadius(deltaValue);
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
        return "radius";
    }

}
