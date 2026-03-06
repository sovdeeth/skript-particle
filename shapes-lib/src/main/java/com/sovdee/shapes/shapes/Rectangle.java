package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.List;
import java.util.function.Supplier;

/**
 * A flat rectangular shape aligned to one of the three axis-aligned planes (XZ, XY, or YZ).
 * The rectangle is centred at the origin of the shape's local coordinate space; an optional
 * centre offset is applied when corners are supplied, shifting the origin to the midpoint of
 * the two corners.
 * <p>
 * Implements {@link LWHShape} where {@code length} and {@code width} describe the two in-plane
 * dimensions; {@code height} is always {@code 0}. The orientation plane is chosen via the
 * {@link Plane} enum.
 * </p>
 * <p>
 * For entity-following rectangles whose corners track moving locations, use the plugin-side
 * {@code DynamicRectangle} wrapper rather than the supplier constructor directly.
 * </p>
 */
public class Rectangle extends AbstractShape implements LWHShape {

    private Plane plane;
    private double halfLength;
    private double halfWidth;
    private double lengthStep = 1.0;
    private double widthStep = 1.0;
    private Vector3d centerOffset = new Vector3d(0, 0, 0);
    private Supplier<Vector3d> cornerASupplier;
    private Supplier<Vector3d> cornerBSupplier;

    /**
     * Constructs a rectangle centred at the local origin with explicit dimensions.
     *
     * @param length the extent along the first axis of {@code plane}; clamped to at least {@link Shape#EPSILON}
     * @param width  the extent along the second axis of {@code plane}; clamped to at least {@link Shape#EPSILON}
     * @param plane  the axis-aligned plane in which the rectangle lies
     */
    public Rectangle(double length, double width, Plane plane) {
        super();
        this.plane = plane;
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
    }

    /**
     * Constructs a static rectangle from two opposite corner positions.
     * The centre offset is computed from the midpoint of the two corners (projected onto
     * {@code plane} so the off-axis component is zeroed).
     *
     * @param cornerA the first corner of the rectangle
     * @param cornerB the diagonally opposite corner; must differ from {@code cornerA}
     * @param plane   the axis-aligned plane in which the rectangle lies
     * @throws IllegalArgumentException if {@code cornerA} equals {@code cornerB}
     */
    public Rectangle(Vector3d cornerA, Vector3d cornerB, Plane plane) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Corners cannot be the same.");
        this.plane = plane;
        setLengthWidth(cornerA, cornerB);
        centerOffset = new Vector3d(cornerB).add(cornerA).mul(0.5);
        switch (plane) {
            case XZ -> centerOffset.y = 0;
            case XY -> centerOffset.z = 0;
            case YZ -> centerOffset.x = 0;
        }
    }

    /**
     * Constructs a dynamic rectangle whose corners are resolved each frame via suppliers.
     * The shape is marked as {@link AbstractShape#isDynamic() dynamic} so points are regenerated
     * every draw call. The initial dimensions are computed from the suppliers' first values.
     *
     * @param cornerA a supplier providing the current position of the first corner
     * @param cornerB a supplier providing the current position of the diagonally opposite corner
     * @param plane   the axis-aligned plane in which the rectangle lies
     */
    public Rectangle(Supplier<Vector3d> cornerA, Supplier<Vector3d> cornerB, Plane plane) {
        super();
        this.plane = plane;
        this.cornerASupplier = cornerA;
        this.cornerBSupplier = cornerB;
        Vector3d a = cornerA.get();
        Vector3d b = cornerB.get();
        setLengthWidth(a, b);
        setDynamic(true);
    }

    private void setLengthWidth(Vector3d cornerA, Vector3d cornerB) {
        double length = switch (plane) {
            case XZ, XY -> Math.abs(cornerA.x - cornerB.x);
            case YZ -> Math.abs(cornerA.y - cornerB.y);
        };
        double width = switch (plane) {
            case XZ, YZ -> Math.abs(cornerA.z - cornerB.z);
            case XY -> Math.abs(cornerA.y - cornerB.y);
        };
        this.halfWidth = Math.abs(width) / 2;
        this.halfLength = Math.abs(length) / 2;
    }

    private Vector3d vectorFromLengthWidth(double length, double width) {
        return switch (plane) {
            case XZ -> new Vector3d(length, 0, width);
            case XY -> new Vector3d(length, width, 0);
            case YZ -> new Vector3d(0, length, width);
        };
    }

    private void calculateSteps(double density) {
        lengthStep = 2 * halfWidth / Math.round(2 * halfWidth / density);
        widthStep = 2 * halfLength / Math.round(2 * halfLength / density);
    }

    /**
     * Called before each sampling pass. If dynamic corner suppliers are present, refreshes the
     * half-length and half-width from their current values. Then pre-computes the per-axis step
     * sizes used by {@link #generateOutline} and {@link #generateSurface}.
     *
     * @param density the target spacing between sampled points
     */
    @Override
    public void beforeSampling(double density) {
        if (cornerASupplier != null && cornerBSupplier != null) {
            Vector3d a = cornerASupplier.get();
            Vector3d b = cornerBSupplier.get();
            setLengthWidth(a, b);
        }
        calculateSteps(density);
    }

    /**
     * Called after each sampling pass to apply the centre offset to all generated points.
     * This translates the points (which are generated relative to the local origin) into the
     * correct world position when the rectangle was constructed from corners.
     *
     * @param points the list of sampled points to translate in-place
     */
    @Override
    public void afterSampling(List<Vector3d> points) {
        points.forEach(vector -> vector.add(centerOffset));
    }

    /**
     * Generates outline (perimeter) points for the rectangle. Points are placed along all four
     * edges at the step sizes calculated in {@link #beforeSampling}.
     *
     * @param points  the list to which outline points are appended
     * @param density the approximate spacing between consecutive points (used implicitly via step sizes)
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        for (double l = -halfLength + widthStep; l < halfLength; l += widthStep) {
            points.add(vectorFromLengthWidth(l, -halfWidth));
            points.add(vectorFromLengthWidth(l, halfWidth));
        }
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            points.add(vectorFromLengthWidth(-halfLength, w));
            points.add(vectorFromLengthWidth(halfLength, w));
        }
    }

    /**
     * Generates surface (filled plane) points for the rectangle by sampling a uniform grid
     * across both in-plane axes.
     *
     * @param points  the list to which surface points are appended
     * @param density the approximate spacing between consecutive points (used implicitly via step sizes)
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            for (double l = -halfLength; l <= halfLength; l += widthStep) {
                points.add(vectorFromLengthWidth(l, w));
            }
        }
    }

    /**
     * Computes the inter-point density needed to reach {@code targetPointCount} sampled points.
     * For SURFACE/FILL: {@code sqrt(area / count)}. For OUTLINE: {@code perimeter / count}.
     *
     * @param style            the sampling style that determines which formula is used
     * @param targetPointCount the desired number of points
     * @return the computed density
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return switch (style) {
            case FILL, SURFACE -> Math.sqrt(4 * halfWidth * halfLength / count);
            case OUTLINE -> 4 * (halfWidth + halfLength) / count;
        };
    }

    /**
     * Returns {@code true} if {@code point} lies within (or on the boundary of) the rectangle,
     * including the constraint that it lies on the rectangle's plane (within {@link #EPSILON}).
     *
     * @param point the point to test in local coordinates
     * @return {@code true} if the point is inside the rectangle
     */
    @Override
    public boolean contains(Vector3d point) {
        return switch (plane) {
            case XZ -> Math.abs(point.x) <= halfLength && Math.abs(point.z) <= halfWidth && Math.abs(point.y) < EPSILON;
            case XY -> Math.abs(point.x) <= halfLength && Math.abs(point.y) <= halfWidth && Math.abs(point.z) < EPSILON;
            case YZ -> Math.abs(point.y) <= halfLength && Math.abs(point.z) <= halfWidth && Math.abs(point.x) < EPSILON;
        };
    }

    /**
     * Returns the full length of the rectangle (twice the internal half-length).
     *
     * @return the length along the first in-plane axis
     */
    @Override
    public double getLength() { return halfLength * 2; }

    /**
     * Sets the rectangle's length and invalidates the point cache.
     *
     * @param length the new full length; clamped to at least {@link Shape#EPSILON}
     */
    @Override
    public void setLength(double length) {
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the full width of the rectangle (twice the internal half-width).
     *
     * @return the width along the second in-plane axis
     */
    @Override
    public double getWidth() { return halfWidth * 2; }

    /**
     * Sets the rectangle's width and invalidates the point cache.
     *
     * @param width the new full width; clamped to at least {@link Shape#EPSILON}
     */
    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Always returns {@code 0}; a rectangle has no height dimension.
     *
     * @return {@code 0}
     */
    @Override
    public double getHeight() { return 0; }

    /**
     * No-op; a rectangle has no height dimension.
     *
     * @param height ignored
     */
    @Override
    public void setHeight(double height) { }

    /**
     * Returns the axis-aligned {@link Plane} in which this rectangle lies.
     *
     * @return the current plane
     */
    public Plane getPlane() { return plane; }

    /**
     * Sets the axis-aligned plane and invalidates the point cache.
     *
     * @param plane the new plane orientation
     */
    public void setPlane(Plane plane) {
        this.plane = plane;
        invalidate();
    }

    /**
     * Returns the supplier used to resolve the first corner for dynamic rectangles, or
     * {@code null} if the rectangle was constructed from static dimensions or corners.
     *
     * @return the corner A supplier, or {@code null}
     */
    public Supplier<Vector3d> getCornerASupplier() { return cornerASupplier; }

    /**
     * Returns the supplier used to resolve the second corner for dynamic rectangles, or
     * {@code null} if the rectangle was constructed from static dimensions or corners.
     *
     * @return the corner B supplier, or {@code null}
     */
    public Supplier<Vector3d> getCornerBSupplier() { return cornerBSupplier; }

    /**
     * Returns a deep copy of this rectangle, preserving dynamic vs. static corner mode, the
     * centre offset, and all inherited {@link AbstractShape} state.
     *
     * @return a new {@link Rectangle} with identical configuration
     */
    @Override
    public Shape clone() {
        Rectangle rectangle;
        if (cornerASupplier != null && cornerBSupplier != null) {
            rectangle = new Rectangle(cornerASupplier, cornerBSupplier, plane);
        } else {
            rectangle = new Rectangle(this.getLength(), this.getWidth(), plane);
        }
        rectangle.centerOffset = new Vector3d(this.centerOffset);
        return this.copyTo(rectangle);
    }

    /**
     * Returns a human-readable description including the plane name, length, and width.
     *
     * @return a string of the form {@code "<plane> rectangle with length <l> and width <w>"}
     */
    @Override
    public String toString() {
        String axis = this.plane.toString().toLowerCase();
        return axis + " rectangle with length " + this.getLength() + " and width " + this.getWidth();
    }

    /**
     * Identifies which of the three axis-aligned planes a {@link Rectangle} lies in.
     * <ul>
     *   <li>{@link #XZ} - the horizontal ground plane (Y = 0)</li>
     *   <li>{@link #XY} - the vertical frontal plane (Z = 0)</li>
     *   <li>{@link #YZ} - the vertical lateral plane (X = 0)</li>
     * </ul>
     */
    public enum Plane {
        /**
         * The horizontal XZ plane; length along X, width along Z.
         */
        XZ,
        /**
         * The vertical XY plane; length along X, width along Y.
         */
        XY,
        /**
         * The vertical YZ plane; length along Y, width along Z.
         */
        YZ
    }
}
