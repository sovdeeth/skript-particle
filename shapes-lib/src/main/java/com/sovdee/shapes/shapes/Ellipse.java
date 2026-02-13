package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

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

    public static void calculateEllipse(List<Vector3d> points, double r1, double r2, double density, double cutoffAngle) {
        double circumference = ellipseCircumference(r1, r2);

        int steps = (int) Math.round(circumference / density);
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + (int) (steps * cutoffAngle / (2 * Math.PI)) + 1);
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
    }

    public static void calculateEllipticalDisc(List<Vector3d> points, double r1, double r2, double density, double cutoffAngle) {
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + (int) (cutoffAngle * r1 * r2 / (density * density)));
        int steps = (int) Math.round(Math.max(r1, r2) / density);
        double r;
        for (double i = 1; i <= steps; i += 1) {
            r = i / steps;
            calculateEllipse(points, r1 * r, r2 * r, density, cutoffAngle);
        }
    }

    public static void calculateCylinder(List<Vector3d> points, double r1, double r2, double height, double density, double cutoffAngle) {
        // Bottom disc
        int discStart = points.size();
        calculateEllipticalDisc(points, r1, r2, density, cutoffAngle);
        int discEnd = points.size();
        // Top disc - copy bottom disc at height
        for (int i = discStart; i < discEnd; i++) {
            Vector3d v = points.get(i);
            points.add(new Vector3d(v.x, height, v.z));
        }
        // Wall
        int wallStart = points.size();
        calculateEllipse(points, r1, r2, density, cutoffAngle);
        fillVertically(points, wallStart, height, density);
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        int start = points.size();
        calculateEllipse(points, xRadius, zRadius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
        }
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        if (height != 0)
            calculateCylinder(points, xRadius, zRadius, height, density, cutoffAngle);
        else
            calculateEllipticalDisc(points, xRadius, zRadius, density, cutoffAngle);
    }

    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        int start = points.size();
        calculateEllipticalDisc(points, xRadius, zRadius, density, cutoffAngle);
        if (height != 0) {
            fillVertically(points, start, height, density);
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
