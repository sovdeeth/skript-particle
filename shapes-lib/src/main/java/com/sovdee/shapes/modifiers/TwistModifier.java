package com.sovdee.shapes.modifiers;

/**
 * Rotates each point in the plane perpendicular to {@code axis} by an angle proportional to
 * its normalised position along that axis. A {@code totalAngle} of 2π produces one full twist.
 * <ul>
 *   <li>Y axis (default): rotates in the XZ plane</li>
 *   <li>X axis: rotates in the YZ plane</li>
 *   <li>Z axis: rotates in the XY plane</li>
 *   <li>T axis: rotates in the XZ plane, driven by normalised draw order</li>
 * </ul>
 */
public class TwistModifier extends PointModifier.PreRenderPointModifier {

    private double totalAngle;
    private SampleAxis axis;
    private EasingFunction easing = EasingFunction.LINEAR;

    public TwistModifier(double totalAngle) {
        this(totalAngle, SampleAxis.Y);
    }

    public TwistModifier(double totalAngle, SampleAxis axis) {
        this.totalAngle = totalAngle;
        this.axis = axis;
    }

    @Override
    public void modify(PointContext point) {
        double t = easing.apply(axis.sampleNormalized(point));
        double angle = totalAngle * t;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        switch (axis) {
            case X -> { // rotate YZ plane
                double newY = point.y * cos - point.z * sin;
                double newZ = point.y * sin + point.z * cos;
                point.y = newY;
                point.z = newZ;
            }
            case Z -> { // rotate XY plane
                double newX = point.x * cos - point.y * sin;
                double newY = point.x * sin + point.y * cos;
                point.x = newX;
                point.y = newY;
            }
            default -> { // Y and T: rotate XZ plane
                double newX = point.x * cos - point.z * sin;
                double newZ = point.x * sin + point.z * cos;
                point.x = newX;
                point.z = newZ;
            }
        }
    }

    @Override
    public int modifierHash() {
        int result = Double.hashCode(totalAngle);
        result = 31 * result + axis.hashCode();
        result = 31 * result + easing.easingHash();
        return result;
    }

    public double getTotalAngle() { return totalAngle; }
    public void setTotalAngle(double totalAngle) { this.totalAngle = totalAngle; }

    public SampleAxis getAxis() { return axis; }
    public void setAxis(SampleAxis axis) { this.axis = axis; }

    public EasingFunction getEasing() { return easing; }
    public void setEasing(EasingFunction easing) { this.easing = easing; }

    @Override
    public TwistModifier clone() {
        return (TwistModifier) super.clone();
    }
}
