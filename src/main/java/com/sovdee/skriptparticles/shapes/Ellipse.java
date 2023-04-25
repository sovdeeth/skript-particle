package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Ellipse extends AbstractShape implements LWHShape, CutoffShape {

    private double lRadius;
    private double wRadius;
    private double height;
    private double cutoffAngle;

    public Ellipse(double lRadius, double wRadius) {
        this(lRadius, wRadius, 0, 2 * Math.PI);
    }

    public Ellipse(double lRadius, double wRadius, double height) {
        this(lRadius, wRadius, height, 2 * Math.PI);
    }

    public Ellipse(double lRadius, double wRadius, double height, double cutoffAngle) {
        super();
        this.lRadius = lRadius;
        this.wRadius = wRadius;
        this.height = height;
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Set<Vector> generateOutline() {
        if (height != 0) {
            return generateSurface();
        }
        return MathUtil.calculateEllipse(lRadius, wRadius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateSurface() {
        // if height is not 0, make it a cylinder
        if (height != 0) {
            return MathUtil.calculateCylinder(lRadius, wRadius, height, particleDensity, cutoffAngle);
        }
        return MathUtil.calculateEllipticalDisc(lRadius, wRadius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateFilled() {
        // if height is 0, revert to surface
        if (height == 0)
            return generateSurface();
        // otherwise, make a solid cylinder
        Set<Vector> disc = MathUtil.calculateEllipticalDisc(lRadius, wRadius, particleDensity, cutoffAngle);
        Set<Vector> points = new HashSet<>(disc);
        double heightStep = height / Math.round(height / particleDensity);
        for (double i = 0; i < height; i += heightStep) {
            for (Vector vector : disc) {
                points.add(vector.clone().setY(i));
            }
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        switch (style) {
            case OUTLINE -> {
                // this is so fucking cringe
                double h = (lRadius - wRadius) * (lRadius - wRadius) / ((lRadius + wRadius) + (lRadius + wRadius));
                double circumferenceXY = Math.PI * (lRadius + wRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.particleDensity = circumferenceXY / particleCount;
            }
            case SURFACE, FILL -> this.particleDensity = Math.sqrt((Math.PI * lRadius * wRadius) / particleCount);
        }
    }

    @Override
    public double getLength() {
        return lRadius * 2;
    }

    @Override
    public double getWidth() {
        return wRadius * 2;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {
        lRadius = Math.max(length / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        wRadius = Math.max(width / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        needsUpdate = true;
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, 2*Math.PI);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(lRadius, wRadius, height, cutoffAngle));
    }
}
