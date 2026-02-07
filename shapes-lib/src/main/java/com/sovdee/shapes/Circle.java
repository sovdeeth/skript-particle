package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

public class Circle extends AbstractShape implements RadialShape, LWHShape {

    private double radius;
    protected double cutoffAngle;
    private double height;

    public Circle(double radius) {
        this(radius, 0);
    }

    public Circle(double radius, double height) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        Set<Vector3d> circle = MathUtil.calculateCircle(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            points.addAll(MathUtil.fillVertically(circle, height, this.getParticleDensity()));
        else
            points.addAll(circle);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        if (height != 0)
            points.addAll(MathUtil.calculateCylinder(radius, height, this.getParticleDensity(), cutoffAngle));
        else
            points.addAll(MathUtil.calculateDisc(radius, this.getParticleDensity(), cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        Set<Vector3d> disc = MathUtil.calculateDisc(radius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            points.addAll(MathUtil.fillVertically(disc, height, this.getParticleDensity()));
        else
            points.addAll(disc);
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
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() { return 0; }

    @Override
    public void setLength(double length) { }

    @Override
    public double getWidth() { return 0; }

    @Override
    public void setWidth(double width) { }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Circle(radius, height));
    }

    @Override
    public String toString() {
        return "Circle{radius=" + radius + ", cutoffAngle=" + cutoffAngle + ", height=" + height + '}';
    }
}
