package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Heart extends AbstractShape implements LWHShape {

    private double height;
    private double width;
    private double eccentricity;

    public Heart(double height, double width, double eccentricity) {
        this.height = height;
        this.width = width;
        this.eccentricity = eccentricity;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateHeart(height/2, width/2, eccentricity, particleDensity);
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        double widthStep = width / particleDensity;
        double heightStep = height / particleDensity;
        for (double w = width, h = height; w > 0; w -= widthStep, h -= heightStep){
            points.addAll(MathUtil.calculateHeart(h/2, w/2, eccentricity, particleDensity));
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {}

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public double getWidth() {
        return width;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {}

    @Override
    public void setWidth(double width) {
        this.width = width;
        needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
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
        return this.copyTo(new Heart(height, width, eccentricity));
    }
}
