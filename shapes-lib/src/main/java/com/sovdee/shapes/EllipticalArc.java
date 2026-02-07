package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.Set;

public class EllipticalArc extends Ellipse implements CutoffShape {

    public EllipticalArc(double xRadius, double zRadius, double cutoffAngle) {
        this(xRadius, zRadius, 0, cutoffAngle);
    }

    public EllipticalArc(double xRadius, double zRadius, double height, double cutoffAngle) {
        super(xRadius, zRadius, height);
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        generateFilled(points);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = MathUtil.clamp(cutoffAngle, 0, Math.PI * 2);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new EllipticalArc(this.getLength(), this.getWidth(), this.getHeight(), cutoffAngle));
    }
}
