package com.sovdee.shapes.sampling;

import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

/**
 * Responsible for sampling points from a {@link Shape}'s geometry.
 * Manages sampling configuration (style, density, ordering) and caching.
 */
public interface PointSampler extends Cloneable {

    SamplingStyle getStyle();
    void setStyle(SamplingStyle style);

    double getDensity();
    void setDensity(double density);

    Comparator<Vector3d> getOrdering();
    void setOrdering(Comparator<Vector3d> ordering);

    UUID getUUID();

    DrawContext getDrawContext();
    void setDrawContext(DrawContext context);

    /**
     * Samples points from the given shape using the shape's own orientation.
     */
    Set<Vector3d> getPoints(Shape shape);

    /**
     * Samples points from the given shape using the given orientation.
     */
    Set<Vector3d> getPoints(Shape shape, Quaterniond orientation);

    /**
     * Computes and sets the density to achieve approximately the given particle count.
     */
    default void setParticleCount(Shape shape, int count) {
        setDensity(shape.computeDensity(getStyle(), count));
    }

    PointSampler clone();
}
