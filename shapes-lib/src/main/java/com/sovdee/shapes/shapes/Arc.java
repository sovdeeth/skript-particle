package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.Set;

public class Arc extends Circle implements CutoffShape {

    public Arc(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    public Arc(double radius, double height, double cutoffAngle) {
        super(radius, height);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        generateFilled(points);
    }

    @Override
    public double getCutoffAngle() {
        return this.cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Arc(this.getRadius(), this.getHeight(), cutoffAngle));
    }

    @Override
    public String toString() {
        return "Arc{radius=" + this.getRadius() + ", cutoffAngle=" + cutoffAngle + ", height=" + this.getHeight() + '}';
    }
}
