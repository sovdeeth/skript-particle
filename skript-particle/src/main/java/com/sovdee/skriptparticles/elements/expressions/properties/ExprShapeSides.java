package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.shapes.PolyShape;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Side Count - Polygonal/Polyhedral")
@Description({
        "Returns the number of sides of a polygonal or polyhedral shape. This determines how many sides the shape has.",
        "Changing this will change the number of sides of the shape accordingly, with a minimum of 3.",
        "Note that custom polygons will return their side count, but will not be affected by this expression. ",
        "Polyhedrons will return their face count, and can only be set to 4, 8, 12, or 20."
})
@Examples({
        "set sides of {_shape} to 5",
        "set {_shape}'s side count to 6",
        "send sides of {_shape}"
})
@Since("1.0.0")
public class ExprShapeSides extends SimplePropertyExpression<Shape, Integer> {

    static {
        register(ExprShapeSides.class, Integer.class, "side(s| count)", "shapes");
    }

    @Override
    @Nullable
    public Integer convert(Shape shape) {
        if (shape instanceof PolyShape ps)
            return ps.getSides();
        return null;
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
        int change = (delta != null) ? ((Number) delta[0]).intValue() : 3;

        switch (mode) {
            case REMOVE:
                change = -change;
            case ADD:
                for (Shape shape : getExpr().getArray(event)) {
                    if (shape instanceof PolyShape ps)
                        ps.setSides(Math.max(3, ps.getSides() + change));
                }
                break;
            case RESET:
            case DELETE:
            case SET:
                change = Math.max(3, change);
                for (Shape shape : getExpr().getArray(event)) {
                    if (shape instanceof PolyShape ps)
                        ps.setSides(change);
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "side count";
    }

}
