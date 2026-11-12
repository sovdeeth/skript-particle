package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.sampling.ShapeRenderer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

/**
 * A {@link ShapeRenderer} that spawns particles through the Bukkit API, one call per player per point.
 * <p>
 * This is the fallback for servers where {@link NMSParticleRenderer} is unavailable. It is slower, but honours
 * the same per-point render context: visibility, displacement, motion, and particle overrides.
 */
public class BukkitParticleRenderer implements ShapeRenderer<ParticleRenderContext> {

    private final List<Player> recipients;
    private final double baseX, baseY, baseZ;
    private final double defaultOffsetX, defaultOffsetY, defaultOffsetZ;
    private final int defaultCount;
    private final double speed;
    private final boolean force;
    private final ParticleRenderContext context;

    /**
     * @param particle   the particle whose settings (type, data, count, offset, speed) are the defaults
     * @param recipients the players to spawn the particles for, already filtered by distance
     * @param baseLoc    the location the points are relative to
     */
    public BukkitParticleRenderer(ParticleEffect particle, List<Player> recipients, Location baseLoc) {
        this.recipients = recipients;
        this.baseX = baseLoc.getX();
        this.baseY = baseLoc.getY();
        this.baseZ = baseLoc.getZ();
        this.defaultOffsetX = particle.offsetX();
        this.defaultOffsetY = particle.offsetY();
        this.defaultOffsetZ = particle.offsetZ();
        this.defaultCount = particle.count();
        this.speed = particle.speedX();
        this.force = particle.force();
        this.context = new ParticleRenderContext(particle);
    }

    @Override
    public ParticleRenderContext getContext() {
        return context;
    }

    @Override
    public void begin(int totalPoints) {}

    @Override
    public void renderPoint(ParticleRenderContext context) {
        if (!context.visible) return;

        double x = baseX + context.x + context.displacementX;
        double y = baseY + context.y + context.displacementY;
        double z = baseZ + context.z + context.displacementZ;

        double offsetX = defaultOffsetX, offsetY = defaultOffsetY, offsetZ = defaultOffsetZ;
        int count = defaultCount;
        // a count of zero makes the client treat the offset as a velocity
        if (context.hasMotion) {
            offsetX = context.motionX;
            offsetY = context.motionY;
            offsetZ = context.motionZ;
            count = 0;
        }

        for (Player player : recipients) {
            player.spawnParticle(context.particle, x, y, z, count, offsetX, offsetY, offsetZ, speed, context.data, force);
        }
    }

    @Override
    public void end() {}
}
