package com.sovdee.shapes.shapes;

/**
 * Represents a shape that has a number of sides and a side length.
 */
public interface PolyShape extends Shape {

    /**
     * Returns the number of sides (faces or edges) of this polygonal shape.
     * Must be at least {@code 3} for a valid polygon.
     *
     * @return the number of sides
     */
    int getSides();

    /**
     * Sets the number of sides for this polygonal shape.
     * Values below the minimum supported by the implementation should be clamped or rejected.
     *
     * @param sides the new number of sides; must be {@code >= 3}
     */
    void setSides(int sides);

    /**
     * Returns the length of each side of this polygonal shape, in local units.
     * The circumradius of a regular n-gon with side length {@code s} is
     * {@code r = s / (2 * sin(π / n))}.
     *
     * @return the side length, in local units
     */
    double getSideLength();

    /**
     * Sets the side length for this polygonal shape.
     * Changing the side length typically invalidates cached geometry.
     *
     * @param sideLength the new side length, in local units; must be {@code > 0}
     */
    void setSideLength(double sideLength);
}
