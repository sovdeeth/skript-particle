package com.sovdee.shapes.shapes;

/**
 * Represents a shape that has a cutoff angle, like an arc.
 */
public interface CutoffShape extends Shape {

    /**
     * Returns the angular cutoff that limits how much of the shape's circumference is generated.
     * The value is in radians in the range {@code [0, 2π]}, where {@code 2π} produces the full shape
     * and smaller values truncate it to a partial arc or wedge.
     *
     * @return the cutoff angle in radians
     */
    double getCutoffAngle();

    /**
     * Sets the angular cutoff for this shape. Values are clamped to {@code [0, 2π]} by implementations.
     * A value of {@code 2π} restores the full shape; a value of {@code 0} produces no points.
     *
     * @param cutoffAngle the new cutoff angle in radians; clamped to {@code [0, 2π]}
     */
    void setCutoffAngle(double cutoffAngle);
}
