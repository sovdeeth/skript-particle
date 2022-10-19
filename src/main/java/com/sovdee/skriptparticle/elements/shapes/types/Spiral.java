package com.sovdee.skriptparticle.elements.shapes.types;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Spiral extends Shape implements RadialShape {

    private double radius;
    private double height;
    private double slope;

    @Override
    public List<Vector> generateOutline() {
        points = new ArrayList<>();
        calculateHelix(radius);
        return null;
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
        double stepSize = Math.sqrt(slope * slope + radius * radius) * height / particleDensity;
        for (int t = 0; t < height; t += stepSize) {
            double x = radius * Math.cos(t);
            double z = radius * Math.sin(t);
            points.add(new Vector(x, t, z));
        }
    }

    @Override
    public Shape particleCount(int count) {
        return null;
    }

    @Override
    public double radius() {
        return 0;
    }

    @Override
    public Shape radius(double radius) {
        return null;
    }

    @Override
    public Shape clone() {
        return null;
    }
}
