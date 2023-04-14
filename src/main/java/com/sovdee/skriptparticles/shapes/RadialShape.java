package com.sovdee.skriptparticles.shapes;

public abstract class RadialShape extends Shape {
    protected double radius;

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        needsUpdate = true;
    }
}
