package com.sovdee.shapes.shapes;

/**
 * Represents a shape that has a radius.
 * The radius must be greater than 0.
 */
public interface RadialShape extends Shape {

    /**
     * Returns the radius of this shape.
     * The radius is always positive (implementations should enforce {@code radius > 0}).
     *
     * @return the radius, in the shape's local units
     */
    double getRadius();

    /**
     * Sets the radius of this shape. The radius must be greater than {@code 0}; values at or
     * below zero should be clamped or rejected by the implementing class.
     *
     * @param radius the new radius, in the shape's local units
     */
    void setRadius(double radius);
}
