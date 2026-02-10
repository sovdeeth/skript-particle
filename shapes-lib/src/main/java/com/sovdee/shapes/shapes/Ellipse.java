package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Ellipse extends AbstractShape implements LWHShape {

    private double xRadius;
    private double zRadius;
    private double height;
    protected double cutoffAngle;

    public Ellipse(double xRadius, double zRadius) {
        this(xRadius, zRadius, 0);
    }

    public Ellipse(double xRadius, double zRadius, double height) {
        super();
        this.xRadius = Math.max(xRadius, Shape.EPSILON);
        this.zRadius = Math.max(zRadius, Shape.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    // --- Static calculation methods ---

    private static double ellipseCircumference(double r1, double r2) {
        double a = Math.max(r1, r2);
        double b = Math.min(r1, r2);
        double h = Math.pow(a - b, 2) / Math.pow(a + b, 2);
        return Math.PI * (a + b) * (1 + 3 * h / (10 + Math.sqrt(4 - 3 * h)));
    }

    public static List<Vector3d> calculateEllipse(double r1, double r2, double density, double cutoffAngle) {
        List<Vector3d> points = new ArrayList<>();
        double circumference = ellipseCircumference(r1, r2);

        int steps = (int) Math.round(circumference / density);
        double theta = 0;
        double angleStep = 0;
        for (int i = 0; i < steps; i++) {
            if (theta > cutoffAngle) {
                break;
            }
            points.add(new Vector3d(r1 * Math.cos(theta), 0, r2 * Math.sin(theta)));
            double dx = r1 * Math.sin(theta + 0.5 * angleStep);
            double dy = r2 * Math.cos(theta + 0.5 * angleStep);
            angleStep = density / Math.sqrt(dx * dx + dy * dy);
            theta += angleStep;
        }
        return points;
    }

    public static Set<Vector3d> calculateEllipticalDisc(double r1, double r2, double density, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        int steps = (int) Math.round(Math.max(r1, r2) / density);
        double r;
        for (double i = 1; i <= steps; i += 1) {
            r = i / steps;
            points.addAll(calculateEllipse(r1 * r, r2 * r, density, cutoffAngle));
        }
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double r1, double r2, double height, double density, double cutoffAngle) {
        Set<Vector3d> points = calculateEllipticalDisc(r1, r2, density, cutoffAngle);
        // Top disc via direct loop
        Set<Vector3d> top = new LinkedHashSet<>();
        for (Vector3d v : points) {
            top.add(new Vector3d(v.x, height, v.z));
        }
        points.addAll(top);
        // Wall
        Set<Vector3d> wall = new LinkedHashSet<>(calculateEllipse(r1, r2, density, cutoffAngle));
        fillVertically(wall, height, density);
        points.addAll(wall);
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        Set<Vector3d> ellipse = new LinkedHashSet<>(calculateEllipse(xRadius, zRadius, density, cutoffAngle));
        if (height != 0) {
            fillVertically(ellipse, height, density);
            points.addAll(ellipse);
        } else {
            points.addAll(ellipse);
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        if (height != 0)
            points.addAll(calculateCylinder(xRadius, zRadius, height, density, cutoffAngle));
        else
            points.addAll(calculateEllipticalDisc(xRadius, zRadius, density, cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points, double density) {
        Set<Vector3d> disc = calculateEllipticalDisc(xRadius, zRadius, density, cutoffAngle);
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
                double h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumference = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                yield circumference / count;
            }
            case SURFACE, FILL -> Math.sqrt((Math.PI * xRadius * zRadius) / count);
        };
    }

    @Override
    public boolean contains(Vector3d point) {
        double nx = point.x / xRadius;
        double nz = point.z / zRadius;
        if (nx * nx + nz * nz > 1) return false;
        if (height > 0) return point.y >= 0 && point.y <= height;
        return Math.abs(point.y) < EPSILON;
    }

    @Override
    public double getLength() { return xRadius * 2; }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getWidth() { return zRadius * 2; }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
