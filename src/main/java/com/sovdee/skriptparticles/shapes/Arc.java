package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.Set;

/**
 * A circle with a cutoff angle. This creates an arc or a sector.
 * The cutoff angle is the angle between the start and end of the arc.
 * It will always be between 0 and 2 * PI.
 */
public class Arc extends Circle implements CutoffShape {

    /**
     * @param radius The radius of the arc. Must be greater than 0.
     * @param cutoffAngle The cutoff angle of the arc, in radians. Will be clamped to a value between 0 and 2 * PI.
     */
    public Arc(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    /**
     * @param radius The radius of the arc. Must be greater than 0.
     * @param height The height of the arc. Must be non-negative.
     * @param cutoffAngle The cutoff angle of the arc, in radians. Will be clamped to a value between 0 and 2 * PI.
     */
    public Arc(double radius, double height, double cutoffAngle) {
        super(radius, height);
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    @Contract(pure = true)
    public void generateSurface(Set<Vector> points) {
        generateFilled(points);
    }

    @Override
    public double getCutoffAngle() {
        return this.cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new Arc(this.getRadius(), this.getHeight(), cutoffAngle));
    }

    @Override
    public String toString() {
        return "Arc{" +
                "radius=" + this.getRadius() +
                ", cutoffAngle=" + cutoffAngle +
                ", height=" + this.getHeight() +
                '}';
    }
}
