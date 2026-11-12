package com.sovdee.shapes.sampling;

/**
 * Client-provided rendering metadata. Implementations are opaque to the library.
 */
public interface DrawContext {
    /**
     * Returns an independent copy of this context.
     * Used by {@link DefaultPointSampler#clone()} to ensure the cloned sampler
     * does not share rendering state with the original.
     *
     * @return a new {@code DrawContext} with the same configuration
     */
    DrawContext copy();
}
