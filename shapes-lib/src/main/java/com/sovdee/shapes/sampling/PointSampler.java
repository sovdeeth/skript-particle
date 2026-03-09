package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
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
     * When density is not explicit, implementations derive it from {@link #getMaxPoints()}.
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
     * @return the target maximum number of points used in auto-density mode
     */
    int getMaxPoints();

    /**
     * Sets the target maximum number of points and enables auto-density mode,
     * where the implementation derives an appropriate density from the shape geometry.
     *
     * @param maxPoints the target maximum particle count
     */
    void setMaxPoints(int maxPoints);

    /**
     * Returns whether density was set explicitly via {@link #setDensity}.
     * When {@code false}, density is auto-computed from the shape to target {@link #getMaxPoints()}.
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
     * Samples points from the given shape using the shape's own orientation.
     */
    List<Vector3d> getPoints(Shape shape);

    /**
     * Samples points from the given shape using the given orientation.
     */
    List<Vector3d> getPoints(Shape shape, Quaterniond orientation);

    /**
     * Computes and sets the density to achieve approximately the given particle count.
     */
    default void setParticleCount(Shape shape, int count) {
        setDensity(shape.computeDensity(getStyle(), count));
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
     * Returns a deep copy of this sampler with the same configuration.
     * The clone must be fully independent from the original.
     *
     * @return a new sampler instance
     */
    PointSampler clone();
}
