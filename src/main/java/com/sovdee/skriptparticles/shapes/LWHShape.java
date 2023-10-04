package com.sovdee.skriptparticles.shapes;

/**
 * Represents a shape that has a length, width, and/or height.
 * Neither the length, width, nor height may be negative.
 */
public interface LWHShape extends Shape {

    /**
     * @return The length of the shape.
     */
    double getLength();

    /**
     * Sets the length of the shape.
     * @param length The length of the shape. Must be non-negative.
     */
    void setLength(double length);

    /**
     * @return The width of the shape.
     */
    double getWidth();

    /**
     * Sets the width of the shape.
     * @param width The width of the shape. Must be non-negative.
     */
    void setWidth(double width);

    /**
     * @return The height of the shape.
     */
    double getHeight();

    /**
     * Sets the height of the shape.
     * @param height The height of the shape. Must be non-negative.
     */
    void setHeight(double height);
}
