package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.sampling.RenderPass;
import com.sovdee.shapes.sampling.ShapeRenderer;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.SkriptParticle;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

/**
 * Static draw methods for rendering library shapes with plugin DrawData.
 * <p>
 * Drawing does not modify the shape or its {@link DrawData}: everything a draw needs is resolved up front
 * and kept local to that draw, so the same shape can safely be drawn from several threads or locations.
 */
public class DrawManager {

    /**
     * Maximum distance at which a player can see non-forced particles.
     * Matches the vanilla client's particle render distance of 32 blocks.
     */
    private static final double MAX_PARTICLE_DISTANCE = 32;

    /**
     * Maximum distance at which a player can see forced particles (256 blocks).
     */
    private static final double MAX_FORCED_PARTICLE_DISTANCE = 256;

    /**
     * Drawing more particles than this in one call earns a warning in the console.
     */
    private static final int LARGE_PARTICLE_THRESHOLD = 16_000;

    private static final boolean USE_NMS = isNMSAvailable();

    private static boolean isNMSAvailable() {
        try {
            return NMSParticleRenderer.isAvailable();
        } catch (Throwable e) {
            // The renderer's classes cannot even be loaded on this server (e.g. different mappings).
            SkriptParticle.warning("Packet-based particle rendering is unavailable on this server (" + e + "). " +
                    "Falling back to the Bukkit API, which is slower.");
            return false;
        }
    }

    /**
     * Draws a shape at its own stored location (from {@link DrawData}).
     * Does nothing if the shape has no location.
     *
     * @param shape      the shape to draw
     * @param recipients the players who should see the particles
     */
    public static void draw(Shape shape, Collection<Player> recipients) {
        draw(shape, new DynamicLocation(), recipients);
    }

    /**
     * Draws a shape at the given location, using the shape's own orientation and particle.
     * If the location is {@linkplain DynamicLocation#isNull() null}, the shape's own stored location is used
     * instead; if it has none, nothing is drawn.
     * <p>
     * If the shape's {@link DrawData} has an animation duration, the particles are spread out over that time
     * on an asynchronous timer. Otherwise they are all sent immediately, on the calling thread.
     *
     * @param shape      the shape to draw
     * @param location   the location to draw the shape at
     * @param recipients the players who should see the particles
     */
    public static void draw(Shape shape, DynamicLocation location, Collection<Player> recipients) {
        DrawData dd = DrawData.of(shape);

        if (location.isNull()) {
            DynamicLocation shapeLocation = dd.getLocation();
            if (shapeLocation == null || shapeLocation.isNull()) return;
            location = shapeLocation;
        }
        Location center = location.getLocation();
        if (center.getWorld() == null) return;

        // a private copy: rendering reads the particle's settings while other draws may be using it too
        ParticleEffect particle = dd.getParticle();
        Quaterniond orientation = shape.getOrientation();
        List<Vector3d> points = shape.getPointSampler().getPoints(shape, orientation);
        if (points.isEmpty()) return;

        if (points.size() > LARGE_PARTICLE_THRESHOLD && dd.markLargeSizeWarned()) {
            SkriptParticle.warning("A shape is drawing " + points.size() + " particles in one call. " +
                    "This may cause lag. Consider reducing its particle density or size.");
        }

        // Filter recipients once instead of per point. A player can see the shape if they are within
        // particle range of any part of it, so the range is extended by how far the shape reaches.
        double reach = shape.getPointSampler().boundsOf(points).maxDistance();
        List<Player> filteredRecipients = filterRecipients(recipients, center, particle.force(), reach);

        if (!filteredRecipients.isEmpty()) {
            ShapeRenderer<ParticleRenderContext> renderer = USE_NMS
                    ? new NMSParticleRenderer(particle, filteredRecipients, center)
                    : new BukkitParticleRenderer(particle, filteredRecipients, center);
            ParticleRenderContext context = renderer.getContext();
            context.orientation = orientation;
            context.scale = shape.getScale();
            RenderPass<ParticleRenderContext> pass = shape.getPointSampler().preparePass(shape, points, context);

            long animationDuration = dd.getAnimationDuration();
            if (animationDuration > 0) {
                drawAnimated(points, animationDuration, pass, renderer);
            } else {
                pass.renderBatch(points, renderer);
            }

            if (dd.showLocalAxes() || dd.showGlobalAxes()) {
                Location axesOrigin = center.clone().add(VectorConversion.toBukkit(shape.getOffset()));
                if (dd.showLocalAxes())
                    ParticleUtil.drawAxes(axesOrigin, new Quaternion(orientation.x, orientation.y, orientation.z, orientation.w), filteredRecipients);
                if (dd.showGlobalAxes())
                    ParticleUtil.drawAxes(axesOrigin, Quaternion.IDENTITY, filteredRecipients);
            }
        }
    }

    /**
     * Renders the points in batches, one batch per tick, so that the whole shape takes
     * {@code animationDuration} milliseconds to appear. Stops at the first failure.
     */
    private static void drawAnimated(List<Vector3d> points, long animationDuration,
                                     RenderPass<ParticleRenderContext> pass,
                                     ShapeRenderer<ParticleRenderContext> renderer) {
        Plugin plugin = SkriptParticle.getInstance();
        if (plugin == null) return;

        double millisecondsPerPoint = animationDuration / (double) points.size();
        Iterator<List<Vector3d>> batchIterator = MathUtil.batch(points, millisecondsPerPoint).iterator();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!batchIterator.hasNext()) {
                    this.cancel();
                    return;
                }
                try {
                    pass.renderBatch(batchIterator.next(), renderer);
                } catch (RuntimeException e) {
                    // every remaining batch would fail the same way, so report it once and stop
                    this.cancel();
                    SkriptParticle.severe("Failed to draw an animated shape, stopping the animation: " + e.getMessage());
                }
            }
        };
        runnable.runTaskTimerAsynchronously(plugin, 0, 1);
    }

    /**
     * Returns the online recipients, in the right world, who are close enough to see some part of a shape.
     *
     * @param center the location the shape is drawn around
     * @param force  whether the particles are forced, which extends how far away they can be seen
     * @param reach  how far the shape extends from its center
     */
    private static List<Player> filterRecipients(Collection<Player> recipients, Location center, boolean force, double reach) {
        double maxDistance = (force ? MAX_FORCED_PARTICLE_DISTANCE : MAX_PARTICLE_DISTANCE) + reach;
        double maxDistanceSquared = maxDistance * maxDistance;
        List<Player> filtered = new ArrayList<>(recipients.size());
        for (Player player : recipients) {
            if (!player.isOnline()) continue;
            Location playerLoc = player.getLocation();
            if (!center.getWorld().equals(playerLoc.getWorld())) continue;
            if (center.distanceSquared(playerLoc) <= maxDistanceSquared) {
                filtered.add(player);
            }
        }
        return filtered;
    }
}
