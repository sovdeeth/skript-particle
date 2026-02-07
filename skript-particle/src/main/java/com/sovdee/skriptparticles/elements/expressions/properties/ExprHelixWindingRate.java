package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.Helix;
import com.sovdee.skriptparticles.shapes.DrawableShape;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Helix Winding Rate")
@Description({
        "Gets or sets the winding rate of a helix. The winding rate is the number of times the helix wraps around the axis per block.",
        "The default winding rate is 1 loop per block, which delete or reset will set it to. The winding rate must be positive and non-zero."
})
@Examples({
        "set {_helix} to helix with radius 5, height 10, and winding rate 2 loops per block",
        "set winding rate of {_helix} to 1",
        "set winding rate of {_helix} to 1/10",
        "set winding rate of {_helix} to 10.4"
})
@Since("1.0.0")
public class ExprHelixWindingRate extends SimplePropertyExpression<Shape, Number> {

    static {
        register(ExprHelixWindingRate.class, Number.class, "winding rate", "shapes");
    }

    @Override
    @Nullable
    public Number convert(Shape shape) {
        if (shape instanceof DrawableShape ds && ds.getShape() instanceof Helix helix)
            return 1 / (2 * Math.PI * helix.getSlope());
        return null;
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, ADD, REMOVE, RESET -> new Class[]{Number.class};
            default -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        Shape[] helices = getExpr().getArray(event);
        if (delta == null && mode != ChangeMode.DELETE && mode != ChangeMode.RESET) return;
        double deltaValue = (delta == null) ? 1 : ((Number) delta[0]).doubleValue();
        switch (mode) {
            case REMOVE:
                deltaValue = -deltaValue;
            case ADD:
                for (Shape shape : helices) {
                    if (shape instanceof DrawableShape ds && ds.getShape() instanceof Helix helix)
                        helix.setSlope(helix.getSlope() / (1 + helix.getSlope() * deltaValue * 2 * Math.PI));
                }
                break;
            case DELETE:
            case RESET:
            case SET:
                for (Shape shape : helices) {
                    if (shape instanceof DrawableShape ds && ds.getShape() instanceof Helix helix)
                        helix.setSlope(1 / (2 * Math.PI * deltaValue));
                }
                break;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "winding rate";
    }

}
