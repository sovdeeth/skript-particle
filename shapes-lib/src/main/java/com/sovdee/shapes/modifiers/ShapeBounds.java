package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;

import java.util.List;

/**
 * Axis-aligned bounding box computed from a set of points.
 * Provides normalization helpers for use in modifiers and shaders.
 */
public final class ShapeBounds {

    private final double minX, minY, minZ;
    private final double maxX, maxY, maxZ;
    private final double rangeX, rangeY, rangeZ;
    private final double maxRadius;

    // Pre-computed inverse ranges for fast normalization (no division in hot loop)
    private final double invRangeX, invRangeY, invRangeZ, invMaxRadius;

    private ShapeBounds(double minX, double minY, double minZ,
                        double maxX, double maxY, double maxZ,
                        double maxRadius) {
        this.minX = minX; this.minY = minY; this.minZ = minZ;
        this.maxX = maxX; this.maxY = maxY; this.maxZ = maxZ;
        this.rangeX = maxX - minX;
        this.rangeY = maxY - minY;
        this.rangeZ = maxZ - minZ;
        this.maxRadius = maxRadius;
        this.invRangeX = rangeX > 1e-12 ? 1.0 / rangeX : 0.0;
        this.invRangeY = rangeY > 1e-12 ? 1.0 / rangeY : 0.0;
        this.invRangeZ = rangeZ > 1e-12 ? 1.0 / rangeZ : 0.0;
        this.invMaxRadius = maxRadius > 1e-12 ? 1.0 / maxRadius : 0.0;
    }

    /**
     * Computes the bounding box from the given list of points.
     * Returns a degenerate (all-zero) bounds if the list is empty.
     */
    public static ShapeBounds compute(List<Vector3d> points) {
        if (points.isEmpty()) {
            return new ShapeBounds(0, 0, 0, 0, 0, 0, 0);
        }
        double minX = Double.MAX_VALUE, minY = Double.MAX_VALUE, minZ = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE, maxY = -Double.MAX_VALUE, maxZ = -Double.MAX_VALUE;
        double maxRadius = 0;
        for (Vector3d p : points) {
            if (p.x < minX) minX = p.x;
            if (p.y < minY) minY = p.y;
            if (p.z < minZ) minZ = p.z;
            if (p.x > maxX) maxX = p.x;
            if (p.y > maxY) maxY = p.y;
            if (p.z > maxZ) maxZ = p.z;
            double r = Math.sqrt(p.x * p.x + p.z * p.z);
            if (r > maxRadius) maxRadius = r;
        }
        return new ShapeBounds(minX, minY, minZ, maxX, maxY, maxZ, maxRadius);
    }

    /** Normalizes x to [0, 1] within the shape's X extent. */
    public double normalizeX(double x) { return (x - minX) * invRangeX; }

    /** Normalizes y to [0, 1] within the shape's Y extent. */
    public double normalizeY(double y) { return (y - minY) * invRangeY; }

    /** Normalizes z to [0, 1] within the shape's Z extent. */
    public double normalizeZ(double z) { return (z - minZ) * invRangeZ; }

    /** Normalizes the XZ radius to [0, 1] relative to the shape's max XZ radius. */
    public double normalizeRadial(double x, double z) {
        return Math.sqrt(x * x + z * z) * invMaxRadius;
    }

    /** Returns the max XZ distance from origin across all points. */
    public double maxRadiusXZ() { return maxRadius; }

    public double height() { return rangeY; }
    public double width()  { return rangeX; }
    public double length() { return rangeZ; }

    public double minX() { return minX; }
    public double minY() { return minY; }
    public double minZ() { return minZ; }
    public double maxX() { return maxX; }
    public double maxY() { return maxY; }
    public double maxZ() { return maxZ; }

    public double invRangeX() { return invRangeX; }
    public double invRangeY() { return invRangeY; }
    public double invRangeZ() { return invRangeZ; }
    public double invMaxRadius() { return invMaxRadius; }
}
