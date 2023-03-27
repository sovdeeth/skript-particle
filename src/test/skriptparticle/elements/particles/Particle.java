package com.sovdee.skriptparticle.elements.particles;

import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticles.particles.ParticleMotion;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Particle extends ParticleBuilder{

    private ParticleMotion motion;

    public Particle(@NotNull org.bukkit.Particle particle) {
        super(particle);
    }

    public Particle(@NotNull org.bukkit.Particle particle, ParticleMotion motion) {
        super(particle);
        this.motion = motion;
    }

    public void draw(Location location, Vector point, Vector yAxis) {
        Vector motion = this.motion.getMotionVector(point, yAxis);
        this.location(location)
                .offset(motion.getX(), motion.getY(), motion.getZ())
                .count(0)
                .spawn();
    }



    @Nullable
    public ParticleMotion getMotion() {
        return motion;
    }

    public void setMotion(ParticleMotion motion) {
        this.motion = motion;
    }
}
