package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Arc extends AbstractShape implements RadialShape, CutoffShape {

    private double cutoffAngle;
    private double radius;

    public Arc(double radius, double cutoffAngle){
        super();
        this.radius = radius;
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Set<Vector> generateOutline() {
        return MathUtil.calculateCircle(this.radius, this.particleDensity, this.cutoffAngle);
    }

    @Override
    public Set<Vector> generateSurface() {
        return MathUtil.calculateDisc(this.radius, this.particleDensity, this.cutoffAngle);
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = switch (style){
            case OUTLINE -> cutoffAngle * radius / particleCount;
            case SURFACE, FILL -> Math.sqrt(0.5 * cutoffAngle * radius * radius / particleCount);
        };
        needsUpdate = true;
    }

    public double getCutoffAngle() {
        return cutoffAngle;
    }

    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, 2*Math.PI);
        needsUpdate = true;
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius,0);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        Arc arc = new Arc(this.radius, this.cutoffAngle);
        return this.copyTo(arc);
    }

    public String toString(){
        return style.toString() + " arc with radius " + this.radius + " and density " + this.particleDensity;
    }
}
