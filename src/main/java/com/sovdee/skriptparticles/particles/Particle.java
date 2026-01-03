package com.sovdee.skriptparticles.particles;

import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

import java.util.Collection;
import java.util.List;

public class Particle extends ParticleEffect {

    private @Nullable ParticleMotion motion;
    private @Nullable ParticleGradient gradient;
    private @Nullable Shape parent;
    private boolean override = false;

    public static Particle of(ParticleEffect effect) {
        org.bukkit.Particle effectParticle = effect.particle();
        Particle particle = new Particle(effectParticle);
        particle.count(effect.count());
        particle.data(effect.data());
        @Nullable Location loc;
        if ((loc = effect.location()) != null) {
            particle.location(loc);
        }

        particle.offset(effect.offsetX(), effect.offsetY(), effect.offsetZ());
        particle.extra(effect.extra());
        particle.force(effect.force());
        particle.receivers(effect.receivers());
        particle.source(effect.source());
        return particle;
    }

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

    //<editor-fold desc="Fluent overrides" defaultstate="collapsed">

    @Override
    public Particle particle(org.bukkit.Particle particle) {
        return (Particle) super.particle(particle);
    }

    @Override
    public Particle allPlayers() {
        return (Particle) super.allPlayers();
    }

    @Override
    public Particle receivers(@Nullable List<Player> receivers) {
        return (Particle) super.receivers(receivers);
    }

    @Override
    public Particle receivers(@Nullable Collection<Player> receivers) {
        return (Particle) super.receivers(receivers);
    }

    @Override
    public Particle receivers(Player @Nullable ... receivers) {
        return (Particle) super.receivers(receivers);
    }

    @Override
    public Particle receivers(int radius) {
        return (Particle) super.receivers(radius);
    }

    @Override
    public Particle receivers(int radius, boolean byDistance) {
        return (Particle) super.receivers(radius, byDistance);
    }

    @Override
    public Particle receivers(int xzRadius, int yRadius) {
        return (Particle) super.receivers(xzRadius, yRadius);
    }

    @Override
    public Particle receivers(int xzRadius, int yRadius, boolean byDistance) {
        return (Particle) super.receivers(xzRadius, yRadius, byDistance);
    }

    @Override
    public Particle receivers(int xRadius, int yRadius, int zRadius) {
        return (Particle) super.receivers(xRadius, yRadius, zRadius);
    }

    @Override
    public Particle source(@Nullable Player source) {
        return (Particle) super.source(source);
    }

    @Override
    public Particle location(Location location) {
        return (Particle) super.location(location);
    }

    @Override
    public Particle location(World world, double x, double y, double z) {
        return (Particle) super.location(world, x, y, z);
    }

    @Override
    public Particle count(int count) {
        return (Particle) super.count(count);
    }

    @Override
    public Particle offset(double offsetX, double offsetY, double offsetZ) {
        return (Particle) super.offset(offsetX, offsetY, offsetZ);
    }

    @Override
    public ParticleEffect extra(double extra) {
        return (ParticleEffect) super.extra(extra);
    }

    @Override
    public Particle force(boolean force) {
        return (Particle) super.force(force);
    }
    //</editor-fold>

}
