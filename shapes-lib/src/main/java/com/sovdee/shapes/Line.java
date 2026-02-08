package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

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

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(MathUtil.calculateLine(getStart(), getEnd(), this.getParticleDensity()));
    }

    public Vector3d getStart() {
        return startSupplier.get();
    }

    public void setStart(Vector3d start) {
        final Vector3d s = new Vector3d(start);
        this.startSupplier = () -> new Vector3d(s);
        this.setNeedsUpdate(true);
    }

    public Vector3d getEnd() {
        return endSupplier.get();
    }

    public void setEnd(Vector3d end) {
        final Vector3d e = new Vector3d(end);
        this.endSupplier = () -> new Vector3d(e);
        this.setNeedsUpdate(true);
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
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        Vector3d start = getStart();
        Vector3d end = getEnd();
        this.setParticleDensity(new Vector3d(end).sub(start).length() / particleCount);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return new Vector3d(getStart()).sub(getEnd()).length();
    }

    @Override
    public void setLength(double length) {
        length = Math.max(length, MathUtil.EPSILON);
        Vector3d start = getStart();
        Vector3d end = getEnd();
        Vector3d direction = new Vector3d(end).sub(start).normalize();
        Vector3d newEnd = new Vector3d(start).add(direction.mul(length));
        setEnd(newEnd);
        this.setNeedsUpdate(true);
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
