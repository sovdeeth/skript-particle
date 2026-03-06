package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
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
        final Vector3d s = new Vector3d(start);
        final Vector3d e = new Vector3d(end);
        this.startSupplier = () -> new Vector3d(s);
        this.endSupplier = () -> new Vector3d(e);
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
        setDynamic(true);
    }

    /**
     * Calculates points along a line from start to end with the given density,
     * adding them directly to the provided list.
     */
    public static void calculateLine(List<Vector3d> points, Vector3d start, Vector3d end, double density) {
        Vector3d direction = new Vector3d(end).sub(start);
        double length = direction.length();
        double step = length / Math.round(length / density);
        direction.normalize().mul(step);

        Vector3d current = new Vector3d(start);
        int count = (int) (length / step);
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + count + 1);
        for (int i = 0; i <= count; i++) {
            points.add(new Vector3d(current));
            current.add(direction);
        }
    }

    /**
     * Connects a list of control points with lines, adding all intermediate points
     * directly to the provided list. Duplicate points at junctions are removed.
     */
    public static void connectPoints(List<Vector3d> result, List<Vector3d> controlPoints, double density) {
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            int before = result.size();
            calculateLine(result, controlPoints.get(i), controlPoints.get(i + 1), density);
            // Remove duplicate junction point (first point of non-first segments)
            if (i > 0 && result.size() > before) {
                result.remove(before);
            }
        }
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
        calculateLine(points, getStart(), getEnd(), density);
    }

    /**
     * Returns the current start position by invoking the start supplier.
     *
     * @return the start endpoint vector (a fresh copy for dynamic lines)
     */
    public Vector3d getStart() {
        return startSupplier.get();
    }

    /**
     * Sets a new static start endpoint. The vector is copied internally so subsequent
     * external mutations do not affect this shape. Invalidates the point cache.
     *
     * @param start the new start position
     */
    public void setStart(Vector3d start) {
        final Vector3d s = new Vector3d(start);
        this.startSupplier = () -> new Vector3d(s);
        invalidate();
    }

    /**
     * Returns the current end position by invoking the end supplier.
     *
     * @return the end endpoint vector (a fresh copy for dynamic lines)
     */
    public Vector3d getEnd() {
        return endSupplier.get();
    }

    /**
     * Sets a new static end endpoint. The vector is copied internally so subsequent
     * external mutations do not affect this shape. Invalidates the point cache.
     *
     * @param end the new end position
     */
    public void setEnd(Vector3d end) {
        final Vector3d e = new Vector3d(end);
        this.endSupplier = () -> new Vector3d(e);
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
        int count = Math.max(targetPointCount, 1);
        return new Vector3d(getEnd()).sub(getStart()).length() / count;
    }

    /**
     * Returns {@code true} if {@code point} lies on the line segment within {@link #EPSILON}
     * tolerance. Uses parametric projection: {@code t = (point-start)·(end-start) / |end-start|²},
     * then checks that the closest point on the segment equals {@code point}.
     *
     * @param point the point to test
     * @return {@code true} if the point is on the segment
     */
    @Override
    public boolean contains(Vector3d point) {
        Vector3d start = getStart();
        Vector3d end = getEnd();
        Vector3d line = new Vector3d(end).sub(start);
        double len = line.length();
        if (len < EPSILON) return point.distance(start) < EPSILON;
        Vector3d toPoint = new Vector3d(point).sub(start);
        double t = toPoint.dot(line) / (len * len);
        if (t < 0 || t > 1) return false;
        Vector3d closest = new Vector3d(start).add(new Vector3d(line).mul(t));
        return point.distance(closest) <= EPSILON;
    }

    /**
     * Returns the Euclidean distance between the start and end endpoints.
     *
     * @return the length of the line segment
     */
    @Override
    public double getLength() {
        return new Vector3d(getStart()).sub(getEnd()).length();
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
        Vector3d end = getEnd();
        Vector3d direction = new Vector3d(end).sub(start).normalize();
        Vector3d newEnd = new Vector3d(start).add(direction.mul(length));
        setEnd(newEnd);
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
        Line clone;
        if (isDynamic()) {
            clone = new Line(this.startSupplier, this.endSupplier);
        } else {
            clone = new Line(getStart(), getEnd());
        }
        return this.copyTo(clone);
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
