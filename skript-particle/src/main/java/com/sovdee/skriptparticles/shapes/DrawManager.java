package com.sovdee.skriptparticles.shapes;

import ch.njol.skript.Skript;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.particles.ParticleGradient;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Static draw methods for rendering library shapes with plugin DrawData.
 */
public class DrawManager {

    public static void draw(Shape shape, Collection<Player> recipients) {
        DrawData dd = DrawData.of(shape);
        DynamicLocation location = dd.getLocation();
        if (location == null) return;
        draw(shape, location, Quaternion.IDENTITY, dd.getParticleRaw(), recipients);
    }

    public static void draw(Shape shape, DynamicLocation location, Collection<Player> recipients) {
        draw(shape, location, Quaternion.IDENTITY, DrawData.of(shape).getParticleRaw(), recipients);
    }

    public static void drawWithConsumer(Shape shape, DynamicLocation location, Consumer<Shape> consumer, Collection<Player> recipients) {
        consumer.accept(shape);
        DrawData dd = DrawData.of(shape);
        Quaterniond shapeOrientation = shape.getOrientation();
        Quaternion shapeOrientationQ = new Quaternion((float) shapeOrientation.x, (float) shapeOrientation.y, (float) shapeOrientation.z, (float) shapeOrientation.w);
        draw(shape, location, shapeOrientationQ, dd.getParticleRaw(), recipients);
    }

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
            @Nullable ParticleGradient gradient = particle.gradient();
            if (gradient != null && gradient.isLocal())
                gradient.setOrientation(dd.getLastOrientation());
        }

        particle.receivers(recipients);

        // Get points from library shape using the last orientation
        Quaterniond lastOrientationD = new Quaterniond(dd.getLastOrientation().x, dd.getLastOrientation().y, dd.getLastOrientation().z, dd.getLastOrientation().w);
        Set<Vector3d> jomlPoints = shape.getPoints(lastOrientationD);
        Collection<Vector> toDraw = VectorConversion.toBukkit(jomlPoints);

        long animationDuration = dd.getAnimationDuration();
        if (animationDuration > 0) {
            int particleCount = toDraw.size();
            double millisecondsPerPoint = animationDuration / (double) particleCount;
            Iterator<List<Vector>> batchIterator = MathUtil.batch(toDraw, millisecondsPerPoint).iterator();
            Particle finalParticle = particle;
            BukkitRunnable runnable = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!batchIterator.hasNext()) {
                        this.cancel();
                        return;
                    }
                    List<Vector> batch = batchIterator.next();
                    try {
                        for (Vector point : batch) {
                            finalParticle.spawn(point);
                        }
                    } catch (IllegalArgumentException e) {
                        Skript.error("Failed to spawn particle! Error: " + e.getMessage());
                    }
                }
            };
            runnable.runTaskTimerAsynchronously(Skript.getInstance(), 0, 1);
        } else {
            for (Vector point : toDraw) {
                try {
                    particle.spawn(point);
                } catch (IllegalArgumentException e) {
                    Skript.error("Failed to spawn particle! Error: " + e.getMessage());
                    return;
                }
            }
        }

        if (dd.showLocalAxes()) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), dd.getLastOrientation(), recipients);
        }
        if (dd.showGlobalAxes()) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), Quaternion.IDENTITY, recipients);
        }
    }
}
