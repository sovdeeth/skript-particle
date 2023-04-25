package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Shape Points")
@Description({
        "Returns all the vectors from the center of the shape to the various points particles would be drawn at.",
        "If you want the locations of the particles, it's advised use the 'shape locations' expression rather than doing it yourself."
})
@Examples({
        "set {_vectors::*} to points of (circle of radius 10)",
        "teleport player to (player ~ random element of {_vectors::*})",
        "",
        "set {_randomVectorInEllipsoid} to random element of points of (ellipsoid of radius 10, 5, 2)"
})
@Since("1.0.0")
public class ExprShapePoints extends SimplePropertyExpression<Shape, Vector[]> {

    static {
        register(ExprShapePoints.class, Vector[].class, "[shape|particle] points", "shapes");
    }

    @Override
    @Nullable
    public Vector[] convert(Shape shape) {
        return shape.getPoints().toArray(new Vector[0]);
    }

    @Override
    public Class<? extends Vector[]> getReturnType() {
        return Vector[].class;
    }

    @Override
    protected String getPropertyName() {
        return "shape points";
    }

}
