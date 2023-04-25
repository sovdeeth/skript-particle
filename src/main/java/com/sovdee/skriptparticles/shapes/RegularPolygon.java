package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class RegularPolygon extends AbstractShape implements PolyShape, RadialShape, LWHShape {

    private double angle;
    private double radius;
    private double height;

    public RegularPolygon(int sides, double radius) {
        this((Math.PI * 2) / sides, radius, 0);
    }

    public RegularPolygon(double angle, double radius) {
        this(angle, radius, 0);
    }

    public RegularPolygon(int sides, double radius, double height) {
        this((Math.PI * 2) / sides, radius, height);
    }

    public RegularPolygon(double angle, double radius, double height) {
        super();
        this.angle = angle;
        this.radius = radius;
        this.height = height;
        this.style = Style.OUTLINE;
    }

    @Override
    public Set<Vector> generateOutline() {
        if (height == 0)
            return MathUtil.calculateRegularPolygon(this.radius, this.angle, this.particleDensity, true);
        return MathUtil.calculateRegularPrism(this.radius, this.angle, this.height, this.particleDensity, true);
    }

    @Override
    public Set<Vector> generateSurface() {
        if (height == 0)
            return MathUtil.calculateRegularPolygon(this.radius, this.angle, this.particleDensity, false);
        return MathUtil.calculateRegularPrism(this.radius, this.angle, this.height, this.particleDensity, false);
    }

    @Override
    public Set<Vector> generateFilled() {
        if (height == 0)
            return generateSurface();
        Set<Vector> polygon = MathUtil.calculateRegularPolygon(this.radius, this.angle, this.particleDensity, false);
        return MathUtil.fillVertically(polygon, height, particleDensity);
    }

    @Override
    public void setParticleCount(int particleCount) {
        int sides = getSides();
        particleDensity = switch (style) {
            case OUTLINE -> {
                if (height == 0)
                    yield 2 * sides * radius * Math.sin(angle / 2) / particleCount;
                yield (4 * sides * radius * Math.sin(angle / 2) + height * sides) / particleCount;
            }
            case SURFACE -> {
                if (height == 0)
                    yield Math.sqrt(sides * radius * radius * Math.sin(angle) / 2 / particleCount);
                yield (sides * radius * radius * Math.sin(angle) + getSideLength() * sides * height) / particleCount;
            }
            case FILL -> (sides * radius * radius * Math.sin(angle) * height) / particleCount;
        };
        needsUpdate = true;
    }

    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / Math.max(sides, 3);
        needsUpdate = true;
    }

    @Override
    public int getSides() {
        return (int) (Math.PI * 2 / this.angle);
    }

    @Override
    public void setSideLength(double sideLength) {
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        this.radius = Math.max(radius, MathUtil.EPSILON);
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
        this.radius = Math.max(radius, MathUtil.EPSILON);
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
        this.height = Math.max(height, 0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new RegularPolygon(angle, radius, height));
    }

    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }

}
