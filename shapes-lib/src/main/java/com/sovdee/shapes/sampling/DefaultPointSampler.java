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

    public DefaultPointSampler() {
        this.uuid = UUID.randomUUID();
        this.lastState = new CacheState(style, 0, 1.0, 0, density, 0, 0);
    }


    @Override
    public List<Vector3d> getPoints(Shape shape) {
        return getPoints(shape, shape.getOrientation());
    }

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

    public void markDirty() {
        needsUpdate = true;
    }

    @Override
    public SamplingStyle getStyle() { return style; }

    @Override
    public void setStyle(SamplingStyle style) {
        this.style = style;
        this.needsUpdate = true;
    }

    @Override
    public double getDensity() { return density; }

    @Override
    public void setDensity(double density) {
        this.density = Math.max(density, Shape.EPSILON);
        this.densityExplicit = true;
        this.needsUpdate = true;
    }

    @Override
    public int getMaxPoints() { return maxPoints; }

    @Override
    public void setMaxPoints(int maxPoints) {
        this.maxPoints = Math.max(1, maxPoints);
        this.densityExplicit = false;
        this.needsUpdate = true;
    }

    @Override
    public boolean isDensityExplicit() { return densityExplicit; }

    @Override
    public Comparator<Vector3d> getOrdering() { return ordering; }

    @Override
    public void setOrdering(Comparator<Vector3d> ordering) {
        this.ordering = ordering;
        this.needsUpdate = true;
    }

    @Override
    public UUID getUUID() { return uuid; }

    @Override
    public DrawContext getDrawContext() { return drawContext; }

    @Override
    public void setDrawContext(DrawContext context) { this.drawContext = context; }

    @Override
    public List<PointModifier<?>> getModifiers() { return modifiers; }

    @Override
    public void addModifier(PointModifier<?> modifier) {
        modifiers.add(modifier);
        if (modifier instanceof PreRenderPointModifier) needsUpdate = true;
    }

    @Override
    public void removeModifier(PointModifier<?> modifier) {
        if (modifiers.remove(modifier)) {
            if (modifier instanceof PreRenderPointModifier) needsUpdate = true;
        }
    }

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
