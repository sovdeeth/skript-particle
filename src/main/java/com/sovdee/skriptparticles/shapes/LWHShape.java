package com.sovdee.skriptparticles.shapes;

public interface LWHShape extends Shape {

    /*
     * Gets the length of the shape
     * @return The length of the shape
     */
    double getLength();

    /*
     * Gets the width of the shape
     * @return The width of the shape
     */
    double getWidth();

    /*
     * Gets the height of the shape
     * @return The height of the shape
     */
    double getHeight();

    /*
     * Sets the length of the shape
     * @param length The length of the shape
     */
    void setLength(double length);

    /*
     * Sets the width of the shape
     * @param width The width of the shape
     */
    void setWidth(double width);

    /*
     * Sets the height of the shape
     * @param height The height of the shape
     */
    void setHeight(double height);
}
