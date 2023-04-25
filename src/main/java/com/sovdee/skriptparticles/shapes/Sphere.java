package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Sphere extends AbstractShape implements RadialShape, CutoffShape {

    private double radius;
    private double cutoffAngle;
    private double cutoffAngleCos;

    public Sphere (double radius){
        this(radius, 2*Math.PI);
    }

    public Sphere (double radius, double cutoffAngle){
        super();
        this.radius = radius;
        this.cutoffAngle = cutoffAngle;
        this.cutoffAngleCos = Math.cos(cutoffAngle);
        this.style = Style.SURFACE;
    }

    @Override
    public Set<Vector> generateOutline() {
        return this.generateSurface();
    }


    @Override
    public Set<Vector> generateSurface() {
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        this.points = MathUtil.calculateFibonacciSphere(pointCount, radius, cutoffAngle);
        return points;
    }

    @Override
    public Set<Vector> generateFilled() {
        this.points = generateSurface();
        int subSpheres = (int) (radius / particleDensity) - 1;
        double radiusStep = radius / subSpheres;
        for (int i = 1; i < subSpheres; i++) {
            double subRadius = i * radiusStep;
            int pointCount = 4 * (int) (Math.PI * subRadius * subRadius / (particleDensity * particleDensity));
            points.addAll(MathUtil.calculateFibonacciSphere(pointCount, subRadius, cutoffAngle));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        this.particleDensity =  switch (style) {
            case OUTLINE, SURFACE -> Math.sqrt(2 * Math.PI * radius * radius * (1 - cutoffAngleCos) / particleCount);
            case FILL -> Math.cbrt(Math.PI / 3 * radius * radius * radius * (2 + cutoffAngleCos) * (1 - cutoffAngleCos) * (1 - cutoffAngleCos) / particleCount);
        };
        needsUpdate = true;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius,0);
        needsUpdate = true;
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Sphere(radius, cutoffAngle));
    }

    public String toString(){
        return style.toString() + " sphere with radius " + this.radius + " and density " + this.particleDensity;
    }
}
