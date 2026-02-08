package com.sovdee.shapes.shapes;

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

    public static List<Vector3d> calculateEllipse(double r1, double r2, double particleDensity, double cutoffAngle) {
        List<Vector3d> points = new ArrayList<>();
        double circumference = ellipseCircumference(r1, r2);

        int steps = (int) Math.round(circumference / particleDensity);
        double theta = 0;
        double angleStep = 0;
        for (int i = 0; i < steps; i++) {
            if (theta > cutoffAngle) {
                break;
            }
            points.add(new Vector3d(r1 * Math.cos(theta), 0, r2 * Math.sin(theta)));
            double dx = r1 * Math.sin(theta + 0.5 * angleStep);
            double dy = r2 * Math.cos(theta + 0.5 * angleStep);
            angleStep = particleDensity / Math.sqrt(dx * dx + dy * dy);
            theta += angleStep;
        }
        return points;
    }

    public static Set<Vector3d> calculateEllipticalDisc(double r1, double r2, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = new LinkedHashSet<>();
        int steps = (int) Math.round(Math.max(r1, r2) / particleDensity);
        double r;
        for (double i = 1; i <= steps; i += 1) {
            r = i / steps;
            points.addAll(calculateEllipse(r1 * r, r2 * r, particleDensity, cutoffAngle));
        }
        return points;
    }

    public static Set<Vector3d> calculateCylinder(double r1, double r2, double height, double particleDensity, double cutoffAngle) {
        Set<Vector3d> points = calculateEllipticalDisc(r1, r2, particleDensity, cutoffAngle);
        // Top disc via direct loop
        Set<Vector3d> top = new LinkedHashSet<>();
        for (Vector3d v : points) {
            top.add(new Vector3d(v.x, height, v.z));
        }
        points.addAll(top);
        // Wall
        Set<Vector3d> wall = new LinkedHashSet<>(calculateEllipse(r1, r2, particleDensity, cutoffAngle));
        fillVertically(wall, height, particleDensity);
        points.addAll(wall);
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points) {
        Set<Vector3d> ellipse = new LinkedHashSet<>(calculateEllipse(xRadius, zRadius, this.getParticleDensity(), cutoffAngle));
        if (height != 0) {
            fillVertically(ellipse, height, this.getParticleDensity());
            points.addAll(ellipse);
        } else {
            points.addAll(ellipse);
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        if (height != 0)
            points.addAll(calculateCylinder(xRadius, zRadius, height, this.getParticleDensity(), cutoffAngle));
        else
            points.addAll(calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        Set<Vector3d> disc = calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle);
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
        switch (this.getStyle()) {
            case OUTLINE -> {
                double h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXY = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.setParticleDensity(circumferenceXY / particleCount);
            }
            case SURFACE, FILL -> this.setParticleDensity(Math.sqrt((Math.PI * xRadius * zRadius) / particleCount));
        }
    }

    @Override
    public double getLength() { return xRadius * 2; }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() { return zRadius * 2; }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
