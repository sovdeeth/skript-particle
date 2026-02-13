package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

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

    public static void calculateCircle(List<Vector3d> points, double radius, double density, double cutoffAngle) {
        double stepSize = density / radius;
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + (int) (cutoffAngle / stepSize) + 1);
        for (double theta = 0; theta < cutoffAngle; theta += stepSize) {
            points.add(new Vector3d(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
    }

    public static void calculateDisc(List<Vector3d> points, double radius, double density, double cutoffAngle) {
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + (int) (cutoffAngle * radius * radius / (density * density * Math.PI)));
        for (double subRadius = density; subRadius < radius; subRadius += density) {
            calculateCircle(points, subRadius, density, cutoffAngle);
        }
        calculateCircle(points, radius, density, cutoffAngle);
    }

    public static void calculateCylinder(List<Vector3d> points, double radius, double height, double density, double cutoffAngle) {
        // Bottom disc
        int discStart = points.size();
        calculateDisc(points, radius, density, cutoffAngle);
        int discEnd = points.size();
        // Top disc - copy bottom disc at height
        for (int i = discStart; i < discEnd; i++) {
            Vector3d v = points.get(i);
            points.add(new Vector3d(v.x, height, v.z));
        }
        // Wall
        int wallStart = points.size();
        calculateCircle(points, radius, density, cutoffAngle);
        fillVertically(points, wallStart, height, density);
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        int start = points.size();
        calculateCircle(points, radius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
        }
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        if (height != 0)
            calculateCylinder(points, radius, height, density, cutoffAngle);
        else
            calculateDisc(points, radius, density, cutoffAngle);
    }

    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        int start = points.size();
        calculateDisc(points, radius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
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
