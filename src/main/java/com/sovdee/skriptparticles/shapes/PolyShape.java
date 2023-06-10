package com.sovdee.skriptparticles.shapes;

public interface PolyShape extends Shape {
    int getSides();

    void setSides(int sides);

    double getSideLength();

    void setSideLength(double sideLength);
}
