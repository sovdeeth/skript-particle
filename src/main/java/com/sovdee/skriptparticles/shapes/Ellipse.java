package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Ellipse extends AbstractShape implements LWHShape {

    protected double xRadius;
    protected double zRadius;
    protected double height;
    protected double cutoffAngle;

    public Ellipse(double xRadius, double zRadius) {
        this(xRadius, zRadius, 0);
    }

    public Ellipse(double xRadius, double zRadius, double height) {
        super();
        this.xRadius = xRadius;
        this.zRadius = zRadius;
        this.height = height;
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> ellipse = MathUtil.calculateEllipse(xRadius, zRadius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(ellipse, height, particleDensity);
        return ellipse;
    }

    @Override
    public Set<Vector> generateSurface() {
        // if height is not 0, make it a cylinder
        if (height != 0) {
            return MathUtil.calculateCylinder(xRadius, zRadius, height, particleDensity, cutoffAngle);
        }
        return MathUtil.calculateEllipticalDisc(xRadius, zRadius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateFilled() {
        Set<Vector> disc = MathUtil.calculateEllipticalDisc(xRadius, zRadius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(disc, height, particleDensity);
        return disc;
    }

    @Override
    public void setParticleCount(int particleCount) {
        switch (style) {
            case OUTLINE -> {
                // this is so fucking cringe
                double h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXY = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.particleDensity = circumferenceXY / particleCount;
            }
            case SURFACE, FILL -> this.particleDensity = Math.sqrt((Math.PI * xRadius * zRadius) / particleCount);
        }
    }

    @Override
    public double getLength() {
        return xRadius * 2;
    }

    @Override
    public double getWidth() {
        return zRadius * 2;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(xRadius, zRadius, height));
    }
}
