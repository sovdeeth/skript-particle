package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;

/**
 * Client-implemented interface for rendering shape points.
 * The library drives the render loop; the client handles the actual output (e.g. NMS packets).
 *
 * <p>Implementations may cast the {@link PointContext} to their own subclass (e.g. {@code RenderContext})
 * to read render-specific properties written by render modifiers.
 */
public interface ShapeRenderer<Context extends PointContext> {

    /**
     * Called once before the point loop. Use to allocate buffers, pre-compute shared state, etc.
     *
     * @param totalPoints the number of points that will be rendered
     */
    void begin(int totalPoints);

    /**
     * Called for each point after all render modifiers have been applied.
     * Should be allocation-free.
     *
     * @param point the mutable per-point context; cast to your subclass to access render properties
     */
    void renderPoint(Context point);

    /**
     * @return Returns the context this renderer uses.
     */
    Context getContext();

    /**
     * Called after all points have been rendered. Flush buffers, send remaining packets, etc.
     */
    void end();
}
