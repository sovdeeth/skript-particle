package com.sovdee.skriptparticles.shapes;

public interface PolyShape extends Shape {
    void setSides(int sides);
    int getSides();

    void setSideLength(double sideLength);
    double getSideLength();
}
