package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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
     * Calculates points along a line from start to end with the given density.
     */
    public static Set<Vector3d> calculateLine(Vector3d start, Vector3d end, double density) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d direction = new Vector3d(end).sub(start);
        double length = direction.length();
        double step = length / Math.round(length / density);
        direction.normalize().mul(step);

        Vector3d current = new Vector3d(start);
        int count = (int) (length / step);
        for (int i = 0; i <= count; i++) {
            points.add(new Vector3d(current));
            current.add(direction);
        }
        return points;
    }

    /**
     * Connects a list of points with lines, returning all intermediate points.
     */
    public static Set<Vector3d> connectPoints(List<Vector3d> points, double density) {
        Set<Vector3d> connectedPoints = new LinkedHashSet<>();
        for (int i = 0; i < points.size() - 1; i++) {
            connectedPoints.addAll(calculateLine(points.get(i), points.get(i + 1), density));
        }
        return connectedPoints;
    }

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        points.addAll(calculateLine(getStart(), getEnd(), density));
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
