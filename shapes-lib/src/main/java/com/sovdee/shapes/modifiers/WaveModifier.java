package com.sovdee.shapes.modifiers;

/**
 * Displaces points by {@code amplitude * sin(2π * frequency * input + phase)},
 * where input and output are independently configurable.
 *
 * <p>{@code frequency} is expressed in cycles over the full [0, 1] input range,
 * so {@code frequency=1} always means exactly one full sine cycle regardless of
 * shape size or which input is used.</p>
 *
 * <p>Output axis: the coordinate component to displace (X, Y, or Z).</p>
 * <p>Input: any {@link NormalizedInput} — X, Y, Z, T, RADIUS, SPHERICAL, or ANGLE.</p>
 */
public class WaveModifier extends PointModifier.PreRenderPointModifier {

    private double amplitude;
    private double frequency;
    private double phase;
    private NormalizedInput input;
    private SpatialAxis outputAxis;

    /**
     * Creates a fully configured wave modifier.
     *
     * @param amplitude  peak displacement distance (positive or negative)
     * @param frequency  cycles over the full [0, 1] input range; {@code 1} = one full cycle
     * @param phase      phase offset in radians
     * @param input      the normalized input used as the sine function's argument
     * @param outputAxis the spatial coordinate component to displace
     */
    public WaveModifier(double amplitude, double frequency, double phase,
                        NormalizedInput input, SpatialAxis outputAxis) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        this.input = input;
        this.outputAxis = outputAxis;
    }

    /**
     * Creates a wave displacing {@code outputAxis} driven by normalized draw order (T),
     * with a phase of {@code 0}.
     *
     * @param amplitude  peak displacement distance
     * @param frequency  cycles over the full [0, 1] T range
     * @param outputAxis the spatial coordinate component to displace
     */
    public WaveModifier(double amplitude, double frequency, SpatialAxis outputAxis) {
        this(amplitude, frequency, 0.0, StandardInput.T, outputAxis);
    }

    /**
     * Displaces the point along {@code outputAxis} by
     * {@code amplitude * sin(2π * frequency * input + phase)},
     * where {@code input} is sampled from the configured {@link NormalizedInput}.
     *
     * @param point the mutable point context to transform
     */
    @Override
    public void modify(PointContext point) {
        double displacement = amplitude * Math.sin(2 * Math.PI * frequency * input.sample(point) + phase);
        switch (outputAxis) {
            case X -> point.x += displacement;
            case Y -> point.y += displacement;
            case Z -> point.z += displacement;
        }
    }

    /**
     * {@inheritDoc}
     * Includes {@code amplitude}, {@code frequency}, {@code phase}, {@code input},
     * and {@code outputAxis}.
     */
    @Override
    public int modifierHash() {
        int result = Double.hashCode(amplitude);
        result = 31 * result + Double.hashCode(frequency);
        result = 31 * result + Double.hashCode(phase);
        result = 31 * result + input.inputHash();
        result = 31 * result + outputAxis.ordinal();
        return result;
    }

    /**
     * @return the peak displacement distance
     */
    public double getAmplitude() {
        return amplitude;
    }

    /**
     * @param amplitude the peak displacement distance
     */
    public void setAmplitude(double amplitude) {
        this.amplitude = amplitude;
    }

    /**
     * @return the number of cycles over the full [0, 1] input range
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the number of cycles over the full [0, 1] input range
     */
    public void setFrequency(double frequency) {
        this.frequency = frequency;
    }

    /**
     * @return the phase offset in radians
     */
    public double getPhase() {
        return phase;
    }

    /**
     * @param phase the phase offset in radians
     */
    public void setPhase(double phase) {
        this.phase = phase;
    }

    /**
     * @return the normalized input driving the wave
     */
    public NormalizedInput getInput() {
        return input;
    }

    /**
     * @param input the normalized input to drive the wave with
     */
    public void setInput(NormalizedInput input) {
        this.input = input;
    }

    /**
     * @return the spatial coordinate component that is displaced
     */
    public SpatialAxis getOutputAxis() {
        return outputAxis;
    }

    /**
     * @param outputAxis the spatial coordinate component to displace
     */
    public void setOutputAxis(SpatialAxis outputAxis) {
        this.outputAxis = outputAxis;
    }

    /**
     * Returns a deep copy of this modifier with independent state.
     *
     * @return a new {@code WaveModifier} with the same configuration
     */
    @Override
    public WaveModifier clone() {
        return (WaveModifier) super.clone();
    }
}
