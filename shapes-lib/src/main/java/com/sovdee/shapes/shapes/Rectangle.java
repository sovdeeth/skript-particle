package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.Set;
import java.util.function.Supplier;

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
    private Supplier<Vector3d> cornerASupplier;
    private Supplier<Vector3d> cornerBSupplier;

    public Rectangle(double length, double width, Plane plane) {
        super();
        this.plane = plane;
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
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
    }

    public Rectangle(Supplier<Vector3d> cornerA, Supplier<Vector3d> cornerB, Plane plane) {
        super();
        this.plane = plane;
        this.cornerASupplier = cornerA;
        this.cornerBSupplier = cornerB;
        Vector3d a = cornerA.get();
        Vector3d b = cornerB.get();
        setLengthWidth(a, b);
        setDynamic(true);
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

    private void calculateSteps(double density) {
        lengthStep = 2 * halfWidth / Math.round(2 * halfWidth / density);
        widthStep = 2 * halfLength / Math.round(2 * halfLength / density);
    }

    @Override
    public void beforeSampling(double density) {
        if (cornerASupplier != null && cornerBSupplier != null) {
            Vector3d a = cornerASupplier.get();
            Vector3d b = cornerBSupplier.get();
            setLengthWidth(a, b);
        }
        calculateSteps(density);
    }

    @Override
    public void afterSampling(Set<Vector3d> points) {
        points.forEach(vector -> vector.add(centerOffset));
    }

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
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
    public void generateSurface(Set<Vector3d> points, double density) {
        for (double w = -halfWidth; w <= halfWidth; w += lengthStep) {
            for (double l = -halfLength; l <= halfLength; l += widthStep) {
                points.add(vectorFromLengthWidth(l, w));
            }
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return switch (style) {
            case FILL, SURFACE -> Math.sqrt(4 * halfWidth * halfLength / count);
            case OUTLINE -> 4 * (halfWidth + halfLength) / count;
        };
    }

    @Override
    public boolean contains(Vector3d point) {
        return switch (plane) {
            case XZ -> Math.abs(point.x) <= halfLength && Math.abs(point.z) <= halfWidth && Math.abs(point.y) < EPSILON;
            case XY -> Math.abs(point.x) <= halfLength && Math.abs(point.y) <= halfWidth && Math.abs(point.z) < EPSILON;
            case YZ -> Math.abs(point.y) <= halfLength && Math.abs(point.z) <= halfWidth && Math.abs(point.x) < EPSILON;
        };
    }

    @Override
    public double getLength() { return halfLength * 2; }

    @Override
    public void setLength(double length) {
        this.halfLength = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getWidth() { return halfWidth * 2; }

    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

    public Plane getPlane() { return plane; }

    public void setPlane(Plane plane) {
        this.plane = plane;
        invalidate();
    }

    public Supplier<Vector3d> getCornerASupplier() { return cornerASupplier; }
    public Supplier<Vector3d> getCornerBSupplier() { return cornerBSupplier; }

    @Override
    public Shape clone() {
        Rectangle rectangle;
        if (cornerASupplier != null && cornerBSupplier != null) {
            rectangle = new Rectangle(cornerASupplier, cornerBSupplier, plane);
        } else {
            rectangle = new Rectangle(this.getLength(), this.getWidth(), plane);
        }
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
