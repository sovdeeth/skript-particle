package com.sovdee.shapes;

/**
 * Represents a shape that has a number of sides and a side length.
 */
public interface PolyShape extends Shape {
    int getSides();
    void setSides(int sides);
    double getSideLength();
    void setSideLength(double sideLength);
}
