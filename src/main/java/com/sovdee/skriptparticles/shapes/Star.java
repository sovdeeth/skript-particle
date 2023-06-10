package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    private double angle;

    public Star(double innerRadius, double outerRadius, double angle) {
        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;
        this.angle = angle;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateStar(innerRadius, outerRadius, angle, particleDensity);
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        double minRadius = Math.min(innerRadius, outerRadius);
        for (double r = 0; r < minRadius; r += particleDensity) {
            points.addAll(MathUtil.calculateStar(innerRadius - r, outerRadius - r, angle, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        double sideLength = Math.sqrt(Math.pow(innerRadius, 2) + Math.pow(outerRadius, 2) - 2 * innerRadius * outerRadius * Math.cos(angle));
        double perimeter = sideLength * getStarPoints() * 2;
        particleDensity = perimeter / particleCount;
    }

    public double getInnerRadius() {
        return innerRadius;
    }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = innerRadius;
        needsUpdate = true;
    }

    public double getOuterRadius() {
        return outerRadius;
    }

    public void setOuterRadius(double outerRadius) {
        this.outerRadius = outerRadius;
        needsUpdate = true;
    }

    public int getStarPoints() {
        return (int) (Math.PI * 2 / angle);
    }

    public void setStarPoints(int starPoints) {
        this.angle = Math.PI * 2 / starPoints;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Star(innerRadius, outerRadius, angle));
    }
}
