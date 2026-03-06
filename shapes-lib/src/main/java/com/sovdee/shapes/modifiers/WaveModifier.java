package com.sovdee.shapes.modifiers;

/**
 * Displaces points by {@code amplitude * sin(frequency * input + phase)},
 * where input and output are independently configurable.
 *
 * <p>Output axis: the coordinate component to displace (X, Y, or Z).</p>
 * <p>Input axis: any {@link SampleAxis} — X, Y, Z (spatial) or T (normalised
 * point index, producing a ripple over draw order).</p>
 */
public class WaveModifier extends PointModifier.PreRenderPointModifier {

    /**
     * The spatial output axis to displace.
     * Each constant identifies which coordinate component the wave displaces.
     */
    public enum Axis { X, Y, Z }

    private double amplitude;
    private double frequency;
    private double phase;
    private SampleAxis inputAxis;
    private Axis outputAxis;

    /**
     * Creates a fully configured wave modifier.
     *
     * @param amplitude  peak displacement distance (positive or negative)
     * @param frequency  oscillation frequency; higher values produce more cycles across the input range
     * @param phase      phase offset in radians, shifting the wave along the input axis
     * @param inputAxis  the axis (spatial or T) used as the sine function's input
     * @param outputAxis the spatial coordinate component to displace
     */
    public WaveModifier(double amplitude, double frequency, double phase,
                        SampleAxis inputAxis, Axis outputAxis) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        this.inputAxis = inputAxis;
        this.outputAxis = outputAxis;
    }

    /**
     * Creates a wave displacing {@code outputAxis} driven by normalised point index (T),
     * with a phase of {@code 0}.
     *
     * @param amplitude  peak displacement distance
     * @param frequency  oscillation frequency
     * @param outputAxis the spatial coordinate component to displace
     */
    public WaveModifier(double amplitude, double frequency, Axis outputAxis) {
        this(amplitude, frequency, 0.0, SampleAxis.T, outputAxis);
    }

    /**
     * Displaces the point along {@code outputAxis} by
     * {@code amplitude * sin(frequency * input + phase)},
     * where {@code input} is sampled from {@code inputAxis}.
     *
     * @param point the mutable point context to transform
     */
    @Override
    public void modify(PointContext point) {
        double displacement = amplitude * Math.sin(frequency * inputAxis.sample(point) + phase);
        switch (outputAxis) {
            case X -> point.x += displacement;
            case Y -> point.y += displacement;
            case Z -> point.z += displacement;
        }
    }

    /**
     * {@inheritDoc}
     * Includes {@code amplitude}, {@code frequency}, {@code phase}, {@code inputAxis},
     * and {@code outputAxis}.
     *
     * @return stable hash of this modifier's configuration
     */
    @Override
    public int modifierHash() {
        int result = Double.hashCode(amplitude);
        result = 31 * result + Double.hashCode(frequency);
        result = 31 * result + Double.hashCode(phase);
        result = 31 * result + inputAxis.ordinal();
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
     * @return the oscillation frequency
     */
    public double getFrequency() {
        return frequency;
    }

    /**
     * @param frequency the oscillation frequency
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
     * @return the axis used as the sine function's input
     */
    public SampleAxis getInputAxis() {
        return inputAxis;
    }

    /**
     * @param inputAxis the axis used as the sine function's input
     */
    public void setInputAxis(SampleAxis inputAxis) {
        this.inputAxis = inputAxis;
    }

    /**
     * @return the spatial coordinate component that is displaced
     */
    public Axis getOutputAxis() {
        return outputAxis;
    }

    /**
     * @param outputAxis the spatial coordinate component to displace
     */
    public void setOutputAxis(Axis outputAxis) {
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
