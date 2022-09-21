package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprShapeRelativeAxis extends SimplePropertyExpression<Shape, Vector> {

    static {
        register(ExprShapeRelativeAxis.class, Vector.class, "relative (:x|:y|:z)(-| )axis", "shapes");
    }

    private String axis;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        axis = parseResult.tags.get(0);
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected String getPropertyName() {
        return "relative axis " + axis;
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        Vector axisVector;
        switch (axis) {
            case "x":
                axisVector = shape.relativeXAxis();
                break;
            case "y":
                axisVector = shape.relativeYAxis();
                break;
            case "z":
                axisVector = shape.relativeZAxis();
                break;
            default:
                return null;
        }
        return axisVector;
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }
}
