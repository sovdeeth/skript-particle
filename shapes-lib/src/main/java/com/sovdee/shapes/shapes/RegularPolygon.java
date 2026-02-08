package com.sovdee.shapes.shapes;

import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

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

    public static Set<Vector3d> calculateRegularPolygon(double radius, double angle, double particleDensity, boolean wireframe) {
        angle = Math.max(angle, Shape.EPSILON);

        Set<Vector3d> points = new LinkedHashSet<>();
        double apothem = radius * Math.cos(angle / 2);
        double radiusStep = radius / Math.round(apothem / particleDensity);
        if (wireframe) {
            radiusStep = 2 * radius;
        } else {
            points.add(new Vector3d(0, 0, 0));
        }
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            Vector3d vertex = new Vector3d(subRadius, 0, 0);
            for (double i = 0; i < 2 * Math.PI; i += angle) {
                points.addAll(Line.calculateLine(
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i),
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle),
                        particleDensity));
            }
        }
        return points;
    }

    public static Set<Vector3d> calculateRegularPrism(double radius, double angle, double height, double particleDensity, boolean wireframe) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d vertex = new Vector3d(radius, 0, 0);
        for (double i = 0; i < 2 * Math.PI; i += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(vertex), i);
            for (Vector3d vector : Line.calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle), particleDensity)) {
                points.add(vector);
                if (wireframe) {
                    points.add(new Vector3d(vector.x, height, vector.z));
                } else {
                    points.addAll(Line.calculateLine(vector, new Vector3d(vector.x, height, vector.z), particleDensity));
                }
            }
            if (wireframe)
                points.addAll(Line.calculateLine(currentVertex, new Vector3d(currentVertex.x, height, currentVertex.z), particleDensity));
        }
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points) {
        if (height == 0)
            points.addAll(calculateRegularPolygon(this.radius, this.angle, this.getParticleDensity(), true));
        else
            points.addAll(calculateRegularPrism(this.radius, this.angle, this.height, this.getParticleDensity(), true));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        if (height == 0)
            points.addAll(calculateRegularPolygon(this.radius, this.angle, this.getParticleDensity(), false));
        else
            points.addAll(calculateRegularPrism(this.radius, this.angle, this.height, this.getParticleDensity(), false));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        if (height == 0)
            generateSurface(points);
        else {
            double particleDensity = this.getParticleDensity();
            Set<Vector3d> polygon = calculateRegularPolygon(this.radius, this.angle, particleDensity, false);
            fillVertically(polygon, height, particleDensity);
            points.addAll(polygon);
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        int sides = getSides();
        this.setParticleDensity(switch (this.getStyle()) {
            case OUTLINE -> {
                if (height == 0)
                    yield 2 * sides * radius * Math.sin(angle / 2) / particleCount;
                yield (4 * sides * radius * Math.sin(angle / 2) + height * sides) / particleCount;
            }
            case SURFACE -> {
                if (height == 0)
                    yield Math.sqrt(sides * radius * radius * Math.sin(angle) / 2 / particleCount);
                yield (sides * radius * radius * Math.sin(angle) + getSideLength() * sides * height) / particleCount;
            }
            case FILL -> (sides * radius * radius * Math.sin(angle) * height) / particleCount;
        });
        this.setNeedsUpdate(true);
    }

    @Override
    public int getSides() { return (int) (Math.PI * 2 / this.angle); }

    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / Math.max(sides, 3);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getSideLength() { return this.radius * 2 * Math.sin(this.angle / 2); }

    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, Shape.EPSILON);
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        this.radius = Math.max(radius, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() { return this.radius; }

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
        return this.copyTo(new RegularPolygon(angle, radius, height));
    }

    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }
}
