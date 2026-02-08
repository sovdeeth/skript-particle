package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Heart extends AbstractShape implements LWHShape {

    private double length;
    private double width;
    private double eccentricity;

    public Heart(double length, double width, double eccentricity) {
        super();
        this.length = Math.max(length, Shape.EPSILON);
        this.width = Math.max(width, Shape.EPSILON);
        this.eccentricity = Math.max(eccentricity, 1);
    }

    private static Set<Vector3d> calculateHeart(double length, double width, double eccentricity, double density) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double angleStep = 4 / 3.0 * density / (width + length);
        for (double theta = 0; theta < Math.PI * 2; theta += angleStep) {
            double x = width * Math.pow(Math.sin(theta), 3);
            double y = length * (Math.cos(theta) - 1 / eccentricity * Math.cos(2 * theta) - 1.0 / 6 * Math.cos(3 * theta) - 1.0 / 16 * Math.cos(4 * theta));
            points.add(new Vector3d(x, 0, y));
        }
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        points.addAll(calculateHeart(length / 2, width / 2, eccentricity, density));
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        for (double w = width, l = length; w > 0 && l > 0; w -= density * 1.5, l -= density * 1.5) {
            points.addAll(calculateHeart(l / 2, w / 2, eccentricity, density));
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        return 0.25; // No good formula available
    }

    @Override
    public boolean contains(Vector3d point) {
        // Approximate: check if point is within parametric heart boundary
        if (Math.abs(point.y) > EPSILON) return false;
        double x = point.x;
        double z = point.z;
        double hw = width / 2;
        double hl = length / 2;
        if (hw < EPSILON || hl < EPSILON) return false;
        // Normalize and check parametric distance
        double nx = x / hw;
        double nz = z / hl;
        // Simple bounding check
        return nx * nx + nz * nz <= 4;
    }

    @Override
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

    @Override
    public double getWidth() { return width; }

    @Override
    public void setWidth(double width) {
        this.width = Math.max(width, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getLength() { return length; }

    @Override
    public void setLength(double length) {
        this.length = Math.max(length, Shape.EPSILON);
        invalidate();
    }

    public double getEccentricity() { return eccentricity; }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = Math.max(1, eccentricity);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Heart(length, width, eccentricity));
    }
}
