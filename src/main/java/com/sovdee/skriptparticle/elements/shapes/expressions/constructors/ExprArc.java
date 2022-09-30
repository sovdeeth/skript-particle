package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Arc;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ExprArc extends SimpleExpression<Arc> {

    static {
        Skript.registerExpression(ExprArc.class, Arc.class, ExpressionType.COMBINED, "[a[n]] arc (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]");
    }

    private Expression<Number> radiusExpr;
    private Expression<Number> angleExpr;
    private boolean isRadians;

    @Override
    protected Arc @NotNull [] get(@NotNull Event event) {
        Number radius = radiusExpr.getSingle(event);
        Number angle = angleExpr.getSingle(event);
        if (radius == null || angle == null)
            return new Arc[0];
        if (!isRadians){
            angle = Math.toRadians(angle.doubleValue());
        }
        return new Arc[]{new Arc(radius.doubleValue(), angle.doubleValue())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends Arc> getReturnType() {
        return Arc.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean b) {
        return "arc";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, @NotNull Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusExpr = (Expression<Number>) expressions[0];
        angleExpr = (Expression<Number>) expressions[1];
        isRadians = parseResult.hasTag("radians");
        return true;
    }
}
