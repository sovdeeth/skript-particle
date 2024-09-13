package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Section;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.skript.util.LiteralUtils;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import com.sovdee.skriptparticles.util.ParticleUtil;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.skriptlang.skript.lang.entry.EntryContainer;
import org.skriptlang.skript.lang.entry.EntryValidator;
import org.skriptlang.skript.lang.entry.util.ExpressionEntryData;

import java.util.List;

@Name("Custom Particle Section")
@Description({
        "This section can be used in conjunction with the `last created particle` expression to create custom particles.",
        "The particle can be any custom particle from skript-particle or from skbee.",
        "Fields include:",
            "\tcount: integer - the number of particles to create (required)",
            "\toffset: vector - the offset value of the particle. See the Minecraft wiki on /particle for more info. (default: 0, 0, 0)",
            "\tvelocity: vector - the velocity of the particle. Can be a vector or a motion (inwards/clockwise/etc.). (default: 0, 0, 0)",
            "\textra: number - the extra value of the particle. Forces `count` to be 0 and cannot be combined with `offset`. " +
                    "See the Minecraft wiki on /particle for more info. (default: 0)",
            "\tdata: object - the data value of the particle. For example, `dustOptions()` for the dust particle. See the Minecraft wiki on /particle for more info. (default: null)",
            "\tforce: boolean - whether or not to force the particle to be seen at long range. (default: false)"
})
@Examples({
        "create a new custom electric spark particle with:",
            "\tcount: 10",
            "\toffset: vector(1, 1, 1)",
            "\textra: 0.2",
            "\tforce: true",
        "set {_particle} to last created particle",
        "",
        "create a new custom dust particle with:",
            "\tcount: 0",
            "\tvelocity: inwards",
            "\textra: 0.5",
            "\tforce: true",
            "\tdata: dustOption(red, 5)",
        "set {_particle} to last created particle",
})
@Since("1.0.2")
public class SecParticle extends Section {
    @Nullable
    public static Particle lastCreatedParticle;
    private static final EntryValidator validator = EntryValidator.builder()
            .addEntryData(new ExpressionEntryData<>("count", new SimpleLiteral<>(1, false), false, Number.class))
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
        Skript.registerSection(SecParticle.class, "create [a] [new] custom %particle% [particle] [with]");
    }

    private Expression<org.bukkit.Particle> particle;
    private Expression<Number> count;
    @Nullable
    private Expression<Vector> offset;
    @Nullable
    private Expression<?> velocity;
    @Nullable
    private Expression<Number> extra;
    @Nullable
    private Expression<Object> data;
    @Nullable
    private Expression<Boolean> force;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> triggerItems) {
        @Nullable EntryContainer entryContainer = validator.validate(sectionNode);
        if (entryContainer == null)
            return false;

        particle = (Expression<org.bukkit.Particle>) expressions[0];
        count = (Expression<Number>) entryContainer.getOptional("count", Expression.class, true);
        offset = (Expression<Vector>) entryContainer.getOptional("offset", Expression.class, true);
        extra = (Expression<Number>) entryContainer.getOptional("extra", Expression.class, true);
        force = (Expression<Boolean>) entryContainer.getOptional("force", Expression.class, true);

        velocity = entryContainer.getOptional("velocity", Expression.class, true);
        if (velocity != null) {
            velocity = LiteralUtils.defendExpression(velocity);
            if (!LiteralUtils.canInitSafely(velocity)){
                Skript.error("Invalid expression for velocity! Must be a vector or a particle motion.");
                return false;
            }
        }

        data = (Expression<Object>) entryContainer.getOptional("data", Expression.class, true);
        if (data != null) {
            data = LiteralUtils.defendExpression(data);
            if (!LiteralUtils.canInitSafely(data)){
                Skript.error("Invalid value for data! Must be a dust options, item, or other particle data!");
                return false;
            }
        }

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
        org.bukkit. @Nullable Particle bukkitParticle = this.particle.getSingle(event);
        if (bukkitParticle == null)
            return;
        @Nullable Number count = this.count.getSingle(event);
        if (count == null)
            return;
        @Nullable Vector offset = this.offset != null ? this.offset.getSingle(event) : null;
        @Nullable Number extra = this.extra != null ? this.extra.getSingle(event) : null;
        @Nullable Object data = this.data != null ? this.data.getSingle(event) : null;
        @Nullable Boolean force = this.force != null ? this.force.getSingle(event) : null;

        Particle particle = (Particle) new Particle(bukkitParticle).count(count.intValue());

        if (velocity != null) {
            @Nullable Object v = velocity.getSingle(event);
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

        if (data != null) {
            data = ParticleUtil.getData(particle.particle(), data);
            if (data != null)
                particle.data(data);
        }

        if (force != null)
            particle.force(force);

        lastCreatedParticle = particle;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "new custom particle from " + particle.toString(event, debug);
    }
}
