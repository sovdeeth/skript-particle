package com.sovdee.skriptparticle.elements.shapes.types;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Helix extends Shape implements RadialShape {

    private double radius;
    private double height;
    private double slope;
    private int rotation = 1;

    public Helix(double radius, double height, double slope) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
    }

    public Helix(double radius, double height, double slope, int rotation) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
        this.rotation = rotation;
    }

    @Override
    public List<Vector> generateOutline() {
        points = new ArrayList<>();
        calculateHelix(radius);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        points = new ArrayList<>();
        for (double r = radius; r > 0; r -= particleDensity){
            calculateHelix(r);
        }
        return points;
    }

    private void calculateHelix(double radius) {
        if (radius <= 0 || height <= 0) {
            return;
        }
        double loops = Math.abs(height / slope);
        double length = slope * slope + radius * radius;
        double stepSize = particleDensity / length;
        for (double t = 0; t < loops; t += stepSize) {
            double x = radius * Math.cos(rotation * t);
            double z = radius * Math.sin(rotation * t);
            points.add(new Vector(x, t*slope, z));
        }
    }

    @Override
    public Shape particleCount(int count) {
        particleDensity = (Math.sqrt(slope * slope + radius * radius) * (height / slope) / count);
        return this;
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public Shape radius(double radius) {
        this.radius = radius;
        return this;
    }

    public double height() {
        return height;
    }

    public Shape height(double height) {
        this.height = height;
        return this;
    }

    public double slope() {
        return slope;
    }

    public Shape slope(double slope) {
        this.slope = slope;
        return this;
    }

    public int rotation() {
        return rotation;
    }

    public Shape rotation(int rotation) {
        this.rotation = rotation;
        return this;
    }

    @Override
    public Shape clone() {
        Helix clone = new Helix(radius, height, slope, rotation);
        this.copyTo(clone);
        return clone;
    }
}
