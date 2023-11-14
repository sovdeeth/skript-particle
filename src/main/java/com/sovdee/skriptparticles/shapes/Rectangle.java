package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * A rectangle shape, defined by a plane and a length and width.
 * Can be defined by two corners, which can be relative ({@link Vector}) or absolute ({@link DynamicLocation}).
 */
public class Rectangle extends AbstractShape implements LWHShape {

    private Plane plane;
    private double halfLength;
    private double halfWidth;
    private double lengthStep = 1.0;
    private double widthStep = 1.0;
    private Vector centerOffset = new Vector(0, 0, 0);
    private @Nullable DynamicLocation negativeCorner;
    private @Nullable DynamicLocation positiveCorner;
    private boolean isDynamic = false;

    /**
     * Creates a new rectangle shape with the given plane and length and width.
     * @param length the length of the rectangle. Must be greater than 0.
     * @param width the width of the rectangle. Must be greater than 0.
     * @param plane the plane of the rectangle.
     */
    public Rectangle(double length, double width, Plane plane) {
        super();
        this.plane = plane;
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        calculateSteps();
    }

    /**
     * Creates a new rectangle shape from the given corners and plane.
     * The corners may not be the same.
     * @param cornerA the first corner of the rectangle.
     * @param cornerB the second corner of the rectangle.
     * @param plane the plane of the rectangle.
     * @throws IllegalArgumentException if the corners are the same
     */
    public Rectangle(Vector cornerA, Vector cornerB, Plane plane) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Corners cannot be the same.");
        this.plane = plane;
        setLengthWidth(cornerA, cornerB);
        centerOffset = cornerB.clone().add(cornerA).multiply(0.5);
        switch (plane) {
            case XZ -> centerOffset.setY(0);
            case XY -> centerOffset.setZ(0);
            case YZ -> centerOffset.setX(0);
        }
        calculateSteps();
    }

    /**
     * Creates a new rectangle shape from the given corners and plane.
     * The corners may not be the same.
     * @param cornerA the first corner of the rectangle.
     * @param cornerB the second corner of the rectangle.
     * @param plane the plane of the rectangle.
     * @throws IllegalArgumentException if the corners are the same
     */
    public Rectangle(DynamicLocation cornerA, DynamicLocation cornerB, Plane plane) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Corners cannot be the same.");

        this.plane = plane;
        Location cornerALocation = cornerA.getLocation();
        Location cornerBLocation = cornerB.getLocation();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Corners cannot be the same.");

        if (cornerA.isDynamic() || cornerB.isDynamic()) {
            this.negativeCorner = cornerA.clone();
            this.positiveCorner = cornerB.clone();
            isDynamic = true;
        } else {
            setLengthWidth(cornerALocation, cornerBLocation);
        }
        // get center of rectangle
        Vector offset = cornerBLocation.toVector().subtract(cornerALocation.toVector()).multiply(0.5);
        switch (plane) {
            case XZ -> offset.setY(0);
            case XY -> offset.setZ(0);
            case YZ -> offset.setX(0);
        }
        this.setLocation(new DynamicLocation(cornerALocation.clone().add(offset)));
        calculateSteps();
    }

    /**
     * Sets the length and width of the rectangle based on the given corners.
     * @param cornerA the first corner
     * @param cornerB the second corner
     */
    private void setLengthWidth(Location cornerA, Location cornerB) {
        setLengthWidth(cornerA.toVector(), cornerB.toVector());
    }

    /**
     * Sets the length and width of the rectangle based on the given corners.
     * @param cornerA the first corner
     * @param cornerB the second corner
     */
    private void setLengthWidth(Vector cornerA, Vector cornerB) {
        double length = switch (plane) {
            case XZ, XY -> Math.abs(cornerA.getX() - cornerB.getX());
            case YZ -> Math.abs(cornerA.getY() - cornerB.getY());
        };
        double width = switch (plane) {
            case XZ, YZ -> Math.abs(cornerA.getZ() - cornerB.getZ());
            case XY -> Math.abs(cornerA.getY() - cornerB.getY());
        };
        this.halfWidth = Math.abs(width) / 2;
        this.halfLength = Math.abs(length) / 2;
    }

    /**
     * Creates a {@link Vector} in the plane of the rectangle from the given length and width.
     * @param length X or Y coordinate, depending on the plane.
     * @param width Z or Y coordinate, depending on the plane.
     * @return a vector in the plane of the rectangle.
     */
    @Contract(pure = true, value = "_, _ -> new")
    private Vector vectorFromLengthWidth(double length, double width) {
        return switch (plane) {
            case XZ -> new Vector(length, 0, width);
            case XY -> new Vector(length, width, 0);
            case YZ -> new Vector(0, length, width);
        };
    }

    /**
     * Calculates the nearest factor to particleDensity as step size for the x and z plane.
     * Used to ensure the shape has a uniform density of particles.
     */
    private void calculateSteps() {
        double particleDensity = this.getParticleDensity();
        lengthStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        widthStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        Set<Vector> points = new LinkedHashSet<>();
        for (double l = -halfLength + widthStep; l < halfLength; l += widthStep) {
            points.add(vectorFromLengthWidth(l, -halfWidth));
            points.add(vectorFromLengthWidth(l, halfWidth));
        }
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            points.add(vectorFromLengthWidth(-halfLength, w));
            points.add(vectorFromLengthWidth(halfLength, w));
        }
        return points;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        Set<Vector> points = new LinkedHashSet<>();
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            for (double l = -halfLength; l <= halfLength; l += widthStep) {
                points.add(vectorFromLengthWidth(l, w));
            }
        }
        return points;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            assert positiveCorner != null;
            assert negativeCorner != null;
            Location pos = positiveCorner.getLocation();
            Location neg = negativeCorner.getLocation();
            setLengthWidth(neg, pos);
            // get center of rectangle
            Vector offset = pos.toVector().subtract(neg.toVector()).multiply(0.5);
            switch (plane) {
                case XZ -> offset.setY(0);
                case XY -> offset.setZ(0);
                case YZ -> offset.setX(0);
            }
            this.setLocation(new DynamicLocation(neg.clone().add(offset)));
        }
        calculateSteps();
        Set<Vector> points = super.generatePoints();
        points.forEach(vector -> vector.add(centerOffset));
        return points;
    }

    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            // Ensure that the points are always needing to be updated if the start or end location is dynamic
            this.setNeedsUpdate(true);
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        switch (this.getStyle()) {
            case FILL, SURFACE -> this.setParticleDensity(Math.sqrt(4 * halfWidth * halfLength / particleCount));
            case OUTLINE -> this.setParticleDensity(4 * (halfWidth + halfLength) / particleCount);
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return halfLength * 2;
    }

    @Override
    public void setLength(double length) {
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() {
        return halfWidth * 2;
    }

    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public void setHeight(double height) {
        // intentionally left blank
    }

    /**
     * Gets the plane of the rectangle.
     * @return the plane of the rectangle.
     */
    public Plane getPlane() {
        return plane;
    }

    /**
     * Sets the plane of the rectangle. Ensures the shape will be updated on the next call to {@link #generatePoints()}.
     * @param plane the plane of the rectangle.
     */
    public void setPlane(Plane plane) {
        this.plane = plane;
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        Rectangle rectangle;
        if (isDynamic) {
            assert negativeCorner != null;
            assert positiveCorner != null;
            rectangle = (new Rectangle(negativeCorner, positiveCorner, plane));
        } else {
            rectangle = (new Rectangle(this.getLength(), this.getWidth(), plane));
        }
        rectangle.isDynamic = this.isDynamic;
        return this.copyTo(rectangle);
    }

    @Override
    public String toString() {
        String axis = this.plane.toString().toLowerCase();
        if (isDynamic)
            return axis + " rectangle from " + negativeCorner + " to " + positiveCorner;
        return axis + " rectangle with length " + this.getLength() + " and width " + this.getWidth();
    }

    /**
     * Represents the plane of a rectangle.
     */
    public enum Plane {
        XZ, XY, YZ
    }
}
