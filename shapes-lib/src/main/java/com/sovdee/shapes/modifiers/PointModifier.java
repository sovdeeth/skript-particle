package com.sovdee.shapes.modifiers;

import com.sovdee.shapes.sampling.DefaultPointSampler;

/**
 * Transforms individual points. Geometry modifiers (taper, twist, wave) transform position
 * in local space before orientation/scale and are cached with shape state.
 * <br>
 * Modifiers that can run prior to rendering (geometry-only modifiers) should extends {@link PreRenderPointModifier}
 */
public interface PointModifier<Context extends PointContext> extends Cloneable {

    /**
     * Called once per pass before the {@link #modify} loop.
     * Pre-compute expensive state here (e.g., LUTs, inverse ranges).
     *
     * @param bounds the bounding box of the points for this pass
     */
    default void prepare(ShapeBounds bounds) {}

    /**
     * Called per point. Avoid allocations in this method.
     * Modify the provided context to modify the point/render.
     *
     * @param point mutable per-point context, reused. Do not hold references.
     */
    void modify(Context point);

    /**
     * Returns a hash code representing this modifier's current configuration.
     * Used for cache invalidation in {@link DefaultPointSampler}.
     */
    int modifierHash();

    PointModifier<Context> clone();

    /**
     * The minimum context type this modifier requires.
     * Defaults to {@link PointContext} (compatible with any renderer).
     * Render modifiers should override this to declare their specific required type,
     * enabling validation in {@link DefaultPointSampler#render}.
     */
    default Class<? extends PointContext> contextType() {
        return PointContext.class;
    }

    /**
     * Marker base class for geometry-only (pre-render) modifiers.
     * These run during point sampling and are cached with the shape state.
     * They always operate on plain {@link PointContext}.
     */
    abstract class PreRenderPointModifier implements PointModifier<PointContext> {

        @Override
        @SuppressWarnings("unchecked")
        public PointModifier<PointContext> clone() {
            try {
                return (PointModifier<PointContext>) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }

    }
}
