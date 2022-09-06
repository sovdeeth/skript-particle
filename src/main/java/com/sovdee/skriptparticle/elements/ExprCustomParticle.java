package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.particles.CustomParticle;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprCustomParticle extends SimpleExpression<CustomParticle> {

    static {
        Skript.registerExpression(ExprCustomParticle.class, CustomParticle.class, ExpressionType.COMBINED,
                "[%-number% [of]] %particle% [particle] [using %-itemtype/blockdata/dustoption/dusttransition/vibration" +
                        "/number%] [with offset %-vector%] [with extra %-number%]");
    }

    private Expression<Number> countExpr;
    private Expression<Particle> particleExpr;
    private Expression<Object> dataExpr;
    private Expression<Vector> offsetExpr;
    private Expression<Number> extraExpr;

    @Override
    protected @Nullable CustomParticle[] get(Event event) {
        int count = (countExpr != null && countExpr.getSingle(event) != null) ? countExpr.getSingle(event).intValue() : 1;
        Particle particle = particleExpr.getSingle(event) != null ? particleExpr.getSingle(event) : Particle.FLAME;
        Object data = dataExpr != null ? dataExpr.getSingle(event) : null;
        Vector offset = offsetExpr != null ? offsetExpr.    getSingle(event) : null;
        double extra = (extraExpr != null && extraExpr.getSingle(event) != null) ? extraExpr.getSingle(event).doubleValue() : 0;
        return new CustomParticle[]{new CustomParticle(particle, count, offset, extra, data)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public @NotNull Class<? extends CustomParticle> getReturnType() {
        return CustomParticle.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "custom particle";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        countExpr = (Expression<Number>) exprs[0];
        particleExpr = (Expression<Particle>) exprs[1];
        dataExpr = (Expression<Object>) exprs[2];
        offsetExpr = (Expression<Vector>) exprs[3];
        extraExpr = (Expression<Number>) exprs[4];
        return true;
    }
}
