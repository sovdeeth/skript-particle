package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

public class Sphere extends AbstractShape implements RadialShape {

    private double radius;
    protected double cutoffAngle;
    protected double cutoffAngleCos;

    public Sphere(double radius) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.cutoffAngle = Math.PI;
        this.cutoffAngleCos = -1.0;
        this.setStyle(Style.SURFACE);
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        this.generateSurface(points);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        points.addAll(MathUtil.calculateFibonacciSphere(pointCount, radius, cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        int subSpheres = (int) (radius / particleDensity) - 1;
        double radiusStep = radius / subSpheres;
        for (int i = 1; i < subSpheres; i++) {
            double subRadius = i * radiusStep;
            int pointCount = 4 * (int) (Math.PI * subRadius * subRadius / (particleDensity * particleDensity));
            points.addAll(MathUtil.calculateFibonacciSphere(pointCount, subRadius, cutoffAngle));
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
        this.radius = Math.max(radius, MathUtil.EPSILON);
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
