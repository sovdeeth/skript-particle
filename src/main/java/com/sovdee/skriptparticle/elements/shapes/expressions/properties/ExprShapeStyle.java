package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeStyle extends SimplePropertyExpression<Shape, Style> {

    static {
        register(ExprShapeStyle.class, Style.class, "style", "shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET) {
            return new Class[]{Style.class};
        }
        return null;
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length != 1) return;
        Style style = (Style) delta[0];
        for (Shape shape : getExpr().getArray(e)) {
            shape.style(style);
        }
    }

    @Override
    protected String getPropertyName() {
        return "style of shape";
    }

    @Override
    public @Nullable Style convert(Shape shape) {
        return shape.style();
    }

    @Override
    public Class<? extends Style> getReturnType() {
        return Style.class;
    }
}
