package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Ellipse extends AbstractShape implements LWHShape {

    protected double lengthRadius;
    protected double widthRadius;
    protected double height;
    protected double cutoffAngle;

    public Ellipse(double lengthRadius, double widthRadius) {
        this(lengthRadius, widthRadius, 0);
    }

    public Ellipse(double lengthRadius, double widthRadius, double height) {
        super();
        this.lengthRadius = lengthRadius;
        this.widthRadius = widthRadius;
        this.height = height;
        this.cutoffAngle = 2 * Math.PI;
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> ellipse = MathUtil.calculateEllipse(lengthRadius, widthRadius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(ellipse, height, particleDensity);
        return ellipse;
    }

    @Override
    public Set<Vector> generateSurface() {
        // if height is not 0, make it a cylinder
        if (height != 0) {
            return MathUtil.calculateCylinder(lengthRadius, widthRadius, height, particleDensity, cutoffAngle);
        }
        return MathUtil.calculateEllipticalDisc(lengthRadius, widthRadius, particleDensity, cutoffAngle);
    }

    @Override
    public Set<Vector> generateFilled() {
        Set<Vector> disc = MathUtil.calculateEllipticalDisc(lengthRadius, widthRadius, particleDensity, cutoffAngle);
        if (height != 0)
            return MathUtil.fillVertically(disc, height, particleDensity);
        return disc;
    }

    @Override
    public void setParticleCount(int particleCount) {
        switch (style) {
            case OUTLINE -> {
                // this is so fucking cringe
                double h = (lengthRadius - widthRadius) * (lengthRadius - widthRadius) / ((lengthRadius + widthRadius) + (lengthRadius + widthRadius));
                double circumferenceXY = Math.PI * (lengthRadius + widthRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.particleDensity = circumferenceXY / particleCount;
            }
            case SURFACE, FILL -> this.particleDensity = Math.sqrt((Math.PI * lengthRadius * widthRadius) / particleCount);
        }
    }

    @Override
    public double getLength() {
        return lengthRadius * 2;
    }

    @Override
    public double getWidth() {
        return widthRadius * 2;
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setLength(double length) {
        lengthRadius = Math.max(length / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        widthRadius = Math.max(width / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipse(lengthRadius, widthRadius, height));
    }
}
