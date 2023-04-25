package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;

public class SphericalCap extends Sphere implements CutoffShape {

    public SphericalCap(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = cutoffAngle;
        this.cutoffAngleCos = Math.cos(cutoffAngle);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new SphericalCap(radius, cutoffAngle));
    }
}
