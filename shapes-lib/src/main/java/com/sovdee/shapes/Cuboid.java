package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;
import java.util.function.Supplier;

/**
 * A cuboid shape, defined either by dimensions or by two corner vectors.
 * For dynamic (entity-following) cuboids, use the plugin-side DynamicCuboid wrapper.
 */
public class Cuboid extends AbstractShape implements LWHShape {

    private double halfLength, halfWidth, halfHeight;
    private double lengthStep, widthStep, heightStep;
    private Vector3d centerOffset = new Vector3d(0, 0, 0);
    private Supplier<Vector3d> cornerASupplier;
    private Supplier<Vector3d> cornerBSupplier;

    public Cuboid(double length, double width, double height) {
        super();
        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.halfHeight = Math.max(height / 2, MathUtil.EPSILON);
        calculateSteps();
    }

    public Cuboid(Vector3d cornerA, Vector3d cornerB) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Cuboid corners cannot be equal.");
        this.halfLength = Math.abs(cornerB.x - cornerA.x) / 2;
        this.halfWidth = Math.abs(cornerB.z - cornerA.z) / 2;
        this.halfHeight = Math.abs(cornerB.y - cornerA.y) / 2;
        centerOffset = new Vector3d(cornerB).add(cornerA).mul(0.5);
        calculateSteps();
    }

    public Cuboid(Supplier<Vector3d> cornerA, Supplier<Vector3d> cornerB) {
        super();
        this.cornerASupplier = cornerA;
        this.cornerBSupplier = cornerB;
        Vector3d a = cornerA.get();
        Vector3d b = cornerB.get();
        this.halfLength = Math.max(Math.abs(b.x - a.x) / 2, MathUtil.EPSILON);
        this.halfWidth = Math.max(Math.abs(b.z - a.z) / 2, MathUtil.EPSILON);
        this.halfHeight = Math.max(Math.abs(b.y - a.y) / 2, MathUtil.EPSILON);
        calculateSteps();
        setDynamic(true);
    }

    private void calculateSteps() {
        widthStep = 2 * halfWidth / Math.round(2 * halfWidth / this.getParticleDensity());
        lengthStep = 2 * halfLength / Math.round(2 * halfLength / this.getParticleDensity());
        heightStep = 2 * halfHeight / Math.round(2 * halfHeight / this.getParticleDensity());
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            points.add(new Vector3d(x, -halfHeight, -halfWidth));
            points.add(new Vector3d(x, -halfHeight, halfWidth));
            points.add(new Vector3d(x, halfHeight, -halfWidth));
            points.add(new Vector3d(x, halfHeight, halfWidth));
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            points.add(new Vector3d(-halfLength, y, -halfWidth));
            points.add(new Vector3d(-halfLength, y, halfWidth));
            points.add(new Vector3d(halfLength, y, -halfWidth));
            points.add(new Vector3d(halfLength, y, halfWidth));
        }
        for (double z = -halfWidth + widthStep; z < halfWidth; z += widthStep) {
            points.add(new Vector3d(-halfLength, -halfHeight, z));
            points.add(new Vector3d(-halfLength, halfHeight, z));
            points.add(new Vector3d(halfLength, -halfHeight, z));
            points.add(new Vector3d(halfLength, halfHeight, z));
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector3d(x, -halfHeight, z));
                points.add(new Vector3d(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector3d(-halfLength, y, z));
                points.add(new Vector3d(halfLength, y, z));
            }
        }
        for (double x = -halfLength + lengthStep; x < halfLength; x += lengthStep) {
            for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
                points.add(new Vector3d(x, y, -halfWidth));
                points.add(new Vector3d(x, y, halfWidth));
            }
        }
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double y = -halfHeight; y <= halfHeight; y += heightStep) {
                for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                    points.add(new Vector3d(x, y, z));
                }
            }
        }
    }

    @Override
    public void generatePoints(Set<Vector3d> points) {
        if (cornerASupplier != null && cornerBSupplier != null) {
            Vector3d a = cornerASupplier.get();
            Vector3d b = cornerBSupplier.get();
            this.halfLength = Math.max(Math.abs(b.x - a.x) / 2, MathUtil.EPSILON);
            this.halfWidth = Math.max(Math.abs(b.z - a.z) / 2, MathUtil.EPSILON);
            this.halfHeight = Math.max(Math.abs(b.y - a.y) / 2, MathUtil.EPSILON);
        }
        calculateSteps();
        super.generatePoints(points);
        points.forEach(vector -> vector.add(centerOffset));
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
    public double getLength() { return halfLength * 2; }

    @Override
    public void setLength(double length) {
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() { return halfWidth * 2; }

    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() { return halfHeight * 2; }

    @Override
    public void setHeight(double height) {
        this.halfHeight = Math.max(height / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    public Supplier<Vector3d> getCornerASupplier() { return cornerASupplier; }
    public Supplier<Vector3d> getCornerBSupplier() { return cornerBSupplier; }

    @Override
    public Shape clone() {
        Cuboid cuboid;
        if (cornerASupplier != null && cornerBSupplier != null) {
            cuboid = new Cuboid(cornerASupplier, cornerBSupplier);
        } else {
            cuboid = new Cuboid(getLength(), getWidth(), getHeight());
        }
        cuboid.centerOffset = new Vector3d(this.centerOffset);
        return this.copyTo(cuboid);
    }
}
