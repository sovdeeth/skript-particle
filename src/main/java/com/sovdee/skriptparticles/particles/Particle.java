package com.sovdee.skriptparticles.particles;

import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Particle extends ParticleBuilder {

    private ParticleMotion motion;
    private ParticleGradient gradient;
    private Shape parent;
    private boolean override = false;

    public Particle(org.bukkit.@NotNull Particle particle) {
        super(particle);
    }

    public Particle(org.bukkit.@NotNull Particle particle, ParticleMotion motion) {
        super(particle);
        this.motion = motion;
    }

    public void spawn(@NotNull Vector delta) {
        if (parent == null || parent.getLastLocation() == null) return;
        if (this.motion != null) {
            Vector motionVector = this.motion.getMotionVector(parent.getRelativeYAxis(true), delta);
            this.offset(motionVector.getX(), motionVector.getY(), motionVector.getZ());
            this.count(0);
        }
        if (this.gradient() != null) {
            this.color(gradient.calculateColour(delta));
        }
        this.location(parent.getLastLocation().clone().add(delta));
        // note that the values we change here do persist, so we may need to reset them after spawning if it causes issues
        super.spawn();
    }

    @Nullable
    public ParticleMotion motion() {
        return motion;
    }

    public Particle motion(ParticleMotion motion) {
        this.motion = motion;
        return this;
    }

    @Nullable
    public Shape parent() {
        return parent;
    }

    public Particle parent(Shape parent) {
        this.parent = parent;
        return this;
    }

    @Nullable
    public ParticleGradient gradient() {
        return gradient;
    }

    public Particle gradient(ParticleGradient gradient) {
        this.gradient = gradient;
        return this;
    }

    public boolean override() {
        return override;
    }

    public Particle override(boolean override) {
        this.override = override;
        return this;
    }

    public Particle clone() {
        Particle particle = (Particle) new Particle(this.particle())
                .count(this.count())
                .extra(this.extra())
                .offset(this.offsetX(), this.offsetY(), this.offsetZ())
                .data(this.data())
                .force(this.force())
                .receivers(this.receivers())
                .source(this.source());

        if (this.location() != null)
            particle.location(this.location());

        particle.motion(this.motion());
        particle.parent(this.parent());
        particle.gradient(this.gradient());
        particle.override(this.override());
        return particle;
    }

    @Override
    public String toString() {
        return "Particle{" +
                "particle=" + this.particle() +
                (motion != null ? ", motion=" + motion : "") +
                (gradient != null ? ", gradient=" + gradient : "") +
                (parent != null ? ", parent=" + parent : "") +
                ", override=" + override +
                '}';
    }
}
