package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.sampling.PointLimitExceededException;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Shape Bounds")
@Description({
        "The box that contains every point of a shape, as drawn: after its rotation, scale and offset.",
        "The minimum and maximum corners are relative to the location the shape is drawn at, " +
                "and the size is the distance between them along each axis.",
        "These are measured from the points that are actually drawn, so they can change slightly with the particle density."
})
@Example("set {_low} to {_location} ~ shape minimum corner of {_shape}")
@Example("set {_high} to {_location} ~ shape maximum corner of {_shape}")
@Example("""
        set {_size} to shape size of {_shape}
        send "The shape is %x component of {_size}% by %y component of {_size}% by %z component of {_size}% blocks"
        """)
@Since("2.0.0")
public class ExprShapeBounds extends SimplePropertyExpression<Shape, Vector> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapeBounds.class, Vector.class,
                                "shape (:min|:max)[imum] corner", "shapes", false)
                        .supplier(ExprShapeBounds::new)
                        .build());
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapeBounds.class, Vector.class,
                                "shape (size|extent)", "shapes", false)
                        .supplier(ExprShapeBounds::new)
                        .build());
    }

    private enum Part { MIN, MAX, SIZE }

    private Part part;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        part = parseResult.hasTag("min") ? Part.MIN : parseResult.hasTag("max") ? Part.MAX : Part.SIZE;
        return super.init(expressions, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        PointSampler sampler = shape.getPointSampler();
        ShapeBounds bounds;
        try {
            bounds = sampler.boundsOf(sampler.getPoints(shape));
        } catch (PointLimitExceededException e) {
            return null;
        }
        return switch (part) {
            case MIN -> new Vector(bounds.minX(), bounds.minY(), bounds.minZ());
            case MAX -> new Vector(bounds.maxX(), bounds.maxY(), bounds.maxZ());
            case SIZE -> new Vector(bounds.width(), bounds.height(), bounds.length());
        };
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected String getPropertyName() {
        return switch (part) {
            case MIN -> "shape minimum corner";
            case MAX -> "shape maximum corner";
            case SIZE -> "shape size";
        };
    }
}
