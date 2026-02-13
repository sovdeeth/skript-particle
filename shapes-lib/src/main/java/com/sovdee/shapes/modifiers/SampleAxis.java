package com.sovdee.shapes.modifiers;

/**
 * The value used to drive a per-point computation (wave input, gradient direction, etc.).
 *
 * <ul>
 *   <li>{@link #X}, {@link #Y}, {@link #Z} — the point's local spatial coordinate.</li>
 *   <li>{@link #T} — the normalised point index ({@code index / totalPoints}), producing
 *       effects that vary over draw order rather than position.</li>
 * </ul>
 */
public enum SampleAxis {
    X, Y, Z, T;

    /** Evaluates this axis against the given context, returning a value in an appropriate range. */
    public double sample(PointContext point) {
        return switch (this) {
            case X -> point.x;
            case Y -> point.y;
            case Z -> point.z;
            case T -> point.totalPoints > 0 ? (double) point.index / point.totalPoints : 0.0;
        };
    }

    /** Same as {@link #sample} but normalised to [0, 1] using the context's bounds. */
    public double sampleNormalized(PointContext point) {
        return switch (this) {
            case X -> point.normalizedX();
            case Y -> point.normalizedY();
            case Z -> point.normalizedZ();
            case T -> point.totalPoints > 0 ? (double) point.index / point.totalPoints : 0.0;
        };
    }
}
