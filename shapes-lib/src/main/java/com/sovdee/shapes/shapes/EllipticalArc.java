package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.List;

public class EllipticalArc extends Ellipse implements CutoffShape {

    public EllipticalArc(double xRadius, double zRadius, double cutoffAngle) {
        this(xRadius, zRadius, 0, cutoffAngle);
    }

    public EllipticalArc(double xRadius, double zRadius, double height, double cutoffAngle) {
        super(xRadius, zRadius, height);
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        generateFilled(points, density);
    }

    /**
     * The surface of an arc is drawn filled, so its density is estimated the same way.
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        return super.computeDensity(style == SamplingStyle.SURFACE ? SamplingStyle.FILL : style, targetPointCount);
    }

    @Override
    public double getCutoffAngle() {
        return cutoffAngle;
    }

    @Override
    public void setCutoffAngle(double cutoffAngle) {
        this.cutoffAngle = Math.clamp(cutoffAngle, 0, Math.PI * 2);
        invalidate();
    }

    @Override
    public boolean contains(Vector3d point) {
        if (!super.contains(point)) return false;
        double angle = Math.atan2(point.z, point.x);
        if (angle < 0) angle += 2 * Math.PI;
        return angle <= cutoffAngle;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new EllipticalArc(this.getLength() / 2, this.getWidth() / 2, this.getHeight(), cutoffAngle));
    }
}
