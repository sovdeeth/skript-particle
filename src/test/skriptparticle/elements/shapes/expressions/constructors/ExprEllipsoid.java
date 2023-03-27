package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Ellipsoid;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprEllipsoid extends SimpleExpression<Ellipsoid> {

    static {
        Skript.registerExpression(ExprEllipsoid.class, Ellipsoid.class, ExpressionType.COMBINED, "[a[n]] [outlined|:hollow|:solid] ellipsoid (with|of) x [radius] %number%[,| and] y [radius] %number%[,| and] z [radius] %number%");
    }

    private Expression<Number> xRadiusExpr;
    private Expression<Number> yRadiusExpr;
    private Expression<Number> zRadiusExpr;
    private Shape.Style style;

    @Override
    protected @Nullable Ellipsoid[] get(Event event) {
        if (xRadiusExpr.getSingle(event) == null || yRadiusExpr.getSingle(event) == null || zRadiusExpr.getSingle(event) == null)
            return new Ellipsoid[0];
        Ellipsoid ellipsoid = new Ellipsoid(xRadiusExpr.getSingle(event).doubleValue(), yRadiusExpr.getSingle(event).doubleValue(), zRadiusExpr.getSingle(event).doubleValue());
        ellipsoid.style(style);
        return new Ellipsoid[]{ellipsoid};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Ellipsoid> getReturnType() {
        return Ellipsoid.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "ellipsoid";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        xRadiusExpr = (Expression<Number>) expressions[0];
        yRadiusExpr = (Expression<Number>) expressions[1];
        zRadiusExpr = (Expression<Number>) expressions[2];
        style = parseResult.hasTag("hollow") ? Shape.Style.SURFACE : parseResult.hasTag("solid") ? Shape.Style.FILL : Shape.Style.OUTLINE;
        return true;
    }
}
