package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * A regular polygon shape. This shape is defined by a radius and either a side count or an angle between adjacent vertices.
 * The polygon can be a prism if a height is given.
 */
public class RegularPolygon extends AbstractShape implements PolyShape, RadialShape, LWHShape {

    private double angle;
    private double radius;
    private double height;

    /**
     * Creates a new regular polygon shape with the given side count and radius.
     * @param sides the number of sides of the polygon. Must be greater than 2.
     * @param radius the radius of the polygon. Must be greater than 0.
     */
    public RegularPolygon(int sides, double radius) {
        this((Math.PI * 2) / sides, radius, 0);
    }

    /**
     * Creates a new regular polygon shape with the given angle and radius.
     * @param angle the angle between adjacent vertices in radians. Must evenly divide 2pi and be between 0 (exclusive) and 2pi/3 (inclusive).
     * @param radius the radius of the polygon. Must be greater than 0.
     */
    public RegularPolygon(double angle, double radius) {
        this(angle, radius, 0);
    }

    /**
     * Creates a new regular polygon shape with the given side count, radius, and height.
     * @param sides the number of sides of the polygon. Must be greater than 2.
     * @param radius the radius of the polygon. Must be greater than 0.
     * @param height the height of the polygon. Must be non-negative.
     */
    public RegularPolygon(int sides, double radius, double height) {
        this((Math.PI * 2) / sides, radius, height);
    }

    /**
     * Creates a new regular polygon shape with the given angle, radius, and height.
     * @param angle the angle between adjacent vertices in radians. Should evenly divide 2pi and must be between 0 (exclusive) and 2pi/3 (inclusive).
     * @param radius the radius of the polygon. Must be greater than 0.
     * @param height the height of the polygon. Must be non-negative.
     */
    public RegularPolygon(double angle, double radius, double height) {
        super();
        this.angle = MathUtil.clamp(angle, MathUtil.EPSILON, Math.PI * 2 / 3);
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.height = Math.max(height, 0);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateOutline(Set<Vector> points) {
        if (height == 0)
            points.addAll(MathUtil.calculateRegularPolygon(this.radius, this.angle, this.getParticleDensity(), true));
        else
            points.addAll(MathUtil.calculateRegularPrism(this.radius, this.angle, this.height, this.getParticleDensity(), true));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateSurface(Set<Vector> points) {
        if (height == 0)
            points.addAll(MathUtil.calculateRegularPolygon(this.radius, this.angle, this.getParticleDensity(), false));
        else
            points.addAll(MathUtil.calculateRegularPrism(this.radius, this.angle, this.height, this.getParticleDensity(), false));
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void generateFilled(Set<Vector> points) {
        if (height == 0)
            generateSurface(points);
        else {
            double particleDensity = this.getParticleDensity();
            Set<Vector> polygon = MathUtil.calculateRegularPolygon(this.radius, this.angle, particleDensity, false);
            points.addAll(MathUtil.fillVertically(polygon, height, particleDensity));
        }
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        int sides = getSides();
        this.setParticleDensity(switch (this.getStyle()) {
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
        });
        this.setNeedsUpdate(true);
    }

    @Override
    public int getSides() {
        return (int) (Math.PI * 2 / this.angle);
    }

    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / Math.max(sides, 3);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getSideLength() {
        return this.radius * 2 * Math.sin(this.angle / 2);
    }

    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, MathUtil.EPSILON);
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() {
        return this.radius;
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
        // intentionally empty
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
        // intentionally empty
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
        return this.copyTo(new RegularPolygon(angle, radius, height));
    }

    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }

}
