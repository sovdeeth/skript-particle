package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.SphericalCap;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprSphericalCap extends SimpleExpression<SphericalCap> {
    
    static {
        Skript.registerExpression(ExprSphericalCap.class, SphericalCap.class, ExpressionType.COMBINED, "[a] spherical (cap|:sector) (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]");
    }

    private Expression<Number> radiusExpr;
    private Expression<Number> angleExpr;
    private boolean isRadians = false;
    private boolean isSector = false;

    @Override
    protected @Nullable SphericalCap[] get(Event event) {
        if (radiusExpr.getSingle(event) == null || angleExpr.getSingle(event) == null)
            return null;
        double angle = angleExpr.getSingle(event).doubleValue();
        if (!isRadians)
            angle = Math.toRadians(angle);
        SphericalCap cap = new SphericalCap(radiusExpr.getSingle(event).doubleValue(), angle);
        if (isSector)
            cap.style(Style.FILL);
        return new SphericalCap[]{cap};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends SphericalCap> getReturnType() {
        return SphericalCap.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "spherical cap";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusExpr = (Expression<Number>) expressions[0];
        angleExpr = (Expression<Number>) expressions[1];
        isRadians = parseResult.hasTag("radians");
        isSector = parseResult.hasTag("sector");
        return true;
    }
}
