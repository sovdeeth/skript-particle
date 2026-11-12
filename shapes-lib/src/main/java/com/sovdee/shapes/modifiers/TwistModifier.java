package com.sovdee.shapes.modifiers;

/**
 * Rotates each point in the specified {@link RotationPlane} by an angle proportional to
 * the value of a {@link NormalizedInput}. A {@code totalAngle} of 2π produces one full twist.
 *
 * <p>Examples:</p>
 * <ul>
 *   <li>Classic Y-axis twist: {@code new TwistModifier(2*PI, StandardInput.Y, RotationPlane.XZ)}</li>
 *   <li>Radial tornado: {@code new TwistModifier(2*PI, StandardInput.RADIUS, RotationPlane.XZ)}</li>
 *   <li>Draw-order twist: {@code new TwistModifier(PI, StandardInput.T, RotationPlane.XZ)}</li>
 * </ul>
 */
public class TwistModifier extends PointModifier.PreRenderPointModifier implements HasInput, Easable {

    private double totalAngle;
    private NormalizedInput input;
    private RotationPlane plane;
    private EasingFunction easing = EasingFunction.LINEAR;

    /**
     * Creates a {@code TwistModifier} in the XZ plane driven by the Y axis.
     *
     * @param totalAngle total rotation in radians; {@code 2π} produces one full revolution
     */
    public TwistModifier(double totalAngle) {
        this(totalAngle, StandardInput.Y, RotationPlane.XZ);
    }

    /**
     * Creates a {@code TwistModifier} with a specified input and rotation plane.
     *
     * @param totalAngle total rotation in radians; {@code 2π} produces one full revolution
     * @param input      the normalized input that drives the rotation angle
     * @param plane      the plane in which points are rotated
     */
    public TwistModifier(double totalAngle, NormalizedInput input, RotationPlane plane) {
        this.totalAngle = totalAngle;
        this.input = input;
        this.plane = plane;
    }

    /**
     * Rotates the point in the configured plane by {@code easing(t) * totalAngle} radians,
     * where {@code t} is sampled from the input.
     *
     * @param point the mutable point context to transform
     */
    @Override
    public void modify(PointContext point) {
        double t = easing.apply(input.sample(point));
        double angle = totalAngle * t;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        switch (plane) {
            case YZ -> {
                double newY = point.y * cos - point.z * sin;
                double newZ = point.y * sin + point.z * cos;
                point.y = newY;
                point.z = newZ;
            }
            case XY -> {
                double newX = point.x * cos - point.y * sin;
                double newY = point.x * sin + point.y * cos;
                point.x = newX;
                point.y = newY;
            }
            default -> { // XZ
                double newX = point.x * cos - point.z * sin;
                double newZ = point.x * sin + point.z * cos;
                point.x = newX;
                point.z = newZ;
            }
        }
    }

    /**
     * {@inheritDoc}
     * Includes {@code totalAngle}, {@code input}, {@code plane}, and the easing hash.
     */
    @Override
    public int modifierHash() {
        int result = Double.hashCode(totalAngle);
        result = 31 * result + input.inputHash();
        result = 31 * result + plane.ordinal();
        result = 31 * result + easing.easingHash();
        return result;
    }

    /**
     * @return the total rotation angle in radians
     */
    public double getTotalAngle() {
        return totalAngle;
    }

    /**
     * @param totalAngle the total rotation angle in radians
     */
    public void setTotalAngle(double totalAngle) {
        this.totalAngle = totalAngle;
    }

    /**
     * @return the rotation plane
     */
    public RotationPlane getPlane() {
        return plane;
    }

    /**
     * @param plane the rotation plane
     */
    public void setPlane(RotationPlane plane) {
        this.plane = plane;
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
     * @return a new {@code TwistModifier} with the same configuration
     */
    @Override
    public TwistModifier clone() {
        return (TwistModifier) super.clone();
    }
}
