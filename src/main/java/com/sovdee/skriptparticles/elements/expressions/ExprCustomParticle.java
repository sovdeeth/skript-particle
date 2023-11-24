package com.sovdee.skriptparticles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.particles.Particle;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle With Data")
@Description({
        "Creates a particle with data. This is useful for creating particles with data such as dust options, dust transitions, vibrations, etc.",
        "The particle can be created with a particle type, or a custom particle. If a custom particle is used, the particle will be cloned. " +
                "If a count is not specified, it will default to 1.",
        "This syntax was based on SkBee's draw particle effect syntax, so please report any conflicts with SkBee immediately. There should not " +
                "be any conflicts, but there's always a risk."
})
@Examples({
        "set {_particle} to electric spark particle with extra 0",
        "set particle of {_shape} to dust particle using dustOption(red, 1) with force"
})
public class ExprCustomParticle extends SimpleExpression<Particle> {

    static {
        Skript.registerExpression(ExprCustomParticle.class, Particle.class, ExpressionType.COMBINED,
                "[%-number% [of]] %customparticle/particle% particle[s] [using %-itemtype/blockdata/dustoption/dusttransition/vibration/number%] " +
                        "[with offset %-vector%] [with extra %-number%] [force:with force]");
    }

    @Nullable
    private Expression<Number> count;
    private Expression<?> particle;
    @Nullable
    private Expression<Object> data;
    @Nullable
    private Expression<Vector> offset;
    @Nullable
    private Expression<Number> extra;
    private boolean force;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        count = (Expression<Number>) exprs[0];
        particle = exprs[1];
        data = (Expression<Object>) exprs[2];
        offset = (Expression<Vector>) exprs[3];
        extra = (Expression<Number>) exprs[4];
        force = parseResult.hasTag("force");
        return true;
    }

    @Override
    @Nullable
    protected Particle[] get(Event event) {
        @Nullable Object particleExpr = this.particle.getSingle(event);

        if (particleExpr == null) return new Particle[0];
        Particle particle;
        if (particleExpr instanceof Particle) {
            particle = ((Particle) particleExpr).clone();
        } else if (particleExpr instanceof org.bukkit.Particle) {
            particle = new Particle((org.bukkit.Particle) particleExpr);
        } else {
            return new Particle[0];
        }

        particle = particle.clone();
        if (count != null) {
            @Nullable Number count = this.count.getSingle(event);
            if (count != null) {
                particle.count(count.intValue());
            } else {
                particle.count(1);
            }

        }

        if (data != null) {
            @Nullable Object data = this.data.getSingle(event);
            if (data instanceof ItemType itemType) {
                data = itemType.getRandom();
            }
            if (data != null) particle.data(data);
        }

        if (offset != null) {
            @Nullable Vector offset = this.offset.getSingle(event);
            if (offset != null) particle.offset(offset.getX(), offset.getY(), offset.getZ());
        }

        if (extra != null) {
            @Nullable Number extra = this.extra.getSingle(event);
            if (extra != null) particle.extra(extra.doubleValue());
        }

        if (force) particle.force(true);

        return new Particle[]{particle};
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
    public String toString(@Nullable Event event, boolean debug) {
        return "custom particle";
    }

}
