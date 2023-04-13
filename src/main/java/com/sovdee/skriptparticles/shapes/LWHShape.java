package com.sovdee.skriptparticles.shapes;

public abstract class LWHShape extends RadialShape {
    abstract public double getLength();
    abstract public double getWidth();
    abstract public double getHeight();
    abstract public void setLength(double length);
    abstract public void setWidth(double width);
    abstract public void setHeight(double height);
}
