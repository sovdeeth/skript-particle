package com.sovdee.shapes.shapes;

import com.sovdee.shapes.DrawContext;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public abstract class AbstractShape implements Shape {

    private final UUID uuid;
    private Set<Vector3d> points;

    private Style style;
    private final Quaterniond orientation;
    private final Quaterniond lastOrientation;
    private double scale;
    private Vector3d offset;
    private Comparator<Vector3d> ordering;
    private double particleDensity = 0.25;

    private DrawContext drawContext;
    private boolean dynamic = false;

    private State lastState;
    private boolean needsUpdate = false;

    public AbstractShape() {
        this.style = Style.OUTLINE;
        this.points = new LinkedHashSet<>();

        this.orientation = new Quaterniond();
        this.lastOrientation = new Quaterniond();
        this.scale = 1;
        this.offset = new Vector3d(0, 0, 0);

        this.uuid = UUID.randomUUID();

        this.lastState = getState();
    }

    @Override
    public Set<Vector3d> getPoints() {
        return getPoints(this.orientation);
    }

    @Override
    public Set<Vector3d> getPoints(Quaterniond orientation) {
        State state = getState(orientation);
        if (dynamic || needsUpdate || !lastState.equals(state) || points.isEmpty()) {
            if (ordering != null)
                points = new TreeSet<>(ordering);
            else
                points = new LinkedHashSet<>();
            generatePoints(points);
            for (Vector3d point : points) {
                orientation.transform(point);
                point.mul(scale);
                point.add(offset);
            }
            lastState = state;
            needsUpdate = false;
        }
        return points;
    }

    @Override
    public void setPoints(Set<Vector3d> points) {
        this.points = points;
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        generateOutline(points);
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        generateSurface(points);
    }

    @Override
    public Vector3d getRelativeXAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector3d(1, 0, 0));
    }

    @Override
    public Vector3d getRelativeYAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector3d(0, 1, 0));
    }

    @Override
    public Vector3d getRelativeZAxis(boolean useLastOrientation) {
        return (useLastOrientation ? lastOrientation : orientation).transform(new Vector3d(0, 0, 1));
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
    public Quaterniond getOrientation() {
        return new Quaterniond(orientation);
    }

    @Override
    public void setOrientation(Quaterniond orientation) {
        this.orientation.set(orientation);
        this.setNeedsUpdate(true);
    }

    public Quaterniond getLastOrientation() {
        return lastOrientation;
    }

    public void setLastOrientation(Quaterniond orientation) {
        this.lastOrientation.set(orientation);
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
    public Vector3d getOffset() {
        return new Vector3d(offset);
    }

    @Override
    public void setOffset(Vector3d offset) {
        this.offset = offset;
        this.setNeedsUpdate(true);
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public Comparator<Vector3d> getOrdering() {
        return ordering;
    }

    @Override
    public void setOrdering(Comparator<Vector3d> comparator) {
        ordering = comparator;
        this.setNeedsUpdate(true);
    }

    @Override
    public double getParticleDensity() {
        return particleDensity;
    }

    @Override
    public void setParticleDensity(double particleDensity) {
        this.particleDensity = Math.max(particleDensity, Shape.EPSILON);
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
    public DrawContext getDrawContext() {
        return drawContext;
    }

    @Override
    public void setDrawContext(DrawContext drawContext) {
        this.drawContext = drawContext;
    }

    @Override
    public boolean isDynamic() {
        return dynamic;
    }

    @Override
    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    /**
     * Adds vertically extruded copies of the given points up to the given height.
     * Mutates the set in-place by adding new points at each height step.
     */
    protected static void fillVertically(Set<Vector3d> points, double height, double particleDensity) {
        Set<Vector3d> base = new LinkedHashSet<>(points);
        double heightStep = height / Math.round(height / particleDensity);
        for (double y = 0; y < height; y += heightStep) {
            for (Vector3d v : base) {
                points.add(new Vector3d(v.x, y, v.z));
            }
        }
    }

    public abstract Shape clone();

    @Override
    public Shape copyTo(Shape shape) {
        shape.setOrientation(new Quaterniond(this.orientation));
        shape.setScale(this.scale);
        shape.setOffset(new Vector3d(this.offset));
        shape.setParticleDensity(this.particleDensity);
        shape.setStyle(this.style);
        shape.setOrdering(this.ordering);
        shape.setPoints(this.getPoints());
        shape.setNeedsUpdate(this.needsUpdate);
        shape.setLastState(this.lastState);
        shape.setDynamic(this.dynamic);
        if (this.drawContext != null)
            shape.setDrawContext(this.drawContext.copy());
        return shape;
    }

    @Override
    public State getState() {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    @Override
    public State getState(Quaterniond orientation) {
        return new State(style, orientation.hashCode(), scale, offset.hashCode(), particleDensity);
    }

    @Override
    public void setLastState(State state) {
        this.lastState = state;
    }
}
