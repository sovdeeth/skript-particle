package com.sovdee.skriptparticles.shapes;

/**
 * Represents a shape that has a cutoff angle, like an arc.
 */
public interface CutoffShape extends Shape {

    /**
     * Gets the cutoff angle of the shape, or the angle at which the shape will stop generating particles.
     *
     * @return The cutoff angle of the shape in radians, between 0 and 2 * PI.
     */
    double getCutoffAngle();

    /**
     * Sets the cutoff angle of the shape, or the angle at which the shape will stop generating particles.
     *
     * @param cutoffAngle The cutoff angle of the shape, in radians. Will be converted to a value between 0 and 2 * PI.
     */
    void setCutoffAngle(double cutoffAngle);

}
