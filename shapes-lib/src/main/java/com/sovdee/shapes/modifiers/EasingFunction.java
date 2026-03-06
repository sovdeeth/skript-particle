package com.sovdee.shapes.modifiers;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * Maps a normalised value {@code t ∈ [0, 1]} to a (typically) {@code [0, 1]} output, shaping
 * how interpolation progresses along a modifier's input axis.
 * <br>
 * Built-in constants and factory methods cover the most common curves, like {@link #SINE_IN_OUT}.
 * <br>
 * Custom implementations must provide a stable {@link #easingHash()} so that modifier cache
 * invalidation works correctly.
 */
public interface EasingFunction {

    /**
     * Given an input between 0 and 1, applies the easing function and returns a mapped output
     * @param t Input between 0 and 1.
     * @return Output value mapped by the easing function.
     */
    double apply(double t);

    /**
     * @return Stable hash of this easing's configuration, used in modifier cache keys.
     * Two easings that produce identical output for all inputs should return the same value.
     */
    int easingHash();

    /**
     * Controls which end of the interpolation range is eased.
     * <ul>
     *   <li>{@link #IN} — slow start, fast end (ease into motion)</li>
     *   <li>{@link #OUT} — fast start, slow end (ease out of motion)</li>
     *   <li>{@link #IN_OUT} — slow start and slow end (symmetric ease)</li>
     * </ul>
     */
    enum Mode {
        IN,
        OUT,
        IN_OUT
    }

    /**
     * Power-curve easing: {@code t^n} (in), {@code 1-(1-t)^n} (out),
     * symmetric combination (in-out).
     * @param exponent  1 = linear, 2 = quadratic, 3 = cubic, etc.
     * @param mode Whether to have the start, end, or both be smooth.
     */
    record PowerEasing(double exponent, Mode mode) implements EasingFunction {
        /**
         * Applies the power-curve easing to {@code t}.
         * <ul>
         *   <li>IN:  {@code t^exponent}</li>
         *   <li>OUT: {@code 1 - (1-t)^exponent}</li>
         *   <li>IN_OUT: piecewise symmetric combination of the above</li>
         * </ul>
         *
         * @param t normalised input in {@code [0, 1]}
         * @return eased output value
         */
        @Override
        public double apply(double t) {
            return switch (mode) {
                case IN -> Math.pow(t, exponent);
                case OUT -> 1.0 - Math.pow(1.0 - t, exponent);
                case IN_OUT -> {
                    if (t < 0.5)
                        yield Math.pow(2.0 * t, exponent) / 2.0;
                    yield 1.0 - Math.pow(-2.0 * t + 2.0, exponent) / 2.0;
                }
            };
        }

        /**
         * @return a hash combining the exponent and mode ordinal,
         *         unique among all standard {@code PowerEasing} configurations
         */
        @Override
        public int easingHash() {
            return 31 * Double.hashCode(exponent) + mode.ordinal();
        }
    }

    /**
     * Sine-based easing.
     * @param mode Whether to have the start, end, or both be smooth.
     */
    record SineEasing(Mode mode) implements EasingFunction {
        /**
         * Applies the sine-based easing to {@code t}.
         * <ul>
         *   <li>IN:  {@code 1 - cos(t * π/2)}</li>
         *   <li>OUT: {@code sin(t * π/2)}</li>
         *   <li>IN_OUT: {@code -(cos(π*t) - 1) / 2}</li>
         * </ul>
         *
         * @param t normalised input in {@code [0, 1]}
         * @return eased output value
         */
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

    /**
     * @param exponent Degree to use for the easing function
     * @return A power-based easing function with a gradual start and abrupt end
     */
    @Contract("_ -> new")
    static @NotNull EasingFunction powerIn(double exponent) {
        return new PowerEasing(exponent, Mode.IN);
    }

    /**
     * @param exponent Degree to use for the easing function
     * @return A power-based easing function with an abrupt start and gradual end
     */
    @Contract("_ -> new")
    static @NotNull EasingFunction powerOut(double exponent) {
        return new PowerEasing(exponent, Mode.OUT);
    }

    /**
     * @param exponent Degree to use for the easing function
     * @return A power-based easing function with a gradual start and end
     */
    @Contract("_ -> new")
    static @NotNull EasingFunction powerInOut(double exponent) {
        return new PowerEasing(exponent, Mode.IN_OUT);
    }

}
