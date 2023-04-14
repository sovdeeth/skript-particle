package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Arc;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprArc extends SimpleExpression<Arc> {

    static {
        Skript.registerExpression(ExprArc.class, Arc.class, ExpressionType.COMBINED,
                "[a[n]] [circular] (arc|:sector) (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]");
    }

    private Expression<Number> radius;
    private Expression<Number> angle;
    private boolean isRadians = false;
    private boolean isSector = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        angle = (Expression<Number>) exprs[1];
        isRadians = parseResult.hasTag("radians");
        isSector = parseResult.hasTag("sector");
        return true;
    }

    @Override
    @Nullable
    protected Arc[] get(Event event) {
        if (radius.getSingle(event) == null || angle.getSingle(event) == null)
            return new Arc[0];

        double angle = this.angle.getSingle(event).doubleValue();
        if (!isRadians)
            angle = Math.toRadians(angle);

        Arc arc = new Arc(radius.getSingle(event).doubleValue(), angle);
        if (isSector)
            arc.setStyle(Shape.Style.FILL);

        return new Arc[]{arc};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Arc> getReturnType() {
        return Arc.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return null;
    }

}