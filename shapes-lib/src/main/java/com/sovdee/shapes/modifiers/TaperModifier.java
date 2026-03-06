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
public class TaperModifier extends PointModifier.PreRenderPointModifier implements Axial, Easable{

    private double startScale;
    private double endScale;
    private SampleAxis axis;
    private EasingFunction easing = EasingFunction.LINEAR;

    /**
     * Creates a {@code TaperModifier} along the Y axis.
     *
     * @param startScale scale factor applied at the axis minimum (e.g., bottom of a Y-axis shape)
     * @param endScale   scale factor applied at the axis maximum (e.g., top of a Y-axis shape)
     */
    public TaperModifier(double startScale, double endScale) {
        this(startScale, endScale, SampleAxis.Y);
    }

    /**
     * Creates a {@code TaperModifier} along the specified axis.
     *
     * @param startScale scale factor applied at the axis minimum
     * @param endScale   scale factor applied at the axis maximum
     * @param axis       the axis along which to measure and apply taper
     */
    public TaperModifier(double startScale, double endScale, SampleAxis axis) {
        this.startScale = startScale;
        this.endScale = endScale;
        this.axis = axis;
    }

    /**
     * Scales the two axes perpendicular to {@code axis} at the given point.
     * The scale factor is {@code startScale + easing(t) * (endScale - startScale)},
     * where {@code t} is the normalised position of the point along the taper axis.
     *
     * @param point the mutable point context to transform
     */
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

    /**
     * {@inheritDoc}
     * Includes {@code startScale}, {@code endScale}, {@code axis}, and the easing hash.
     *
     * @return stable hash of this modifier's configuration
     */
    @Override
    public int modifierHash() {
        int result = Double.hashCode(startScale);
        result = 31 * result + Double.hashCode(endScale);
        result = 31 * result + axis.hashCode();
        result = 31 * result + easing.easingHash();
        return result;
    }

    /**
     * @return the scale factor applied at the axis minimum
     */
    public double getStartScale() {
        return startScale;
    }

    /**
     * @param startScale the scale factor to apply at the axis minimum
     */
    public void setStartScale(double startScale) {
        this.startScale = startScale;
    }

    /**
     * @return the scale factor applied at the axis maximum
     */
    public double getEndScale() {
        return endScale;
    }

    /**
     * @param endScale the scale factor to apply at the axis maximum
     */
    public void setEndScale(double endScale) {
        this.endScale = endScale;
    }

    @Override
    public SampleAxis getAxis() {
        return axis;
    }

    @Override
    public void setAxis(SampleAxis axis) {
        this.axis = axis;
    }

    @Override
    public EasingFunction getEasing() {
        return easing;
    }

    @Override

    public void setEasing(EasingFunction easing) {
        this.easing = easing;
    }

    /**
     * Returns a deep copy of this modifier with independent state.
     *
     * @return a new {@code TaperModifier} with the same configuration
     */
    @Override
    public TaperModifier clone() {
        return (TaperModifier) super.clone();
    }
}
