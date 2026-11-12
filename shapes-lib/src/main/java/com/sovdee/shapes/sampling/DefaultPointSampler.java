package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.PointModifier.PreRenderPointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Default implementation of {@link PointSampler}.
 * <p>
 * The last sampled points are cached as an immutable snapshot, which is shared with clones of this sampler
 * until either side's configuration or shape changes.
 */
public class DefaultPointSampler implements PointSampler {

    /** The point spacing used when no density has been set explicitly. */
    public static final double DEFAULT_DENSITY = 0.25;

    private static final int DEFAULT_MAX_POINTS = 10_000;
    /** How many times a sampling may be retried at a coarser density to get under the cap. */
    private static final int MAX_CAP_ATTEMPTS = 48;
    /** How many samplings {@link #setParticleCount(Shape, int)} may use to home in on its target. */
    private static final int MAX_COUNT_REFINEMENTS = 10;

    private SamplingStyle style = SamplingStyle.OUTLINE;
    private double density = DEFAULT_DENSITY;
    private int maxPoints = DEFAULT_MAX_POINTS;
    private boolean densityExplicit = false;
    private Comparator<Vector3d> ordering;
    private UUID uuid;
    private DrawContext drawContext;
    private List<PointModifier<?>> modifiers = new ArrayList<>();

    // Cache: an immutable snapshot, swapped atomically and shared with clones.
    private volatile Cache cache;

    /**
     * Creates a new {@code DefaultPointSampler} with default settings:
     * {@link SamplingStyle#OUTLINE}, density {@code 0.25}, max points {@code 10 000},
     * no ordering, no modifiers, and an empty point cache.
     * A fresh {@link UUID} is assigned at construction time.
     */
    public DefaultPointSampler() {
        this.uuid = UUID.randomUUID();
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
    public synchronized List<Vector3d> getPoints(Shape shape, Quaterniond orientation) {
        // let the shape pick up any changes to its external inputs before checking the cache
        shape.refresh();

        CacheState state = new CacheState(style, new Quaterniond(orientation), shape.getScale(), shape.getOffset(),
                density, densityExplicit, maxPoints, shape.getVersion(), computeModifierHash());
        Cache cache = this.cache;
        if (cache != null && cache.state.equals(state))
            return cache.points;

        Sample sample = densityExplicit
                ? new Sample(generate(shape, style, density, Integer.MAX_VALUE), density)
                : sampleWithinCap(shape);
        List<Vector3d> points = sample.points;

        applyGeometryModifiers(shape, points);

        double scale = shape.getScale();
        Vector3d offset = shape.getOffset();
        for (Vector3d point : points) {
            orientation.transform(point);
            point.mul(scale);
            point.add(offset);
        }

        // List#sort is stable, so points the ordering considers equal are all kept in their generated order.
        if (ordering != null) {
            points.sort(ordering);
        }

        List<Vector3d> result = Collections.unmodifiableList(points);
        this.cache = new Cache(state, result, sample.density);
        return result;
    }

    /**
     * Generates the untransformed points of a shape at the given density.
     *
     * @param limit the most points that may be generated before a {@link PointLimitExceededException} is thrown
     */
    private static List<Vector3d> generate(Shape shape, SamplingStyle style, double density, int limit) {
        List<Vector3d> points = new BoundedList(limit);
        shape.beforeSampling(density);
        switch (style) {
            case OUTLINE -> shape.generateOutline(points, density);
            case SURFACE -> shape.generateSurface(points, density);
            case FILL -> shape.generateFilled(points, density);
        }
        shape.afterSampling(points);
        return points;
    }

    /**
     * Samples a shape at the current density, coarsening the density as needed so that no more than
     * {@link #getMaxPoints()} points are produced.
     */
    private Sample sampleWithinCap(Shape shape) {
        double working = density;
        // the shape's own estimate is only a starting point: it is never trusted to be finer than the base density.
        double estimate = shape.computeDensity(style, maxPoints);
        if (Double.isFinite(estimate) && estimate > working)
            working = estimate;

        // generating far more points than the cap is wasted work, so stop early and coarsen instead
        int limit = (int) Math.min((long) maxPoints * 2 + 64, Integer.MAX_VALUE);
        double dimensions = switch (style) {
            case OUTLINE -> 1;
            case SURFACE -> 2;
            case FILL -> 3;
        };
        List<Vector3d> points = null;
        for (int attempt = 0; attempt < MAX_CAP_ATTEMPTS; attempt++) {
            try {
                points = generate(shape, style, working, limit);
            } catch (PointLimitExceededException e) {
                points = null;
                working *= 2;
                continue;
            }
            if (points.size() <= maxPoints)
                return new Sample(points, working);
            // slightly overshoot so shapes that respond as expected land under the cap in one step
            working *= Math.pow((double) points.size() / maxPoints, 1 / dimensions) * 1.02;
        }
        // The shape did not respond to density. Keep what fits rather than exceeding the cap.
        if (points == null)
            points = new ArrayList<>();
        else if (points.size() > maxPoints)
            points = new ArrayList<>(points.subList(0, maxPoints));
        return new Sample(points, working);
    }

    /**
     * Applies the geometry modifiers in order. Each modifier sees the bounds of the points as the previous
     * modifiers left them.
     */
    @SuppressWarnings("unchecked")
    private void applyGeometryModifiers(Shape shape, List<Vector3d> points) {
        PointContext ctx = null;
        for (PointModifier<?> modifier : modifiers) {
            if (!(modifier instanceof PreRenderPointModifier))
                continue;
            PointModifier<PointContext> mod = (PointModifier<PointContext>) modifier;
            if (ctx == null) {
                ctx = new PointContext();
                ctx.shape = shape;
                ctx.totalPoints = points.size();
            }
            ShapeBounds bounds = ShapeBounds.compute(points);
            ctx.bounds = bounds;
            mod.prepare(bounds);
            for (int i = 0; i < points.size(); i++) {
                Vector3d p = points.get(i);
                ctx.x = p.x; ctx.y = p.y; ctx.z = p.z;
                ctx.index = i;
                mod.modify(ctx);
                p.x = ctx.x; p.y = ctx.y; p.z = ctx.z;
            }
        }
    }

    /**
     * Returns the density the shape is actually sampled at. This differs from {@link #getDensity()} when the
     * density is not explicit and had to be coarsened to stay within {@link #getMaxPoints()}.
     */
    @Override
    public synchronized double getEffectiveDensity(Shape shape) {
        getPoints(shape);
        Cache cache = this.cache;
        return cache != null ? cache.density : density;
    }

    /**
     * Sets an explicit density so that the shape samples to approximately the given number of points.
     * Starts from the shape's {@link Shape#computeDensity(SamplingStyle, int) estimate} and corrects it by
     * measuring how the point count actually responds, so shapes without an exact formula still land close.
     */
    @Override
    public synchronized void setParticleCount(Shape shape, int count) {
        int target = Math.max(count, 1);
        shape.refresh();

        // Start no finer than the density currently in use: the first sample then costs no more than drawing the
        // shape as it is, and every later step towards more points is bounded.
        double current = getEffectiveDensity(shape);
        double estimate = shape.computeDensity(style, target);
        if (Double.isFinite(estimate) && estimate > current)
            current = estimate;

        int limit = (int) Math.min((long) target * 8 + 1024, Integer.MAX_VALUE);
        double dimensions = switch (style) {
            case OUTLINE -> 1;
            case SURFACE -> 2;
            case FILL -> 3;
        };
        long tolerance = Math.max(1, target / 50);
        double bestDensity = current;
        long bestError = Long.MAX_VALUE;
        double previousDensity = Double.NaN;
        int previousCount = -1;

        for (int i = 0; i < MAX_COUNT_REFINEMENTS; i++) {
            int actual;
            try {
                actual = generate(shape, style, current, limit).size();
            } catch (PointLimitExceededException e) {
                // far too many points: back off and try again
                previousCount = -1;
                current *= 2;
                continue;
            }
            long error = Math.abs((long) actual - target);
            if (error < bestError) {
                bestError = error;
                bestDensity = current;
            }
            if (error <= tolerance)
                break;

            // measure how the count scales with density (count ~ density^-dimensions)
            if (previousCount > 0 && actual > 0 && actual != previousCount && current != previousDensity) {
                double measured = Math.log((double) previousCount / actual) / Math.log(current / previousDensity);
                if (Double.isFinite(measured))
                    dimensions = Math.clamp(measured, 1, 3);
            }
            previousDensity = current;
            previousCount = actual;

            double next = current * Math.pow(Math.max(actual, 1) / (double) target, 1 / dimensions);
            if (actual < target) {
                // when adding points, never step further than is safe for a 3-dimensional response
                double safest = current * Math.pow(Math.max(actual, 1) / (target * 4.0), 1 / 3.0);
                next = Math.max(next, safest);
            }
            next = Math.max(next, Shape.EPSILON);
            if (next == current)
                break;
            current = next;
        }
        setDensity(bestDensity);
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
        preparePass(shape, points, renderer.getContext()).renderBatch(points, renderer);
    }

    @Override
    public <Context extends PointContext> RenderPass<Context> preparePass(Shape shape, List<Vector3d> points, Context context) {
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

        // Prepare render modifiers against the full point set
        ShapeBounds bounds = ShapeBounds.compute(points);
        context.bounds = bounds;
        context.totalPoints = points.size();
        context.shape = shape;
        for (PointModifier<Context> mod : renderMods) {
            mod.prepare(bounds);
        }
        return new RenderPass<>(context, renderMods);
    }

    /**
     * Forces the point cache to be regenerated on the next call to
     * {@link #getPoints(Shape)} or {@link #getPoints(Shape, Quaterniond)}.
     * Useful when an external change affects the shape that is not tracked by the cache key.
     */
    public void markDirty() {
        cache = null;
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
    }

    /**
     * Returns the current point spacing density.
     * This is the base spacing between points. When it has not been set explicitly via {@link #setDensity},
     * sampling may use a coarser density to stay within {@link #getMaxPoints()};
     * see {@link #getEffectiveDensity(Shape)}.
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
    }

    /**
     * @return the maximum number of points a sampling may produce while the density is not explicit
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
    }

    /**
     * Returns to the default density, limited by {@link #getMaxPoints()}.
     */
    @Override
    public void resetDensity() {
        this.density = DEFAULT_DENSITY;
        this.densityExplicit = false;
    }

    /**
     * Returns whether density was set explicitly via {@link #setDensity}.
     * When {@code false}, the density is coarsened as needed to stay within {@link #getMaxPoints()}.
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
        this.cache = null;
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
        if (modifier instanceof PreRenderPointModifier) cache = null;
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
            if (modifier instanceof PreRenderPointModifier) cache = null;
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
            if (hadGeo) cache = null;
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
    public synchronized DefaultPointSampler clone() {
        try {
            DefaultPointSampler copy = (DefaultPointSampler) super.clone();
            // The copy is its own sampler, but keeps the cached points: they are immutable, and stay valid for
            // as long as the copy's configuration and shape match the ones they were sampled with.
            copy.uuid = UUID.randomUUID();
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

    private record CacheState(SamplingStyle style, Quaterniond orientation, double scale, Vector3d offset,
                              double density, boolean densityExplicit, int maxPoints, long shapeVersion,
                              int modifierHash) {}

    /**
     * @param density the density the points were actually sampled at
     */
    private record Cache(CacheState state, List<Vector3d> points, double density) {}

    private record Sample(List<Vector3d> points, double density) {}

    /**
     * A list that refuses to grow beyond a set size, so that a bad density fails fast instead of exhausting memory.
     */
    private static final class BoundedList extends ArrayList<Vector3d> {

        private final int limit;

        BoundedList(int limit) {
            this.limit = limit;
        }

        @Override
        public boolean add(Vector3d vector) {
            if (size() >= limit)
                throw new PointLimitExceededException(limit);
            return super.add(vector);
        }

        @Override
        public void add(int index, Vector3d vector) {
            if (size() >= limit)
                throw new PointLimitExceededException(limit);
            super.add(index, vector);
        }

        @Override
        public boolean addAll(Collection<? extends Vector3d> vectors) {
            if ((long) size() + vectors.size() > limit)
                throw new PointLimitExceededException(limit);
            return super.addAll(vectors);
        }

        @Override
        public boolean addAll(int index, Collection<? extends Vector3d> vectors) {
            if ((long) size() + vectors.size() > limit)
                throw new PointLimitExceededException(limit);
            return super.addAll(index, vectors);
        }

        @Override
        public void ensureCapacity(int minCapacity) {
            // shapes pre-size from rough estimates, which can be wildly off; never reserve beyond the limit
            super.ensureCapacity(Math.min(minCapacity, limit));
        }
    }
}
