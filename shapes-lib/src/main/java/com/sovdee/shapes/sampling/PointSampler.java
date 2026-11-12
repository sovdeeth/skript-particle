package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Responsible for sampling points from a {@link Shape}'s geometry.
 * Manages sampling configuration (style, density, ordering) and caching.
 */
public interface PointSampler extends Cloneable {

    /**
     * @return the current {@link SamplingStyle} (outline, surface, or fill)
     */
    SamplingStyle getStyle();

    /**
     * Sets the style used when sampling the shape's geometry.
     *
     * @param style the new sampling style
     */
    void setStyle(SamplingStyle style);

    /**
     * Returns the explicit point spacing density, if one has been set.
     * When density is not explicit, implementations may coarsen it to stay within {@link #getMaxPoints()}.
     *
     * @return the current density value
     */
    double getDensity();

    /**
     * Sets an explicit point spacing density.
     * Smaller values produce more points; calling this switches the sampler to explicit-density mode.
     *
     * @param density the desired point spacing
     */
    void setDensity(double density);

    /**
     * @return the maximum number of points a sampling may produce while the density is not explicit
     */
    int getMaxPoints();

    /**
     * Sets the maximum number of points and makes the density non-explicit, so that the cap applies:
     * the shape is sampled at {@link #getDensity()}, or coarser if that would exceed the cap.
     *
     * @param maxPoints the maximum particle count
     */
    void setMaxPoints(int maxPoints);

    /**
     * Returns whether density was set explicitly via {@link #setDensity}.
     * When {@code false}, the density is coarsened as needed to stay within {@link #getMaxPoints()}.
     *
     * @return {@code true} if density is explicit
     */
    boolean isDensityExplicit();

    /**
     * @return the comparator used to sort sampled points, or {@code null} for insertion order
     */
    Comparator<Vector3d> getOrdering();

    /**
     * Sets the comparator used to sort sampled points after generation.
     *
     * @param ordering a comparator over {@link Vector3d}, or {@code null} to disable sorting
     */
    void setOrdering(Comparator<Vector3d> ordering);

    /**
     * Returns the unique identifier assigned to this sampler at construction time.
     *
     * @return the immutable UUID of this sampler
     */
    UUID getUUID();

    /**
     * @return the client-provided {@link DrawContext}, or {@code null} if none is set
     */
    DrawContext getDrawContext();

    /**
     * Associates a client-provided rendering context with this sampler.
     *
     * @param context the {@link DrawContext} to use during rendering
     */
    void setDrawContext(DrawContext context);

    /**
     * Returns to the implementation's default density, limited by {@link #getMaxPoints()}.
     */
    default void resetDensity() {
        setMaxPoints(getMaxPoints());
    }

    /**
     * Returns the density the given shape is actually sampled at, which may be coarser than
     * {@link #getDensity()} when the density is not explicit.
     */
    default double getEffectiveDensity(Shape shape) {
        return getDensity();
    }

    /**
     * @return the number of points the given shape currently samples to
     */
    default int getPointCount(Shape shape) {
        return getPoints(shape).size();
    }

    /**
     * Samples points from the given shape using the shape's own orientation.
     * The returned list is unmodifiable and may be cached and shared: callers must not modify its vectors.
     */
    List<Vector3d> getPoints(Shape shape);

    /**
     * Samples points from the given shape using the given orientation.
     * The returned list is unmodifiable and may be cached and shared: callers must not modify its vectors.
     */
    List<Vector3d> getPoints(Shape shape, Quaterniond orientation);

    /**
     * Computes and sets the density to achieve approximately the given particle count.
     */
    default void setParticleCount(Shape shape, int count) {
        double density = shape.computeDensity(getStyle(), Math.max(count, 1));
        // an estimate of zero or less would mean an unbounded number of points
        if (Double.isFinite(density) && density > Shape.EPSILON)
            setDensity(density);
    }

    // ---- Modifier management ----

    /**
     * Returns the list of registered {@link PointModifier}s.
     * Geometry modifiers are applied during point sampling; render modifiers are applied per frame.
     *
     * @return the mutable modifier list
     */
    List<PointModifier<?>> getModifiers();

    /**
     * Appends a modifier to the modifier list.
     * Geometry modifiers may additionally invalidate the point cache.
     *
     * @param modifier the modifier to add
     */
    void addModifier(PointModifier<?> modifier);

    /**
     * Removes a modifier from the list if present.
     * Geometry modifiers may additionally invalidate the point cache.
     *
     * @param modifier the modifier to remove
     */
    void removeModifier(PointModifier<?> modifier);

    /**
     * Removes all modifiers from the list.
     * Geometry modifiers may additionally invalidate the point cache.
     */
    void clearModifiers();

    /**
     * Drives the full render loop: gets cached points, runs render modifiers, calls renderer.
     *
     * @param shape       the shape to render
     * @param orientation the orientation for point sampling
     * @param renderer    client-provided renderer
     */
    <Context extends PointContext>  void render(Shape shape, Quaterniond orientation, ShapeRenderer<Context> renderer);

    /**
     * Returns the bounds of a point list, typically one returned by {@link #getPoints}.
     * Implementations may cache the bounds of their own cached points; the default computes them.
     *
     * @param points the points to bound
     * @return the bounds of the points
     */
    default ShapeBounds boundsOf(List<Vector3d> points) {
        return ShapeBounds.compute(points);
    }

    /**
     * Collects and prepares this sampler's render modifiers against the given (already sampled)
     * point set, returning a {@link RenderPass} that can render the points in one or more
     * sequential batches with a continuous point index. Used by clients that spread rendering
     * over multiple ticks (e.g. animated draws); {@link #render} is the single-batch shortcut.
     *
     * @param shape   the shape being rendered
     * @param points  the full, ordered point set for this pass
     * @param context the renderer's context; its bounds/totalPoints/shape fields are populated
     */
    <Context extends PointContext> RenderPass<Context> preparePass(Shape shape, List<Vector3d> points, Context context);

    /**
     * Returns a deep copy of this sampler with the same configuration.
     * The clone must be fully independent from the original.
     *
     * @return a new sampler instance
     */
    PointSampler clone();
}
