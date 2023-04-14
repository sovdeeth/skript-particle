package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Sphere extends AbstractShape implements RadialShape {

    private double radius;

    public Sphere (double radius){
        super();
        this.style = Shape.Style.SURFACE;
        this.radius = radius;
    }

    @Override
    public Set<Vector> generateOutline() {
        return this.generateSurface();
    }


    @Override
    public Set<Vector> generateSurface() {
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        this.points = MathUtil.calculateFibonacciSphere(pointCount, radius);
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
            points.addAll(MathUtil.calculateFibonacciSphere(pointCount, subRadius));
        }
        return points;
    }

    @Override
    public void setParticleCount(int count) {
        this.setParticleDensity(switch (style) {
            case OUTLINE,SURFACE -> Math.sqrt(4 * Math.PI * radius * radius / count);
            case FILL -> Math.cbrt(1.33333 * Math.PI * radius * radius * radius / count);
        });
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
    public Shape clone() {
        Sphere sphere = new Sphere(radius);
        return this.copyTo(sphere);
    }

    public String toString(){
        return style.toString() + " sphere with radius " + this.radius + " and density " + this.particleDensity;
    }
}
