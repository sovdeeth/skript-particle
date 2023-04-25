package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Circle extends AbstractShape implements RadialShape, CutoffShape, LWHShape {

    private double radius;
    private double cutoffAngle;
    private double height;

    public Circle (double radius){
        this(radius, 0, 2*Math.PI);
    }

    public Circle (double radius, double height){
        this(radius, height, 2*Math.PI);
    }

    public Circle (double radius, double height, double cutoffAngle){
        super();
        this.radius = radius;
        this.height = height;
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Set<Vector> generateOutline() {
        if (height != 0)
            return generateSurface();
        return MathUtil.calculateCircle(radius, particleDensity, cutoffAngle);
    }


    @Override
    public Set<Vector> generateSurface() {
        if (height != 0)
            return MathUtil.calculateCylinder(radius, height, particleDensity, cutoffAngle);
        return MathUtil.calculateDisc(radius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateFilled() {
        if (height == 0)
            return generateSurface();
        Set<Vector> disc = MathUtil.calculateDisc(radius, particleDensity, cutoffAngle);
        Set<Vector> points = new HashSet<>(disc);
        double heightStep = height / Math.round(height / particleDensity);
        for (double i = 0; i < height; i += heightStep) {
            for (Vector vector : disc) {
                points.add(vector.clone().setY(i));
            }
        }
        return points;
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
        this.radius = Math.max(radius,0);
        needsUpdate = true;
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, 2*Math.PI);
        needsUpdate = true;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {}

    @Override
    public void setWidth(double width) {}

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height,0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Circle(radius, height, cutoffAngle));
    }

    public String toString(){
        return style.toString() + " circle with radius " + this.radius + " and density " + this.particleDensity;
    }
}
