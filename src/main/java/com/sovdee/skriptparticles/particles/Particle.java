package com.sovdee.skriptparticles.particles;

import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class Particle extends ParticleBuilder {

    private @Nullable ParticleMotion motion;
    private @Nullable ParticleGradient gradient;
    private @Nullable Shape parent;
    private boolean override = false;

    public Particle(org.bukkit.Particle particle) {
        super(particle);
    }

    public Particle(org.bukkit.Particle particle, ParticleMotion motion) {
        super(particle);
        this.motion = motion;
    }

    public void spawn(Vector delta) {
        if (parent == null || parent.getLastLocation() == null) return;
        if (motion != null) {
            Vector motionVector = motion.getMotionVector(parent.getRelativeYAxis(true), delta);
            this.offset(motionVector.getX(), motionVector.getY(), motionVector.getZ());
            this.count(0);
        }
        if (gradient != null) {
            color(gradient.calculateColour(delta));
        }
        location(parent.getLastLocation().getLocation().add(delta));
        // note that the values we change here do persist, so we may need to reset them after spawning if it causes issues
        super.spawn();
    }

    @Nullable
    public ParticleMotion motion() {
        return motion;
    }

    public Particle motion(@Nullable ParticleMotion motion) {
        this.motion = motion;
        return this;
    }

    @Nullable
    public Shape parent() {
        return parent;
    }

    public Particle parent(@Nullable Shape parent) {
        this.parent = parent;
        return this;
    }

    @Nullable
    public ParticleGradient gradient() {
        return gradient;
    }

    public Particle gradient(@Nullable ParticleGradient gradient) {
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

    @Contract("-> new")
    public Particle clone() {
        Particle particle = (Particle) new Particle(this.particle())
                .count(this.count())
                .extra(this.extra())
                .offset(this.offsetX(), this.offsetY(), this.offsetZ())
                .data(this.data())
                .force(this.force())
                .receivers(this.receivers())
                .source(this.source());
        @Nullable Location location = this.location();
        if (location != null)
            particle.location(location);

        return particle.motion(this.motion())
                .parent(this.parent())
                .gradient(this.gradient())
                .override(this.override());
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
