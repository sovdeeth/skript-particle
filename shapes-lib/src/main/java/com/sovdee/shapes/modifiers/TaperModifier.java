package com.sovdee.shapes.modifiers;

/**
 * Scales the two axes perpendicular to {@code axis} by a value that linearly interpolates
 * from {@code startScale} (axis min) to {@code endScale} (axis max) based on the normalised
 * position along that axis.
 * <ul>
 *   <li>Y axis (default): scales X and Z</li>
 *   <li>X axis: scales Y and Z</li>
 *   <li>Z axis: scales X and Y</li>
 *   <li>T axis: scales X and Z, driven by normalised draw order</li>
 * </ul>
 */
public class TaperModifier extends PointModifier.PreRenderPointModifier {

    private double startScale;
    private double endScale;
    private SampleAxis axis;
    private EasingFunction easing = EasingFunction.LINEAR;

    public TaperModifier(double startScale, double endScale) {
        this(startScale, endScale, SampleAxis.Y);
    }

    public TaperModifier(double startScale, double endScale, SampleAxis axis) {
        this.startScale = startScale;
        this.endScale = endScale;
        this.axis = axis;
    }

    @Override
    public void modify(PointContext point) {
        double t = easing.apply(axis.sampleNormalized(point));
        double scale = startScale + t * (endScale - startScale);
        switch (axis) {
            case X -> { point.y *= scale; point.z *= scale; }
            case Z -> { point.x *= scale; point.y *= scale; }
            default -> { point.x *= scale; point.z *= scale; } // Y and T: scale XZ plane
        }
    }

    @Override
    public int modifierHash() {
        int result = Double.hashCode(startScale);
        result = 31 * result + Double.hashCode(endScale);
        result = 31 * result + axis.hashCode();
        result = 31 * result + easing.easingHash();
        return result;
    }

    public double getStartScale() { return startScale; }
    public void setStartScale(double startScale) { this.startScale = startScale; }

    public double getEndScale() { return endScale; }
    public void setEndScale(double endScale) { this.endScale = endScale; }

    public SampleAxis getAxis() { return axis; }
    public void setAxis(SampleAxis axis) { this.axis = axis; }

    public EasingFunction getEasing() { return easing; }
    public void setEasing(EasingFunction easing) { this.easing = easing; }

    @Override
    public TaperModifier clone() {
        return (TaperModifier) super.clone();
    }
}
