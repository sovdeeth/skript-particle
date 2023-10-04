package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

/**
 * A star shape. This shape is defined by an inner radius, an outer radius, and an angle.
 * The angle is the angle between the points of the star.
 */
public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    private double angle;

    /**
     * Creates a new star shape with the given inner radius, outer radius, and angle.
     * @param innerRadius the inner radius of the star. Must be greater than 0.
     * @param outerRadius the outer radius of the star. Must be greater than 0.
     * @param angle the angle between the points of the star in radians. Must be between 0 (exclusive) and pi (inclusive), and should evenly divide 2*pi.
     */
    public Star(double innerRadius, double outerRadius, double angle) {
        super();
        this.innerRadius = Math.max(innerRadius, MathUtil.EPSILON);
        this.outerRadius = Math.max(outerRadius, MathUtil.EPSILON);
        this.angle = MathUtil.clamp(angle, MathUtil.EPSILON, Math.PI);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        return MathUtil.calculateStar(innerRadius, outerRadius, angle, this.getParticleDensity());
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        double minRadius = Math.min(innerRadius, outerRadius);
        double particleDensity = this.getParticleDensity();
        for (double r = 0; r < minRadius; r += particleDensity) {
            points.addAll(MathUtil.calculateStar(innerRadius - r, outerRadius - r, angle, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        double sideLength = Math.sqrt(Math.pow(innerRadius, 2) + Math.pow(outerRadius, 2) - 2 * innerRadius * outerRadius * Math.cos(angle));
        double perimeter = sideLength * getStarPoints() * 2;
        this.setParticleDensity(perimeter / particleCount);
    }

    /**
     * Gets the inner radius of the star.
     * @return the inner radius of the star.
     */
    public double getInnerRadius() {
        return innerRadius;
    }

    /**
     * Sets the inner radius of the star.
     * @param innerRadius the new inner radius of the star. Must be greater than 0.
     */
    public void setInnerRadius(double innerRadius) {
        this.innerRadius = Math.max(innerRadius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    /**
     * Gets the outer radius of the star.
     * @return the outer radius of the star.
     */
    public double getOuterRadius() {
        return outerRadius;
    }

    /**
     * Sets the outer radius of the star.
     * @param outerRadius the new outer radius of the star. Must be greater than 0.
     */
    public void setOuterRadius(double outerRadius) {
        this.outerRadius = Math.max(outerRadius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    /**
     * Gets the number of points on the star.
     *
     * @return the number of points on the star.
     */
    public int getStarPoints() {
        return (int) (Math.PI * 2 / angle);
    }

    /**
     * Sets the number of points on the star.
     *
     * @param starPoints the new number of points on the star. Must be at least 2.
     */
    public void setStarPoints(int starPoints) {
        starPoints = Math.max(starPoints, 2);
        this.angle = Math.PI * 2 / starPoints;
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new Star(innerRadius, outerRadius, angle));
    }
}
