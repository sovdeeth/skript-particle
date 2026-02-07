package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

public class Helix extends AbstractShape implements RadialShape, LWHShape {

    private double radius;
    private double height;
    private double slope;
    private int direction = 1;

    public Helix(double radius, double height, double slope) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.height = Math.max(height, MathUtil.EPSILON);
        this.slope = Math.max(slope, MathUtil.EPSILON);
    }

    public Helix(double radius, double height, double slope, int direction) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.height = Math.max(height, MathUtil.EPSILON);
        this.slope = Math.max(slope, MathUtil.EPSILON);
        if (direction != 1 && direction != -1)
            throw new IllegalArgumentException("Direction must be 1 or -1");
        this.direction = direction;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(MathUtil.calculateHelix(radius, height, slope, direction, this.getParticleDensity()));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        for (double r = radius; r > 0; r -= particleDensity) {
            points.addAll(MathUtil.calculateHelix(r, height, slope, direction, particleDensity));
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(switch (this.getStyle()) {
            case OUTLINE -> (Math.sqrt(slope * slope + radius * radius) * (height / slope) / particleCount);
            case FILL, SURFACE -> Math.sqrt(slope * slope + radius * radius * (height / slope) / particleCount);
        });
    }

    public double getSlope() { return slope; }

    public void setSlope(double slope) {
        this.slope = Math.max(slope, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    public int getDirection() { return direction; }

    public void setDirection(int direction) {
        if (direction != 1 && direction != -1)
            throw new IllegalArgumentException("Direction must be 1 or -1");
        this.direction = direction;
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() { return height; }

    @Override
    public void setLength(double length) {
        height = Math.max(length, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() { return 0; }

    @Override
    public void setWidth(double width) { }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Helix(radius, height, slope, direction));
    }
}
