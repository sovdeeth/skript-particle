package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.lang.*;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.util.SectionValidatorPlus;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import org.bukkit.Particle;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.List;

public class SecParticle extends Section {
    static {
        Skript.registerSection(SecParticle.class, "create [a] [new] [custom] %particle% particle");
    }

    private static final SectionValidatorPlus SECTION_VALIDATOR = new SectionValidatorPlus()
            .addEntry("count", Number.class,true)
            .addEntry("offset", Vector.class, true)
            .addEntry("velocity", ParticleMotion.class, true)
            .addEntry("extra", Number.class, true)
            .addEntry("data", Object.class, true)
            .addEntry("force", Boolean.class, true)
            .addEntry("variable", VariableString.class, true);

    private Expression<Particle> particleExpr;
    private Expression<Number> countExpr;
    private Expression<Vector> offsetExpr;
    private Expression<Number> extraExpr;
    private Expression<Object> dataExpr;
    private Expression<Boolean> forceExpr;
    private Expression<VariableString> variableExpr;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult, SectionNode sectionNode, List<TriggerItem> list) {
        particleExpr = (Expression<Particle>) expressions[0];
        sectionNode.convertToEntries(0);
        if (SECTION_VALIDATOR.validate(sectionNode)) {
            countExpr = (Expression<Number>) SECTION_VALIDATOR.getEntry("count");
            offsetExpr = (Expression<Vector>) SECTION_VALIDATOR.getEntry("offset");
            extraExpr = (Expression<Number>) SECTION_VALIDATOR.getEntry("extra");
            dataExpr = (Expression<Object>) SECTION_VALIDATOR.getEntry("data");
            forceExpr = (Expression<Boolean>) SECTION_VALIDATOR.getEntry("force");
            variableExpr = (Expression<VariableString>) SECTION_VALIDATOR.getEntry("variable");
            Expression<Vector> velocityExpr = (Expression<Vector>) SECTION_VALIDATOR.getEntry("velocity");
            if (velocityExpr != null) {
                if (offsetExpr != null) {
                    Skript.error("You can't use both 'offset' and 'velocity' in the same particle.");
                    return false;
                }
                if (countExpr != null) {
                    Skript.error("You can't use both 'count' and 'velocity' in the same particle.");
                    return false;
                }
                offsetExpr = velocityExpr;
                countExpr = new SimpleLiteral<>(0, false);
            }
            return true;

        }
        return false;
    }

    @Override
    protected @Nullable TriggerItem walk(Event event) {
        return null;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return null;
    }
}
