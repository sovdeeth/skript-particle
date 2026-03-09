package com.sovdee.shapes.modifiers;

/**
 * A normalized input source for driving per-point modifier computations.
 * Returns a value in [0, 1] derived from the point's current context.
 */
public interface NormalizedInput {

    /**
     * Returns a value in [0, 1] derived from the point's current context.
     *
     * @param point the current point context
     * @return a value in [0, 1]
     */
    double sample(PointContext point);

    /**
     * Stable hash of this input's identity, for use in {@code modifierHash()}.
     *
     * @return a stable integer hash
     */
    int inputHash();
}
