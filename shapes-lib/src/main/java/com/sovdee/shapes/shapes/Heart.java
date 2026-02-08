package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Heart extends AbstractShape implements LWHShape {

    private double length;
    private double width;
    private double eccentricity;

    public Heart(double length, double width, double eccentricity) {
        super();
        this.length = Math.max(length, Shape.EPSILON);
        this.width = Math.max(width, Shape.EPSILON);
        this.eccentricity = Math.max(eccentricity, 1);
    }

    private static Set<Vector3d> calculateHeart(double length, double width, double eccentricity, double particleDensity) {
        Set<Vector3d> points = new LinkedHashSet<>();
        double angleStep = 4 / 3.0 * particleDensity / (width + length);
        for (double theta = 0; theta < Math.PI * 2; theta += angleStep) {
            double x = width * Math.pow(Math.sin(theta), 3);
            double y = length * (Math.cos(theta) - 1 / eccentricity * Math.cos(2 * theta) - 1.0 / 6 * Math.cos(3 * theta) - 1.0 / 16 * Math.cos(4 * theta));
            points.add(new Vector3d(x, 0, y));
        }
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(calculateHeart(length / 2, width / 2, eccentricity, this.getParticleDensity()));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        for (double w = width, l = length; w > 0 && l > 0; w -= particleDensity * 1.5, l -= particleDensity * 1.5) {
            points.addAll(calculateHeart(l / 2, w / 2, eccentricity, particleDensity));
        }
    }

    @Override
    public void setParticleCount(int particleCount) { }

    @Override
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

    @Override
    public double getWidth() { return width; }

    @Override
    public void setWidth(double width) {
        this.width = Math.max(width, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() { return length; }

    @Override
    public void setLength(double length) {
        this.length = Math.max(length, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    public double getEccentricity() { return eccentricity; }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = Math.max(1, eccentricity);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Heart(length, width, eccentricity));
    }
}
