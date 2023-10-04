package com.sovdee.skriptparticles.shapes;

/**
 * Represents a shape that has a number of sides and a side length.
 * The number of sides must be greater than 2.
 * The side length must be greater than 0.
 */
public interface PolyShape extends Shape {

    /**
     * @return The number of sides of the shape.
     */
    int getSides();

    /**
     * Sets the number of sides of the shape.
     * @param sides The number of sides of the shape. Must be greater than 2.
     */
    void setSides(int sides);

    /**
     * @return The side length of the shape.
     */
    double getSideLength();

    /**
     * Sets the side length of the shape.
     * @param sideLength The side length of the shape. Must be greater than 0.
     */
    void setSideLength(double sideLength);
}
