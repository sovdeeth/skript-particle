package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.PointModifier.PreRenderPointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link PointSampler} with hash-based caching.
 */
public class DefaultPointSampler implements PointSampler {

    private static final int DEFAULT_MAX_POINTS = 10_000;

    private SamplingStyle style = SamplingStyle.OUTLINE;
    private double density = 0.25;
    private int maxPoints = DEFAULT_MAX_POINTS;
    private boolean densityExplicit = false;
    private Comparator<Vector3d> ordering;
    private final UUID uuid;
    private DrawContext drawContext;
    private List<PointModifier<?>> modifiers = new ArrayList<>();

    // Cache
    private List<Vector3d> cachedPoints = new ArrayList<>();
    private CacheState lastState;
    private boolean needsUpdate = false;

    /**
     * Creates a new {@code DefaultPointSampler} with default settings:
     * {@link SamplingStyle#OUTLINE}, density {@code 0.25}, max points {@code 10 000},
     * no ordering, no modifiers, and an empty point cache.
     * A fresh {@link UUID} is assigned at construction time.
     */
    public DefaultPointSampler() {
        this.uuid = UUID.randomUUID();
        this.lastState = new CacheState(style, 0, 1.0, 0, density, 0, 0);
    }


    /**
     * Returns sampled points for the given shape using the shape's own orientation.
     * Results are cached and only recomputed when the shape or sampler state changes.
     *
     * @param shape the shape to sample
     * @return an ordered list of transformed world-space points
     */
    @Override
    public List<Vector3d> getPoints(Shape shape) {
        return getPoints(shape, shape.getOrientation());
    }

    /**
     * Returns sampled points for the given shape using the supplied orientation quaternion.
     * The cache is invalidated when the style, orientation, scale, offset, density,
     * shape version, or geometry-modifier configuration changes.
     * Points are transformed (orientation → scale → offset) before being returned.
     *
     * @param shape       the shape to sample
     * @param orientation the orientation to apply to the sampled points
     * @return an ordered list of transformed world-space points
     */
    @Override
    public List<Vector3d> getPoints(Shape shape, Quaterniond orientation) {
        double workingDensity;
        if (densityExplicit) {
            workingDensity = density;
        } else {
            double auto = shape.computeDensity(style, maxPoints);
            workingDensity = (auto > Shape.EPSILON) ? auto : density;
        }

        CacheState state = new CacheState(style, orientation.hashCode(),
                shape.getScale(), shape.getOffset().hashCode(), workingDensity, shape.getVersion(),
                computeModifierHash());
        if (shape.isDynamic() || needsUpdate || !state.equals(lastState) || cachedPoints.isEmpty()) {
            List<Vector3d> points = new ArrayList<>();

            shape.beforeSampling(workingDensity);
            switch (style) {
                case OUTLINE -> shape.generateOutline(points, workingDensity);
                case SURFACE -> shape.generateSurface(points, workingDensity);
                case FILL -> shape.generateFilled(points, workingDensity);
            }
            shape.afterSampling(points);

            // Apply geometry modifiers per-point
            List<PointModifier<PointContext>> geoMods = new ArrayList<>();
            for (PointModifier<?> mod : modifiers) {
                if (mod instanceof PreRenderPointModifier) //noinspection unchecked
                    geoMods.add((PointModifier<PointContext>) mod);
            }
            if (!geoMods.isEmpty()) {
                ShapeBounds bounds = ShapeBounds.compute(points);
                PointContext ctx = new PointContext();
                ctx.shape = shape;
                ctx.bounds = bounds;
                ctx.totalPoints = points.size();
                for (PointModifier<PointContext> mod : geoMods) {
                    mod.prepare(bounds);
                }
                for (int i = 0; i < points.size(); i++) {
                    Vector3d p = points.get(i);
                    ctx.x = p.x; ctx.y = p.y; ctx.z = p.z;
                    ctx.index = i;
                    for (PointModifier<PointContext> mod : geoMods) {
                        mod.modify(ctx);
                    }
                    p.x = ctx.x; p.y = ctx.y; p.z = ctx.z;
                }
            }

            for (Vector3d point : points) {
                orientation.transform(point);
                point.mul(shape.getScale());
                point.add(shape.getOffset());
            }

            if (ordering != null) {
                points.sort(ordering);
            }

            cachedPoints = points;
            lastState = state;
            needsUpdate = false;
        }
        return cachedPoints;
    }

    /**
     * Drives the render loop for a shape. Gets cached points, prepares render modifiers,
     * then calls the renderer for each point.
     *
     * @param shape       the shape to render
     * @param orientation the orientation to use for point sampling
     * @param renderer    the client-provided renderer
     */
    @Override
    public <Context extends PointContext> void render(Shape shape, Quaterniond orientation, ShapeRenderer<Context> renderer) {
        List<Vector3d> points = getPoints(shape, orientation);
        Context context = renderer.getContext();

        // Collect render modifiers (everything that is not a pre-render/geometry modifier)
        List<PointModifier<Context>> renderMods = new ArrayList<>();
        Class<?> ctxClass = context.getClass();
        for (PointModifier<?> mod : modifiers) {
            if (mod instanceof PreRenderPointModifier) continue;
            if (!mod.contextType().isAssignableFrom(ctxClass)) {
                throw new IllegalStateException(
                    "Render modifier " + mod.getClass().getSimpleName() +
                    " requires context type " + mod.contextType().getSimpleName() +
                    " but renderer provides " + ctxClass.getSimpleName()
                );
            }
            //noinspection unchecked
            renderMods.add((PointModifier<Context>) mod);
        }

        // Prepare render modifiers
        ShapeBounds bounds = ShapeBounds.compute(points);
        context.bounds = bounds;
        context.totalPoints = points.size();
        context.shape = shape;
        for (PointModifier<Context> mod : renderMods) {
            mod.prepare(bounds);
        }

        // Render loop
        renderer.begin(points.size());
        for (int i = 0; i < points.size(); i++) {
            Vector3d p = points.get(i);
            context.reset();
            context.x = p.x; context.y = p.y; context.z = p.z;
            context.index = i;
            for (PointModifier<Context> mod : renderMods) {
                mod.modify(context);
            }
            renderer.renderPoint(context);
        }
        renderer.end();
    }

    /**
     * Forces the point cache to be regenerated on the next call to
     * {@link #getPoints(Shape)} or {@link #getPoints(Shape, Quaterniond)}.
     * Useful when an external change affects the shape that is not tracked by the cache key.
     */
    public void markDirty() {
        needsUpdate = true;
    }

    /**
     * @return the current sampling style (outline, surface, or fill)
     */
    @Override
    public SamplingStyle getStyle() {
        return style;
    }

    /**
     * Sets the sampling style and invalidates the point cache.
     *
     * @param style the new sampling style
     */
    @Override
    public void setStyle(SamplingStyle style) {
        this.style = style;
        this.needsUpdate = true;
    }

    /**
     * Returns the current point spacing density.
     * Only meaningful when density has been set explicitly via {@link #setDensity};
     * otherwise the value is overridden by the auto-computed density from {@link #setMaxPoints}.
     *
     * @return the current density value
     */
    @Override
    public double getDensity() {
        return density;
    }

    /**
     * Sets the point spacing density explicitly, overriding any max-points-based auto-density.
     * Clamps the value to at least {@code Shape.EPSILON}. Invalidates the point cache.
     *
     * @param density the desired point spacing; smaller values produce more points
     */
    @Override
    public void setDensity(double density) {
        this.density = Math.max(density, Shape.EPSILON);
        this.densityExplicit = true;
        this.needsUpdate = true;
    }

    /**
     * @return the maximum number of points that auto-density calculation will target
     */
    @Override
    public int getMaxPoints() {
        return maxPoints;
    }

    /**
     * Sets the target maximum number of points and switches to auto-density mode,
     * where density is derived from the shape geometry to approximate this count.
     * Clamps the value to at least {@code 1}. Invalidates the point cache.
     *
     * @param maxPoints the target maximum particle count
     */
    @Override
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = Math.max(1, maxPoints);
        this.densityExplicit = false;
        this.needsUpdate = true;
    }

    /**
     * Returns whether density was set explicitly via {@link #setDensity}.
     * When {@code false}, density is auto-computed from the shape to target {@link #getMaxPoints()}.
     *
     * @return {@code true} if density was set explicitly
     */
    @Override
    public boolean isDensityExplicit() {
        return densityExplicit;
    }

    /**
     * @return the comparator used to sort sampled points, or {@code null} for insertion order
     */
    @Override
    public Comparator<Vector3d> getOrdering() {
        return ordering;
    }

    /**
     * Sets the comparator used to sort sampled points after generation.
     * Pass {@code null} to disable sorting. Invalidates the point cache.
     *
     * @param ordering a comparator over {@link org.joml.Vector3d}, or {@code null}
     */
    @Override
    public void setOrdering(Comparator<Vector3d> ordering) {
        this.ordering = ordering;
        this.needsUpdate = true;
    }

    /**
     * Returns the unique identifier for this sampler instance.
     * The UUID is generated at construction time and never changes.
     *
     * @return the immutable UUID of this sampler
     */
    @Override
    public UUID getUUID() {
        return uuid;
    }

    /**
     * @return the client-provided {@link DrawContext}, or {@code null} if none is set
     */
    @Override
    public DrawContext getDrawContext() {
        return drawContext;
    }

    /**
     * Sets the client-provided rendering context.
     *
     * @param context the {@link DrawContext} to associate with this sampler
     */
    @Override
    public void setDrawContext(DrawContext context) { this.drawContext = context; }

    /**
     * Returns the live modifier list. Callers should prefer {@link #addModifier},
     * {@link #removeModifier}, and {@link #clearModifiers} to ensure cache invalidation.
     *
     * @return the mutable list of registered modifiers
     */
    @Override
    public List<PointModifier<?>> getModifiers() {
        return modifiers;
    }

    /**
     * Appends a modifier to the end of the modifier list.
     * Geometry modifiers ({@link PointModifier.PreRenderPointModifier}) additionally
     * invalidate the point cache.
     *
     * @param modifier the modifier to add
     */
    @Override
    public void addModifier(PointModifier<?> modifier) {
        modifiers.add(modifier);
        if (modifier instanceof PreRenderPointModifier) needsUpdate = true;
    }

    /**
     * Removes a modifier from the list if present.
     * If the removed modifier is a geometry modifier, the point cache is invalidated.
     *
     * @param modifier the modifier to remove
     */
    @Override
    public void removeModifier(PointModifier<?> modifier) {
        if (modifiers.remove(modifier)) {
            if (modifier instanceof PreRenderPointModifier) needsUpdate = true;
        }
    }

    /**
     * Removes all modifiers. If any geometry modifier was registered,
     * the point cache is invalidated.
     */
    @Override
    public void clearModifiers() {
        if (!modifiers.isEmpty()) {
            // If any geometry modifier was present, invalidate cache
            boolean hadGeo = modifiers.stream().anyMatch(m -> m instanceof PreRenderPointModifier);
            modifiers.clear();
            if (hadGeo) needsUpdate = true;
        }
    }

    private int computeModifierHash() {
        int hash = 1;
        for (PointModifier<?> mod : modifiers) {
            if (mod instanceof PreRenderPointModifier) hash = 31 * hash + mod.modifierHash();
        }
        return hash;
    }

    /**
     * Returns a deep copy of this sampler. The clone has an empty point cache
     * (forcing a fresh sample on first use) but otherwise copies all configuration,
     * including a deep-copied modifier list and a copied {@link DrawContext}.
     * The UUID is <em>not</em> shared; the clone inherits the same UUID value.
     *
     * @return a new {@code DefaultPointSampler} with the same configuration
     */
    @Override
    public DefaultPointSampler clone() {
        try {
            DefaultPointSampler copy = (DefaultPointSampler) super.clone();
            // Don't share the cache
            copy.cachedPoints = new ArrayList<>();
            copy.needsUpdate = true;
            // maxPoints and densityExplicit are primitives — copied by super.clone()
            if (drawContext != null)
                copy.drawContext = drawContext.copy();
            // Deep-copy modifiers
            copy.modifiers = new ArrayList<>(modifiers.size());
            for (PointModifier<?> mod : modifiers) {
                copy.modifiers.add(mod.clone());
            }
            return copy;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    private record CacheState(SamplingStyle style, int orientationHash, double scale,
                               int offsetHash, double density, long shapeVersion,
                               int modifierHash) {}
}
