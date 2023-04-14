package com.sovdee.skriptparticles.shapes;

public interface CutoffShape extends Shape {

    /*
     * Gets the cutoff angle of the shape, or the angle at which the shape will stop generating particles
     * In radians
     * @return The cutoff angle of the shape
     */
    double getCutoffAngle();

    /*
     * Sets the cutoff angle of the shape, or the angle at which the shape will stop generating particles
     * In radians
     * @param cutoffAngle The cutoff angle of the shape
     */
    void setCutoffAngle(double cutoffAngle);

}
