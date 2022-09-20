package com.sovdee.skriptparticle.elements.shapes.expressions;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprShapeRelativeAxis extends SimplePropertyExpression<Shape, Vector> {

    static {
        register(ExprShapeRelativeAxis.class, Vector.class, "[relative] :(x|y|z)(-| )axis", "shapes");
    }

    private String axis;

    @Override
    protected String getPropertyName() {
        return "relative axis";
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        switch (axis) {
            case "x":
                return shape.relativeXAxis();
            case "y":
                return shape.relativeYAxis();
            case "z":
                return shape.relativeZAxis();
            default:
                return null;
        }
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!super.init(exprs, matchedPattern, isDelayed, parseResult))
            return false;
        axis = parseResult.tags.get(0);
        return true;
    }
}
