package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.List;
import java.util.function.Supplier;

/**
 * A cuboid (rectangular prism) shape, defined either by explicit length/width/height dimensions
 * or by two opposing corner {@link Vector3d} positions. The cuboid is always axis-aligned in
 * local space and centred at the origin, offset by any {@code centerOffset} computed from the
 * corner positions.
 * <br>
 * When constructed from two {@link java.util.function.Supplier Supplier&lt;Vector3d&gt;} corners the
 * cuboid is marked as {@link AbstractShape#setDynamic(boolean) dynamic}, meaning the corner
 * positions are re-evaluated each time the cuboid is sampled (see {@link #refresh()}).
 * <br>
 * For entity-following dynamic cuboids in the plugin layer, use the {@code DynamicCuboid} wrapper.
 */
public class Cuboid extends AbstractShape implements LWHShape {

    private double halfLength, halfWidth, halfHeight;
    private Vector3d centerOffset = new Vector3d(0, 0, 0);
    private Supplier<Vector3d> cornerASupplier;
    private Supplier<Vector3d> cornerBSupplier;
    // the corners last read from the suppliers, to detect when they move
    private Vector3d lastCornerA;
    private Vector3d lastCornerB;

    /**
     * Constructs a cuboid centred at the local origin with the given dimensions.
     * Each half-dimension is clamped to at least {@link Shape#EPSILON} to prevent
     * degenerate geometry.
     *
     * @param length extent along the X axis (total, not half)
     * @param width  extent along the Z axis (total, not half)
     * @param height extent along the Y axis (total, not half)
     */
    public Cuboid(double length, double width, double height) {
        super();
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        this.halfHeight = Math.max(height / 2, Shape.EPSILON);
    }

    /**
     * Constructs a cuboid from two opposing corner positions expressed as world-space
     * {@link Vector3d} values. The cuboid's dimensions are derived from the axis-aligned
     * bounding box of the two corners, and the {@code centerOffset} is set to the midpoint
     * so that drawing is centred correctly.
     *
     * @param cornerA first corner of the cuboid
     * @param cornerB second corner, diagonally opposite to {@code cornerA}
     * @throws IllegalArgumentException if both corners are equal
     */
    public Cuboid(Vector3d cornerA, Vector3d cornerB) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Cuboid corners cannot be equal.");
        setCorners(cornerA, cornerB);
    }

    /**
     * Constructs a dynamic cuboid whose corners are provided by {@link Supplier} functions.
     * The suppliers are called once during construction to compute initial dimensions,
     * and again before every sampling (see {@link #refresh()}) so that the cuboid tracks
     * positions that change over time (e.g. entity locations).
     * <br>
     * This constructor marks the shape as {@link AbstractShape#setDynamic(boolean) dynamic}.
     *
     * @param cornerA supplier returning the first corner position
     * @param cornerB supplier returning the second corner position, diagonally opposite
     */
    public Cuboid(Supplier<Vector3d> cornerA, Supplier<Vector3d> cornerB) {
        super();
        this.cornerASupplier = cornerA;
        this.cornerBSupplier = cornerB;
        this.lastCornerA = new Vector3d(cornerA.get());
        this.lastCornerB = new Vector3d(cornerB.get());
        setCorners(lastCornerA, lastCornerB);
        setDynamic(true);
    }

    /**
     * Sizes the cuboid to span the two corners, and centers it between them.
     */
    private void setCorners(Vector3d cornerA, Vector3d cornerB) {
        this.halfLength = Math.max(Math.abs(cornerB.x - cornerA.x) / 2, Shape.EPSILON);
        this.halfWidth = Math.max(Math.abs(cornerB.z - cornerA.z) / 2, Shape.EPSILON);
        this.halfHeight = Math.max(Math.abs(cornerB.y - cornerA.y) / 2, Shape.EPSILON);
        this.centerOffset = new Vector3d(cornerB).add(cornerA).mul(0.5);
    }

    /**
     * Re-reads the corner suppliers, if any, and invalidates the cuboid when a corner moved.
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

    /**
     * Generates the 12-edge wireframe of the cuboid. Points are placed along each of the
     * four parallel edges in the X, Y, and Z directions using the pre-computed step sizes.
     * Corner vertices are included; interior edge steps along non-primary directions avoid
     * re-adding the already-placed corner points.
     *
     * @param points  the list to which generated points are appended
     * @param density the target spacing between adjacent points (pre-computed step sizes
     *                are used instead of {@code density} directly)
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        int start = points.size();
        int xSegments = segments(2 * halfLength, density);
        int ySegments = segments(2 * halfHeight, density);
        int zSegments = segments(2 * halfWidth, density);
        // the four edges along x include the corners; the other edges only add the points between them
        for (int i = 0; i <= xSegments; i++) {
            double x = -halfLength + 2 * halfLength * i / xSegments;
            points.add(new Vector3d(x, -halfHeight, -halfWidth));
            points.add(new Vector3d(x, -halfHeight, halfWidth));
            points.add(new Vector3d(x, halfHeight, -halfWidth));
            points.add(new Vector3d(x, halfHeight, halfWidth));
        }
        for (int i = 1; i < ySegments; i++) {
            double y = -halfHeight + 2 * halfHeight * i / ySegments;
            points.add(new Vector3d(-halfLength, y, -halfWidth));
            points.add(new Vector3d(-halfLength, y, halfWidth));
            points.add(new Vector3d(halfLength, y, -halfWidth));
            points.add(new Vector3d(halfLength, y, halfWidth));
        }
        for (int i = 1; i < zSegments; i++) {
            double z = -halfWidth + 2 * halfWidth * i / zSegments;
            points.add(new Vector3d(-halfLength, -halfHeight, z));
            points.add(new Vector3d(-halfLength, halfHeight, z));
            points.add(new Vector3d(halfLength, -halfHeight, z));
            points.add(new Vector3d(halfLength, halfHeight, z));
        }
        applyCenterOffset(points, start);
    }

    /**
     * Generates points covering all six faces of the cuboid. The top and bottom (XZ) faces are
     * sampled as full grids; the front/back (XY) and left/right (YZ) faces fill in the remaining
     * interior rows/columns to avoid duplicating edge points already added by the adjacent faces.
     *
     * @param points  the list to which generated points are appended
     * @param density the target spacing between adjacent points (pre-computed step sizes
     *                are used instead of {@code density} directly)
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        int start = points.size();
        int xSegments = segments(2 * halfLength, density);
        int ySegments = segments(2 * halfHeight, density);
        int zSegments = segments(2 * halfWidth, density);
        // top and bottom faces, including their edges
        for (int i = 0; i <= xSegments; i++) {
            double x = -halfLength + 2 * halfLength * i / xSegments;
            for (int k = 0; k <= zSegments; k++) {
                double z = -halfWidth + 2 * halfWidth * k / zSegments;
                points.add(new Vector3d(x, -halfHeight, z));
                points.add(new Vector3d(x, halfHeight, z));
            }
        }
        // the remaining faces, without the edges already covered
        for (int j = 1; j < ySegments; j++) {
            double y = -halfHeight + 2 * halfHeight * j / ySegments;
            for (int k = 0; k <= zSegments; k++) {
                double z = -halfWidth + 2 * halfWidth * k / zSegments;
                points.add(new Vector3d(-halfLength, y, z));
                points.add(new Vector3d(halfLength, y, z));
            }
            for (int i = 1; i < xSegments; i++) {
                double x = -halfLength + 2 * halfLength * i / xSegments;
                points.add(new Vector3d(x, y, -halfWidth));
                points.add(new Vector3d(x, y, halfWidth));
            }
        }
        applyCenterOffset(points, start);
    }

    /**
     * Generates a uniform 3-D grid of points filling the interior of the cuboid. Every lattice
     * site within {@code [-halfLength, halfLength] × [-halfHeight, halfHeight] × [-halfWidth, halfWidth]}
     * is included, using the pre-computed axis step sizes.
     *
     * @param points  the list to which generated points are appended
     * @param density the target spacing between adjacent points (pre-computed step sizes
     *                are used instead of {@code density} directly)
     */
    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        int start = points.size();
        int xSegments = segments(2 * halfLength, density);
        int ySegments = segments(2 * halfHeight, density);
        int zSegments = segments(2 * halfWidth, density);
        for (int i = 0; i <= xSegments; i++) {
            double x = -halfLength + 2 * halfLength * i / xSegments;
            for (int j = 0; j <= ySegments; j++) {
                double y = -halfHeight + 2 * halfHeight * j / ySegments;
                for (int k = 0; k <= zSegments; k++) {
                    double z = -halfWidth + 2 * halfWidth * k / zSegments;
                    points.add(new Vector3d(x, y, z));
                }
            }
        }
        applyCenterOffset(points, start);
    }

    /**
     * Moves the points generated since {@code start} from around the origin to around the cuboid's center.
     */
    private void applyCenterOffset(List<Vector3d> points, int start) {
        if (centerOffset.x == 0 && centerOffset.y == 0 && centerOffset.z == 0)
            return;
        for (int i = start; i < points.size(); i++) {
            points.get(i).add(centerOffset);
        }
    }

    /**
     * Estimates the point spacing required to produce approximately {@code targetPointCount} points
     * for the given {@link SamplingStyle}.
     * <ul>
     *   <li>OUTLINE: {@code density = totalEdgeLength / count}, where total edge length
     *       is {@code 8 * (L/2 + H/2 + W/2)}</li>
     *   <li>SURFACE: {@code density = sqrt(totalSurfaceArea / count)}, where surface area
     *       is {@code 2*(L*H + L*W + H*W)}</li>
     *   <li>FILL: {@code density = cbrt(volume / count)}, where volume is {@code L*W*H}</li>
     * </ul>
     *
     * @param style            the sampling style (OUTLINE, SURFACE, or FILL)
     * @param targetPointCount the desired number of output points
     * @return the estimated point spacing to achieve the target count
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(1, targetPointCount);
        return switch (style) {
            case OUTLINE -> 8 * (halfLength + halfHeight + halfWidth) / count;
            case SURFACE -> Math.sqrt(8 * (halfLength * halfHeight + halfLength * halfWidth + halfHeight * halfWidth) / count);
            case FILL -> Math.cbrt(8 * halfLength * halfHeight * halfWidth / count);
        };
    }

    /**
     * Returns {@code true} if the given point lies inside or on the surface of this cuboid.
     * Tests are performed in local (centred) space: the point is considered contained when
     * {@code |x| ≤ halfLength && |y| ≤ halfHeight && |z| ≤ halfWidth}.
     *
     * @param point the point to test, in local shape space
     * @return {@code true} if {@code point} is within the axis-aligned bounds of the cuboid
     */
    @Override
    public boolean contains(Vector3d point) {
        return Math.abs(point.x) <= halfLength &&
               Math.abs(point.y) <= halfHeight &&
               Math.abs(point.z) <= halfWidth;
    }

    /**
     * Returns the total length of the cuboid along the X axis ({@code halfLength * 2}).
     *
     * @return the full X-axis extent
     */
    @Override
    public double getLength() { return halfLength * 2; }

    /**
     * Sets the total length of the cuboid along the X axis. Values below {@link Shape#EPSILON}
     * are clamped to {@code EPSILON} to prevent degenerate geometry. Invalidates the point cache.
     *
     * @param length the new X-axis extent (total, not half)
     */
    @Override
    public void setLength(double length) {
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the total width of the cuboid along the Z axis ({@code halfWidth * 2}).
     *
     * @return the full Z-axis extent
     */
    @Override
    public double getWidth() { return halfWidth * 2; }

    /**
     * Sets the total width of the cuboid along the Z axis. Values below {@link Shape#EPSILON}
     * are clamped to {@code EPSILON}. Invalidates the point cache.
     *
     * @param width the new Z-axis extent (total, not half)
     */
    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the total height of the cuboid along the Y axis ({@code halfHeight * 2}).
     *
     * @return the full Y-axis extent
     */
    @Override
    public double getHeight() { return halfHeight * 2; }

    /**
     * Sets the total height of the cuboid along the Y axis. Values below {@link Shape#EPSILON}
     * are clamped to {@code EPSILON}. Invalidates the point cache.
     *
     * @param height the new Y-axis extent (total, not half)
     */
    @Override
    public void setHeight(double height) {
        this.halfHeight = Math.max(height / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the {@link Supplier} used to compute the first corner of this cuboid, or
     * {@code null} if this cuboid was not constructed with suppliers.
     *
     * @return the corner-A position supplier, or {@code null}
     */
    public Supplier<Vector3d> getCornerASupplier() { return cornerASupplier; }

    /**
     * Returns the {@link Supplier} used to compute the second corner of this cuboid, or
     * {@code null} if this cuboid was not constructed with suppliers.
     *
     * @return the corner-B position supplier, or {@code null}
     */
    public Supplier<Vector3d> getCornerBSupplier() { return cornerBSupplier; }

    /**
     * Creates a deep copy of this cuboid, preserving all dimensions, the corner suppliers
     * (if present), the {@code centerOffset}, and all inherited {@link AbstractShape} state
     * copied via {@link AbstractShape#copyTo(AbstractShape)}.
     *
     * @return a new {@link Cuboid} identical to this one
     */
    @Override
    public Shape clone() {
        // copy the current state rather than re-reading the suppliers, so the copy has identical geometry
        Cuboid cuboid = new Cuboid(getLength(), getWidth(), getHeight());
        cuboid.centerOffset = new Vector3d(this.centerOffset);
        cuboid.cornerASupplier = this.cornerASupplier;
        cuboid.cornerBSupplier = this.cornerBSupplier;
        cuboid.lastCornerA = this.lastCornerA;
        cuboid.lastCornerB = this.lastCornerB;
        return this.copyTo(cuboid);
    }
}
