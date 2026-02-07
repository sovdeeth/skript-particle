package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

/**
 * A line shape defined by two vector endpoints.
 * For dynamic (entity-following) lines, use the plugin-side DynamicLine wrapper.
 */
public class Line extends AbstractShape implements LWHShape {

    private Vector3d start;
    private Vector3d end;

    public Line(Vector3d end) {
        this(new Vector3d(0, 0, 0), end);
    }

    public Line(Vector3d start, Vector3d end) {
        super();
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");
        this.start = new Vector3d(start);
        this.end = new Vector3d(end);
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(MathUtil.calculateLine(start, end, this.getParticleDensity()));
    }

    public Vector3d getStart() {
        return new Vector3d(start);
    }

    public void setStart(Vector3d start) {
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end points must not be identical");
        this.start = new Vector3d(start);
        this.setNeedsUpdate(true);
    }

    public Vector3d getEnd() {
        return new Vector3d(end);
    }

    public void setEnd(Vector3d end) {
        if (end.equals(start))
            throw new IllegalArgumentException("Start and end points must not be identical");
        this.end = new Vector3d(end);
        this.setNeedsUpdate(true);
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(new Vector3d(end).sub(start).length() / particleCount);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return new Vector3d(start).sub(end).length();
    }

    @Override
    public void setLength(double length) {
        length = Math.max(length, MathUtil.EPSILON);
        Vector3d direction = new Vector3d(end).sub(start).normalize();
        end = new Vector3d(start).add(direction.mul(length));
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
        return this.copyTo(new Line(new Vector3d(this.start), new Vector3d(this.end)));
    }

    public String toString() {
        return "Line from " + this.start + " to " + this.end;
    }
}
