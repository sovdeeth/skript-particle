package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

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

    private static void calculateHelix(List<Vector3d> points, double radius, double height, double slope, int direction, double density) {
        if (radius <= 0 || height <= 0) {
            return;
        }
        // the helix is parameterised by its angle: one radian covers this much arc length
        double totalAngle = Math.abs(height / slope);
        double lengthPerRadian = Math.sqrt(slope * slope + radius * radius);
        int steps = segments(totalAngle * lengthPerRadian, density);
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + steps + 1);
        for (int i = 0; i <= steps; i++) {
            double t = totalAngle * i / steps;
            double x = radius * Math.cos(direction * t);
            double z = radius * Math.sin(direction * t);
            points.add(new Vector3d(x, t * slope, z));
        }
    }

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateHelix(points, radius, height, slope, direction, density);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        int rings = segments(radius, density);
        for (int i = rings; i >= 1; i--) {
            calculateHelix(points, radius * i / rings, height, slope, direction, density);
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        double totalAngle = height / slope;
        return switch (style) {
            case OUTLINE -> Math.sqrt(slope * slope + radius * radius) * totalAngle / count;
            case FILL, SURFACE -> {
                // area of the helicoid swept between the axis and the helix
                double area = totalAngle * 0.5 * (radius * Math.sqrt(slope * slope + radius * radius)
                        + slope * slope * Math.log((radius + Math.sqrt(slope * slope + radius * radius)) / slope));
                yield Math.sqrt(area / count);
            }
        };
    }

    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        // Approximate: compare against the point of the helix at the same height
        if (point.y < -tolerance || point.y > height + tolerance) return false;
        double y = Math.clamp(point.y, 0.0, height);
        double t = y / slope;
        double dx = point.x - radius * Math.cos(direction * t);
        double dz = point.z - radius * Math.sin(direction * t);
        double dy = point.y - y;
        return dx * dx + dy * dy + dz * dz <= tolerance * tolerance;
    }

    public double getSlope() { return slope; }

    public void setSlope(double slope) {
        this.slope = Math.max(slope, Shape.EPSILON);
        invalidate();
    }

    public int getDirection() { return direction; }

    public void setDirection(int direction) {
        if (direction != 1 && direction != -1)
            throw new IllegalArgumentException("Direction must be 1 or -1");
        this.direction = direction;
        invalidate();
    }

    @Override
    public double getLength() { return height; }

    @Override
    public void setLength(double length) {
        height = Math.max(length, Shape.EPSILON);
        invalidate();
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
        invalidate();
    }

    @Override
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, Shape.EPSILON);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Helix(radius, height, slope, direction));
    }
}
