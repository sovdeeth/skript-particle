package com.sovdee.shapes.modifiers;

/**
 * Standard normalized inputs for driving geometry modifiers.
 * Each constant samples a specific aspect of the point's context,
 * returning a value in [0, 1].
 */
public enum StandardInput implements NormalizedInput {
    /**
     * Normalized position within the X extent of the shape's bounds.
     */
    X {
        @Override
        public double sample(PointContext point) { return point.normalizedX(); }
    },

    /**
     * Normalized position within the Y extent of the shape's bounds.
     */
    Y {
        @Override
        public double sample(PointContext point) { return point.normalizedY(); }
    },

    /**
     * Normalized position within the Z extent of the shape's bounds.
     */
    Z {
        @Override
        public double sample(PointContext point) { return point.normalizedZ(); }
    },

    /**
     * Normalized draw order: {@code index / totalPoints}.
     */
    T {
        @Override
        public double sample(PointContext point) {
            return point.totalPoints > 0 ? (double) point.index / point.totalPoints : 0.0;
        }
    },

    /**
     * XZ distance from the origin, normalized to the maximum XZ radius.
     */
    RADIUS {
        @Override
        public double sample(PointContext point) { return point.normalizedRadius(); }
    },

    /**
     * 3D distance from the origin, normalized to the maximum XZ radius.
     */
    SPHERICAL {
        @Override
        public double sample(PointContext point) { return point.normalizedSpherical(); }
    },

    /**
     * XZ angle mapped to [0, 1] over a full revolution (0 = +X axis).
     */
    ANGLE {
        @Override
        public double sample(PointContext point) { return point.angle(); }
    };

    @Override
    public int inputHash() {
        return ordinal();
    }
}
