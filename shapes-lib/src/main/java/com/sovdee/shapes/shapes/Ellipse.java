package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * An ellipse (or extruded elliptical cylinder) defined by two radii along the X and Z axes
 * and an optional height along the Y axis.
 * <br>
 * When {@code height} is zero the shape is a flat 2-D ellipse lying in the XZ plane.
 * When {@code height} is positive the shape is extruded into a full cylinder-like solid
 * whose cross-sections are ellipses.
 * <br>
 * Point sampling is delegated to the public static helpers {@link #calculateEllipse},
 * {@link #calculateEllipticalDisc}, and {@link #calculateCylinder}, which are also reused by
 * {@link Ellipsoid} and {@link EllipticalArc}. An arc cutoff is stored in {@link #cutoffAngle}
 * (inherited by {@link EllipticalArc}) and defaults to {@code 2π} for a full ellipse.
 */
public class Ellipse extends AbstractShape implements LWHShape {

    private static final double FULL_CIRCLE = 2 * Math.PI;

    private double xRadius;
    private double zRadius;
    private double height;
    protected double cutoffAngle;

    /**
     * Constructs a flat 2-D ellipse with the given X- and Z-axis radii and zero height.
     * Equivalent to {@code new Ellipse(xRadius, zRadius, 0)}.
     *
     * @param xRadius the semi-axis length along the X axis; clamped to at least {@link Shape#EPSILON}
     * @param zRadius the semi-axis length along the Z axis; clamped to at least {@link Shape#EPSILON}
     */
    public Ellipse(double xRadius, double zRadius) {
        this(xRadius, zRadius, 0);
    }

    /**
     * Constructs a (possibly extruded) ellipse. When {@code height} is zero the result is a flat
     * ellipse in the XZ plane. When {@code height} is positive the outline consists of a vertical
     * ellipse ring extruded upward, the surface is a closed cylinder, and the fill is a solid
     * elliptical prism.
     *
     * @param xRadius the semi-axis length along the X axis; clamped to at least {@link Shape#EPSILON}
     * @param zRadius the semi-axis length along the Z axis; clamped to at least {@link Shape#EPSILON}
     * @param height  the extrusion distance along the Y axis; clamped to at least {@code 0}
     */
    public Ellipse(double xRadius, double zRadius, double height) {
        super();
        this.xRadius = Math.max(xRadius, Shape.EPSILON);
        this.zRadius = Math.max(zRadius, Shape.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    // --- Static calculation methods ---

    static double ellipseCircumference(double r1, double r2) {
        double a = Math.max(r1, r2);
        double b = Math.min(r1, r2);
        if (!(a + b > 0))
            return 0;
        double h = Math.pow(a - b, 2) / Math.pow(a + b, 2);
        return Math.PI * (a + b) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
    }

    /**
     * Appends points along the perimeter of an ellipse with semi-axes {@code r1} (X) and
     * {@code r2} (Z), using an arc-length-adaptive angle step so that consecutive points are
     * spaced approximately {@code density} units apart.
     * <br>
     * Sampling stops when the accumulated angle reaches {@code cutoffAngle}. Pass {@code 2π}
     * for a complete ellipse.
     *
     * @param points       the list to which points are appended
     * @param r1           semi-axis length along the X axis
     * @param r2           semi-axis length along the Z axis
     * @param density      desired arc-length spacing between consecutive points
     * @param cutoffAngle  maximum sweep angle in radians ({@code 0} to {@code 2π})
     */
    public static void calculateEllipse(List<Vector3d> points, double r1, double r2, double density, double cutoffAngle) {
        double circumference = ellipseCircumference(r1, r2);
        boolean full = cutoffAngle >= FULL_CIRCLE - Shape.EPSILON;

        long steps = Math.round(circumference / density);
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + (int) Math.min(steps * Math.min(cutoffAngle, FULL_CIRCLE) / FULL_CIRCLE + 1, Integer.MAX_VALUE - 8));
        double theta = 0;
        double angleStep = 0;
        for (long i = 0; i < steps; i++) {
            // a full ellipse stops before wrapping onto its first point; an arc stops once past its end
            if (full ? theta >= FULL_CIRCLE - angleStep / 2 : theta > cutoffAngle) {
                break;
            }
            points.add(new Vector3d(r1 * Math.cos(theta), 0, r2 * Math.sin(theta)));
            double dx = r1 * Math.sin(theta + 0.5 * angleStep);
            double dy = r2 * Math.cos(theta + 0.5 * angleStep);
            angleStep = density / Math.sqrt(dx * dx + dy * dy);
            theta += angleStep;
        }
    }

    /**
     * Appends points filling the area of an elliptical disc (filled 2-D ellipse) by sampling
     * concentric ellipses at uniform radial increments from the outermost radius down to zero.
     * Each ring is generated by {@link #calculateEllipse} with proportionally scaled radii.
     *
     * @param points       the list to which points are appended
     * @param r1           outer semi-axis length along the X axis
     * @param r2           outer semi-axis length along the Z axis
     * @param density      desired arc-length spacing used for both the ring step and {@link #calculateEllipse}
     * @param cutoffAngle  angular cutoff applied to each ring ({@code 2π} for a full disc)
     */
    public static void calculateEllipticalDisc(List<Vector3d> points, double r1, double r2, double density, double cutoffAngle) {
        points.add(new Vector3d(0, 0, 0));
        int rings = segments(Math.max(r1, r2), density);
        for (int i = 1; i <= rings; i++) {
            double r = (double) i / rings;
            calculateEllipse(points, r1 * r, r2 * r, density, cutoffAngle);
        }
    }

    /**
     * Appends points covering a solid elliptical cylinder: bottom disc, top disc (offset by
     * {@code height} along Y), and the lateral wall extruded vertically between them.
     * <br>
     * The bottom and top discs are generated via {@link #calculateEllipticalDisc}; the wall is
     * generated via {@link #calculateEllipse} and then extended vertically using the inherited
     * {@code fillVertically} helper.
     *
     * @param points       the list to which points are appended
     * @param r1           semi-axis along the X axis
     * @param r2           semi-axis along the Z axis
     * @param height       extrusion distance along the Y axis
     * @param density      desired spacing between adjacent points
     * @param cutoffAngle  angular cutoff applied to discs and the elliptical wall ({@code 2π} for full)
     */
    public static void calculateCylinder(List<Vector3d> points, double r1, double r2, double height, double density, double cutoffAngle) {
        // Bottom disc
        int discStart = points.size();
        calculateEllipticalDisc(points, r1, r2, density, cutoffAngle);
        int discEnd = points.size();
        // Wall: copies of the outer ring between the two discs. The discs already contain the rings at the
        // bottom and top, so the base ring is only generated to be copied, and is then removed.
        calculateEllipse(points, r1, r2, density, cutoffAngle);
        int ringEnd = points.size();
        fillVertically(points, discEnd, height, density, false);
        points.subList(discEnd, ringEnd).clear();
        // Top disc - copy bottom disc at height
        for (int i = discStart; i < discEnd; i++) {
            Vector3d v = points.get(i);
            points.add(new Vector3d(v.x, height, v.z));
        }
    }

    // --- Generation methods ---

    /**
     * Generates the outline of this ellipse. For a flat ellipse ({@code height == 0}) this is just
     * the perimeter arc. When {@code height > 0} the outline ring is extruded vertically to form a
     * rectangular strip connecting the bottom arc to the top arc.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        int start = points.size();
        calculateEllipse(points, xRadius, zRadius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
        }
    }

    /**
     * Generates the surface of this ellipse. When {@code height == 0} the surface is the filled
     * elliptical disc in the XZ plane. When {@code height > 0} the surface consists of the top
     * and bottom discs plus the lateral wall, computed by {@link #calculateCylinder}.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        if (height != 0)
            calculateCylinder(points, xRadius, zRadius, height, density, cutoffAngle);
        else
            calculateEllipticalDisc(points, xRadius, zRadius, density, cutoffAngle);
    }

    /**
     * Generates a solid fill of this ellipse. For a flat ellipse ({@code height == 0}) this is
     * identical to the surface (the filled disc). When {@code height > 0} the bottom disc is
     * generated then extended vertically by {@code fillVertically} to produce a solid elliptical prism.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points
     */
    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        int start = points.size();
        calculateEllipticalDisc(points, xRadius, zRadius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
        }
    }

    /**
     * Estimates the point spacing required to produce approximately {@code targetPointCount} points
     * for the given {@link SamplingStyle}.
     * <ul>
     *   <li>OUTLINE: uses Ramanujan's first approximation for the ellipse circumference divided
     *       by {@code count}</li>
     *   <li>SURFACE / FILL: {@code density = sqrt(π·xRadius·zRadius / count)}, the spacing
     *       that gives the target count over the disc area</li>
     * </ul>
     *
     * @param style            the sampling style (OUTLINE, SURFACE, or FILL)
     * @param targetPointCount the desired number of output points
     * @return the estimated point spacing to achieve the target count
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        double fraction = Math.min(cutoffAngle, FULL_CIRCLE) / FULL_CIRCLE;
        double arcLength = ellipseCircumference(xRadius, zRadius) * fraction;
        double discArea = Math.PI * xRadius * zRadius * fraction;
        return switch (style) {
            case OUTLINE -> {
                if (height == 0) yield arcLength / count;
                // with a height, the outline is the wall
                yield Math.sqrt(arcLength * height / count);
            }
            case SURFACE -> {
                if (height == 0) yield Math.sqrt(discArea / count);
                yield Math.sqrt((2 * discArea + arcLength * height) / count);
            }
            case FILL -> {
                // a flat ellipse has no volume: it is filled as a disc
                if (height == 0) yield Math.sqrt(discArea / count);
                yield Math.cbrt(discArea * height / count);
            }
        };
    }

    /**
     * Returns {@code true} if the given point lies inside or on the boundary of this ellipse.
     * The test first checks the normalised ellipse equation {@code (x/xRadius)² + (z/zRadius)² ≤ 1}.
     * If the shape has height, the Y coordinate must additionally satisfy {@code 0 ≤ y ≤ height};
     * otherwise the point must lie in the XZ plane ({@code |y| < EPSILON}).
     *
     * @param point the point to test in local shape space
     * @param tolerance how far outside the exact shape still counts as contained
     * @return {@code true} if the point is contained within the ellipse (or cylinder)
     */
    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        if (height > 0) {
            if (point.y < -tolerance || point.y > height + tolerance) return false;
        } else if (Math.abs(point.y) > tolerance) {
            return false;
        }
        double nx = point.x / (xRadius + tolerance);
        double nz = point.z / (zRadius + tolerance);
        return nx * nx + nz * nz <= 1;
    }

    /**
     * Returns the full diameter of the ellipse along the X axis ({@code xRadius * 2}).
     *
     * @return the total X-axis extent
     */
    @Override
    public double getLength() { return xRadius * 2; }

    /**
     * Sets the X-axis semi-axis from half of {@code length}. Values below {@link Shape#EPSILON}
     * are clamped. Invalidates the point cache.
     *
     * @param length the new total X-axis extent (diameter, not radius)
     */
    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the full diameter of the ellipse along the Z axis ({@code zRadius * 2}).
     *
     * @return the total Z-axis extent
     */
    @Override
    public double getWidth() { return zRadius * 2; }

    /**
     * Sets the Z-axis semi-axis from half of {@code width}. Values below {@link Shape#EPSILON}
     * are clamped. Invalidates the point cache.
     *
     * @param width the new total Z-axis extent (diameter, not radius)
     */
    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the extrusion height along the Y axis, or {@code 0} for a flat ellipse.
     *
     * @return the Y-axis extrusion distance
     */
    @Override
    public double getHeight() { return height; }

    /**
     * Sets the extrusion height along the Y axis. A value of {@code 0} produces a flat 2-D ellipse.
     * Negative values are clamped to {@code 0}. Invalidates the point cache.
     *
     * @param height the new Y-axis extrusion distance
     */
    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        invalidate();
    }

    /**
     * Creates a deep copy of this ellipse, preserving radii, height, the inherited cutoff angle,
     * and all {@link AbstractShape} state copied via {@link AbstractShape#copyTo(AbstractShape)}.
     *
     * @return a new {@link Ellipse} identical to this one
     */
    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
