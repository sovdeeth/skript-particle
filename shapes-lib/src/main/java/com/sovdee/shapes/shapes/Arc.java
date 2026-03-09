package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.List;

/**
 * An arc is a partial {@link Circle}: a sector of a circle swept from angle {@code 0} to
 * {@code cutoffAngle} radians around the Y axis. When a non-zero height is provided it becomes
 * a partial cylinder (a cylindrical wedge).
 * <p>
 * The cutoff angle is clamped to {@code [0, 2π]}. A cutoff of {@code 2π} produces a full
 * circle or cylinder, identical in geometry to the parent {@link Circle}. Angles are measured
 * in the XZ plane starting from the positive X axis.
 * <p>
 * Surface generation delegates to {@link #generateFilled} so that sector fills are consistent
 * across all three {@link com.sovdee.shapes.sampling.SamplingStyle} modes.
 */
public class Arc extends Circle implements CutoffShape {

    /**
     * Creates a flat (zero-height) arc with the given radius and angular span.
     *
     * @param radius      the radius of the arc; clamped to at least {@link Shape#EPSILON}
     * @param cutoffAngle the angular span of the arc in radians; clamped to {@code [0, 2π]}
     */
    public Arc(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    /**
     * Creates a cylindrical-wedge arc with the given radius, height, and angular span.
     *
     * @param radius      the radius of the arc; clamped to at least {@link Shape#EPSILON}
     * @param height      the vertical extent of the arc; clamped to {@code >= 0}
     * @param cutoffAngle the angular span of the arc in radians; clamped to {@code [0, 2π]}
     */
    public Arc(double radius, double height, double cutoffAngle) {
        super(radius, height);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    /**
     * Generates surface points by delegating to {@link #generateFilled(List, double)} so that
     * the surface of an arc sector is treated as a filled disc segment rather than just an outline.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        generateFilled(points, density);
    }

    /**
     * {@inheritDoc}
     *
     * @return the cutoff angle in radians, in the range {@code [0, 2π]}
     */
    @Override
    public double getCutoffAngle() {
        return this.cutoffAngle;
    }

    /**
     * {@inheritDoc}
     * Clamps the given angle to {@code [0, 2π]} and invalidates the point cache.
     *
     * @param cutoffAngle the new cutoff angle in radians; clamped to {@code [0, 2π]}
     */
    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
        invalidate();
    }

    /**
     * Returns {@code true} if {@code point} is within the parent {@link Circle}'s disc/cylinder
     * and its XZ angle is within the cutoff span. The angle is measured from the positive X axis
     * and normalised to {@code [0, 2π]} using {@code atan2(z, x)}.
     *
     * @param point the point to test, in local coordinates
     * @return {@code true} if the point lies inside this arc sector
     */
    @Override
    public boolean contains(Vector3d point) {
        if (!super.contains(point)) return false;
        double angle = Math.atan2(point.z, point.x);
        if (angle < 0) angle += 2 * Math.PI;
        return angle <= cutoffAngle;
    }

    /**
     * Returns a deep copy of this arc with identical radius, height, cutoff angle, and base
     * transform state.
     *
     * @return a new {@link Arc} equal to this one
     */
    @Override
    public Shape clone() {
        return this.copyTo(new Arc(this.getRadius(), this.getHeight(), cutoffAngle));
    }

    /**
     * Returns a human-readable description of this arc, including its radius, cutoff angle, and height.
     *
     * @return a string of the form {@code Arc{radius=..., cutoffAngle=..., height=...}}
     */
    @Override
    public String toString() {
        return "Arc{radius=" + this.getRadius() + ", cutoffAngle=" + cutoffAngle + ", height=" + this.getHeight() + '}';
    }
}
