package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Sphere extends AbstractShape implements RadialShape {

    private static final double PHI = Math.PI * (3.0 - Math.sqrt(5.0));
    private static final double[] SPHERE_THETA_COS = new double[4096];
    private static final double[] SPHERE_THETA_SIN = new double[4096];

    static {
        for (int i = 0; i < SPHERE_THETA_COS.length; i++) {
            SPHERE_THETA_COS[i] = Math.cos(PHI * i);
            SPHERE_THETA_SIN[i] = Math.sin(PHI * i);
        }
    }

    private double radius;
    protected double cutoffAngle;
    protected double cutoffAngleCos;

    public Sphere(double radius) {
        super();
        this.radius = Math.max(radius, Shape.EPSILON);
        this.cutoffAngle = Math.PI;
        this.cutoffAngleCos = -1.0;
        this.setStyle(Style.SURFACE);
    }

    // --- Static calculation methods ---

    private static Set<Vector3d> calculateFibonacciSphere(int pointCount, double radius) {
        return calculateFibonacciSphere(pointCount, radius, Math.PI);
    }

    private static Set<Vector3d> calculateFibonacciSphere(int pointCount, double radius, double angleCutoff) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double y = 1;
        if (angleCutoff > Math.PI) angleCutoff = Math.PI;
        double yLimit = Math.cos(angleCutoff);

        double yStep = 2.0 / pointCount;
        int preCompPoints = Math.min(pointCount, SPHERE_THETA_COS.length);
        for (int i = 0; i < preCompPoints; i++) {
            double r = Math.sqrt(1 - y * y) * radius;
            points.add(new Vector3d(r * SPHERE_THETA_COS[i], y * radius, r * SPHERE_THETA_SIN[i]));
            y -= yStep;
            if (y <= yLimit) {
                return points;
            }
        }
        if (pointCount > preCompPoints) {
            for (int i = preCompPoints; i < pointCount; i++) {
                double r = Math.sqrt(1 - y * y) * radius;
                double theta = PHI * i;
                points.add(new Vector3d(r * Math.cos(theta), y * radius, r * Math.sin(theta)));
                y -= yStep;
                if (y <= yLimit) {
                    return points;
                }
            }
        }
        return points;
    }

    // --- Generation methods ---

    @Override
    public void generateOutline(Set<Vector3d> points) {
        this.generateSurface(points);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        points.addAll(calculateFibonacciSphere(pointCount, radius, cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        int subSpheres = (int) (radius / particleDensity) - 1;
        double radiusStep = radius / subSpheres;
        for (int i = 1; i < subSpheres; i++) {
            double subRadius = i * radiusStep;
            int pointCount = 4 * (int) (Math.PI * subRadius * subRadius / (particleDensity * particleDensity));
            points.addAll(calculateFibonacciSphere(pointCount, subRadius, cutoffAngle));
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(switch (this.getStyle()) {
            case OUTLINE, SURFACE -> Math.sqrt(2 * Math.PI * radius * radius * (1 - cutoffAngleCos) / particleCount);
            case FILL ->
                    Math.cbrt(Math.PI / 3 * radius * radius * radius * (2 + cutoffAngleCos) * (1 - cutoffAngleCos) * (1 - cutoffAngleCos) / particleCount);
        });
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
    public Shape clone() {
        return this.copyTo(new Sphere(radius));
    }

    public String toString() {
        return this.getStyle() + " sphere with radius " + this.radius;
    }
}
