package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Shape Relative Axis")
@Description({
        "Returns the relative axis of a shape. These are the x, y, z axis of the shape AFTER rotation.",
        "These are meant to be used to work within the rotated frame of reference of the shape."
})
@Examples({
        "set {_vX} to {_shape}'s relative x axis",
        "set {_vY} to {_shape}'s relative y axis",
        "set {_vZ} to {_shape}'s relative z axis",
        "rotate shape {_shape} around {_vy} by 90 degrees",
        "set {_vY-offset} to {_shape}'s relative y axis ++ {_shape}'s relative z axis",
        "rotate shape {_shape} around {_vY-offset} by 60 degrees"
})
@Since("1.0.0")
public class ExprShapeRelativeAxis extends SimplePropertyExpression<Shape, Vector> {

    static {
        PropertyExpression.register(ExprShapeRelativeAxis.class, Vector.class, "(relative|local) (:x|:y|:z)(-| )axis", "shapes");
    }

    private int axis;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        axis = parseResult.hasTag("x") ? 0 : parseResult.hasTag("y") ? 1 : 2;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Vector convert(Shape shape) {
        return switch (axis) {
            case 0 -> VectorConversion.toBukkit(shape.getRelativeXAxis());
            case 1 -> VectorConversion.toBukkit(shape.getRelativeYAxis());
            case 2 -> VectorConversion.toBukkit(shape.getRelativeZAxis());
            default -> null;
        };
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    protected String getPropertyName() {
        return "relative axis " + axis;
    }

}
