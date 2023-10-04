package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * A circle with a radius and optionally a height.
 * Circle does not implement {@link LWHShape#setWidth(double)} or {@link LWHShape#setLength(double)}.
 */
public class Circle extends AbstractShape implements RadialShape, LWHShape {

    private double radius;
    protected double cutoffAngle;
    private double height;

    /**
     * Creates a circle with the given radius and a height of 0.
     *
     * @param radius the radius of the circle. Must be greater than 0.
     */
    public Circle(double radius) {
        this(radius, 0);
    }

    /**
     * Creates a circle with the given radius and height.
     *
     * @param radius the radius of the circle. Must be greater than 0.
     * @param height the height of the circle. Must be non-negative.
     */
    public Circle(double radius, double height) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.height = Math.max(height, 0);

        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        Set<Vector> circle = MathUtil.calculateCircle(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(circle, height, this.getParticleDensity());
        return circle;
    }


    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        if (height != 0)
            return MathUtil.calculateCylinder(radius, height, this.getParticleDensity(), cutoffAngle);
        return MathUtil.calculateDisc(radius, this.getParticleDensity(), cutoffAngle);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateFilled() {
        Set<Vector> disc = MathUtil.calculateDisc(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(disc, height, this.getParticleDensity());
        return disc;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);

        if (this.getStyle() == Style.OUTLINE && height == 0) {
            this.setParticleDensity(cutoffAngle * radius / particleCount);
        } else if (this.getStyle() == Style.SURFACE || height == 0) {
            double discArea = cutoffAngle * 0.5 * radius * radius;
            double wallArea = cutoffAngle * radius * height;
            this.setParticleDensity(Math.sqrt((discArea + wallArea) / particleCount));
        } else {
            this.setParticleDensity(Math.cbrt(cutoffAngle * 0.5 * radius * radius * height / particleCount));
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public void setLength(double length) {
        // intentionally left blank
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
        // intentionally left blank
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
        return this.copyTo(new Circle(radius, height));
    }

    @Override
    public String toString() {
        return "Circle{" +
                "radius=" + radius +
                ", cutoffAngle=" + cutoffAngle +
                ", height=" + height +
                '}';
    }
}
