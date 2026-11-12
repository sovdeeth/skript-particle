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
    private Vector3d centerOffset = new Vector3d(0, 0, 0);
    private Supplier<Vector3d> cornerASupplier;
    private Supplier<Vector3d> cornerBSupplier;
    // the corners last read from the suppliers, to detect when they move
    private Vector3d lastCornerA;
    private Vector3d lastCornerB;

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
        setCorners(cornerA, cornerB);
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
        this.lastCornerA = new Vector3d(cornerA.get());
        this.lastCornerB = new Vector3d(cornerB.get());
        setCorners(lastCornerA, lastCornerB);
        setDynamic(true);
    }

    /**
     * Sizes the rectangle to span the two corners, and centers it between them within its plane.
     */
    private void setCorners(Vector3d cornerA, Vector3d cornerB) {
        setLengthWidth(cornerA, cornerB);
        centerOffset = new Vector3d(cornerB).add(cornerA).mul(0.5);
        switch (plane) {
            case XZ -> centerOffset.y = 0;
            case XY -> centerOffset.z = 0;
            case YZ -> centerOffset.x = 0;
        }
    }

    /**
     * Re-reads the corner suppliers, if any, and invalidates the rectangle when a corner moved.
     */
    @Override
    public void refresh() {
        if (cornerASupplier == null || cornerBSupplier == null)
            return;
        Vector3d a = cornerASupplier.get();
        Vector3d b = cornerBSupplier.get();
        if (a.equals(lastCornerA) && b.equals(lastCornerB))
            return;
        lastCornerA = new Vector3d(a);
        lastCornerB = new Vector3d(b);
        setCorners(a, b);
        invalidate();
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
        this.halfWidth = Math.max(Math.abs(width) / 2, Shape.EPSILON);
        this.halfLength = Math.max(Math.abs(length) / 2, Shape.EPSILON);
    }

    private Vector3d vectorFromLengthWidth(double length, double width) {
        return switch (plane) {
            case XZ -> new Vector3d(length, 0, width);
            case XY -> new Vector3d(length, width, 0);
            case YZ -> new Vector3d(0, length, width);
        };
    }

    /**
     * Generates outline (perimeter) points for the rectangle. Points are placed along all four
     * edges, roughly {@code density} apart.
     *
     * @param points  the list to which outline points are appended
     * @param density the approximate spacing between consecutive points (used implicitly via step sizes)
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        int start = points.size();
        int lengthSegments = segments(2 * halfLength, density);
        int widthSegments = segments(2 * halfWidth, density);
        // the two sides along the width include the corners; the other two only add the points between them
        for (int i = 1; i < lengthSegments; i++) {
            double l = -halfLength + 2 * halfLength * i / lengthSegments;
            points.add(vectorFromLengthWidth(l, -halfWidth));
            points.add(vectorFromLengthWidth(l, halfWidth));
        }
        for (int i = 0; i <= widthSegments; i++) {
            double w = -halfWidth + 2 * halfWidth * i / widthSegments;
            points.add(vectorFromLengthWidth(-halfLength, w));
            points.add(vectorFromLengthWidth(halfLength, w));
        }
        applyCenterOffset(points, start);
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
        int start = points.size();
        int lengthSegments = segments(2 * halfLength, density);
        int widthSegments = segments(2 * halfWidth, density);
        for (int i = 0; i <= widthSegments; i++) {
            double w = -halfWidth + 2 * halfWidth * i / widthSegments;
            for (int j = 0; j <= lengthSegments; j++) {
                double l = -halfLength + 2 * halfLength * j / lengthSegments;
                points.add(vectorFromLengthWidth(l, w));
            }
        }
        applyCenterOffset(points, start);
    }

    /**
     * Moves the points generated since {@code start} from around the origin to around the rectangle's center.
     */
    private void applyCenterOffset(List<Vector3d> points, int start) {
        if (centerOffset.x == 0 && centerOffset.y == 0 && centerOffset.z == 0)
            return;
        for (int i = start; i < points.size(); i++) {
            points.get(i).add(centerOffset);
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
     * @param tolerance how far outside the exact shape still counts as contained
     * @return {@code true} if the point is inside the rectangle
     */
    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        point.sub(centerOffset);
        double l = halfLength + tolerance;
        double w = halfWidth + tolerance;
        return switch (plane) {
            case XZ -> Math.abs(point.x) <= l && Math.abs(point.z) <= w && Math.abs(point.y) <= tolerance;
            case XY -> Math.abs(point.x) <= l && Math.abs(point.y) <= w && Math.abs(point.z) <= tolerance;
            case YZ -> Math.abs(point.y) <= l && Math.abs(point.z) <= w && Math.abs(point.x) <= tolerance;
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
        // copy the current state rather than re-reading the suppliers, so the copy has identical geometry
        Rectangle rectangle = new Rectangle(this.getLength(), this.getWidth(), plane);
        rectangle.centerOffset = new Vector3d(this.centerOffset);
        rectangle.cornerASupplier = this.cornerASupplier;
        rectangle.cornerBSupplier = this.cornerBSupplier;
        rectangle.lastCornerA = this.lastCornerA;
        rectangle.lastCornerB = this.lastCornerB;
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
