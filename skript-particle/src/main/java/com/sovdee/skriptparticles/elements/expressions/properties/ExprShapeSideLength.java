package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.PolyShape;
import com.sovdee.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Side Length - Polygonal/Polyhedral")
@Description({
        "Returns the side length of a polygonal/polyhedral shape. This determines how long each side of the shape is.",
        "Changing this will change the side length of the shape accordingly. Likewise, this changes the radius of the shape. " +
                "Resetting this will set the side length to the default value of 1 and non-positive values will be set to 0.001.",
        "Note that changing this property will not affect custom polygons, only regular polygons and polyhedrons."
})
@Examples({
        "set side length of {_shape} to 5",
        "set {_shape}'s side length to 6",
        "send side length of {_shape}"
})
@Since("1.0.0")
public class ExprShapeSideLength extends SimplePropertyExpression<Shape, Number> {

    static {
        register(ExprShapeSideLength.class, Number.class, "side length", "shapes");
    }

    @Override
    @Nullable
    public Number convert(Shape shape) {
        if (shape instanceof PolyShape ps)
            return ps.getSideLength();
        return null;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, DELETE, RESET -> new Class[]{Number.class};
            case REMOVE_ALL -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        double change = (delta != null) ? ((Number) delta[0]).doubleValue() : 1;

        switch (mode) {
            case REMOVE:
                change = -change;
            case ADD:
                for (Shape shape : getExpr().getArray(event)) {
                    if (shape instanceof PolyShape ps)
                        ps.setSideLength(Math.max(0.001, ps.getSideLength() + change));
                }
                break;
            case RESET:
            case DELETE:
            case SET:
                change = Math.max(0.001, change);
                for (Shape shape : getExpr().getArray(event)) {
                    if (shape instanceof PolyShape ps)
                        ps.setSideLength(change);
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
        return "side length";
    }

}
