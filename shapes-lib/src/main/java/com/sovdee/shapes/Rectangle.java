package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

/**
 * A rectangle shape, defined by a plane and dimensions.
 * For dynamic (entity-following) rectangles, use the plugin-side DynamicRectangle wrapper.
 */
public class Rectangle extends AbstractShape implements LWHShape {

    private Plane plane;
    private double halfLength;
    private double halfWidth;
    private double lengthStep = 1.0;
    private double widthStep = 1.0;
    private Vector3d centerOffset = new Vector3d(0, 0, 0);

    public Rectangle(double length, double width, Plane plane) {
        super();
        this.plane = plane;
        this.halfLength = Math.max(length / 2, MathUtil.EPSILON);
        this.halfWidth = Math.max(width / 2, MathUtil.EPSILON);
        calculateSteps();
    }

    public Rectangle(Vector3d cornerA, Vector3d cornerB, Plane plane) {
        super();
        if (cornerA.equals(cornerB))
            throw new IllegalArgumentException("Corners cannot be the same.");
        this.plane = plane;
        setLengthWidth(cornerA, cornerB);
        centerOffset = new Vector3d(cornerB).add(cornerA).mul(0.5);
        switch (plane) {
            case XZ -> centerOffset.y = 0;
            case XY -> centerOffset.z = 0;
            case YZ -> centerOffset.x = 0;
        }
        calculateSteps();
    }

    private void setLengthWidth(Vector3d cornerA, Vector3d cornerB) {
        double length = switch (plane) {
            case XZ, XY -> Math.abs(cornerA.x - cornerB.x);
            case YZ -> Math.abs(cornerA.y - cornerB.y);
        };
        double width = switch (plane) {
            case XZ, YZ -> Math.abs(cornerA.z - cornerB.z);
            case XY -> Math.abs(cornerA.y - cornerB.y);
        };
        this.halfWidth = Math.abs(width) / 2;
        this.halfLength = Math.abs(length) / 2;
    }

    private Vector3d vectorFromLengthWidth(double length, double width) {
        return switch (plane) {
            case XZ -> new Vector3d(length, 0, width);
            case XY -> new Vector3d(length, width, 0);
            case YZ -> new Vector3d(0, length, width);
        };
    }

    private void calculateSteps() {
        double particleDensity = this.getParticleDensity();
        lengthStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        widthStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        for (double l = -halfLength + widthStep; l < halfLength; l += widthStep) {
            points.add(vectorFromLengthWidth(l, -halfWidth));
            points.add(vectorFromLengthWidth(l, halfWidth));
        }
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            points.add(vectorFromLengthWidth(-halfLength, w));
            points.add(vectorFromLengthWidth(halfLength, w));
        }
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            for (double l = -halfLength; l <= halfLength; l += widthStep) {
                points.add(vectorFromLengthWidth(l, w));
            }
        }
    }

    @Override
    public void generatePoints(Set<Vector3d> points) {
        calculateSteps();
        super.generatePoints(points);
        points.forEach(vector -> vector.add(centerOffset));
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
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

    public Plane getPlane() { return plane; }

    public void setPlane(Plane plane) {
        this.plane = plane;
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        Rectangle rectangle = new Rectangle(this.getLength(), this.getWidth(), plane);
        rectangle.centerOffset = new Vector3d(this.centerOffset);
        return this.copyTo(rectangle);
    }

    @Override
    public String toString() {
        String axis = this.plane.toString().toLowerCase();
        return axis + " rectangle with length " + this.getLength() + " and width " + this.getWidth();
    }

    public enum Plane {
        XZ, XY, YZ
    }
}
