package com.sovdee.shapes.sampling;

import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

/**
 * Default implementation of {@link PointSampler} with hash-based caching.
 */
public class DefaultPointSampler implements PointSampler {

    private SamplingStyle style = SamplingStyle.OUTLINE;
    private double density = 0.25;
    private Comparator<Vector3d> ordering;
    private final UUID uuid;
    private DrawContext drawContext;

    // Cache
    private Set<Vector3d> cachedPoints = new LinkedHashSet<>();
    private CacheState lastState;
    private boolean needsUpdate = false;

    public DefaultPointSampler() {
        this.uuid = UUID.randomUUID();
        this.lastState = new CacheState(style, 0, 1.0, 0, density, 0);
    }

    @Override
    public Set<Vector3d> getPoints(Shape shape) {
        return getPoints(shape, shape.getOrientation());
    }

    @Override
    public Set<Vector3d> getPoints(Shape shape, Quaterniond orientation) {
        CacheState state = new CacheState(style, orientation.hashCode(),
                shape.getScale(), shape.getOffset().hashCode(), density, shape.getVersion());
        if (shape.isDynamic() || needsUpdate || !state.equals(lastState) || cachedPoints.isEmpty()) {
            Set<Vector3d> points = (ordering != null) ? new TreeSet<>(ordering) : new LinkedHashSet<>();

            shape.beforeSampling(density);
            switch (style) {
                case OUTLINE -> shape.generateOutline(points, density);
                case SURFACE -> shape.generateSurface(points, density);
                case FILL -> shape.generateFilled(points, density);
            }
            shape.afterSampling(points);

            for (Vector3d point : points) {
                orientation.transform(point);
                point.mul(shape.getScale());
                point.add(shape.getOffset());
            }
            cachedPoints = points;
            lastState = state;
            needsUpdate = false;
        }
        return cachedPoints;
    }

    public void markDirty() {
        needsUpdate = true;
    }

    @Override
    public SamplingStyle getStyle() { return style; }

    @Override
    public void setStyle(SamplingStyle style) {
        this.style = style;
        this.needsUpdate = true;
    }

    @Override
    public double getDensity() { return density; }

    @Override
    public void setDensity(double density) {
        this.density = Math.max(density, Shape.EPSILON);
        this.needsUpdate = true;
    }

    @Override
    public Comparator<Vector3d> getOrdering() { return ordering; }

    @Override
    public void setOrdering(Comparator<Vector3d> ordering) {
        this.ordering = ordering;
        this.needsUpdate = true;
    }

    @Override
    public UUID getUUID() { return uuid; }

    @Override
    public DrawContext getDrawContext() { return drawContext; }

    @Override
    public void setDrawContext(DrawContext context) { this.drawContext = context; }

    @Override
    public DefaultPointSampler clone() {
        try {
            DefaultPointSampler copy = (DefaultPointSampler) super.clone();
            // Don't share the cache
            copy.cachedPoints = new LinkedHashSet<>();
            copy.needsUpdate = true;
            if (drawContext != null)
                copy.drawContext = drawContext.copy();
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    private record CacheState(SamplingStyle style, int orientationHash, double scale,
                               int offsetHash, double density, long shapeVersion) {}
}
