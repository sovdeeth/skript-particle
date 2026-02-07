package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.DrawableShape;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@Name("Particle Irregular Polygon")
@Description({
        "Creates an irregular polygon from a list of vectors or locations. If locations are used, the polygon can be drawn without giving a specific location to draw at.",
        "The height of the polygon will be the height between the lowest and highest points. It can also be set with the optional height parameter.",
        "",
        "Irregular polygons currently only support the wireframe style. Also, they do not currently support Dynamic Locations like lines and cuboids do."
})
@Examples({
        "set {_shape} to a polygon with points vector(0, 0, 0), vector(1, 0, 0), and vector(1, 1, 1)",
        "set {_shape} to a 2d polygon from points vector(0,0,1), vector(1,0,1), vector(0,0,-1) and height 0.5"
})
@Since("1.0.0")
public class ExprIrregularPolygon extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprIrregularPolygon.class, Shape.class, ExpressionType.COMBINED,
                "[a] [2d] polygon (from|with) [vertices|points] %vectors/locations% [and height %-number%]");
    }

    private Expression<?> points;
    private Expression<Number> height;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        points = exprs[0];
        if (exprs.length > 1) {
            height = (Expression<Number>) exprs[1];

            if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() < 0) {
                Skript.error("The height of the polygon must be greater than or equal to 0. (height: " +
                        literal.getSingle().doubleValue() + ")");
                return false;
            }
        }

        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Object[] points = this.points.getArray(event);
        List<Vector3d> vertices = new ArrayList<>(points.length);
        Vector locationOffset = null;
        for (Object point : points) {
            if (point instanceof Vector) {
                vertices.add(VectorConversion.toJOML((Vector) point));
            } else if (point instanceof Location) {
                if (locationOffset == null) {
                    locationOffset = ((Location) point).toVector();
                }
                Vector relative = ((Location) point).toVector().subtract(locationOffset);
                vertices.add(VectorConversion.toJOML(relative));
            }
        }
        Number height = this.height != null ? this.height.getSingle(event) : null;
        com.sovdee.shapes.IrregularPolygon libPolygon;
        if (height != null) {
            libPolygon = new com.sovdee.shapes.IrregularPolygon(vertices, height.doubleValue());
        } else {
            libPolygon = new com.sovdee.shapes.IrregularPolygon(vertices);
        }
        DrawableShape shape = new DrawableShape(libPolygon);
        if (locationOffset != null) {
            shape.setLocation(new DynamicLocation((Location) points[0]));
        }
        return new Shape[]{shape};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "2d polygon from points " + points.toString(event, debug) + " and height " + (height == null ? 0 : height.toString(event, debug));
    }
}
