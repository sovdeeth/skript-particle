package com.sovdee.skriptparticles.rendering;

import ch.njol.skript.Skript;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;


/**
 * Static draw methods for rendering library shapes with plugin DrawData.
 */
public class DrawManager {

    /**
     * Maximum distance (squared) at which a player can see non-forced particles.
     * Matches the vanilla client's particle render distance of 32 blocks.
     */
    private static final double MAX_PARTICLE_DISTANCE_SQ = 32 * 32;

    /**
     * Maximum distance (squared) at which a player can see forced particles (256 blocks).
     */
    private static final double MAX_FORCED_PARTICLE_DISTANCE_SQ = 256 * 256;

    /**
     * Draws {@code shape} at its own stored {@link DrawData} location using the shape's stored
     * particle and the identity orientation. Equivalent to calling
     * {@link #draw(Shape, DynamicLocation, Quaternion, Particle, Collection)} with the shape's
     * own location and particle.
     *
     * @param shape      the shape to draw
     * @param recipients the players who should receive the particles
     */
    public static void draw(Shape shape, Collection<Player> recipients) {
        DrawData dd = DrawData.of(shape);
        DynamicLocation location = dd.getLocation();
        if (location == null) return;
        draw(shape, location, Quaternion.IDENTITY, dd.getParticleRaw(), recipients);
    }

    /**
     * Draws {@code shape} at the given {@code location} using the shape's stored particle and
     * the identity orientation.
     *
     * @param shape      the shape to draw
     * @param location   the world-space location to draw at
     * @param recipients the players who should receive the particles
     */
    public static void draw(Shape shape, DynamicLocation location, Collection<Player> recipients) {
        draw(shape, location, Quaternion.IDENTITY, DrawData.of(shape).getParticleRaw(), recipients);
    }

    /**
     * Applies {@code consumer} to {@code shape} (e.g. to set its orientation) and then draws it
     * at the given {@code location} using the shape's stored particle.
     * <br>
     * The consumer runs synchronously before any rendering begins. The shape's orientation after
     * the consumer returns is used as the base orientation for the draw call.
     *
     * @param shape      the shape to draw
     * @param location   the world-space location to draw at
     * @param consumer   a callback invoked on the shape immediately before rendering
     * @param recipients the players who should receive the particles
     */
    public static void drawWithConsumer(Shape shape, DynamicLocation location, Consumer<Shape> consumer, Collection<Player> recipients) {
        consumer.accept(shape);
        DrawData dd = DrawData.of(shape);
        Quaterniond shapeOrientation = shape.getOrientation();
        Quaternion shapeOrientationQ = new Quaternion((float) shapeOrientation.x, (float) shapeOrientation.y, (float) shapeOrientation.z, (float) shapeOrientation.w);
        draw(shape, location, shapeOrientationQ, dd.getParticleRaw(), recipients);
    }

    /**
     * Draws {@code shape} at {@code location} with an explicit base orientation and particle.
     * This is the primary draw method; all other overloads delegate to it.
     * <br>
     * If {@code location} is null the shape's stored location is used instead. Recipients are
     * pre-filtered by distance from the shape centre before any per-point work is done. If
     * {@link DrawData#getAnimationDuration()} is greater than zero the points are spread across
     * multiple async ticks; otherwise all points are sent in a single pass.
     *
     * @param shape           the shape to draw
     * @param location        the world-space location to draw at; falls back to the shape's own
     *                        stored location if {@link DynamicLocation#isNull()} returns true
     * @param baseOrientation additional rotation applied on top of the shape's own orientation
     * @param particle        the particle to use for rendering; ignored when
     *                        {@link Particle#override()} is false and the shape has its own particle
     * @param recipients      the candidate players to send particles to (filtered by distance)
     */
    public static void draw(Shape shape, DynamicLocation location, Quaternion baseOrientation, Particle particle, Collection<Player> recipients) {
        DrawData dd = DrawData.of(shape);

        if (location.isNull()) {
            DynamicLocation shapeLocation = dd.getLocation();
            if (shapeLocation == null) return;
            location = shapeLocation.clone();
        }

        dd.setLastLocation(location.clone());
        Quaterniond shapeOrientation = shape.getOrientation();
        Quaternion shapeOrientationQ = new Quaternion((float) shapeOrientation.x, (float) shapeOrientation.y, (float) shapeOrientation.z, (float) shapeOrientation.w);
        dd.getLastOrientation().set(baseOrientation.clone().mul(shapeOrientationQ));

        if (!particle.override()) {
            dd.getParticleRaw().parent(shape);
            particle = dd.getParticleRaw();
        }

        // Pre-filter recipients once instead of per-point.
        Location center = location.getLocation();
        List<Player> filteredRecipients = filterRecipients(recipients, center, particle.force());

        if (filteredRecipients.isEmpty()) return;

        Quaterniond lastOrientationD = new Quaterniond(
                dd.getLastOrientation().x, dd.getLastOrientation().y,
                dd.getLastOrientation().z, dd.getLastOrientation().w);

        long animationDuration = dd.getAnimationDuration();
        boolean useNMS = NMSParticleRenderer.isAvailable();

        if (animationDuration > 0) {
            // Animated: get points list for batching, then use NMS/Bukkit per batch
            List<Vector3d> jomlPoints = shape.getPointSampler().getPoints(shape, lastOrientationD);
            drawAnimated(shape, jomlPoints, lastOrientationD, animationDuration, particle, dd,
                    filteredRecipients, useNMS, center);
        } else {
            drawImmediate(shape, lastOrientationD, particle, dd, filteredRecipients, useNMS, center);
        }

        if (dd.showLocalAxes()) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), dd.getLastOrientation(), filteredRecipients);
        }
        if (dd.showGlobalAxes()) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), Quaternion.IDENTITY, filteredRecipients);
        }
    }

    /**
     * Draws all points immediately (non-animated).
     * NMS path: uses the library render loop via shape.render().
     * Fallback path: converts JOML points to Bukkit Vectors one at a time.
     */
    private static void drawImmediate(Shape shape, Quaterniond orientation, Particle particle,
                                      DrawData dd, List<Player> filteredRecipients, boolean useNMS,
                                      Location baseLoc) {
        try {
            if (useNMS) {
                NMSParticleRenderer renderer = new NMSParticleRenderer(particle, dd, filteredRecipients, baseLoc);
                ParticleRenderContext ctx = renderer.getContext();
                ctx.orientation = orientation;
                ctx.scale = shape.getScale();
                shape.render(orientation, renderer);
            } else {
                List<Vector3d> jomlPoints = shape.getPointSampler().getPoints(shape, orientation);
                for (Vector3d point : jomlPoints) {
                    particle.prepareForPoint(dd, new Vector(point.x, point.y, point.z));
                    particle.spawnToPlayers(filteredRecipients);
                }
            }
        } catch (IllegalArgumentException e) {
            Skript.error("Failed to spawn particle! Error: " + e.getMessage());
        }
    }

    /**
     * Draws points over time using batched async ticks.
     * NMS: uses the library render loop with sub-range batching.
     * Fallback: converts JOML points to Bukkit Vectors per batch.
     */
    private static void drawAnimated(Shape shape, List<Vector3d> jomlPoints, Quaterniond orientation,
                                     long animationDuration, Particle particle, DrawData dd,
                                     List<Player> filteredRecipients, boolean useNMS, Location baseLoc) {
        int particleCount = jomlPoints.size();
        double millisecondsPerPoint = animationDuration / (double) particleCount;
        Iterator<List<Vector3d>> batchIterator = MathUtil.batch(jomlPoints, millisecondsPerPoint).iterator();

        BukkitRunnable runnable = new BukkitRunnable() {
            @Override
            public void run() {
                if (!batchIterator.hasNext()) {
                    this.cancel();
                    return;
                }
                List<Vector3d> batch = batchIterator.next();
                try {
                    if (useNMS) {
                        // Use a simple inline renderer for batch sub-lists
                        sendBatchNMS(batch, particle, dd, filteredRecipients, baseLoc);
                    } else {
                        for (Vector3d point : batch) {
                            particle.prepareForPoint(dd, new Vector(point.x, point.y, point.z));
                            particle.spawnToPlayers(filteredRecipients);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Skript.error("Failed to spawn particle! Error: " + e.getMessage());
                }
            }
        };
        runnable.runTaskTimerAsynchronously(Skript.getInstance(), 0, 1);
    }

    /**
     * Sends a pre-computed batch of JOML points via NMS without running the full render pipeline.
     * Used by animated drawing where the points are already computed.
     */
    private static void sendBatchNMS(List<Vector3d> batch, Particle particle, DrawData dd,
                                     List<Player> filteredRecipients, Location baseLoc) {
        // For batched animation we use NMSParticleRenderer directly on the pre-computed points.
        // Render modifiers are skipped for animated batches (they run only on full render passes).
        NMSParticleRenderer sender = new NMSParticleRenderer(particle, dd, filteredRecipients, baseLoc);
        ParticleRenderContext ctx = new ParticleRenderContext(particle);
        sender.begin(batch.size());
        for (int i = 0; i < batch.size(); i++) {
            Vector3d p = batch.get(i);
            ctx.reset();
            ctx.x = p.x; ctx.y = p.y; ctx.z = p.z;
            ctx.index = i;
            sender.renderPoint(ctx);
        }
        sender.end();
    }

    /**
     * Filters recipients by distance from the shape center.
     * This is done once per shape draw instead of per-point, eliminating redundant checks.
     */
    private static List<Player> filterRecipients(Collection<Player> recipients, Location center, boolean force) {
        double maxDistSq = force ? MAX_FORCED_PARTICLE_DISTANCE_SQ : MAX_PARTICLE_DISTANCE_SQ;
        List<Player> filtered = new ArrayList<>(recipients.size());
        for (Player player : recipients) {
            if (!player.isOnline()) continue;
            Location playerLoc = player.getLocation();
            if (!center.getWorld().equals(playerLoc.getWorld())) continue;
            if (center.distanceSquared(playerLoc) <= maxDistSq) {
                filtered.add(player);
            }
        }
        return filtered;
    }
}
