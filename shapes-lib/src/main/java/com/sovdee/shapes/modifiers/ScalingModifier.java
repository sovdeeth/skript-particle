package com.sovdee.shapes.modifiers;

/**
 * Scales any combination of axes by a value that linearly interpolates from
 * {@code startScale} to {@code endScale} based on a {@link NormalizedInput}.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Taper a cylinder along Y: {@code new ScalingModifier(1, 0, StandardInput.Y, ScaleAxes.XZ)}</li>
 *   <li>Taper inward from radial center: {@code new ScalingModifier(1, 0, StandardInput.RADIUS, ScaleAxes.XYZ)}</li>
 *   <li>Scale by draw order: {@code new ScalingModifier(0, 1, StandardInput.T, ScaleAxes.XYZ)}</li>
 * </ul>
 */
public class ScalingModifier extends PointModifier.PreRenderPointModifier implements HasInput, Easable {

    private double startScale;
    private double endScale;
    private NormalizedInput input;
    private ScaleAxes axes;
    private EasingFunction easing = EasingFunction.LINEAR;

    /**
     * Creates a {@code ScalingModifier} that scales the XZ plane driven by the Y axis.
     *
     * @param startScale scale factor at input minimum
     * @param endScale   scale factor at input maximum
     */
    public ScalingModifier(double startScale, double endScale) {
        this(startScale, endScale, StandardInput.Y, ScaleAxes.XZ);
    }

    /**
     * Creates a {@code ScalingModifier} with a specified input and set of scaled axes.
     *
     * @param startScale scale factor at input minimum
     * @param endScale   scale factor at input maximum
     * @param input      the normalized input that drives the scale factor
     * @param axes       which axes to scale
     */
    public ScalingModifier(double startScale, double endScale, NormalizedInput input, ScaleAxes axes) {
        this.startScale = startScale;
        this.endScale = endScale;
        this.input = input;
        this.axes = axes;
    }

    /**
     * Scales the configured axes by {@code startScale + easing(t) * (endScale - startScale)},
     * where {@code t} is sampled from the input.
     *
     * @param point the mutable point context to transform
     */
    @Override
    public void modify(PointContext point) {
        double t = easing.apply(input.sample(point));
        double scale = startScale + t * (endScale - startScale);
        if (axes.x) point.x *= scale;
        if (axes.y) point.y *= scale;
        if (axes.z) point.z *= scale;
    }

    /**
     * {@inheritDoc}
     * Includes {@code startScale}, {@code endScale}, {@code input}, {@code axes},
     * and the easing hash.
     */
    @Override
    public int modifierHash() {
        int result = Double.hashCode(startScale);
        result = 31 * result + Double.hashCode(endScale);
        result = 31 * result + input.inputHash();
        result = 31 * result + axes.ordinal();
        result = 31 * result + easing.easingHash();
        return result;
    }

    /**
     * @return the scale factor at input minimum
     */
    public double getStartScale() {
        return startScale;
    }

    /**
     * @param startScale the scale factor at input minimum
     */
    public void setStartScale(double startScale) {
        this.startScale = startScale;
    }

    /**
     * @return the scale factor at input maximum
     */
    public double getEndScale() {
        return endScale;
    }

    /**
     * @param endScale the scale factor at input maximum
     */
    public void setEndScale(double endScale) {
        this.endScale = endScale;
    }

    /**
     * @return which axes are scaled
     */
    public ScaleAxes getAxes() {
        return axes;
    }

    /**
     * @param axes which axes to scale
     */
    public void setAxes(ScaleAxes axes) {
        this.axes = axes;
    }

    @Override
    public NormalizedInput getInput() {
        return input;
    }

    @Override
    public void setInput(NormalizedInput input) {
        this.input = input;
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
     * @return a new {@code ScalingModifier} with the same configuration
     */
    @Override
    public ScalingModifier clone() {
        return (ScalingModifier) super.clone();
    }
}
