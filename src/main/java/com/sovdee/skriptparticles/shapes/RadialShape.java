package com.sovdee.skriptparticles.shapes;

/**
 * Represents a shape that has a radius.
 * The radius must be greater than 0.
 */
public interface RadialShape extends Shape {

    /**
     * Gets the radius of the shape.
     * @return The radius of the shape.
     */
    double getRadius();

    /**
     * Sets the radius of the shape.
     * @param radius The radius of the shape. Must be greater than 0.
     */
    void setRadius(double radius);

}
