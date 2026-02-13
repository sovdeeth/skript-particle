package com.sovdee.skriptparticles.particles;

import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.shapes.DrawData;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

import java.util.Collection;
import java.util.List;

public class Particle extends ParticleEffect {

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

    public void spawn(org.bukkit.util.Vector delta) {
        if (parent == null) return;
        DrawData dd = DrawData.of(parent);
        if (dd.getLastLocation() == null) return;
        location(dd.getLastLocation().getLocation().add(delta));
        super.spawn();
    }

    /**
     * Prepares this particle's location for a given point delta without spawning it.
     * Call this before using {@link #spawnToPlayers(Collection)} to send to pre-filtered recipients.
     */
    public void prepareForPoint(DrawData dd, org.bukkit.util.Vector delta) {
        location(dd.getLastLocation().getLocation().add(delta));
    }

    /**
     * Spawns the particle to each player individually, bypassing per-player distance/vanish
     * checks. Assumes the caller has already pre-filtered the recipient list.
     */
    public void spawnToPlayers(Collection<Player> players) {
        Location loc = location();
        if (loc == null || loc.getWorld() == null) return;
        for (Player player : players) {
            player.spawnParticle(particle(), loc, count(), offsetX(), offsetY(), offsetZ(), extra(), data());
        }
    }

    /**
     * Calls the given consumer with the particle type and prepared data for NMS packet construction.
     * This allows external code to build packets without going through the Bukkit API.
     *
     * @param consumer receives the Bukkit particle type and the particle-specific data (may be null)
     */
    public void forPacketData(java.util.function.BiConsumer<org.bukkit.Particle, @Nullable Object> consumer) {
        consumer.accept(particle(), data());
    }

    @Nullable
    public Shape parent() {
        return parent;
    }

    public Particle parent(@Nullable Shape parent) {
        this.parent = parent;
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

        return particle.parent(this.parent())
                .override(this.override());
    }

    @Override
    public String toString() {
        return "Particle{" +
                "particle=" + this.particle() +
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
    public Particle extra(double extra) {
        return (Particle) super.extra(extra);
    }

    @Override
    public Particle force(boolean force) {
        return (Particle) super.force(force);
    }
    //</editor-fold>

}
