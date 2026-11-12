package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.sampling.PointLimitExceededException;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.shapes.Shape;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape Reach")
@Description({
        "How far the furthest point of a shape is from the location it is drawn at, after its rotation, scale and offset.",
        "Useful for finding the players close enough to see a shape, or how much room it needs."
})
@Example("""
        set {_reach} to shape reach of {_shape}
        loop all players in radius {_reach} + 32 of {_location}:
            send "Look up!" to loop-player
        """)
@Since("2.0.0")
public class ExprShapeReach extends SimplePropertyExpression<Shape, Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapeReach.class, Number.class, "shape reach", "shapes", false)
                        .supplier(ExprShapeReach::new)
                        .build());
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        PointSampler sampler = shape.getPointSampler();
        try {
            return sampler.boundsOf(sampler.getPoints(shape)).maxDistance();
        } catch (PointLimitExceededException e) {
            return null;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "shape reach";
    }
}
