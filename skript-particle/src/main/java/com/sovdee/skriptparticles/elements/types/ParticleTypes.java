package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.util.ContextlessEvent;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticles.particles.Particle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;
import org.skriptlang.skript.lang.converter.Converters;

/**
 * Registers Skript type definitions for particle-related classes.
 * Registers the {@code customparticle} type (backed by {@link com.sovdee.skriptparticles.particles.Particle})
 * and a converter from Skript's built-in {@code ParticleEffect} to {@code Particle}.
 * All registration happens in the static initializer, which is loaded by the plugin's class loader.
 */
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
                    public @NotNull String toString(Particle particle, int flags) {
                        return particle.toString(ContextlessEvent.get(), false);
                    }

                    @Override
                    public @NotNull String toVariableNameString(Particle particle) {
                        return "particle:" + toString(particle, 0);
                    }
                })
        );

        Converters.registerConverter(ParticleEffect.class, Particle.class, Particle::of);
    }
}
