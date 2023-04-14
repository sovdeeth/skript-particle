package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Arc extends RadialShape {

    private double cutoffAngle;

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
        this.cutoffAngle = cutoffAngle;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        Arc arc = new Arc(this.radius, this.cutoffAngle);
        return this.copyTo(arc);
    }
}
