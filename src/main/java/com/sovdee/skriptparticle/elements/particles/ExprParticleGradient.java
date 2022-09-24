package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprParticleGradient extends SimpleExpression<ParticleGradient> {

    static {
        Skript.registerExpression(ExprParticleGradient.class, ParticleGradient.class, ExpressionType.SIMPLE, "[a] [new] [:local|global] particle gradient");
    }

    private boolean localFlag = false;

    @Override
    protected @Nullable ParticleGradient[] get(Event event) {
        ParticleGradient pg = new ParticleGradient();
        if (localFlag)
            pg.isLocal(true);
        return new ParticleGradient[]{pg};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class getReturnType() {
        return ParticleGradient.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "particle gradient";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        localFlag = parseResult.hasTag("local");
        return true;
    }
}
