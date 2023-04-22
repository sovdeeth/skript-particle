package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class RegularPolygon extends AbstractShape implements PolyShape, RadialShape {

    private double angle;
    private double radius;

    public RegularPolygon(int sides, double radius) {
        super();
        this.angle = (Math.PI * 2) / sides;
        this.radius = radius;
        this.style = Style.OUTLINE;
    }

    public RegularPolygon(double angle, double radius) {
        super();
        this.angle = angle;
        this.radius = radius;
        this.style = Style.OUTLINE;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateRegularPolygon(this.radius, this.angle, this.particleDensity);
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        double apothem = radius * Math.cos(this.angle/2);
        double radiusStep = radius / Math.round(apothem/particleDensity);
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            points.addAll(MathUtil.calculateRegularPolygon(subRadius, angle, particleDensity));
        }
        points.add(new Vector(0, 0, 0));
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {

    }

    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / sides;
        needsUpdate = true;
    }

    @Override
    public int getSides() {
        return (int) (Math.PI * 2 / this.angle);
    }

    @Override
    public void setSideLength(double sideLength) {
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        needsUpdate = true;
    }

    @Override
    public double getSideLength() {
        return this.radius * 2 * Math.sin(this.angle / 2);
    }

    @Override
    public double getRadius() {
        return this.radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = radius;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        RegularPolygon clone = new RegularPolygon(this.angle, this.radius);
        this.copyTo(clone);
        return clone;
    }

    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }
}
