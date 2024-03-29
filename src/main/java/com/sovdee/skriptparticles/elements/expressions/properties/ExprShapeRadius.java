package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.RadialShape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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
public class ExprShapeRadius extends SimplePropertyExpression<RadialShape, Number> {

    static {
        PropertyExpression.register(ExprShapeRadius.class, Number.class, "radius", "radialshapes");
    }

    @Override
    @Nullable
    public Number convert(RadialShape shape) {
        return shape.getRadius();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE, ADD, REMOVE -> new Class[]{Number.class};
            case REMOVE_ALL -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        RadialShape[] radialShapes = getExpr().getArray(event);
        if (radialShapes.length == 0)
            return;

        double deltaValue = (delta != null) ? ((Number) delta[0]).doubleValue() : 1;
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
