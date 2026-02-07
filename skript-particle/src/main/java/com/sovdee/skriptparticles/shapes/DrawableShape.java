package com.sovdee.skriptparticles.shapes;

import ch.njol.skript.Skript;
import com.sovdee.shapes.AbstractShape;
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
import org.jetbrains.annotations.Contract;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Wraps a library {@link com.sovdee.shapes.Shape} with rendering metadata (particle, location, animation, etc.)
 * This is the type that Skript elements operate on.
 */
public class DrawableShape implements Shape {

    private final com.sovdee.shapes.Shape shape;

    private Particle particle;
    private @Nullable DynamicLocation location;
    private @Nullable DynamicLocation lastLocation;
    private final Quaternion lastOrientation;
    private long animationDuration = 0;
    private boolean drawLocalAxes = false;
    private boolean drawGlobalAxes = false;

    public DrawableShape(com.sovdee.shapes.Shape shape) {
        this.shape = shape;
        this.particle = (Particle) new Particle(org.bukkit.Particle.FLAME).parent(this).extra(0);
        this.lastOrientation = Quaternion.IDENTITY.clone();
    }

    /**
     * Gets the underlying library shape.
     */
    public com.sovdee.shapes.Shape getShape() {
        return shape;
    }

    // ---- Delegated geometric methods ----

    @Override
    public Set<Vector> getPoints() {
        return VectorConversion.toBukkit(shape.getPoints());
    }

    @Override
    public void setPoints(Set<Vector> points) {
        shape.setPoints(VectorConversion.toJOML(points));
    }

    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        Quaterniond qd = new Quaterniond(orientation.x, orientation.y, orientation.z, orientation.w);
        return VectorConversion.toBukkit(shape.getPoints(qd));
    }

    @Override
    public void generatePoints(Set<Vector> points) {
        Set<Vector3d> jomlPoints = new LinkedHashSet<>();
        shape.generatePoints(jomlPoints);
        points.addAll(VectorConversion.toBukkit(jomlPoints));
    }

    @Override
    public void generateOutline(Set<Vector> points) {
        Set<Vector3d> jomlPoints = new LinkedHashSet<>();
        shape.generateOutline(jomlPoints);
        points.addAll(VectorConversion.toBukkit(jomlPoints));
    }

    @Override
    public void generateSurface(Set<Vector> points) {
        Set<Vector3d> jomlPoints = new LinkedHashSet<>();
        shape.generateSurface(jomlPoints);
        points.addAll(VectorConversion.toBukkit(jomlPoints));
    }

    @Override
    public void generateFilled(Set<Vector> points) {
        Set<Vector3d> jomlPoints = new LinkedHashSet<>();
        shape.generateFilled(jomlPoints);
        points.addAll(VectorConversion.toBukkit(jomlPoints));
    }

    // ---- Draw methods ----

    @Override
    public void draw(Collection<Player> recipients) {
        assert location != null;
        draw(location, Quaternion.IDENTITY, particle, recipients);
    }

    @Override
    public void draw(DynamicLocation location, Collection<Player> recipients) {
        draw(location, Quaternion.IDENTITY, particle, recipients);
    }

    @Override
    public void draw(DynamicLocation location, Quaternion baseOrientation, Particle particle, Collection<Player> recipients) {
        if (location.isNull()) {
            if (this.location == null)
                return;
            location = this.location.clone();
        }

        lastLocation = location.clone();
        Quaterniond shapeOrientation = shape.getOrientation();
        Quaternion shapeOrientationQ = new Quaternion((float) shapeOrientation.x, (float) shapeOrientation.y, (float) shapeOrientation.z, (float) shapeOrientation.w);
        lastOrientation.set(baseOrientation.clone().mul(shapeOrientationQ));

        if (!particle.override()) {
            this.particle.parent(this);
            particle = this.particle;
            @Nullable ParticleGradient gradient = particle.gradient();
            if (gradient != null && gradient.isLocal())
                gradient.setOrientation(lastOrientation);
        }

        particle.receivers(recipients);

        // Get points from library using the last orientation
        Quaterniond lastOrientationD = new Quaterniond(lastOrientation.x, lastOrientation.y, lastOrientation.z, lastOrientation.w);
        Collection<Vector> toDraw = VectorConversion.toBukkit(shape.getPoints(lastOrientationD));

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

        if (drawLocalAxes) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), lastOrientation, recipients);
        }
        if (drawGlobalAxes) {
            ParticleUtil.drawAxes(location.getLocation().add(VectorConversion.toBukkit(shape.getOffset())), Quaternion.IDENTITY, recipients);
        }
    }

    @Override
    public void draw(DynamicLocation location, Consumer<Shape> consumer, Collection<Player> recipients) {
        consumer.accept(this);
        Quaterniond shapeOrientation = shape.getOrientation();
        Quaternion shapeOrientationQ = new Quaternion((float) shapeOrientation.x, (float) shapeOrientation.y, (float) shapeOrientation.z, (float) shapeOrientation.w);
        draw(location, shapeOrientationQ, this.particle, recipients);
    }

    // ---- Axis methods ----

    @Override
    public Vector getRelativeXAxis(boolean useLastOrientation) {
        if (useLastOrientation) {
            return lastOrientation.transform(new Vector(1, 0, 0));
        }
        return VectorConversion.toBukkit(shape.getRelativeXAxis(false));
    }

    @Override
    public Vector getRelativeYAxis(boolean useLastOrientation) {
        if (useLastOrientation) {
            return lastOrientation.transform(new Vector(0, 1, 0));
        }
        return VectorConversion.toBukkit(shape.getRelativeYAxis(false));
    }

    @Override
    public Vector getRelativeZAxis(boolean useLastOrientation) {
        if (useLastOrientation) {
            return lastOrientation.transform(new Vector(0, 0, 1));
        }
        return VectorConversion.toBukkit(shape.getRelativeZAxis(false));
    }

    // ---- Debug axes ----

    @Override
    public void showLocalAxes(boolean show) { drawLocalAxes = show; }

    @Override
    public boolean showLocalAxes() { return drawLocalAxes; }

    @Override
    public void showGlobalAxes(boolean show) { drawGlobalAxes = show; }

    @Override
    public boolean showGlobalAxes() { return drawGlobalAxes; }

    // ---- Location ----

    @Override
    @Nullable
    public DynamicLocation getLastLocation() { return lastLocation; }

    @Override
    @Nullable
    public DynamicLocation getLocation() {
        if (location == null) return null;
        return location.clone();
    }

    @Override
    public void setLocation(DynamicLocation location) { this.location = location; }

    // ---- Style ----

    @Override
    public Style getStyle() {
        return Style.valueOf(shape.getStyle().name());
    }

    @Override
    public void setStyle(Style style) {
        shape.setStyle(com.sovdee.shapes.Shape.Style.valueOf(style.name()));
    }

    // ---- Orientation ----

    @Override
    public Quaternion getOrientation() {
        Quaterniond q = shape.getOrientation();
        return new Quaternion((float) q.x, (float) q.y, (float) q.z, (float) q.w);
    }

    @Override
    public void setOrientation(Quaternionf orientation) {
        shape.setOrientation(new Quaterniond(orientation.x, orientation.y, orientation.z, orientation.w));
    }

    // ---- Scale ----

    @Override
    public double getScale() { return shape.getScale(); }

    @Override
    public void setScale(double scale) { shape.setScale(scale); }

    // ---- Offset ----

    @Override
    public Vector getOffset() {
        return VectorConversion.toBukkit(shape.getOffset());
    }

    @Override
    public void setOffset(Vector offset) {
        shape.setOffset(VectorConversion.toJOML(offset));
    }

    // ---- UUID ----

    @Override
    public UUID getUUID() { return shape.getUUID(); }

    // ---- Particle ----

    @Override
    public Particle getParticle() { return particle.clone(); }

    @Override
    public void setParticle(Particle particle) { this.particle = particle; }

    // ---- Ordering ----

    @Override
    @Nullable
    public Comparator<Vector> getOrdering() {
        // We store ordering on the library shape via a Vector3d comparator.
        // This getter returns a Bukkit Vector comparator for Skript compatibility.
        Comparator<Vector3d> libOrdering = shape.getOrdering();
        if (libOrdering == null) return null;
        return (a, b) -> libOrdering.compare(VectorConversion.toJOML(a), VectorConversion.toJOML(b));
    }

    @Override
    public void setOrdering(@Nullable Comparator<Vector> comparator) {
        if (comparator == null) {
            shape.setOrdering(null);
        } else {
            shape.setOrdering((a, b) -> comparator.compare(VectorConversion.toBukkit(a), VectorConversion.toBukkit(b)));
        }
    }

    // ---- Density ----

    @Override
    public double getParticleDensity() { return shape.getParticleDensity(); }

    @Override
    public void setParticleDensity(double particleDensity) { shape.setParticleDensity(particleDensity); }

    @Override
    public int getParticleCount() { return shape.getParticleCount(); }

    @Override
    public void setParticleCount(int particleCount) { shape.setParticleCount(particleCount); }

    // ---- Update state ----

    @Override
    public boolean needsUpdate() { return shape.needsUpdate(); }

    @Override
    public void setNeedsUpdate(boolean needsUpdate) { shape.setNeedsUpdate(needsUpdate); }

    // ---- Animation ----

    @Override
    public long getAnimationDuration() { return animationDuration; }

    @Override
    public void setAnimationDuration(long animationDuration) { this.animationDuration = animationDuration; }

    // ---- Clone/Copy ----

    @Override
    @Contract("-> new")
    public DrawableShape clone() {
        DrawableShape copy = new DrawableShape(shape.clone());
        copy.particle = this.particle.clone();
        if (this.location != null)
            copy.location = this.location.clone();
        copy.animationDuration = this.animationDuration;
        copy.drawLocalAxes = this.drawLocalAxes;
        copy.drawGlobalAxes = this.drawGlobalAxes;
        copy.lastOrientation.set(this.lastOrientation);
        // Preserve ordering via the library shape's copy
        return copy;
    }

    @Override
    @Contract("_ -> param1")
    public Shape copyTo(Shape other) {
        if (other instanceof DrawableShape ds) {
            shape.copyTo(ds.shape);
            ds.particle = this.particle.clone();
            if (this.location != null)
                ds.location = this.location.clone();
            ds.animationDuration = this.animationDuration;
            ds.drawLocalAxes = this.drawLocalAxes;
            ds.drawGlobalAxes = this.drawGlobalAxes;
            ds.lastOrientation.set(this.lastOrientation);
        }
        return other;
    }

    // ---- State ----

    @Override
    @Contract("-> new")
    public State getState() {
        com.sovdee.shapes.Shape.State libState = shape.getState();
        Style style = Style.valueOf(libState.style().name());
        return new State(style, libState.orientationHash(), libState.scale(), libState.offsetHash(), libState.particleDensity());
    }

    @Override
    @Contract("_ -> new")
    public State getState(Quaternion orientation) {
        Quaterniond qd = new Quaterniond(orientation.x, orientation.y, orientation.z, orientation.w);
        com.sovdee.shapes.Shape.State libState = shape.getState(qd);
        Style style = Style.valueOf(libState.style().name());
        return new State(style, libState.orientationHash(), libState.scale(), libState.offsetHash(), libState.particleDensity());
    }

    @Override
    public void setLastState(State state) {
        com.sovdee.shapes.Shape.Style libStyle = com.sovdee.shapes.Shape.Style.valueOf(state.style().name());
        shape.setLastState(new com.sovdee.shapes.Shape.State(libStyle, state.orientationHash(), state.scale(), state.offsetHash(), state.particleDensity()));
    }

    @Override
    public String toString() {
        return shape.toString();
    }
}
