package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.jetbrains.annotations.Contract;

/**
 * A spherical cap is a sphere with a portion of it cut off.
 * The cutoff angle is the angle between the center of the sphere and the cutoff plane.
 */
public class SphericalCap extends Sphere implements CutoffShape {

    /**
     * Generates a spherical cap with the given radius and cutoff angle.
     * @param radius the radius of the sphere. Must be greater than 0.
     * @param cutoffAngle the angle in radians between the center of the sphere and the cutoff plane. Will be clamped to be between 0 and pi.
     */
    public SphericalCap(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI);
        this.cutoffAngleCos = Math.cos(this.cutoffAngle);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new SphericalCap(this.getRadius(), cutoffAngle));
    }
}
