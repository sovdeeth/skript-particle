package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * A shape that is a section of an ellipse. An arc, but an ellipse instead of a circle.
 * The radii must be greater than 0, and the height must be non-negative.
 * The cutoff angle will always be between 0 and 2π.
 */
public class EllipticalArc extends Ellipse implements CutoffShape {

    /**
     * Creates an elliptical arc with the given x radius, z radius, and cutoff angle.
     * The height will be 0.
     *
     * @param xRadius the x radius. Must be greater than 0.
     * @param zRadius the z radius. Must be greater than 0.
     * @param cutoffAngle the cutoff angle, in radians. will be clamped to between 0 and 2π.
     */
    public EllipticalArc(double xRadius, double zRadius, double cutoffAngle) {
        this(xRadius, zRadius, 0, cutoffAngle);
    }

    /**
     * Creates an elliptical arc with the given x radius, z radius, height, and cutoff angle.
     *
     * @param xRadius the x radius. Must be greater than 0.
     * @param zRadius the z radius. Must be greater than 0.
     * @param height the height. Must be non-negative.
     * @param cutoffAngle the cutoff angle, in radians. will be clamped to between 0 and 2π.
     */
    public EllipticalArc(double xRadius, double zRadius, double height, double cutoffAngle) {
        super(xRadius, zRadius, height);
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        return generateFilled();
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new EllipticalArc(this.getLength(), this.getWidth(), this.getHeight(), cutoffAngle));
    }
}
