package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A straight-line segment defined by two vector endpoints, lying along the direction from start
 * to end. The line implements {@link LWHShape} where only {@code length} (the distance between
 * the endpoints) is meaningful; {@code width} and {@code height} always return {@code 0}.
 * <p>
 * Both static and dynamic (supplier-based) endpoints are supported. Dynamic lines automatically
 * mark themselves as {@link AbstractShape#isDynamic() dynamic} so the point cache is invalidated
 * every frame.
 * </p>
 */
public class Line extends AbstractShape implements LWHShape {

    // The current endpoints. For a dynamic line these are the values last read from the suppliers.
    private final Vector3d start = new Vector3d();
    private final Vector3d end = new Vector3d();
    private Supplier<Vector3d> startSupplier;
    private Supplier<Vector3d> endSupplier;

    /**
     * Constructs a line from the origin {@code (0, 0, 0)} to the given endpoint.
     *
     * @param end the far endpoint of the line; must differ from the origin
     */
    public Line(Vector3d end) {
        this(new Vector3d(0, 0, 0), end);
    }

    /**
     * Constructs a static line between two explicit endpoints.
     * The supplied vectors are copied so subsequent external mutations do not affect this shape.
     *
     * @param start the starting endpoint
     * @param end   the ending endpoint; must differ from {@code start}
     * @throws IllegalArgumentException if {@code start} equals {@code end}
     */
    public Line(Vector3d start, Vector3d end) {
        super();
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");
        this.start.set(start);
        this.end.set(end);
    }

    /**
     * Constructs a dynamic line whose endpoints are resolved fresh each frame via suppliers.
     * This variant marks the shape as {@link AbstractShape#isDynamic() dynamic} so points are
     * regenerated every draw call (useful for entity-following lines).
     *
     * @param start a supplier that returns the current start position
     * @param end   a supplier that returns the current end position
     */
    public Line(Supplier<Vector3d> start, Supplier<Vector3d> end) {
        super();
        this.startSupplier = start;
        this.endSupplier = end;
        this.start.set(start.get());
        this.end.set(end.get());
        setDynamic(true);
    }

    /**
     * Copies the endpoints and suppliers of another line without re-reading the suppliers,
     * so the copy describes exactly the same geometry as the original.
     */
    private Line(Line line) {
        super();
        this.start.set(line.start);
        this.end.set(line.end);
        this.startSupplier = line.startSupplier;
        this.endSupplier = line.endSupplier;
    }

    /**
     * Calculates points along a line from start to end with the given density,
     * adding them directly to the provided list.
     */
    public static void calculateLine(List<Vector3d> points, Vector3d start, Vector3d end, double density) {
        calculateLine(points, start, end, density, true);
    }

    /**
     * Appends evenly spaced points along the segment from {@code start} to {@code end}, roughly
     * {@code density} apart. The start point is always included.
     *
     * @param points     the list to append the generated points to
     * @param start      the start of the segment
     * @param end        the end of the segment
     * @param density    the desired spacing between consecutive points
     * @param includeEnd whether to include the end point. Pass {@code false} when chaining segments,
     *                   so shared corners are not generated twice.
     */
    public static void calculateLine(List<Vector3d> points, Vector3d start, Vector3d end, double density, boolean includeEnd) {
        double length = start.distance(end);
        if (!(length > 0)) {
            // a degenerate segment is a single point
            if (includeEnd)
                points.add(new Vector3d(start));
            return;
        }
        int segments = segments(length, density);
        int last = includeEnd ? segments : segments - 1;
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + last + 1);
        for (int i = 0; i <= last; i++) {
            points.add(new Vector3d(start).lerp(end, (double) i / segments));
        }
    }

    /**
     * Connects a list of control points with lines, adding all intermediate points
     * directly to the provided list. Duplicate points at junctions are removed.
     */
    public static void connectPoints(List<Vector3d> result, List<Vector3d> controlPoints, double density) {
        int last = controlPoints.size() - 2;
        for (int i = 0; i <= last; i++) {
            // only the final segment contributes its end point, so each junction appears once
            calculateLine(result, controlPoints.get(i), controlPoints.get(i + 1), density, i == last);
        }
    }

    /**
     * Re-reads the endpoint suppliers, if any, and invalidates the line when an endpoint moved.
     */
    @Override
    public void refresh() {
        boolean changed = false;
        if (startSupplier != null) {
            Vector3d value = startSupplier.get();
            if (!start.equals(value)) {
                start.set(value);
                changed = true;
            }
        }
        if (endSupplier != null) {
            Vector3d value = endSupplier.get();
            if (!end.equals(value)) {
                end.set(value);
                changed = true;
            }
        }
        if (changed)
            invalidate();
    }

    /**
     * Generates outline points along the line from start to end at the given density.
     * Delegates to {@link #calculateLine(List, Vector3d, Vector3d, double)}.
     *
     * @param points  the list to which sampled points are appended
     * @param density the approximate spacing between consecutive points
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateLine(points, start, end, density);
    }

    /**
     * Returns the current start position by invoking the start supplier.
     *
     * @return the start endpoint vector (a fresh copy for dynamic lines)
     */
    public Vector3d getStart() {
        refresh();
        return new Vector3d(start);
    }

    /**
     * Sets a new static start endpoint. The vector is copied internally so subsequent
     * external mutations do not affect this shape. Invalidates the point cache.
     *
     * @param start the new start position
     */
    public void setStart(Vector3d start) {
        this.start.set(start);
        this.startSupplier = null;
        setDynamic(endSupplier != null);
        invalidate();
    }

    /**
     * Returns the current end position by invoking the end supplier.
     *
     * @return the end endpoint vector (a fresh copy for dynamic lines)
     */
    public Vector3d getEnd() {
        refresh();
        return new Vector3d(end);
    }

    /**
     * Sets a new static end endpoint. The vector is copied internally so subsequent
     * external mutations do not affect this shape. Invalidates the point cache.
     *
     * @param end the new end position
     */
    public void setEnd(Vector3d end) {
        this.end.set(end);
        this.endSupplier = null;
        setDynamic(startSupplier != null);
        invalidate();
    }

    /**
     * Returns the raw supplier used to resolve the start position each frame.
     *
     * @return the start {@link Supplier}
     */
    public Supplier<Vector3d> getStartSupplier() {
        return startSupplier;
    }

    /**
     * Replaces the start supplier without invalidating the point cache.
     * Intended for dynamic-line bookkeeping; prefer {@link #setStart(Vector3d)} for
     * static updates that should trigger re-sampling.
     *
     * @param startSupplier the new start supplier
     */
    public void setStartSupplier(Supplier<Vector3d> startSupplier) {
        this.startSupplier = startSupplier;
        setDynamic(startSupplier != null || endSupplier != null);
    }

    /**
     * Returns the raw supplier used to resolve the end position each frame.
     *
     * @return the end {@link Supplier}
     */
    public Supplier<Vector3d> getEndSupplier() {
        return endSupplier;
    }

    /**
     * Replaces the end supplier without invalidating the point cache.
     * Intended for dynamic-line bookkeeping; prefer {@link #setEnd(Vector3d)} for
     * static updates that should trigger re-sampling.
     *
     * @param endSupplier the new end supplier
     */
    public void setEndSupplier(Supplier<Vector3d> endSupplier) {
        this.endSupplier = endSupplier;
        setDynamic(startSupplier != null || endSupplier != null);
    }

    /**
     * Computes the inter-point density required to distribute {@code targetPointCount} points
     * evenly along the line. Density equals {@code length / targetPointCount}.
     *
     * @param style           ignored; a line has no surface or fill variant
     * @param targetPointCount the desired number of sampled points
     * @return the spacing between consecutive points
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        // n segments produce n + 1 points
        int segments = Math.max(targetPointCount - 1, 1);
        return getLength() / segments;
    }

    /**
     * Returns {@code true} if {@code point} lies on the line segment within {@link #EPSILON}
     * tolerance. Uses parametric projection: {@code t = (point-start)·(end-start) / |end-start|²},
     * then checks that the closest point on the segment equals {@code point}.
     *
     * @param point the point to test
     * @param tolerance how far outside the exact shape still counts as contained
     * @return {@code true} if the point is on the segment
     */
    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        return VectorUtil.distanceToSegmentSquared(point, start, end) <= tolerance * tolerance;
    }

    /**
     * Returns the Euclidean distance between the start and end endpoints.
     *
     * @return the length of the line segment
     */
    @Override
    public double getLength() {
        refresh();
        return start.distance(end);
    }

    /**
     * Rescales the line so its total length equals {@code length} by moving the end endpoint
     * along the current direction while keeping the start fixed. Invalidates the point cache.
     *
     * @param length the new length; clamped to at least {@link Shape#EPSILON}
     */
    @Override
    public void setLength(double length) {
        length = Math.max(length, Shape.EPSILON);
        Vector3d start = getStart();
        Vector3d direction = getEnd().sub(start);
        if (direction.lengthSquared() == 0)
            return;
        setEnd(start.add(direction.normalize().mul(length)));
    }

    /**
     * Always returns {@code 0}; a line has no width dimension.
     *
     * @return {@code 0}
     */
    @Override
    public double getWidth() { return 0; }

    /**
     * No-op; a line has no width dimension.
     *
     * @param width ignored
     */
    @Override
    public void setWidth(double width) { }

    /**
     * Always returns {@code 0}; a line has no height dimension.
     *
     * @return {@code 0}
     */
    @Override
    public double getHeight() { return 0; }

    /**
     * No-op; a line has no height dimension.
     *
     * @param height ignored
     */
    @Override
    public void setHeight(double height) { }

    /**
     * Returns a deep copy of this line, preserving dynamic vs. static endpoint mode and all
     * inherited {@link AbstractShape} state.
     *
     * @return a new {@link Line} with identical configuration
     */
    @Override
    public Shape clone() {
        return this.copyTo(new Line(this));
    }

    /**
     * Returns a human-readable description of this line including its start and end positions.
     *
     * @return a string of the form {@code "Line from <start> to <end>"}
     */
    public String toString() {
        return "Line from " + getStart() + " to " + getEnd();
    }
}
