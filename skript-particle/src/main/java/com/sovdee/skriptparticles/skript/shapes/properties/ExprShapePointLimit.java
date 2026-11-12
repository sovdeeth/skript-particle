package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape Point Limit")
@Description({
        "The most points a shape will be drawn with. If its particle density would produce more, the shape is drawn " +
                "at the finest density that stays within the limit instead.",
        "By default, the limit is 10,000 points, or 100,000 if the shape's particle density was set. " +
                "100,000 is also the highest the limit can be set to.",
        "Resetting or deleting it sets it back to the default."
})
@Example("set the shape point limit of {_sphere} to 500")
@Example("add 1000 to {_shape}'s shape point limit")
@Example("reset the shape point limit of {_shape}")
@Since("2.0.0")
public class ExprShapePointLimit extends SimplePropertyExpression<Shape, Integer> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapePointLimit.class, Integer.class, "shape point limit", "shapes", false)
                        .supplier(ExprShapePointLimit::new)
                        .build());
    }

    @Override
    public Integer convert(Shape shape) {
        return shape.getPointSampler().getMaxPoints();
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE, RESET, DELETE -> new Class[]{Number.class};
            case REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        Shape[] shapes = getExpr().getArray(event);
        if (mode == ChangeMode.RESET || mode == ChangeMode.DELETE) {
            for (Shape shape : shapes)
                shape.getPointSampler().resetMaxPoints();
            return;
        }
        if (delta == null || delta.length == 0)
            return;
        int change = ((Number) delta[0]).intValue();
        for (Shape shape : shapes) {
            PointSampler sampler = shape.getPointSampler();
            // the sampler clamps the limit to its allowed range
            switch (mode) {
                case SET -> sampler.setMaxPoints(change);
                case ADD -> sampler.setMaxPoints(sampler.getMaxPoints() + change);
                case REMOVE -> sampler.setMaxPoints(sampler.getMaxPoints() - change);
                default -> {}
            }
        }
    }

    @Override
    public Class<? extends Integer> getReturnType() {
        return Integer.class;
    }

    @Override
    protected String getPropertyName() {
        return "shape point limit";
    }
}
