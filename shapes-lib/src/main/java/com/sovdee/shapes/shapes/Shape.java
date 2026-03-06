package com.sovdee.shapes.shapes;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.sampling.ShapeRenderer;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;

/**
 * Represents a geometric shape. Pure geometry interface — sampling/caching/drawing
 * configuration lives in {@link PointSampler}.
 */
public interface Shape extends Cloneable {

    double EPSILON = 0.0001;

    // --- Spatial transform ---

    /**
     * Returns a copy of this shape's orientation quaternion.
     * The orientation describes the rotation applied to all generated points before they are
     * returned to the caller; it does not affect the shape's local geometry.
     *
     * @return a defensive copy of the current {@link Quaterniond} orientation
     */
    Quaterniond getOrientation();

    /**
     * Sets the orientation of this shape.
     * The provided quaternion is copied, so subsequent mutation of the argument has no effect.
     *
     * @param orientation the new orientation quaternion; must not be null
     */
    void setOrientation(Quaterniond orientation);

    /**
     * Returns the uniform scale factor applied to this shape's generated points.
     * A value of {@code 1.0} means no scaling; values greater than {@code 1.0} enlarge the shape.
     *
     * @return the current scale factor
     */
    double getScale();

    /**
     * Sets the uniform scale factor for this shape.
     * The scale is applied to every generated point relative to the shape's local origin.
     *
     * @param scale the new scale factor; typically positive
     */
    void setScale(double scale);

    /**
     * Returns a copy of this shape's positional offset vector.
     * The offset is added to every generated point after orientation and scale are applied,
     * shifting the entire shape in local space.
     *
     * @return a defensive copy of the current offset {@link Vector3d}
     */
    Vector3d getOffset();

    /**
     * Sets the positional offset applied to this shape's generated points.
     *
     * @param offset the new offset vector; must not be null
     */
    void setOffset(Vector3d offset);

    // --- Oriented axes ---

    /**
     * Returns the world-space direction of this shape's local X axis after applying the current orientation.
     * Equivalent to rotating {@code (1, 0, 0)} by {@link #getOrientation()}.
     *
     * @return the transformed X axis as a unit {@link Vector3d}
     */
    Vector3d getRelativeXAxis();

    /**
     * Returns the world-space direction of this shape's local Y axis after applying the current orientation.
     * Equivalent to rotating {@code (0, 1, 0)} by {@link #getOrientation()}.
     *
     * @return the transformed Y axis as a unit {@link Vector3d}
     */
    Vector3d getRelativeYAxis();

    /**
     * Returns the world-space direction of this shape's local Z axis after applying the current orientation.
     * Equivalent to rotating {@code (0, 0, 1)} by {@link #getOrientation()}.
     *
     * @return the transformed Z axis as a unit {@link Vector3d}
     */
    Vector3d getRelativeZAxis();

    // --- Geometry query ---

    /**
     * Returns {@code true} if the given point lies within or on the surface of this shape's volume.
     * Points are tested in the shape's local coordinate space (before orientation and scale are applied).
     * Implementations should use {@link #EPSILON} for floating-point boundary comparisons.
     *
     * @param point the point to test, in local coordinates
     * @return {@code true} if the point is contained by this shape
     */
    boolean contains(Vector3d point);

    // --- Change detection ---

    /**
     * Returns a monotonically increasing version counter that is incremented each time the shape's
     * geometry changes (e.g. when a radius or height setter is called). {@link PointSampler}
     * implementations use this value to detect stale caches without performing a deep comparison.
     *
     * @return the current version number; starts at {@code 0} and only increases
     */
    long getVersion();

    // --- Dynamic support ---

    /**
     * Returns {@code true} if this shape is dynamic, meaning its geometry may change between
     * render frames (e.g. a Bezier curve driven by a supplier of live locations).
     * Dynamic shapes bypass the point cache and re-generate points every frame.
     *
     * @return {@code true} if this shape is marked dynamic
     */
    boolean isDynamic();

    /**
     * Marks this shape as dynamic or static. Dynamic shapes skip the {@link PointSampler}
     * cache and regenerate points on every render call.
     *
     * @param dynamic {@code true} to enable dynamic mode; {@code false} to enable caching
     */
    void setDynamic(boolean dynamic);

    // --- Point generation (density as parameter) ---

    /**
     * Generates points that trace the wireframe outline of this shape and appends them to {@code points}.
     * For most shapes this produces one or more closed loops of evenly spaced points.
     * The approximate arc-length distance between consecutive points equals {@code density}.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points; smaller values produce more points
     */
    void generateOutline(List<Vector3d> points, double density);

    /**
     * Generates points that cover the surface of this shape and appends them to {@code points}.
     * The surface is the outermost shell, distinct from the filled interior.
     * Falls back to {@link #generateOutline} in {@link AbstractShape} unless overridden.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points
     */
    void generateSurface(List<Vector3d> points, double density);

    /**
     * Generates points that fill the volume of this shape and appends them to {@code points}.
     * The result includes both the surface and interior points.
     * Falls back to {@link #generateSurface} in {@link AbstractShape} unless overridden.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points; smaller values produce denser fills
     */
    void generateFilled(List<Vector3d> points, double density);

    /**
     * Called by PointSampler before point generation. Override for supplier refresh, step recalc, etc.
     */
    default void beforeSampling(double density) {}

    /**
     * Called by PointSampler after point generation. Override for centerOffset adjustment, etc.
     */
    default void afterSampling(List<Vector3d> points) {}

    /**
     * Computes the density needed to achieve approximately the given number of points.
     */
    double computeDensity(SamplingStyle style, int targetPointCount);

    // --- PointSampler ---

    /**
     * Returns the {@link PointSampler} responsible for caching, ordering, and modifying this
     * shape's generated points. Each shape owns exactly one sampler.
     *
     * @return the current {@link PointSampler}; never null after construction
     */
    PointSampler getPointSampler();

    /**
     * Replaces the {@link PointSampler} used by this shape.
     * The new sampler takes over all caching and render-modifier management immediately.
     *
     * @param sampler the new sampler to use; must not be null
     */
    void setPointSampler(PointSampler sampler);

    /**
     * Convenience default: drives the full render loop using the shape's own orientation.
     */
    default void render(Quaterniond orientation, ShapeRenderer<?> renderer) {
        getPointSampler().render(this, orientation, renderer);
    }

    // --- Replication ---

    /**
     * Creates and returns a deep copy of this shape, including its spatial transform (orientation,
     * scale, offset), dynamic flag, and point sampler.
     * Implementations must return the most specific concrete type possible.
     *
     * @return a new, independent {@link Shape} with identical state
     */
    Shape clone();

    /**
     * Copies this shape's base state (orientation, scale, offset, dynamic flag, point sampler)
     * into {@code target} and returns {@code target}. Dimension-specific fields are the
     * responsibility of each concrete class's own {@link #clone()} implementation.
     *
     * @param target the shape to copy state into; must not be null
     * @return {@code target}, for chaining
     */
    Shape copyTo(Shape target);
}
