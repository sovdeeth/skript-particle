package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Cylinder;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprCylinder extends SimpleExpression<Cylinder> {

    static {
        Skript.registerExpression(ExprCylinder.class, Cylinder.class, ExpressionType.COMBINED, "[a] [open|:closed|:solid] cylinder (with|of) radius %number%[,| and] height %number%");
    }

    private Expression<Number> radiusExpr;
    private Expression<Number> heightExpr;
    private Style style;

    @Override
    protected @Nullable Cylinder[] get(Event event) {
        if (radiusExpr.getSingle(event) == null || heightExpr.getSingle(event) == null)
            return new Cylinder[0];
        Cylinder cylinder = new Cylinder(radiusExpr.getSingle(event).doubleValue(), heightExpr.getSingle(event).doubleValue());
        cylinder.style(style);
        return new Cylinder[]{cylinder};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Cylinder> getReturnType() {
        return Cylinder.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "a cylinder with radius " + radiusExpr.toString(event, b) + " and height " + heightExpr.toString(event, b);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusExpr = (Expression<Number>) expressions[0];
        heightExpr = (Expression<Number>) expressions[1];
        style = parseResult.hasTag("closed") ? Style.SURFACE : parseResult.hasTag("solid") ? Style.FILL : Style.OUTLINE;
        return true;
    }
}
