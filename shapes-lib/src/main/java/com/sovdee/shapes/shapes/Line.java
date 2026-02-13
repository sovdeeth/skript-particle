package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A line shape defined by two vector endpoints.
 * Supports both static endpoints and dynamic suppliers for entity-following.
 */
public class Line extends AbstractShape implements LWHShape {

    private Supplier<Vector3d> startSupplier;
    private Supplier<Vector3d> endSupplier;

    public Line(Vector3d end) {
        this(new Vector3d(0, 0, 0), end);
    }

    public Line(Vector3d start, Vector3d end) {
        super();
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");
        final Vector3d s = new Vector3d(start);
        final Vector3d e = new Vector3d(end);
        this.startSupplier = () -> new Vector3d(s);
        this.endSupplier = () -> new Vector3d(e);
    }

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

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateLine(points, getStart(), getEnd(), density);
    }

    public Vector3d getStart() {
        return startSupplier.get();
    }

    public void setStart(Vector3d start) {
        final Vector3d s = new Vector3d(start);
        this.startSupplier = () -> new Vector3d(s);
        invalidate();
    }

    public Vector3d getEnd() {
        return endSupplier.get();
    }

    public void setEnd(Vector3d end) {
        final Vector3d e = new Vector3d(end);
        this.endSupplier = () -> new Vector3d(e);
        invalidate();
    }

    public Supplier<Vector3d> getStartSupplier() {
        return startSupplier;
    }

    public void setStartSupplier(Supplier<Vector3d> startSupplier) {
        this.startSupplier = startSupplier;
    }

    public Supplier<Vector3d> getEndSupplier() {
        return endSupplier;
    }

    public void setEndSupplier(Supplier<Vector3d> endSupplier) {
        this.endSupplier = endSupplier;
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return new Vector3d(getEnd()).sub(getStart()).length() / count;
    }

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

    @Override
    public double getLength() {
        return new Vector3d(getStart()).sub(getEnd()).length();
    }

    @Override
    public void setLength(double length) {
        length = Math.max(length, Shape.EPSILON);
        Vector3d start = getStart();
        Vector3d end = getEnd();
        Vector3d direction = new Vector3d(end).sub(start).normalize();
        Vector3d newEnd = new Vector3d(start).add(direction.mul(length));
        setEnd(newEnd);
    }

    @Override
    public double getWidth() { return 0; }

    @Override
    public void setWidth(double width) { }

    @Override
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

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

    public String toString() {
        return "Line from " + getStart() + " to " + getEnd();
    }
}
