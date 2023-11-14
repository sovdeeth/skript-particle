package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Represents an ellipse shape with an x radius, z radius, and optional height.
 * The radii must be greater than 0, and the height must be non-negative.
 */
public class Ellipse extends AbstractShape implements LWHShape {

    private double xRadius;
    private double zRadius;
    private double height;
    protected double cutoffAngle;

    /**
     * Creates an ellipse with the given x radius and z radius.
     *
     * @param xRadius the x radius. Must be greater than 0.
     * @param zRadius the z radius. Must be greater than 0.
     */
    public Ellipse(double xRadius, double zRadius) {
        this(xRadius, zRadius, 0);
    }

    /**
     * Creates an ellipse with the given x radius, z radius, and height.
     *
     * @param xRadius the x radius. Must be greater than 0.
     * @param zRadius the z radius. Must be greater than 0.
     * @param height the height. Must be non-negative.
     */
    public Ellipse(double xRadius, double zRadius, double height) {
        super();
        this.xRadius = Math.max(xRadius, MathUtil.EPSILON);
        this.zRadius = Math.max(zRadius, MathUtil.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        Set<Vector> ellipse = new LinkedHashSet<>(MathUtil.calculateEllipse(xRadius, zRadius, this.getParticleDensity(), cutoffAngle));
        if (height != 0)
            return MathUtil.fillVertically(ellipse, height, this.getParticleDensity());
        return ellipse;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        // if height is not 0, make it a cylinder
        if (height != 0) {
            return MathUtil.calculateCylinder(xRadius, zRadius, height, this.getParticleDensity(), cutoffAngle);
        }
        return MathUtil.calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateFilled() {
        Set<Vector> disc = MathUtil.calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(disc, height, this.getParticleDensity());
        return disc;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        switch (this.getStyle()) {
            case OUTLINE -> {
                // this is so fucking cringe
                double h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXY = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.setParticleDensity(circumferenceXY / particleCount);
            }
            case SURFACE, FILL -> this.setParticleDensity(Math.sqrt((Math.PI * xRadius * zRadius) / particleCount));
        }
    }

    @Override
    public double getLength() {
        return xRadius * 2;
    }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() {
        return zRadius * 2;
    }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
