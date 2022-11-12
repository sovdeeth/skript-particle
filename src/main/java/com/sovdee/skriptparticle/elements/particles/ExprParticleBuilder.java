package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprParticleBuilder extends SimpleExpression<ParticleBuilder> {

    static {
        Skript.registerExpression(ExprParticleBuilder.class, ParticleBuilder.class, ExpressionType.COMBINED,
                "[%number% [of]] %customparticle% particle[s] [using %-itemtype/blockdata/dustoption/dusttransition/vibration" +
                        "/number%] [with offset %-vector%] [with extra %-number%]");
    }

    private Expression<Number> countExpr;
    private Expression<ParticleBuilder> particleExpr;
    private Expression<Object> dataExpr;
    private Expression<Vector> offsetExpr;
    private Expression<Number> extraExpr;

    @Override
    protected @Nullable ParticleBuilder[] get(Event event) {
        int count = (countExpr != null && countExpr.getSingle(event) != null) ? countExpr.getSingle(event).intValue() : 1;
        ParticleBuilder particleBuilder = particleExpr.getSingle(event) != null ? particleExpr.getSingle(event) : new ParticleBuilder(Particle.FLAME);
        Object data = dataExpr != null ? dataExpr.getSingle(event) : null;
        Vector offset = offsetExpr != null ? offsetExpr.    getSingle(event) : null;
        double extra = (extraExpr != null && extraExpr.getSingle(event) != null) ? extraExpr.getSingle(event).doubleValue() : 0;
        particleBuilder.count(count).extra(extra);
        if (data != null)
            particleBuilder.data(data);
        if (offset != null)
            particleBuilder.offset(offset.getX(), offset.getY(), offset.getZ());
        return new ParticleBuilder[]{particleBuilder};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends ParticleBuilder> getReturnType() {
        return ParticleBuilder.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "particle builder";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        countExpr = (Expression<Number>) exprs[0];
        particleExpr = (Expression<ParticleBuilder>) exprs[1];
        dataExpr = (Expression<Object>) exprs[2];
        offsetExpr = (Expression<Vector>) exprs[3];
        extraExpr = (Expression<Number>) exprs[4];
        return true;
    }
}
