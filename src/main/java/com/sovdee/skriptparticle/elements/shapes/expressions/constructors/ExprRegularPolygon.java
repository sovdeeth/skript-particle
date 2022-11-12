package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.RegularPolygon;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprRegularPolygon extends SimpleExpression<RegularPolygon> {

    static {
        Skript.registerExpression(ExprRegularPolygon.class, RegularPolygon.class, ExpressionType.COMBINED, "[a] [solid:(solid|filled)] regular polygon with %number% sides(,| and) side length %number%",
                "[a] [solid:(solid|filled)] regular polygon with %number% sides(,| and) radius %number%");
    }

    private Expression<Number> sidesExpr;
    private Expression<Number> sideLengthExpr;
    private Expression<Number> radiusExpr;
    private int matchedPattern;
    private Style style;

    @Override
    protected @Nullable RegularPolygon[] get(Event event) {
        RegularPolygon polygon;
        switch (matchedPattern) {
            case 0:
                if (sidesExpr.getSingle(event) == null || sideLengthExpr.getSingle(event) == null)
                    return new RegularPolygon[0];
                polygon = new RegularPolygon(sidesExpr.getSingle(event).intValue(), sideLengthExpr.getSingle(event).doubleValue());
                break;
            case 1:
                if (sidesExpr.getSingle(event) == null || radiusExpr.getSingle(event) == null)
                    return new RegularPolygon[0];
                double sideLength = radiusExpr.getSingle(event).doubleValue() * 2 * Math.sin(Math.PI / sidesExpr.getSingle(event).intValue());
                polygon = new RegularPolygon(sidesExpr.getSingle(event).intValue(), sideLength);
                break;
            default:
                return new RegularPolygon[0];
        }
        polygon.style(style);
        return new RegularPolygon[]{polygon};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends RegularPolygon> getReturnType() {
        return RegularPolygon.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return switch (matchedPattern) {
            case 0 -> "regular polygon with " + sidesExpr.toString(event, b) + " sides and side length " + sideLengthExpr.toString(event, b);
            case 1 -> "regular polygon with " + sidesExpr.toString(event, b) + " sides and radius " + radiusExpr.toString(event, b);
            default -> "regular polygon";
        };
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        sidesExpr = (Expression<Number>) expressions[0];
        switch (matchedPattern) {
            case 0 -> sideLengthExpr = (Expression<Number>) expressions[1];
            case 1 -> radiusExpr = (Expression<Number>) expressions[1];
        }
        style = parseResult.hasTag("solid") ? Style.SURFACE : Style.OUTLINE;
        return true;
    }
}
