package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

import java.util.Collection;
import java.util.List;

/**
 * Plugin-side particle descriptor extending Skript's {@link ParticleEffect} with a fluent builder API.
 * Adds an optional {@link #parent(Shape) parent shape} reference (used to look up {@link DrawData} at
 * spawn time) and an {@link #override()} flag that controls whether the draw manager should replace
 * this particle with the shape's own stored particle.
 * <br>
 * All mutating methods return {@code this} (or a new {@code Particle}) so calls can be chained.
 * Use {@link #of(ParticleEffect)} to promote an existing {@link ParticleEffect} into a {@code Particle},
 * or construct one directly from a Bukkit {@link org.bukkit.Particle} type.
 */
public class Particle extends ParticleEffect {

    private @Nullable Shape parent;
    private boolean override = false;

    /**
     * Creates a new {@code Particle} by copying all settings from an existing {@link ParticleEffect}.
     * Properties copied include particle type, count, data, location, offset, extra speed, force flag,
     * receivers, and source player.
     *
     * @param effect the {@link ParticleEffect} whose settings should be copied
     * @return a new {@code Particle} containing all settings from {@code effect}
     */
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

    /**
     * Creates a new {@code Particle} wrapping the given Bukkit particle type with default settings
     * (count 1, no offset, no extra, no force, no receivers, no source, no location).
     *
     * @param particle the Bukkit particle type to use
     */
    public Particle(org.bukkit.Particle particle) {
        super(particle);
    }

    /**
     * Spawns this particle at the parent shape's last draw location offset by {@code delta}.
     * Does nothing if no {@link #parent(Shape) parent shape} has been set or if the parent has
     * no recorded last location.
     *
     * @param delta the offset from the shape's last draw location at which to spawn the particle
     */
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

    /**
     * Returns the shape this particle is associated with, or {@code null} if none has been set.
     * The parent is used to retrieve {@link DrawData} (e.g. the last draw location) when
     * {@link #spawn(org.bukkit.util.Vector)} is called.
     *
     * @return the parent {@link Shape}, or {@code null}
     */
    @Nullable
    public Shape parent() {
        return parent;
    }

    /**
     * Sets the parent shape for this particle and returns {@code this} for chaining.
     *
     * @param parent the shape to associate with this particle, or {@code null} to clear it
     * @return this {@code Particle} instance
     */
    public Particle parent(@Nullable Shape parent) {
        this.parent = parent;
        return this;
    }

    /**
     * Returns {@code true} if this particle should override the shape's own stored particle when
     * passed to the draw manager. When {@code false} (the default), the draw manager replaces this
     * particle with the shape's stored particle.
     *
     * @return {@code true} if this particle overrides the shape's particle
     */
    public boolean override() {
        return override;
    }

    /**
     * Sets whether this particle should override the shape's stored particle and returns
     * {@code this} for chaining.
     *
     * @param override {@code true} to use this particle instead of the shape's own particle
     * @return this {@code Particle} instance
     */
    public Particle override(boolean override) {
        this.override = override;
        return this;
    }

    /**
     * Creates a deep copy of this particle, duplicating all settings including particle type,
     * count, extra, offset, data, force flag, receivers, source, location, parent shape, and
     * override flag.
     *
     * @return a new {@code Particle} with identical settings
     */
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

    /**
     * Returns a human-readable description of this particle, including particle type, parent shape
     * (if set), and override flag.
     *
     * @return a string representation of this particle
     */
    @Override
    public String toString() {
        return "Particle{" +
                "particle=" + this.particle() +
                (parent != null ? ", parent=" + parent : "") +
                ", override=" + override +
                '}';
    }

    //<editor-fold desc="Fluent overrides" defaultstate="collapsed">

    /**
     * Sets the Bukkit particle type and returns {@code this} for chaining.
     *
     * @param particle the Bukkit particle type
     * @return this {@code Particle} instance
     */
    @Override
    public Particle particle(org.bukkit.Particle particle) {
        return (Particle) super.particle(particle);
    }

    /**
     * Configures this particle to be sent to all online players and returns {@code this} for
     * chaining.
     *
     * @return this {@code Particle} instance
     */
    @Override
    public Particle allPlayers() {
        return (Particle) super.allPlayers();
    }

    /**
     * Sets the explicit list of recipient players and returns {@code this} for chaining.
     *
     * @param receivers the list of players who will receive this particle, or {@code null} to clear
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(@Nullable List<Player> receivers) {
        return (Particle) super.receivers(receivers);
    }

    /**
     * Sets the recipient players from a collection and returns {@code this} for chaining.
     *
     * @param receivers the collection of players who will receive this particle, or {@code null} to clear
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(@Nullable Collection<Player> receivers) {
        return (Particle) super.receivers(receivers);
    }

    /**
     * Sets the recipient players from a varargs array and returns {@code this} for chaining.
     *
     * @param receivers the players who will receive this particle, or {@code null} to clear
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(Player @Nullable ... receivers) {
        return (Particle) super.receivers(receivers);
    }

    /**
     * Sets the recipient radius, targeting all players within {@code radius} blocks of the spawn
     * location, and returns {@code this} for chaining.
     *
     * @param radius the block radius within which players receive this particle
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(int radius) {
        return (Particle) super.receivers(radius);
    }

    /**
     * Sets the recipient radius with an optional distance sort and returns {@code this} for
     * chaining.
     *
     * @param radius     the block radius within which players receive this particle
     * @param byDistance {@code true} to sort recipients by distance before sending
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(int radius, boolean byDistance) {
        return (Particle) super.receivers(radius, byDistance);
    }

    /**
     * Sets separate horizontal and vertical recipient radii and returns {@code this} for chaining.
     *
     * @param xzRadius the horizontal (XZ-plane) radius in blocks
     * @param yRadius  the vertical (Y-axis) radius in blocks
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(int xzRadius, int yRadius) {
        return (Particle) super.receivers(xzRadius, yRadius);
    }

    /**
     * Sets separate horizontal and vertical recipient radii with an optional distance sort and
     * returns {@code this} for chaining.
     *
     * @param xzRadius   the horizontal (XZ-plane) radius in blocks
     * @param yRadius    the vertical (Y-axis) radius in blocks
     * @param byDistance {@code true} to sort recipients by distance before sending
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(int xzRadius, int yRadius, boolean byDistance) {
        return (Particle) super.receivers(xzRadius, yRadius, byDistance);
    }

    /**
     * Sets independent per-axis recipient radii and returns {@code this} for chaining.
     *
     * @param xRadius the radius along the X axis in blocks
     * @param yRadius the radius along the Y axis in blocks
     * @param zRadius the radius along the Z axis in blocks
     * @return this {@code Particle} instance
     */
    @Override
    public Particle receivers(int xRadius, int yRadius, int zRadius) {
        return (Particle) super.receivers(xRadius, yRadius, zRadius);
    }

    /**
     * Sets the source player (used for particle visibility checks) and returns {@code this} for
     * chaining.
     *
     * @param source the player to treat as the particle source, or {@code null} to clear
     * @return this {@code Particle} instance
     */
    @Override
    public Particle source(@Nullable Player source) {
        return (Particle) super.source(source);
    }

    /**
     * Sets the spawn location from a {@link Location} and returns {@code this} for chaining.
     *
     * @param location the world-space location at which to spawn the particle
     * @return this {@code Particle} instance
     */
    @Override
    public Particle location(Location location) {
        return (Particle) super.location(location);
    }

    /**
     * Sets the spawn location from world and coordinate components and returns {@code this} for
     * chaining.
     *
     * @param world the world in which to spawn the particle
     * @param x     the X coordinate
     * @param y     the Y coordinate
     * @param z     the Z coordinate
     * @return this {@code Particle} instance
     */
    @Override
    public Particle location(World world, double x, double y, double z) {
        return (Particle) super.location(world, x, y, z);
    }

    /**
     * Sets the particle count and returns {@code this} for chaining. A count of 0 enables
     * directional mode, where offset values are interpreted as velocity components.
     *
     * @param count the number of particles to spawn per call
     * @return this {@code Particle} instance
     */
    @Override
    public Particle count(int count) {
        return (Particle) super.count(count);
    }

    /**
     * Sets the offset (spread) for each axis and returns {@code this} for chaining. When count
     * is 0, these values are used as directional velocity instead of random spread.
     *
     * @param offsetX the X-axis spread or velocity component
     * @param offsetY the Y-axis spread or velocity component
     * @param offsetZ the Z-axis spread or velocity component
     * @return this {@code Particle} instance
     */
    @Override
    public Particle offset(double offsetX, double offsetY, double offsetZ) {
        return (Particle) super.offset(offsetX, offsetY, offsetZ);
    }

    /**
     * Sets the extra data value (typically speed or intensity) and returns {@code this} for
     * chaining.
     *
     * @param extra the extra value to pass to the particle (e.g. particle speed)
     * @return this {@code Particle} instance
     */
    @Override
    public Particle extra(double extra) {
        return (Particle) super.extra(extra);
    }

    /**
     * Sets whether this particle should be forced visible beyond the normal viewing distance and
     * returns {@code this} for chaining. Forced particles are visible up to 256 blocks away.
     *
     * @param force {@code true} to bypass the normal 32-block render distance
     * @return this {@code Particle} instance
     */
    @Override
    public Particle force(boolean force) {
        return (Particle) super.force(force);
    }
    //</editor-fold>

}
