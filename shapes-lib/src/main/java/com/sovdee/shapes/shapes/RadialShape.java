package com.sovdee.shapes.shapes;

/**
 * Represents a shape that has a radius.
 * The radius must be greater than 0.
 */
public interface RadialShape extends Shape {
    double getRadius();
    void setRadius(double radius);
}
