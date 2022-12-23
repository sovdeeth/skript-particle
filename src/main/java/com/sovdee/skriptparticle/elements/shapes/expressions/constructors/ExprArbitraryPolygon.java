package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.ArbitraryPolygon;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ExprArbitraryPolygon extends SimpleExpression<ArbitraryPolygon> {

    static{
        Skript.registerExpression(ExprArbitraryPolygon.class, ArbitraryPolygon.class, ExpressionType.COMBINED, "[a[n]] [2d] polygon from [points] %vectors/locations% [and height %number%]");
    }

    private Expression<Number> heightExpr;
    private Expression<?> pointsExpr;

    @Override
    protected @Nullable ArbitraryPolygon[] get(Event event) {
        if (pointsExpr.getAll(event).length == 0)
            return new ArbitraryPolygon[0];

        List<Vector> points = new ArrayList<>();
        Object[] pointsObj = pointsExpr.getAll(event);
        Location center = null;
        if (pointsObj[0] instanceof Location) {
            center = (Location) pointsObj[0];
            for (Object pointObj : pointsObj) {
                Location point = (Location) pointObj;
                points.add(point.toVector().subtract(center.toVector()));
            }

        } else {
            for (Object pointObj : pointsObj) {
                Vector point = (Vector) pointObj;
                points.add(point);
            }
        }

        double maxY = 0;
        double minY = 0;
        for (Vector point : points) {
            if (point.getY() > maxY)
                maxY = point.getY();
            if (point.getY() < minY)
                minY = point.getY();
        }
        if (center != null) {
            center.setY(center.getY() + minY);
        }
        for (Vector point : points) {
            point.setY(minY);
        }
        double height = maxY - minY;
        if (heightExpr != null && heightExpr.getSingle(event) != null)
            height = heightExpr.getSingle(event).doubleValue();
        return new ArbitraryPolygon[]{new ArbitraryPolygon(points, height)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ArbitraryPolygon> getReturnType() {
        return ArbitraryPolygon.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "arbitrary polygon from "+pointsExpr.getAll(event).length+" points";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        pointsExpr = expressions[0];
        heightExpr = (Expression<Number>) expressions[1];
        return true;
    }
}
