package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

public class Ellipse extends AbstractShape implements LWHShape {

    private double xRadius;
    private double zRadius;
    private double height;
    protected double cutoffAngle;

    public Ellipse(double xRadius, double zRadius) {
        this(xRadius, zRadius, 0);
    }

    public Ellipse(double xRadius, double zRadius, double height) {
        super();
        this.xRadius = Math.max(xRadius, MathUtil.EPSILON);
        this.zRadius = Math.max(zRadius, MathUtil.EPSILON);
        this.height = Math.max(height, 0);
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        Set<Vector3d> ellipse = new LinkedHashSet<>(MathUtil.calculateEllipse(xRadius, zRadius, this.getParticleDensity(), cutoffAngle));
        if (height != 0)
            points.addAll(MathUtil.fillVertically(ellipse, height, this.getParticleDensity()));
        else
            points.addAll(ellipse);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        if (height != 0)
            points.addAll(MathUtil.calculateCylinder(xRadius, zRadius, height, this.getParticleDensity(), cutoffAngle));
        else
            points.addAll(MathUtil.calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        Set<Vector3d> disc = MathUtil.calculateEllipticalDisc(xRadius, zRadius, this.getParticleDensity(), cutoffAngle);
        if (height != 0)
            points.addAll(MathUtil.fillVertically(disc, height, this.getParticleDensity()));
        else
            points.addAll(disc);
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        switch (this.getStyle()) {
            case OUTLINE -> {
                double h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXY = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.setParticleDensity(circumferenceXY / particleCount);
            }
            case SURFACE, FILL -> this.setParticleDensity(Math.sqrt((Math.PI * xRadius * zRadius) / particleCount));
        }
    }

    @Override
    public double getLength() { return xRadius * 2; }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() { return zRadius * 2; }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
