package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

public class SecParticle extends Section {
    public static Particle lastCreatedParticle;
    private static final EntryValidator validator = EntryValidator.builder()
            .addEntryData(new ExpressionEntryData<>("count", null, false, Number.class))
            .addEntryData(new ExpressionEntryData<>("offset", null, true, Vector.class))
            .addEntryData(new ExpressionEntryData<>("velocity", null, true, Object.class))
            .addEntryData(new ExpressionEntryData<>("extra", null, true, Number.class))
            .addEntryData(new ExpressionEntryData<>("data", null, true, Object.class))
            .addEntryData(new ExpressionEntryData<>("force", null, true, Boolean.class))
            .build();

    // Particle section
    // create a new %particle% [particle]:
    //- count: int
    //- offset: vector
    //- velocity: vector or inwards/outwards (exclusive w/ offset & count)
    //- extra: double
    //- data: dustOptions, item, etc. (take skbee code)
    //- - if particle is dust, allow "color: color" and "size: double"
    //- force: boolean
    static {
        Skript.registerSection(SecParticle.class, "create [a] [new] custom %particle% [particle]");
    }

    private Expression<org.bukkit.Particle> particle;
    private Expression<Number> count;
    private Expression<Vector> offset;
    private Expression<?> velocity;
    private Expression<Number> extra;
    private Expression<Object> data;
    private Expression<Boolean> force;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        EntryContainer entryContainer = validator.validate(sectionNode);
        if (entryContainer == null)
            return false;
        particle = LiteralUtils.defendExpression((Expression<org.bukkit.Particle>) exprs[0]);
        count = LiteralUtils.defendExpression((Expression<Number>) entryContainer.get("count", false));
        offset = LiteralUtils.defendExpression((Expression<Vector>) entryContainer.getOptional("offset", true));
        velocity = LiteralUtils.defendExpression((Expression<?>) entryContainer.getOptional("velocity", true));
        extra = LiteralUtils.defendExpression((Expression<Number>) entryContainer.getOptional("extra", true));
        data = LiteralUtils.defendExpression((Expression<Object>) entryContainer.getOptional("data", true));
        force = LiteralUtils.defendExpression((Expression<Boolean>) entryContainer.getOptional("force", true));
        if (offset != null && velocity != null) {
            Skript.error("You cannot have both an offset and a velocity for a particle!");
            return false;
        }


        return true;
    }

    @Override
    @Nullable
    protected TriggerItem walk(Event event) {
        execute(event);
        return walk(event, false);
    }

    private void execute(Event event) {
        org.bukkit.Particle bukkitParticle = this.particle.getSingle(event);
        if (bukkitParticle == null)
            return;
        Number count = this.count.getSingle(event);
        if (count == null)
            return;
        Vector offset = this.offset != null ? this.offset.getSingle(event) : null;
        Number extra = this.extra != null ? this.extra.getSingle(event) : null;
        Object data = this.data != null ? this.data.getSingle(event) : null;
        Boolean force = this.force != null ? this.force.getSingle(event) : null;

        Particle particle = (Particle) new Particle(bukkitParticle).count(count.intValue());

        if (velocity != null) {
            Object v = velocity.getSingle(event);
            if (v instanceof ParticleMotion motion) {
                particle.motion(motion);
            } else if (v instanceof Vector vector) {
                particle.count(0).offset(vector.getX(), vector.getY(), vector.getZ());
            }
        }

        if (offset != null)
            particle.offset(offset.getX(), offset.getY(), offset.getZ());

        if (extra != null)
            particle.extra(extra.doubleValue());

        if (data != null)
            particle.data(data);

        if (force != null)
            particle.force(force);

        lastCreatedParticle = particle;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "new custom particle from " + particle.toString(event, debug);
    }
}
