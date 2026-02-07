package com.sovdee.shapes;

/**
 * Represents a shape that has a cutoff angle, like an arc.
 */
public interface CutoffShape extends Shape {
    double getCutoffAngle();
    void setCutoffAngle(double cutoffAngle);
}
