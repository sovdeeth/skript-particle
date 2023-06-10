package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;

import java.util.Set;

public class Line extends AbstractShape implements LWHShape {

    private Vector start;
    private Vector end;

    private DynamicLocation startLocation;
    private DynamicLocation endLocation;
    private boolean isDynamic = false;


    public Line() {
        this(new Vector(0, 0, 0), new Vector(0, 0, 0));
    }

    public Line(Vector end) {
        this(new Vector(0, 0, 0), end);
    }

    public Line(Vector start, Vector end) {
        super();
        this.start = start;
        this.end = end;
    }

    public Line(DynamicLocation start, DynamicLocation end) {
        super();
        if (start.isDynamic() || end.isDynamic()) {
            this.startLocation = start.clone();
            this.endLocation = end.clone();
            this.isDynamic = true;
        } else {
            this.start = new Vector(0, 0, 0);
            this.end = end.getLocation().toVector().subtract(start.getLocation().toVector());
        }
        this.location = start.clone();
    }

    // Ensure that the points are always needing to be updated if the start or end location is dynamic
    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            this.needsUpdate = true;
        return points;
    }

    @Override
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            this.start = new Vector(0, 0, 0);
            this.end = endLocation.getLocation().toVector().subtract(startLocation.getLocation().toVector());
        }
        return super.generatePoints();
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateLine(start, end, particleDensity);
    }

    public Vector getStart() {
        return start.clone();
    }

    public void setStart(Vector start) {
        this.start = start.clone();
    }

    public Vector getEnd() {
        return end.clone();
    }

    public void setEnd(Vector end) {
        this.end = end.clone();
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = (end.clone().subtract(start).length() / particleCount);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        Line line = (isDynamic ? new Line(this.startLocation, this.endLocation) : new Line(this.start, this.end));
        line.isDynamic = this.isDynamic;
        return this.copyTo(line);
    }

    public String toString() {
        return "Line from " + this.start + " to " + this.end;
    }

    @Override
    public double getLength() {
        return start.clone().subtract(end).length();
    }

    @Override
    public void setLength(double length) {
        Vector direction = end.clone().subtract(start).normalize();
        end = start.clone().add(direction.multiply(length));
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void setHeight(double height) {
    }
}
