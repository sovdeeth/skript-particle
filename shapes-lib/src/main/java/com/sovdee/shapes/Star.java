package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    private double angle;

    public Star(double innerRadius, double outerRadius, double angle) {
        super();
        this.innerRadius = Math.max(innerRadius, MathUtil.EPSILON);
        this.outerRadius = Math.max(outerRadius, MathUtil.EPSILON);
        this.angle = MathUtil.clamp(angle, MathUtil.EPSILON, Math.PI);
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(MathUtil.calculateStar(innerRadius, outerRadius, angle, this.getParticleDensity()));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double minRadius = Math.min(innerRadius, outerRadius);
        double particleDensity = this.getParticleDensity();
        for (double r = 0; r < minRadius; r += particleDensity) {
            points.addAll(MathUtil.calculateStar(innerRadius - r, outerRadius - r, angle, particleDensity));
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        double sideLength = Math.sqrt(Math.pow(innerRadius, 2) + Math.pow(outerRadius, 2) - 2 * innerRadius * outerRadius * Math.cos(angle));
        double perimeter = sideLength * getStarPoints() * 2;
        this.setParticleDensity(perimeter / particleCount);
    }

    public double getInnerRadius() { return innerRadius; }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = Math.max(innerRadius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    public double getOuterRadius() { return outerRadius; }

    public void setOuterRadius(double outerRadius) {
        this.outerRadius = Math.max(outerRadius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    public int getStarPoints() {
        return (int) (Math.PI * 2 / angle);
    }

    public void setStarPoints(int starPoints) {
        starPoints = Math.max(starPoints, 2);
        this.angle = Math.PI * 2 / starPoints;
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Star(innerRadius, outerRadius, angle));
    }
}
