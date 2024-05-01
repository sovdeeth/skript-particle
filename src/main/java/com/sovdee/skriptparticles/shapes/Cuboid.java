package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/*
 * A cuboid shape, defined either by two corners or by length, width, and height.
 * Cuboids utilising the {@link DynamicLocation} constructor do not require a location to be drawn at and will
 * automatically update their positions when drawn.
 */
public class Cuboid extends AbstractShape implements LWHShape {

    private double halfLength, halfWidth, halfHeight;
    private double lengthStep, widthStep, heightStep;
    private Vector centerOffset = new Vector(0, 0, 0);
    private @Nullable DynamicLocation negativeCorner, positiveCorner;
    private boolean isDynamic = false;

    /**
     * Creates a cuboid with the given length, width, and height.
     * @param length The length of the cuboid. Must be greater than 0.
     * @param width The width of the cuboid. Must be greater than 0.
     * @param height The height of the cuboid. Must be greater than 0.
     */
    public Cuboid(double length, double width, double height) {
        super();

        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.halfHeight = Math.max(height / 2, MathUtil.EPSILON);
        calculateSteps();
    }

    /**
     * Creates a cuboid from the given corner vectors. Using asymmetric vectors will result in a cuboid that is offset from the origin of the shape.
     * <p>
     * For example, using (0, 0, 0) and (1, 1, 1) and drawing the cuboid at (2, 0, 2) will result
     * in the negative corner being drawn at (2, 0, 2) and the positive corner being at (3, 1, 3).
     *
     * @param cornerA A vector from the origin of the shape to the first corner.
     * @param cornerB A vector from the origin of the shape to the second corner.
     * @throws IllegalArgumentException If the given vectors are equal.
     */
    public Cuboid(Vector cornerA, Vector cornerB) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Cuboid corners cannot be equal.");
        this.halfLength = Math.abs(cornerB.getX() - cornerA.getX()) / 2;
        this.halfWidth = Math.abs(cornerB.getZ() - cornerA.getZ()) / 2;
        this.halfHeight = Math.abs(cornerB.getY() - cornerA.getY()) / 2;
        centerOffset = cornerB.clone().add(cornerA).multiply(0.5);
        calculateSteps();
    }

    /**
     * Creates a cuboid from the given corner locations. Unlike the vector constructor, this constructor will not offset the cuboid from the origin of the shape.
     * When drawn with the default location, the visual corner locations will be at the given corners. If the cuboid is drawn at a different location, the
     * shape's length, width, and height will be dependent on the given corners, but the shape will be drawn at the given location instead.
     *
     * @param cornerA The first corner of the cuboid.
     * @param cornerB The second corner of the cuboid.
     * @throws IllegalArgumentException If the given locations are equal.
     */
    public Cuboid(DynamicLocation cornerA, DynamicLocation cornerB) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Cuboid corners cannot be equal.");
        Location cornerALocation = cornerA.getLocation();
        Location cornerBLocation = cornerB.getLocation();
        if (cornerALocation.equals(cornerBLocation))
            throw new IllegalArgumentException("Cuboid corners cannot be equal.");

        if (cornerA.isDynamic() || cornerB.isDynamic()) {
            this.negativeCorner = cornerA.clone();
            this.positiveCorner = cornerB.clone();
            isDynamic = true;
        } else {
            this.halfLength = Math.abs(cornerBLocation.getX() - cornerALocation.getX()) / 2;
            this.halfWidth = Math.abs(cornerBLocation.getZ() - cornerALocation.getZ()) / 2;
            this.halfHeight = Math.abs(cornerBLocation.getY() - cornerALocation.getY()) / 2;
        }
        this.setLocation(new DynamicLocation(cornerALocation.clone().add(cornerBLocation.subtract(cornerALocation).toVector().multiply(0.5))));
        calculateSteps();
    }

    /**
     * Calculates the step size for each axis based on the given particle density.
     * todo: Use config option to toggle between adaptive and fixed step size.
     */
    private void calculateSteps() {
        widthStep = 2 * halfWidth / Math.round(2 * halfWidth / this.getParticleDensity());
        lengthStep = 2 * halfLength / Math.round(2 * halfLength / this.getParticleDensity());
        heightStep = 2 * halfHeight / Math.round(2 * halfHeight / this.getParticleDensity());
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateOutline(Set<Vector> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            points.add(new Vector(x, -halfHeight, -halfWidth));
            points.add(new Vector(x, -halfHeight, halfWidth));
            points.add(new Vector(x, halfHeight, -halfWidth));
            points.add(new Vector(x, halfHeight, halfWidth));
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            points.add(new Vector(-halfLength, y, -halfWidth));
            points.add(new Vector(-halfLength, y, halfWidth));
            points.add(new Vector(halfLength, y, -halfWidth));
            points.add(new Vector(halfLength, y, halfWidth));
        }
        for (double z = -halfWidth + widthStep; z < halfWidth; z += widthStep) {
            points.add(new Vector(-halfLength, -halfHeight, z));
            points.add(new Vector(-halfLength, halfHeight, z));
            points.add(new Vector(halfLength, -halfHeight, z));
            points.add(new Vector(halfLength, halfHeight, z));
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateSurface(Set<Vector> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector(x, -halfHeight, z));
                points.add(new Vector(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector(-halfLength, y, z));
                points.add(new Vector(halfLength, y, z));
            }
        }
        for (double x = -halfLength + lengthStep; x < halfLength; x += lengthStep) {
            for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
                points.add(new Vector(x, y, -halfWidth));
                points.add(new Vector(x, y, halfWidth));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    @Contract(pure = true)
    public void generateFilled(Set<Vector> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double y = -halfHeight; y <= halfHeight; y += heightStep) {
                for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                    points.add(new Vector(x, y, z));
                }
            }
        }
    }

    @Override
    @Contract(pure = true)
    public void generatePoints(Set<Vector> points) {
        if (isDynamic) {
            assert negativeCorner != null;
            assert positiveCorner != null;
            Location negative = negativeCorner.getLocation();
            Location positive = positiveCorner.getLocation();
            this.halfLength = Math.abs(positive.getX() - negative.getX()) / 2;
            this.halfWidth = Math.abs(positive.getZ() - negative.getZ()) / 2;
            this.halfHeight = Math.abs(positive.getY() - negative.getY()) / 2;
            this.setLocation(new DynamicLocation(negative.clone().add(positive.subtract(negative).toVector().multiply(0.5))));
        }
        calculateSteps();
        super.generatePoints(points);
        points.forEach(vector -> vector.add(centerOffset));
    }

    // Ensure that the points are always needing to be updated if the start or end location is dynamic
    @Override
    public Set<Vector> getPoints(@NonNull @NotNull Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            this.setNeedsUpdate(true);
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(1, particleCount);
        this.setParticleDensity(switch (this.getStyle()) {
            case OUTLINE -> 8 * (halfLength + halfHeight + halfWidth) / particleCount;
            case SURFACE ->
                    Math.sqrt(8 * (halfLength * halfHeight + halfLength * halfWidth + halfHeight * halfWidth) / particleCount);
            case FILL -> Math.cbrt(8 * halfLength * halfHeight * halfWidth / particleCount);
        });
        calculateSteps();
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
        return halfHeight * 2;
    }

    @Override
    public void setHeight(double height) {
        this.halfHeight = Math.max(height / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        Cuboid cuboid;
        if (isDynamic) {
            assert negativeCorner != null;
            assert positiveCorner != null;
            cuboid = (new Cuboid(negativeCorner, positiveCorner));
        } else {
            cuboid = (new Cuboid(getLength(), getWidth(), getHeight()));
        }
        cuboid.isDynamic = isDynamic;
        return this.copyTo(cuboid);
    }

}
