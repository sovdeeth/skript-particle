package com.sovdee.shapes.modifiers;

/**
 * Maps a normalised value {@code t ∈ [0, 1]} to a (typically) {@code [0, 1]} output, shaping
 * how interpolation progresses along a modifier's input axis.
 *
 * <p>Built-in constants and factory methods cover the most common curves:
 * <ul>
 *   <li>{@link #LINEAR} — identity, no reshaping</li>
 *   <li>{@link #powerIn}/{@link #powerOut}/{@link #powerInOut} — t^n family (quadratic, cubic, …)</li>
 *   <li>{@link #SINE_IN}/{@link #SINE_OUT}/{@link #SINE_IN_OUT} — sine-based gentle curves</li>
 * </ul>
 *
 * <p>Custom implementations must provide a stable {@link #easingHash()} so that modifier cache
 * invalidation works correctly.
 */
public interface EasingFunction {

    /** Applies the easing curve to {@code t}. */
    double apply(double t);

    /**
     * Stable hash of this easing's configuration, used in modifier cache keys.
     * Two easings that produce identical output for all inputs should return the same value.
     */
    int easingHash();

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** Direction of the easing curve. */
    enum Mode { IN, OUT, IN_OUT }

    /**
     * Power-curve easing: {@code t^n} (in), {@code 1-(1-t)^n} (out),
     * symmetric combination (in-out).
     * Exponent 1 = linear, 2 = quadratic, 3 = cubic, etc.
     */
    record PowerEasing(double exponent, Mode mode) implements EasingFunction {
        @Override
        public double apply(double t) {
            return switch (mode) {
                case IN -> Math.pow(t, exponent);
                case OUT -> 1.0 - Math.pow(1.0 - t, exponent);
                case IN_OUT -> t < 0.5
                        ? Math.pow(2.0 * t, exponent) / 2.0
                        : 1.0 - Math.pow(-2.0 * t + 2.0, exponent) / 2.0;
            };
        }

        @Override
        public int easingHash() {
            return 31 * Double.hashCode(exponent) + mode.ordinal();
        }
    }

    /** Sine-based easing; smoother start/end than power curves. */
    record SineEasing(Mode mode) implements EasingFunction {
        @Override
        public double apply(double t) {
            return switch (mode) {
                case IN -> 1.0 - Math.cos(t * Math.PI / 2.0);
                case OUT -> Math.sin(t * Math.PI / 2.0);
                case IN_OUT -> -(Math.cos(Math.PI * t) - 1.0) / 2.0;
            };
        }

        @Override
        public int easingHash() {
            return 1000 + mode.ordinal();
        }
    }

    // -------------------------------------------------------------------------
    // Named constants
    // -------------------------------------------------------------------------

    EasingFunction LINEAR      = new PowerEasing(1.0, Mode.IN);

    EasingFunction QUAD_IN     = new PowerEasing(2.0, Mode.IN);
    EasingFunction QUAD_OUT    = new PowerEasing(2.0, Mode.OUT);
    EasingFunction QUAD_IN_OUT = new PowerEasing(2.0, Mode.IN_OUT);

    EasingFunction CUBIC_IN     = new PowerEasing(3.0, Mode.IN);
    EasingFunction CUBIC_OUT    = new PowerEasing(3.0, Mode.OUT);
    EasingFunction CUBIC_IN_OUT = new PowerEasing(3.0, Mode.IN_OUT);

    EasingFunction SINE_IN     = new SineEasing(Mode.IN);
    EasingFunction SINE_OUT    = new SineEasing(Mode.OUT);
    EasingFunction SINE_IN_OUT = new SineEasing(Mode.IN_OUT);

    // -------------------------------------------------------------------------
    // Factory methods
    // -------------------------------------------------------------------------

    static EasingFunction powerIn(double exponent)    { return new PowerEasing(exponent, Mode.IN); }
    static EasingFunction powerOut(double exponent)   { return new PowerEasing(exponent, Mode.OUT); }
    static EasingFunction powerInOut(double exponent) { return new PowerEasing(exponent, Mode.IN_OUT); }
}
