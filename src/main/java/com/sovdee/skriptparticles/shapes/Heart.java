package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Heart extends AbstractShape implements LWHShape {

    private double length;
    private double width;
    private double eccentricity;

    public Heart(double height, double width, double eccentricity) {
        this.length = height;
        this.width = width;
        this.eccentricity = eccentricity;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateHeart(length /2, width/2, eccentricity, particleDensity);
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        for (double w = width, h = length; w > 0 && h > 0; w -= particleDensity * 1.5, h -= particleDensity * 1.5){
            points.addAll(MathUtil.calculateHeart(h/2, w/2, eccentricity, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {}

    @Override
    public double getHeight() {
        return 0;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getLength() {
        return length;
    }

    @Override
    public void setHeight(double length) {}

    @Override
    public void setWidth(double width) {
        this.width = width;
        needsUpdate = true;
    }

    @Override
    public void setLength(double length) {
        this.length = length;
        needsUpdate = true;
    }

    public double getEccentricity() {
        return eccentricity;
    }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = eccentricity;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Heart(length, width, eccentricity));
    }
}
