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

    /** The spatial axis to displace. */
    public enum Axis { X, Y, Z }

    private double amplitude;
    private double frequency;
    private double phase;
    private SampleAxis inputAxis;
    private Axis outputAxis;

    public WaveModifier(double amplitude, double frequency, double phase,
                        SampleAxis inputAxis, Axis outputAxis) {
        this.amplitude = amplitude;
        this.frequency = frequency;
        this.phase = phase;
        this.inputAxis = inputAxis;
        this.outputAxis = outputAxis;
    }

    /** Creates a wave displacing {@code outputAxis} driven by normalised point index (T). */
    public WaveModifier(double amplitude, double frequency, Axis outputAxis) {
        this(amplitude, frequency, 0.0, SampleAxis.T, outputAxis);
    }

    @Override
    public void modify(PointContext point) {
        double displacement = amplitude * Math.sin(frequency * inputAxis.sample(point) + phase);
        switch (outputAxis) {
            case X -> point.x += displacement;
            case Y -> point.y += displacement;
            case Z -> point.z += displacement;
        }
    }

    @Override
    public int modifierHash() {
        int result = Double.hashCode(amplitude);
        result = 31 * result + Double.hashCode(frequency);
        result = 31 * result + Double.hashCode(phase);
        result = 31 * result + inputAxis.ordinal();
        result = 31 * result + outputAxis.ordinal();
        return result;
    }

    public double getAmplitude() { return amplitude; }
    public void setAmplitude(double amplitude) { this.amplitude = amplitude; }

    public double getFrequency() { return frequency; }
    public void setFrequency(double frequency) { this.frequency = frequency; }

    public double getPhase() { return phase; }
    public void setPhase(double phase) { this.phase = phase; }

    public SampleAxis getInputAxis() { return inputAxis; }
    public void setInputAxis(SampleAxis inputAxis) { this.inputAxis = inputAxis; }

    public Axis getOutputAxis() { return outputAxis; }
    public void setOutputAxis(Axis outputAxis) { this.outputAxis = outputAxis; }

    @Override
    public WaveModifier clone() {
        return (WaveModifier) super.clone();
    }
}
