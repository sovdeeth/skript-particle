package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Shape Points")
@Description({
        "Returns all the vectors from the center of the shape to the various points particles would be drawn at.",
        "If you want the locations of the particles, it's advised use the 'shape locations' expression rather than doing it yourself."
})
@Examples({
        "set {_vectors::*} to points of (circle of radius 10)",
        "teleport player to (player ~ random element of {_vectors::*})",
        "",
        "set {_randomVectorInEllipsoid} to random element of points of (solid ellipsoid of radius 10, 5, 2)"
})
@Since("1.0.0")
public class ExprShapePoints extends PropertyExpression<Shape, Vector> {

    static {
        register(ExprShapePoints.class, Vector.class, "points", "shapes");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        return true;
    }

    @Override
    protected Vector[] get(Event event, Shape[] source) {
        List<Vector> points = new ArrayList<>();
        for (Shape shape : source) {
            points.addAll(shape.getPoints());
        }
        return points.toArray(new Vector[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Vector> getReturnType() {
        return Vector.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "points of " + getExpr().toString(event, debug);
    }
}
