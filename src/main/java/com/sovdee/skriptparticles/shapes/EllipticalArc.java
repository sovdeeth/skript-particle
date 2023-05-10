package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class EllipticalArc extends Ellipse implements CutoffShape {

    public EllipticalArc(double xRadius, double zRadius, double cutoffAngle) {
        this(xRadius, zRadius, 0, cutoffAngle);
    }

    public EllipticalArc(double xRadius, double zRadius, double height, double cutoffAngle) {
        super(xRadius, zRadius, height);
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public Set<Vector> generateSurface() {
        return generateFilled();
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
        return this.copyTo(new EllipticalArc(xRadius, zRadius, height, cutoffAngle));
    }
}
