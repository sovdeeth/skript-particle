package com.sovdee.shapes.modifiers;

import com.sovdee.shapes.shapes.Shape;

/**
 * Geometry-only context for a single point during modifier and render passes.
 * A single instance is allocated per draw call and reused across all points.
 *
 * <p>Clients that need render-specific properties (color, motion, visibility) should
 * extend this class and override {@link #reset()} to clear their additional fields.
 */
public class PointContext {

    /**
     * Shape reference.
     */
    public Shape shape;

    /**
     * Local-space position (writable by geometry modifiers).
     */
    public double x, y, z;

    /**
     * 0-based index of this point in the draw order.
     */
    public int index;

    /**
     * Total number of points being drawn this pass.
     */
    public int totalPoints;

    /**
     * Pre-computed bounds for normalization helpers. Set once per pass before the modify loop.
     */
    public ShapeBounds bounds;

    // ---- Normalization convenience (delegate to bounds) ----

    /**
     * @return Normalised X position within shape bounds [0, 1].
     */
    public double normalizedX() { return bounds.normalizeX(x); }

    /**
     * @return Normalised Y position within shape bounds [0, 1].
     */
    public double normalizedY() { return bounds.normalizeY(y); }

    /**
     * @return Normalised Z position within shape bounds [0, 1].
     */
    public double normalizedZ() { return bounds.normalizeZ(z); }

    /**
     * @return Normalised XZ radius [0, 1] relative to max XZ extent.
     */
    public double normalizedRadius() { return bounds.normalizeRadial(x, z); }

    /**
     * @return Normalised 3D spherical radius [0, 1] relative to max XZ radius.
     */
    public double normalizedSpherical() {
        double r = Math.sqrt(x * x + y * y + z * z);
        double maxR = bounds.maxRadiusXZ();
        return maxR > 1e-12 ? r / maxR : 0.0;
    }

    /**
     * @return XZ angle mapped to [0, 1] (0 = +X axis, wraps around).
     */
    public double angle() {
        double a = Math.atan2(z, x);
        if (a < 0) a += 2 * Math.PI;
        return a / (2 * Math.PI);
    }

    /**
     * Resets mutable (non-geometry) fields between points.
     * Override in subclasses to clear render-specific properties (color, motion, etc.).
     */
    public void reset() {}
}
