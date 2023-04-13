package com.sovdee.skriptparticles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.elements.sections.SecParticle;
import com.sovdee.skriptparticles.particles.Particle;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprLastCreatedParticle extends SimpleExpression<Particle> {

    static {
        Skript.registerExpression(ExprLastCreatedParticle.class, Particle.class, ExpressionType.SIMPLE, "last created [custom] particle");
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        return true;
    }

    @Override
    @Nullable
    protected Particle[] get(Event e) {
        return new Particle[]{SecParticle.lastCreatedParticle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Particle> getReturnType() {
        return Particle.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "last created particle";
    }
}
