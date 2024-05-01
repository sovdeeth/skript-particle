package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * A line shape. This shape is defined by two points, a start and an end.
 * These points can be relative ({@link Vector}) or absolute ({@link DynamicLocation}).
 */
public class Line extends AbstractShape implements LWHShape {

    private Vector start;
    private Vector end;

    private @Nullable DynamicLocation startLocation;
    private @Nullable DynamicLocation endLocation;
    private boolean isDynamic = false;

    /**
     * Creates a new line shape with the start point at the origin and the end point at the given vector.
     * The vector cannot be the origin.
     * @param end the end point of the line
     * @throws IllegalArgumentException if the end vector is the origin
     */
    public Line(Vector end) {
        this(new Vector(0, 0, 0), end);
    }

    /**
     * Creates a new line shape with the start and end points at the given vectors.
     * The vectors cannot be the same.
     * @param start the start point of the line
     * @param end the end point of the line
     * @throws IllegalArgumentException if the start and end vectors are the same
     */
    public Line(Vector start, Vector end) {
        super();
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");
        this.start = start;
        this.end = end;
    }

    /**
     * Creates a new line shape with the start and end points at the given locations.
     * The locations cannot be the same.
     * @param start the start point of the line
     * @param end the end point of the line
     */
    public Line(DynamicLocation start, DynamicLocation end) {
        super();
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");
        if (start.isDynamic() || end.isDynamic()) {
            this.startLocation = start.clone();
            this.endLocation = end.clone();
            this.isDynamic = true;
        }
        this.start = new Vector(0, 0, 0);
        this.end = end.getLocation().toVector().subtract(start.getLocation().toVector());
        if (this.end.equals(this.start))
            throw new IllegalArgumentException("Start and end locations cannot be the same.");

        this.setLocation(start.clone());
    }


    @Override
    @Contract(pure = true)
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            // Ensure that the points are always needing to be updated if the start or end location is dynamic
            this.setNeedsUpdate(true);
        return points;
    }

    @Override
    @Contract(pure = true)
    public void generatePoints(Set<Vector> points) {
        if (isDynamic) {
            assert startLocation != null;
            assert endLocation != null;
            this.start = new Vector(0, 0, 0);
            this.end = endLocation.getLocation().toVector().subtract(startLocation.getLocation().toVector());
        }
        super.generatePoints(points);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateOutline(Set<Vector> points) {
        points.addAll(MathUtil.calculateLine(start, end, this.getParticleDensity()));
    }

    /**
     * Gets the start point of the line as a vector. May change between calls if the start point is dynamic.
     * @return the start point of the line
     */
    public Vector getStart() {
        if (isDynamic) {
            assert startLocation != null;
            return startLocation.getLocation().toVector();
        }
        return start.clone();
    }

    /**
     * Sets the start point of the line as a vector.
     * @param start the start point of the line. Must not be identical to the end point.
     * @throws IllegalArgumentException if the start and end points are identical.
     */
    public void setStart(Vector start) {
        if (start.equals(end))
            throw new IllegalArgumentException("Start and end points must not be identical");
        this.start = start.clone();
    }

    /**
     * Gets the end point of the line as a vector. May change between calls if the end point is dynamic.
     * @return the end point of the line
     */
    public Vector getEnd() {
        if (isDynamic) {
            assert endLocation != null;
            return endLocation.getLocation().toVector();
        }
        return end.clone();
    }

    /**
     * Sets the end point of the line as a vector.
     * @param end the end point of the line. Must not be identical to the start point.
     * @throws IllegalArgumentException if the start and end points are identical.
     */
    public void setEnd(Vector end) {
        if (end.equals(start))
            throw new IllegalArgumentException("Start and end points must not be identical");
        this.end = end.clone();
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(end.clone().subtract(start).length() / particleCount);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return start.clone().subtract(end).length();
    }

    @Override
    public void setLength(double length) {
        length = Math.max(length, MathUtil.EPSILON);
        Vector direction = end.clone().subtract(start).normalize();
        end = start.clone().add(direction.multiply(length));
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
        return 0;
    }

    @Override
    public void setHeight(double height) {
        // intentionally left blank
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        Line line;
        if (isDynamic) {
            assert this.startLocation != null;
            assert this.endLocation != null;
            line = (new Line(this.startLocation, this.endLocation));
        } else {
            line = (new Line(this.start, this.end));
        }
        line.isDynamic = this.isDynamic;
        return this.copyTo(line);
    }

    public String toString() {
        return "Line from " + this.start + " to " + this.end;
    }
}
