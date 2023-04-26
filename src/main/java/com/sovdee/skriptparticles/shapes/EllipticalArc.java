package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.Set;

public class EllipticalArc extends Ellipse implements CutoffShape {

    public EllipticalArc(double lengthRadius, double widthRadius, double cutoffAngle) {
        this(lengthRadius, widthRadius, 0, cutoffAngle);
    }

    public EllipticalArc(double lengthRadius, double widthRadius, double height, double cutoffAngle) {
        super(lengthRadius, widthRadius, height);
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
        return this.copyTo(new EllipticalArc(lengthRadius, widthRadius, height, cutoffAngle));
    }
}
