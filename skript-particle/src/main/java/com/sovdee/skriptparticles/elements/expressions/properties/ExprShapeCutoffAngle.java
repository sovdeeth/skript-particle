package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.shapes.CutoffShape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Cutoff Angle")
@Description({
        "The cutoff angle of a shape in degrees. This determines the portion of the whole shape that will be drawn in shapes like arcs and spherical caps. " +
                "For example, a cutoff angle of 90 on an arc will create a quarter of a circle.",
        "Note that an arc can range from 0 to 360 degrees, while a spherical cap can range from 0 to 180 degrees."
})
@Examples({
        "set {_shape}'s cutoff angle to 90",
        "set cutoff angle of {_shape} to 180",
        "reset {_shape}'s cutoff angle"
})
@Since("1.0.0")
public class ExprShapeCutoffAngle extends SimplePropertyExpression<CutoffShape, Number> {

    static {
        register(ExprShapeCutoffAngle.class, Number.class, "cutoff angle", "cutoffshapes");
    }

    @Override
    public @Nullable Number convert(CutoffShape shape) {
        return shape.getCutoffAngle();
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
        double angle = 0;
        if (delta != null && delta.length != 0)
            angle = ((Number) delta[0]).doubleValue();
        angle = Math.toRadians(angle);
        switch (mode) {
            case REMOVE:
                angle = -angle;
            case ADD:
                for (CutoffShape shape : getExpr().getArray(event)) {
                    shape.setCutoffAngle(shape.getCutoffAngle() + angle);
                }
                break;
            case DELETE:
            case RESET:
            case SET:
                for (CutoffShape shape : getExpr().getArray(event)) {
                    shape.setCutoffAngle(angle);
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
                break;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "cutoff angle";
    }

}
