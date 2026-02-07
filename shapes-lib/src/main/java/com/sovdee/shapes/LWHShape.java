package com.sovdee.shapes;

/**
 * Represents a shape that has a length, width, and/or height.
 */
public interface LWHShape extends Shape {
    double getLength();
    void setLength(double length);
    double getWidth();
    void setWidth(double width);
    double getHeight();
    void setHeight(double height);
}
