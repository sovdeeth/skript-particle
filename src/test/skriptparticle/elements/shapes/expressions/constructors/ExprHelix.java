package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Helix;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprHelix extends SimpleExpression<Helix> {

    static {
        Skript.registerExpression(ExprHelix.class, Helix.class, ExpressionType.COMBINED, "[a] [clockwise|:counterclockwise] (helix|spiral) (with|of) radius %number%[,] [and] height %number%[[,] [and] winding rate %number% [loops per (meter|block)]]");
    }

    private Expression<Number> radiusExpr;
    private Expression<Number> heightExpr;
    private Expression<Number> windingRateExpr;
    private boolean isClockwise = true;

    @Override
    protected @Nullable Helix[] get(Event event) {
        Number radius = radiusExpr.getSingle(event);
        Number height = heightExpr.getSingle(event);
        Number windingRate = windingRateExpr.getSingle(event);
        if (radius == null || height == null || radius.doubleValue() <= 0 || height.doubleValue() <= 0)
            return new Helix[0];
        double slope;
        if (windingRate == null)
            slope = 1;
        else
            slope = 1 / windingRate.doubleValue();

        return new Helix[]{new Helix(radius.doubleValue(), height.doubleValue(), slope/(2*Math.PI), isClockwise ? 1 : -1)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Helix> getReturnType() {
        return Helix.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "helix with radius " + radiusExpr.toString(event, b) + ", height " + heightExpr.toString(event, b) + ", and winding rate " + windingRateExpr.toString(event, b);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusExpr = (Expression<Number>) expressions[0];
        heightExpr = (Expression<Number>) expressions[1];
        if (expressions.length > 2)
            windingRateExpr = (Expression<Number>) expressions[2];
        isClockwise = !parseResult.hasTag("counterclockwise");
        return true;
    }
}
