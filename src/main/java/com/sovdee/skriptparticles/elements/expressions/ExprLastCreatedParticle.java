package com.sovdee.skriptparticles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.elements.sections.SecParticle;
import com.sovdee.skriptparticles.particles.Particle;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Last Created Particle")
@Description("Returns the last particle created with the custom particle section.")
@Examples("set {_particle} to last created particle")
@Since("1.0.2")
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
    protected Particle[] get(Event evente) {
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
