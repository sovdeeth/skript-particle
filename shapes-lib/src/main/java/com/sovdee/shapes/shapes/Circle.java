package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Circle extends AbstractShape implements RadialShape, LWHShape {

    private double radius;
    protected double cutoffAngle;
    private double height;

    public Circle(double radius) {
        this(radius, 0);
    }

    public Circle(double radius, double height) {
        super();
        this.radius = Math.max(radius, Shape.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    // --- Static calculation methods ---

    public static Set<Vector3d> calculateCircle(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double stepSize = particleDensity / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector3d(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static Set<Vector3d> calculateDisc(double radius, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        for (double subRadius = particleDensity; subRadius < radius; subRadius += particleDensity) {
            points.addAll(calculateCircle(subRadius, particleDensity, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, particleDensity, cutoffAngle));
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double radius, double height, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = calculateDisc(radius, particleDensity, cutoffAngle);
        // Top disc via direct loop
        Set<Vector3d> top = new LinkedHashSet<>();
        for (Vector3d v : points) {
            top.add(new Vector3d(v.x, height, v.z));
        }
        points.addAll(top);
        // Wall
        Set<Vector3d> wall = calculateCircle(radius, particleDensity, cutoffAngle);
        fillVertically(wall, height, particleDensity);
        points.addAll(wall);
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points) {
        Set<Vector3d> circle = calculateCircle(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0) {
            fillVertically(circle, height, this.getParticleDensity());
            points.addAll(circle);
        } else {
            points.addAll(circle);
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        if (height != 0)
            points.addAll(calculateCylinder(radius, height, this.getParticleDensity(), cutoffAngle));
        else
            points.addAll(calculateDisc(radius, this.getParticleDensity(), cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        Set<Vector3d> disc = calculateDisc(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0) {
            fillVertically(disc, height, this.getParticleDensity());
            points.addAll(disc);
        } else {
            points.addAll(disc);
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        if (this.getStyle() == Style.OUTLINE && height == 0) {
            this.setParticleDensity(cutoffAngle * radius / particleCount);
        } else if (this.getStyle() == Style.SURFACE || height == 0) {
            double discArea = cutoffAngle * 0.5 * radius * radius;
            double wallArea = cutoffAngle * radius * height;
            this.setParticleDensity(Math.sqrt((discArea + wallArea) / particleCount));
        } else {
            this.setParticleDensity(Math.cbrt(cutoffAngle * 0.5 * radius * radius * height / particleCount));
        }
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
    public double getLength() { return 0; }

    @Override
    public void setLength(double length) { }

    @Override
    public double getWidth() { return 0; }

    @Override
    public void setWidth(double width) { }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Circle(radius, height));
    }

    @Override
    public String toString() {
        return "Circle{radius=" + radius + ", cutoffAngle=" + cutoffAngle + ", height=" + height + '}';
    }
}
