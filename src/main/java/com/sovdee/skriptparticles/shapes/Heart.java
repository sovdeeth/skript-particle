package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a heart shape with a length, width, and eccentricity.
 * The length and width must be greater than 0, and the eccentricity must be greater than 1. Values of 3 or so are recommended.
 * The eccentricity determines how much the heart is "pointed" at the bottom.
 */
public class Heart extends AbstractShape implements LWHShape {

    private double length;
    private double width;
    private double eccentricity;

    /**
     * Creates a heart with the given length, width, and eccentricity.
     *
     * @param length the length. Must be greater than 0.
     * @param width the width. Must be greater than 0.
     * @param eccentricity the eccentricity. Must be greater than 1.
     */
    public Heart(double length, double width, double eccentricity) {
        super();
        this.length = Math.max(length, MathUtil.EPSILON);
        this.width = Math.max(width, MathUtil.EPSILON);
        this.eccentricity = Math.max(eccentricity, 1);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        return MathUtil.calculateHeart(length / 2, width / 2, eccentricity, this.getParticleDensity());
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        double particleDensity = this.getParticleDensity();
        for (double w = width, l = length; w > 0 && l > 0; w -= particleDensity * 1.5, l -= particleDensity * 1.5) {
            points.addAll(MathUtil.calculateHeart(l / 2, w / 2, eccentricity, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        // intentionally empty
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void setHeight(double height) {
        // intentionally empty
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public void setWidth(double width) {
        this.width = Math.max(width, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public void setLength(double length) {
        this.length = Math.max(length, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    /**
     * Gets the eccentricity of this heart.
     * The eccentricity determines how much the heart is "pointed" at the bottom.
     *
     * @return the eccentricity
     */
    public double getEccentricity() {
        return eccentricity;
    }

    /**
     * Sets the eccentricity of this heart.
     * The eccentricity determines how much the heart is "pointed" at the bottom.
     *
     * @param eccentricity the eccentricity. Must be greater than 1.
     */
    public void setEccentricity(double eccentricity) {
        this.eccentricity = Math.max(1, eccentricity);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new Heart(length, width, eccentricity));
    }
}
