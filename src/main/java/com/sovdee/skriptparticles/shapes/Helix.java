package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Helix extends AbstractShape implements RadialShape, LWHShape {

    protected double radius;
    protected double height;
    protected double slope;
    protected int direction = 1;

    public Helix(double radius, double height, double slope) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
    }

    public Helix(double radius, double height, double slope, int direction) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
        this.direction = direction;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateHelix(radius, height, slope, direction, particleDensity);
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        for (double r = radius; r > 0; r -= particleDensity){
            points.addAll(MathUtil.calculateHelix(r, height, slope, direction, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = switch (style) {
            case OUTLINE -> (Math.sqrt(slope * slope + radius * radius) * (height / slope) / particleCount);
            case FILL, SURFACE -> Math.sqrt(slope * slope + radius * radius * (height / slope) / particleCount);
        };
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = Math.max(slope, MathUtil.EPSILON);
        needsUpdate = true;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
        needsUpdate = true;
    }

    @Override
    public double getLength() {
        return height;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {
        height = Math.max(length, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {}

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Helix(radius, height, slope, direction));
    }
}
