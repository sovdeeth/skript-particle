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
    public boolean contains(Vector3d point) {
        if (!super.contains(point)) return false;
        // Check if within the cap's polar angle
        double y = point.y / getRadius();
        return y >= cutoffAngleCos;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new SphericalCap(this.getRadius(), cutoffAngle));
    }
}
