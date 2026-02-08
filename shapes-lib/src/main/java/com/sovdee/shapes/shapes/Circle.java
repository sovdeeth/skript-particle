package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
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

    public static Set<Vector3d> calculateCircle(double radius, double density, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double stepSize = density / radius;
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector3d(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    public static Set<Vector3d> calculateDisc(double radius, double density, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        for (double subRadius = density; subRadius < radius; subRadius += density) {
            points.addAll(calculateCircle(subRadius, density, cutoffAngle));
        }
        points.addAll(calculateCircle(radius, density, cutoffAngle));
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double radius, double height, double density, double cutoffAngle) {
        Set<Vector3d> points = calculateDisc(radius, density, cutoffAngle);
        // Top disc via direct loop
        Set<Vector3d> top = new LinkedHashSet<>();
        for (Vector3d v : points) {
            top.add(new Vector3d(v.x, height, v.z));
        }
        points.addAll(top);
        // Wall
        Set<Vector3d> wall = calculateCircle(radius, density, cutoffAngle);
        fillVertically(wall, height, density);
        points.addAll(wall);
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        Set<Vector3d> circle = calculateCircle(radius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(circle, height, density);
            points.addAll(circle);
        } else {
            points.addAll(circle);
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        if (height != 0)
            points.addAll(calculateCylinder(radius, height, density, cutoffAngle));
        else
            points.addAll(calculateDisc(radius, density, cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points, double density) {
        Set<Vector3d> disc = calculateDisc(radius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(disc, height, density);
            points.addAll(disc);
        } else {
            points.addAll(disc);
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return switch (style) {
            case OUTLINE -> {
                if (height == 0) yield cutoffAngle * radius / count;
                double circumference = cutoffAngle * radius;
                double wallArea = circumference * height;
                yield Math.sqrt((circumference + wallArea) / count);
            }
            case SURFACE -> {
                double discArea = cutoffAngle * 0.5 * radius * radius;
                double wallArea = cutoffAngle * radius * height;
                yield Math.sqrt((discArea + wallArea) / count);
            }
            case FILL -> Math.cbrt(cutoffAngle * 0.5 * radius * radius * height / count);
        };
    }

    @Override
    public boolean contains(Vector3d point) {
        double distSq = point.x * point.x + point.z * point.z;
        if (distSq > radius * radius) return false;
        if (height > 0) return point.y >= 0 && point.y <= height;
        return Math.abs(point.y) < EPSILON;
    }

    @Override
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, Shape.EPSILON);
        invalidate();
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
        invalidate();
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
