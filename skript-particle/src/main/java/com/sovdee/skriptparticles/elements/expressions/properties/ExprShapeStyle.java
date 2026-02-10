package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Style")
@Description({
        "Returns the style of a shape. This determines how the shape is drawn. See the shape style type for more information.",
        "Changing this will change the style of the shape accordingly."
})
@Examples({
        "set style of {_shape} to solid",
        "set {_shape}'s style to wireframe",
        "set style of {_shape} to hollow"
})
@Since("1.0.0")
public class ExprShapeStyle extends SimplePropertyExpression<Shape, SamplingStyle> {

    static {
        PropertyExpression.register(ExprShapeStyle.class, SamplingStyle.class, "style", "shapes");
    }

    @Override
    @Nullable
    public SamplingStyle convert(Shape shape) {
        return shape.getPointSampler().getStyle();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET -> new Class[]{SamplingStyle.class};
            case ADD, REMOVE, REMOVE_ALL, DELETE, RESET -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        if (delta == null || delta.length != 1) return;
        SamplingStyle style = (SamplingStyle) delta[0];
        for (Shape shape : getExpr().getArray(event)) {
            shape.getPointSampler().setStyle(style);
        }
    }

    @Override
    public Class<? extends SamplingStyle> getReturnType() {
        return SamplingStyle.class;
    }

    @Override
    protected String getPropertyName() {
        return "style of shape";
    }

}
