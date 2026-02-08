package com.sovdee.shapes.shapes;

import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    private double angle;

    public Star(double innerRadius, double outerRadius, double angle) {
        super();
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
        this.angle = Math.clamp(angle, Shape.EPSILON, Math.PI);
    }

    private static Set<Vector3d> calculateStar(double innerRadius, double outerRadius, double angle, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d outerVertex = new Vector3d(outerRadius, 0, 0);
        Vector3d innerVertex = new Vector3d(innerRadius, 0, 0);
        for (double theta = 0; theta < 2 * Math.PI; theta += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(outerVertex), theta);
            points.addAll(Line.calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta + angle / 2), particleDensity));
            points.addAll(Line.calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta - angle / 2), particleDensity));
        }
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(calculateStar(innerRadius, outerRadius, angle, this.getParticleDensity()));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double minRadius = Math.min(innerRadius, outerRadius);
        double particleDensity = this.getParticleDensity();
        for (double r = 0; r < minRadius; r += particleDensity) {
            points.addAll(calculateStar(innerRadius - r, outerRadius - r, angle, particleDensity));
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
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    public double getOuterRadius() { return outerRadius; }

    public void setOuterRadius(double outerRadius) {
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
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
