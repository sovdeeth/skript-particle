package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Helix extends AbstractShape implements RadialShape, LWHShape {

    private double radius;
    private double height;
    private double slope;
    private int direction = 1;

    public Helix(double radius, double height, double slope) {
        super();
        this.radius = Math.max(radius, Shape.EPSILON);
        this.height = Math.max(height, Shape.EPSILON);
        this.slope = Math.max(slope, Shape.EPSILON);
    }

    public Helix(double radius, double height, double slope, int direction) {
        super();
        this.radius = Math.max(radius, Shape.EPSILON);
        this.height = Math.max(height, Shape.EPSILON);
        this.slope = Math.max(slope, Shape.EPSILON);
        if (direction != 1 && direction != -1)
            throw new IllegalArgumentException("Direction must be 1 or -1");
        this.direction = direction;
    }

    private static Set<Vector3d> calculateHelix(double radius, double height, double slope, int direction, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        if (radius <= 0 || height <= 0) {
            return points;
        }
        double loops = Math.abs(height / slope);
        double length = slope * slope + radius * radius;
        double stepSize = particleDensity / length;
        for (double t = 0; t < loops; t += stepSize) {
            double x = radius * Math.cos(direction * t);
            double z = radius * Math.sin(direction * t);
            points.add(new Vector3d(x, t * slope, z));
        }
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(calculateHelix(radius, height, slope, direction, this.getParticleDensity()));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        for (double r = radius; r > 0; r -= particleDensity) {
            points.addAll(calculateHelix(r, height, slope, direction, particleDensity));
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
        this.slope = Math.max(slope, Shape.EPSILON);
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
        height = Math.max(length, Shape.EPSILON);
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
        this.height = Math.max(height, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Helix(radius, height, slope, direction));
    }
}
