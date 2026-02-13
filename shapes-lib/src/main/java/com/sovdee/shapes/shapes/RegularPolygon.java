package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class RegularPolygon extends AbstractShape implements PolyShape, RadialShape, LWHShape {

    private double angle;
    private double radius;
    private double height;

    public RegularPolygon(int sides, double radius) {
        this((Math.PI * 2) / sides, radius, 0);
    }

    public RegularPolygon(double angle, double radius) {
        this(angle, radius, 0);
    }

    public RegularPolygon(int sides, double radius, double height) {
        this((Math.PI * 2) / sides, radius, height);
    }

    public RegularPolygon(double angle, double radius, double height) {
        super();
        this.angle = Math.clamp(angle, Shape.EPSILON, Math.PI * 2 / 3);
        this.radius = Math.max(radius, Shape.EPSILON);
        this.height = Math.max(height, 0);
    }

    // --- Static calculation methods ---

    public static void calculateRegularPolygon(List<Vector3d> points, double radius, double angle, double density, boolean wireframe) {
        angle = Math.max(angle, Shape.EPSILON);

        double apothem = radius * Math.cos(angle / 2);
        double radiusStep = radius / Math.round(apothem / density);
        if (wireframe) {
            radiusStep = 2 * radius;
        } else {
            points.add(new Vector3d(0, 0, 0));
        }
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            Vector3d vertex = new Vector3d(subRadius, 0, 0);
            for (double i = 0; i < 2 * Math.PI; i += angle) {
                Line.calculateLine(points,
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i),
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle),
                        density);
            }
        }
    }

    public static void calculateRegularPrism(List<Vector3d> points, double radius, double angle, double height, double density, boolean wireframe) {
        Vector3d vertex = new Vector3d(radius, 0, 0);
        // Need a temp list for the edge points since each spawns vertical points
        List<Vector3d> edgePoints = new ArrayList<>();
        for (double i = 0; i < 2 * Math.PI; i += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(vertex), i);
            edgePoints.clear();
            Line.calculateLine(edgePoints, currentVertex, VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle), density);
            for (Vector3d vector : edgePoints) {
                points.add(vector);
                if (wireframe) {
                    points.add(new Vector3d(vector.x, height, vector.z));
                } else {
                    Line.calculateLine(points, vector, new Vector3d(vector.x, height, vector.z), density);
                }
            }
            if (wireframe)
                Line.calculateLine(points, currentVertex, new Vector3d(currentVertex.x, height, currentVertex.z), density);
        }
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        if (height == 0)
            calculateRegularPolygon(points, this.radius, this.angle, density, true);
        else
            calculateRegularPrism(points, this.radius, this.angle, this.height, density, true);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        if (height == 0)
            calculateRegularPolygon(points, this.radius, this.angle, density, false);
        else
            calculateRegularPrism(points, this.radius, this.angle, this.height, density, false);
    }

    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        if (height == 0)
            generateSurface(points, density);
        else {
            int start = points.size();
            calculateRegularPolygon(points, this.radius, this.angle, density, false);
            fillVertically(points, start, height, density);
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        int sides = getSides();
        return switch (style) {
            case OUTLINE -> {
                if (height == 0)
                    yield 2 * sides * radius * Math.sin(angle / 2) / count;
                yield (4 * sides * radius * Math.sin(angle / 2) + height * sides) / count;
            }
            case SURFACE -> {
                if (height == 0)
                    yield Math.sqrt(sides * radius * radius * Math.sin(angle) / 2 / count);
                yield (sides * radius * radius * Math.sin(angle) + getSideLength() * sides * height) / count;
            }
            case FILL -> (sides * radius * radius * Math.sin(angle) * height) / count;
        };
    }

    @Override
    public boolean contains(Vector3d point) {
        if (height > 0 && (point.y < 0 || point.y > height)) return false;
        if (height == 0 && Math.abs(point.y) > EPSILON) return false;
        // Check if point is within the polygon on XZ plane using inscribed radius
        double dist = Math.sqrt(point.x * point.x + point.z * point.z);
        double apothem = radius * Math.cos(angle / 2);
        return dist <= radius && dist <= apothem / Math.cos(Math.atan2(point.z, point.x) % angle - angle / 2);
    }

    @Override
    public int getSides() { return (int) (Math.PI * 2 / this.angle); }

    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / Math.max(sides, 3);
        invalidate();
    }

    @Override
    public double getSideLength() { return this.radius * 2 * Math.sin(this.angle / 2); }

    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, Shape.EPSILON);
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        this.radius = Math.max(radius, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getRadius() { return this.radius; }

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
        return this.copyTo(new RegularPolygon(angle, radius, height));
    }

    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }
}
