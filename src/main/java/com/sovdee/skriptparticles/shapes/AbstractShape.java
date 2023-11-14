package com.sovdee.skriptparticles.shapes;

import ch.njol.skript.Skript;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.particles.ParticleGradient;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.ParticleUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class AbstractShape implements Shape {

    private final UUID uuid;
    private Set<Vector> points;

    private Style style;
    private final Quaternion orientation;
    private final Quaternion lastOrientation;
    private double scale;
    private Vector offset;
    private @Nullable DynamicLocation location;
    private Particle particle;
    private double particleDensity = 0.25; // todo: make this configurable
    private long animationDuration = 0;

    private State lastState;
    private @Nullable DynamicLocation lastLocation;
    private boolean needsUpdate = false;

    private boolean drawLocalAxes = false;
    private boolean drawGlobalAxes = false;

    public AbstractShape() {
        this.style = Style.OUTLINE;
        this.points = new LinkedHashSet<>();

        this.orientation = Quaternion.IDENTITY.clone();
        this.lastOrientation = Quaternion.IDENTITY.clone();
        this.scale = 1;
        this.offset = new Vector(0, 0, 0);

        this.particle = (Particle) new Particle(org.bukkit.Particle.FLAME).parent(this).extra(0);

        this.uuid = UUID.randomUUID();

        this.lastState = getState();
    }

    @Override
    public Set<Vector> getPoints() {
        return getPoints(this.orientation);
    }

    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        State state = getState(orientation);
        if (needsUpdate || !lastState.equals(state) || points.isEmpty()) {
            points = generatePoints();
            for (Vector point : points) {
                orientation.transform(point);
                point.multiply(scale);
                point.add(offset);
            }
            lastState = state;
            needsUpdate = false;
        }
        return points;
    }

    @Override
    public void setPoints(Set<Vector> points) {
        this.points = points;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        return generateOutline();
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateFilled() {
        return generateSurface();
    }

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
        // cache the last location and orientation used to draw the shape
        if (location.isNull()) {
            if (this.location == null)
                return;
            location = this.location.clone();
        }

        lastLocation = location.clone();
        lastOrientation.set(baseOrientation.clone().mul(orientation));

        // If the particle doesn't override the shape's particle, use the shape's particle
        if (!particle.override()) {
            this.particle.parent(this);
            particle = this.particle;
            // update the gradient if needed
            @Nullable ParticleGradient gradient = particle.gradient();
            if (gradient != null && gradient.isLocal())
                gradient.setOrientation(lastOrientation);
        }

        particle.receivers(recipients);
        Collection<Vector> toDraw = getPoints(lastOrientation);
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
            // no animation needed, draw all particles at once
            for (Vector point :toDraw) {
                try {
                    particle.spawn(point);
                } catch (IllegalArgumentException e) {
                    Skript.error("Failed to spawn particle! Error: " + e.getMessage());
                    return;
                }
            }
        }

        if (drawLocalAxes) {
            ParticleUtil.drawAxes(location.getLocation().add(offset), lastOrientation, recipients);
        }
        if (drawGlobalAxes) {
            ParticleUtil.drawAxes(location.getLocation().add(offset), Quaternion.IDENTITY, recipients);
        }
    }

    @Override
    public void draw(DynamicLocation location, Consumer<Shape> consumer, Collection<Player> recipients) {
        consumer.accept(this);
        draw(location, this.orientation, this.particle, recipients);
    }

    @Override
    public Vector getRelativeXAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(1, 0, 0));
    }

    @Override
    public Vector getRelativeYAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 1, 0));
    }

    @Override
    public Vector getRelativeZAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector(0, 0, 1));
    }

    @Override
    public void showLocalAxes(boolean show) {
        drawLocalAxes = show;
    }

    @Override
    public boolean showLocalAxes() {
        return drawLocalAxes;
    }

    @Override
    public void showGlobalAxes(boolean show) {
        drawGlobalAxes = show;
    }

    @Override
    public boolean showGlobalAxes() {
        return drawGlobalAxes;
    }

    @Override
    @Nullable
    public DynamicLocation getLastLocation() {
        return lastLocation;
    }

    @Override
    public Style getStyle() {
        return style;
    }

    @Override
    public void setStyle(Style style) {
        this.style = style;
        this.setNeedsUpdate(true);
    }

    @Override
    public Quaternion getOrientation() {
        return new Quaternion(orientation);
    }

    @Override
    public void setOrientation(Quaternionf orientation) {
        this.orientation.set(orientation);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public void setScale(double scale) {
        this.scale = scale;
        this.setNeedsUpdate(true);
    }

    @Override
    public Vector getOffset() {
        return offset.clone();
    }

    @Override
    public void setOffset(Vector offset) {
        this.offset = offset;
        this.setNeedsUpdate(true);
    }

    @Override
    @Nullable
    public DynamicLocation getLocation() {
        if (location == null)
            return null;
        return location.clone();
    }

    @Override
    public void setLocation(DynamicLocation location) {
        this.location = location;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Particle getParticle() {
        return particle.clone();
    }

    @Override
    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    @Override
    public double getParticleDensity() {
        return particleDensity;
    }


    @Override
    public void setParticleDensity(double particleDensity) {
        this.particleDensity = Math.max(particleDensity, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public int getParticleCount() {
        return getPoints().size();
    }

    @Override
    public boolean needsUpdate() {
        return needsUpdate;
    }

    @Override
    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    @Override
    public long getAnimationDuration() {
        return animationDuration;
    }

    @Override
    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    @Contract("-> new")
    public abstract Shape clone();

    @Override
    @Contract("_ -> param1")
    public Shape copyTo(Shape shape) {
        shape.setOrientation(this.orientation);
        shape.setScale(this.scale);
        shape.setOffset(this.offset.clone());
        shape.setParticle(this.particle.clone());
        if (this.location != null)
            shape.setLocation(this.location.clone());
        shape.setParticleDensity(this.particleDensity);
        shape.setStyle(this.style);
        shape.showLocalAxes(this.drawLocalAxes);
        shape.showGlobalAxes(this.drawGlobalAxes);
        // ensure that the shape's points are updated, so we don't have to recalculate them unless we change the copy.
        shape.setPoints(this.getPoints());
        shape.setNeedsUpdate(this.needsUpdate);
        shape.setLastState(this.lastState);
        return shape;
    }

    @Override
    @Contract("-> new")
    public State getState() {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    @Override
    @Contract("_ -> new")
    public State getState(Quaternion orientation) {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    @Override
    public void setLastState(State state) {
        this.lastState = state;
    }

}
