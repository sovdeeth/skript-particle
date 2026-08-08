package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import org.joml.Vector3d;

import java.util.List;

/**
 * A prepared render pass over a shape's points. Created by
 * {@link PointSampler#preparePass(com.sovdee.shapes.shapes.Shape, List, PointContext)} after
 * render modifiers have been collected, validated, and prepared against the full point set.
 * <p>
 * Points may then be rendered in one or more sequential batches via {@link #renderBatch};
 * the pass keeps a running point index so that {@link PointContext#index} (and anything
 * derived from it, such as normalized draw order) stays continuous across batches.
 * A pass is single-use and not thread-safe; batches must be rendered in order.
 */
public final class RenderPass<Context extends PointContext> {

    private final Context context;
    private final List<PointModifier<Context>> renderModifiers;
    private int nextIndex = 0;

    RenderPass(Context context, List<PointModifier<Context>> renderModifiers) {
        this.context = context;
        this.renderModifiers = renderModifiers;
    }

    /**
     * Runs the render loop over one batch of points: for each point the context is reset,
     * populated, passed through every render modifier, and handed to the renderer.
     * {@link ShapeRenderer#begin} and {@link ShapeRenderer#end} are called around the batch.
     *
     * @param batch    the points to render, in draw order
     * @param renderer the renderer to send points to; must be the renderer whose context
     *                 this pass was prepared with
     */
    public void renderBatch(List<Vector3d> batch, ShapeRenderer<Context> renderer) {
        renderer.begin(batch.size());
        for (Vector3d p : batch) {
            context.reset();
            context.x = p.x;
            context.y = p.y;
            context.z = p.z;
            context.index = nextIndex++;
            for (PointModifier<Context> mod : renderModifiers) {
                mod.modify(context);
            }
            renderer.renderPoint(context);
        }
        renderer.end();
    }
}
