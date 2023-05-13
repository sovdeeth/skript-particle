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
import com.sovdee.skriptparticles.shapes.IrregularPolygon;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

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
public class ExprIrregularPolygon extends SimpleExpression<IrregularPolygon> {

    static {
        Skript.registerExpression(ExprIrregularPolygon.class, IrregularPolygon.class, ExpressionType.COMBINED, "[a] [2d] polygon (from|with) [vertices|points] %vectors/locations% [and height %-number%]");
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
    protected IrregularPolygon[] get(Event event) {
        Object[] points = this.points.getArray(event);
        List<Vector> vertices = new ArrayList<>(points.length);
        Vector locationOffset = null;
        for (Object point : points) {
            if (point instanceof Vector) {
                vertices.add((Vector) point);
            } else if (point instanceof Location) {
                if (locationOffset == null) {
                    locationOffset = ((Location) point).toVector();
                }
                vertices.add(((Location) point).toVector().subtract(locationOffset));
            }
        }
        Number height = this.height != null ? this.height.getSingle(event) : null;
        IrregularPolygon polygon;
        if (height != null) {
            polygon = new IrregularPolygon(vertices, height.doubleValue());
        } else {
            polygon = new IrregularPolygon(vertices);
        }
        if (locationOffset != null) {
            polygon.setLocation(new DynamicLocation((Location) points[0]));
        }
        return new IrregularPolygon[]{polygon};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends IrregularPolygon> getReturnType() {
        return IrregularPolygon.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "2d polygon from points " + points.toString(event, debug) + " and height " + height.toString(event, debug);
    }
}
