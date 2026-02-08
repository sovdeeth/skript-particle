package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.Set;

public class EllipticalArc extends Ellipse implements CutoffShape {

    public EllipticalArc(double xRadius, double zRadius, double cutoffAngle) {
        this(xRadius, zRadius, 0, cutoffAngle);
    }

    public EllipticalArc(double xRadius, double zRadius, double height, double cutoffAngle) {
        super(xRadius, zRadius, height);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        generateFilled(points, density);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
        invalidate();
    }

    @Override
    public boolean contains(Vector3d point) {
        if (!super.contains(point)) return false;
        double angle = Math.atan2(point.z, point.x);
        if (angle < 0) angle += 2 * Math.PI;
        return angle <= cutoffAngle;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new EllipticalArc(this.getLength() / 2, this.getWidth() / 2, this.getHeight(), cutoffAngle));
    }
}
