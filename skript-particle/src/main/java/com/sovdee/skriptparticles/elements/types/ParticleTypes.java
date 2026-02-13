package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticles.particles.Particle;
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
                .description("Represents a particle with extra shape-related data.")
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

        Converters.registerConverter(ParticleEffect.class, Particle.class, Particle::of);
    }
}
