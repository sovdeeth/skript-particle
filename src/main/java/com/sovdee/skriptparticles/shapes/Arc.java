package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class Arc extends Circle implements CutoffShape {

    public Arc(double radius, double cutoffAngle) {
        super(radius);
        this.cutoffAngle = cutoffAngle;
    }

    public Arc(double radius, double height, double cutoffAngle) {
        super(radius, height);
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Set<Vector> generateSurface() {
        return generateFilled();
    }

    @Override
    public double getCutoffAngle() {
        return this.cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, 2 * Math.PI);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Arc(radius, height, cutoffAngle));
    }
}
