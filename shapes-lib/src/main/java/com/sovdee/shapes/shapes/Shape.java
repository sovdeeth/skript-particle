package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Set;

/**
 * Represents a geometric shape. Pure geometry interface — sampling/caching/drawing
 * configuration lives in {@link PointSampler}.
 */
public interface Shape extends Cloneable {

    double EPSILON = 0.0001;

    // --- Spatial transform ---

    Quaterniond getOrientation();
    void setOrientation(Quaterniond orientation);

    double getScale();
    void setScale(double scale);

    Vector3d getOffset();
    void setOffset(Vector3d offset);

    // --- Oriented axes ---

    Vector3d getRelativeXAxis();
    Vector3d getRelativeYAxis();
    Vector3d getRelativeZAxis();

    // --- Geometry query ---

    boolean contains(Vector3d point);

    // --- Change detection ---

    long getVersion();

    // --- Dynamic support ---

    boolean isDynamic();
    void setDynamic(boolean dynamic);

    // --- Point generation (density as parameter) ---

    void generateOutline(Set<Vector3d> points, double density);
    void generateSurface(Set<Vector3d> points, double density);
    void generateFilled(Set<Vector3d> points, double density);

    /**
     * Called by PointSampler before point generation. Override for supplier refresh, step recalc, etc.
     */
    default void beforeSampling(double density) {}

    /**
     * Called by PointSampler after point generation. Override for centerOffset adjustment, etc.
     */
    default void afterSampling(Set<Vector3d> points) {}

    /**
     * Computes the density needed to achieve approximately the given number of points.
     */
    double computeDensity(SamplingStyle style, int targetPointCount);

    // --- PointSampler ---

    PointSampler getPointSampler();
    void setPointSampler(PointSampler sampler);

    // --- Replication ---

    Shape clone();
    Shape copyTo(Shape target);
}
