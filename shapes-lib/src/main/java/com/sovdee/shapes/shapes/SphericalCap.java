package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

public class SphericalCap extends Sphere implements CutoffShape {

    public SphericalCap(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
        invalidate();
    }

    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        if (!super.containsLocal(point, tolerance)) return false;
        // The solid form of a cap is a spherical sector: everything within the cutoff angle of the y-axis.
        double distance = point.length();
        if (distance <= tolerance) return true;
        return point.y >= distance * cutoffAngleCos - tolerance;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new SphericalCap(this.getRadius(), cutoffAngle));
    }
}
