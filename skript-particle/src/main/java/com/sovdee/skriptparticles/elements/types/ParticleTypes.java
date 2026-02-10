package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.EnumClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.converter.Converters;

public class ParticleTypes {
    static {

        // Particle Builder class
        Classes.registerClass(new ClassInfo<>(Particle.class, "customparticle")
                .user("customparticles?")
                .name("Custom Particle")
                .description("Represents a particle with extra shape-related data, like motion.")
                .parser(new Parser<>() {

                    @Nullable
                    @Override
                    public Particle parse(String s, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public @NonNull String toString(Particle particle, int flags) {
                        return particle.toString(ContextlessEvent.get(), false);
                    }

                    @Override
                    public @NonNull String toVariableNameString(Particle particle) {
                        return "particle:" + toString(particle, 0);
                    }
                })
        );

        // Particle motion class
        Classes.registerClass(new EnumClassInfo<>(ParticleMotion.class, "particlemotion", "particle motions")
                .user("particle ?motions?")
                .name("Particle Motion")
                .description("Represents the motion of a particle relative to a shape.")
        );

        Converters.registerConverter(ParticleEffect.class, Particle.class, Particle::of);
    }
}
