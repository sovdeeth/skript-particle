package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Ellipse;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprEllipse extends SimpleExpression<Ellipse> {

    static {
        Skript.registerExpression(ExprEllipse.class, Ellipse.class, ExpressionType.COMBINED, "[a[n]] [solid:(solid|filled)] ellipse (with|of) radius [x] %number%[,] [and] [z] %number%");
    }

    private Expression<Number> radiusXExpr;
    private Expression<Number> radiusZExpr;
    private Style style;

    @Override
    protected @Nullable Ellipse[] get(Event event) {
        if (radiusXExpr == null || radiusZExpr == null) return new Ellipse[0];
        Ellipse ellipse = new Ellipse(radiusXExpr.getSingle(event).doubleValue(), radiusZExpr.getSingle(event).doubleValue());
        ellipse.style(style);
        return new Ellipse[]{ellipse};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Ellipse> getReturnType() {
        return Ellipse.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "ellipse with radius x " + radiusXExpr.toString(event, b) + " and radius z " + radiusZExpr.toString(event, b);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusXExpr = (Expression<Number>) expressions[0];
        radiusZExpr = (Expression<Number>) expressions[1];
        style = parseResult.hasTag("solid") ? Style.SURFACE : Style.OUTLINE;
        return true;
    }
}
