package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Circle extends AbstractShape implements RadialShape, LWHShape {

    protected double radius;
    protected double cutoffAngle;
    protected double height;

    public Circle(double radius) {
        this(radius, 0);
    }


    public Circle(double radius, double height) {
        super();
        this.radius = radius;
        this.height = height;
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> circle = MathUtil.calculateCircle(radius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(circle, height, particleDensity);
        return circle;
    }


    @Override
    public Set<Vector> generateSurface() {
        if (height != 0)
            return MathUtil.calculateCylinder(radius, height, particleDensity, cutoffAngle);
        return MathUtil.calculateDisc(radius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateFilled() {
        Set<Vector> disc = MathUtil.calculateDisc(radius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(disc, height, particleDensity);
        return disc;
    }

    @Override
    public void setParticleCount(int particleCount) {
        if (style == Style.OUTLINE && height == 0) {
            particleDensity = cutoffAngle * radius / particleCount;
        } else if (style == Style.SURFACE || height == 0) {
            double discArea = cutoffAngle * 0.5 * radius * radius;
            double wallArea = cutoffAngle * radius * height;
            particleDensity = Math.sqrt((discArea + wallArea) / particleCount);
        } else {
            particleDensity = Math.cbrt(cutoffAngle * 0.5 * radius * radius * height / particleCount);
        }
        needsUpdate = true;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, 0);
        needsUpdate = true;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public void setLength(double length) {
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Circle(radius, height));
    }

    public String toString() {
        return style.toString() + " circle with radius " + this.radius + " and density " + this.particleDensity;
    }
}
