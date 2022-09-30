package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.elements.shapes.types.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprShapeRadius extends SimplePropertyExpression<RadialShape, Number> {
    @Override
    protected @NotNull String getPropertyName() {
        return "radius";
    }

    @Override
    public @Nullable Number convert(RadialShape shape) {
        return shape.radius();
    }

    @Override
    public @NotNull Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
