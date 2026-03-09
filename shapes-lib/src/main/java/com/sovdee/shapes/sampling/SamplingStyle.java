package com.sovdee.shapes.sampling;

/**
 * Determines how a shape's geometry is sampled into points.
 */
public enum SamplingStyle {
    OUTLINE,
    SURFACE,
    FILL;

    /**
     * Returns a lowercase string representation of this style, e.g. {@code "outline"}.
     *
     * @return the lowercase enum name
     */
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
