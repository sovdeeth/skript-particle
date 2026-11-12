package com.sovdee.shapes.shapes;

/**
 * Represents a shape that has a length, width, and/or height.
 */
public interface LWHShape extends Shape {

    /**
     * Returns the length of this shape along its primary axis (typically X).
     * Not all implementors use all three dimensions; those that do not use length may return {@code 0}.
     *
     * @return the length, in the shape's local units
     */
    double getLength();

    /**
     * Sets the length of this shape along its primary axis.
     *
     * @param length the new length, in the shape's local units
     */
    void setLength(double length);

    /**
     * Returns the width of this shape (typically along the Z axis).
     * Not all implementors use all three dimensions; those that do not use width may return {@code 0}.
     *
     * @return the width, in the shape's local units
     */
    double getWidth();

    /**
     * Sets the width of this shape.
     *
     * @param width the new width, in the shape's local units
     */
    void setWidth(double width);

    /**
     * Returns the height of this shape (typically along the Y axis).
     * A height of {@code 0} collapses the shape to a flat, 2-D cross-section.
     *
     * @return the height, in the shape's local units
     */
    double getHeight();

    /**
     * Sets the height of this shape. A value of {@code 0} collapses the shape to a 2-D cross-section.
     *
     * @param height the new height, in the shape's local units; must be {@code >= 0}
     */
    void setHeight(double height);
}
