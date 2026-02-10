package com.sovdee.shapes.sampling;

/**
 * Determines how a shape's geometry is sampled into points.
 */
public enum SamplingStyle {
    OUTLINE,
    SURFACE,
    FILL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
