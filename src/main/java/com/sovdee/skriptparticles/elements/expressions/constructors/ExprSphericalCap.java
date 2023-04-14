package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.SphericalCap;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprSphericalCap extends SimpleExpression<SphericalCap> {

    static {
        Skript.registerExpression(ExprSphericalCap.class, SphericalCap.class, ExpressionType.COMBINED,
                "[a] spherical (cap|:sector) (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]");
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
    protected SphericalCap[] get(Event e) {
        if (radius.getSingle(e) == null || angle.getSingle(e) == null)
            return new SphericalCap[0];

        double angle = this.angle.getSingle(e).doubleValue();
        if (!isRadians)
            angle = Math.toRadians(angle);

        SphericalCap cap = new SphericalCap(radius.getSingle(e).doubleValue(), angle);
        if (isSector)
            cap.setStyle(Shape.Style.FILL);

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
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

}
