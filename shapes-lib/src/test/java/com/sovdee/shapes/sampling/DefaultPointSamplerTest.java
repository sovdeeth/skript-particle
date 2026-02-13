package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.modifiers.TaperModifier;
import com.sovdee.shapes.shapes.Circle;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPointSamplerTest {

    // ----- Minimal ShapeRenderer helpers -----

    static class CountingRenderer implements ShapeRenderer<PointContext> {
        int beginCount, endCount, renderCount;
        int lastBeginTotal;
        final PointContext ctx = new PointContext();

        @Override public void begin(int total) { beginCount++; lastBeginTotal = total; }
        @Override public void renderPoint(PointContext p) { renderCount++; }
        @Override public PointContext getContext() { return ctx; }
        @Override public void end() { endCount++; }
    }

    /** A render modifier (not PreRender) that counts prepare() and modify() calls. */
    static class CountingRenderModifier implements PointModifier<PointContext> {
        int prepareCount, modifyCount;

        @Override public void prepare(ShapeBounds b) { prepareCount++; }
        @Override public void modify(PointContext p) { modifyCount++; }
        @Override public int modifierHash() { return 42; }
        @Override public PointModifier<PointContext> clone() {
            try { return (CountingRenderModifier) super.clone(); }
            catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        }
    }

    /** A geometry modifier (PreRender) that counts calls. */
    static class CountingGeoModifier extends PointModifier.PreRenderPointModifier {
        int modifyCount;
        @Override public void modify(PointContext p) { modifyCount++; }
        @Override public int modifierHash() { return 7; }
    }

    // A PointContext subclass for context-type validation tests
    static class SubContext extends PointContext {
        int extra;
        @Override public void reset() { extra = 0; }
    }

    static class SubContextModifier implements PointModifier<SubContext> {
        @Override public void modify(SubContext p) { p.extra = 99; }
        @Override public int modifierHash() { return 1; }
        @Override public Class<? extends PointContext> contextType() { return SubContext.class; }
        @Override public PointModifier<SubContext> clone() {
            try { return (SubContextModifier) super.clone(); }
            catch (CloneNotSupportedException e) { throw new RuntimeException(e); }
        }
    }

    static class SubContextRenderer implements ShapeRenderer<SubContext> {
        final SubContext ctx = new SubContext();
        int renderCount;
        @Override public void begin(int total) {}
        @Override public void renderPoint(SubContext p) { renderCount++; }
        @Override public SubContext getContext() { return ctx; }
        @Override public void end() {}
    }

    // ----- Test setup -----

    Circle circle;
    DefaultPointSampler sampler;

    @BeforeEach
    void setUp() {
        circle = new Circle(1.0);
        sampler = new DefaultPointSampler();
        sampler.setDensity(0.5); // coarse for speed
    }

    // ----- Caching tests -----

    @Test
    void cacheHit_sameState() {
        List<Vector3d> first = sampler.getPoints(circle);
        List<Vector3d> second = sampler.getPoints(circle);
        assertSame(first, second, "Same state should return cached list");
    }

    @Test
    void cacheMiss_onDensityChange() {
        List<Vector3d> first = sampler.getPoints(circle);
        sampler.setDensity(0.2);
        List<Vector3d> second = sampler.getPoints(circle);
        assertNotSame(first, second, "Density change should invalidate cache");
    }

    @Test
    void cacheMiss_onStyleChange() {
        List<Vector3d> first = sampler.getPoints(circle);
        sampler.setStyle(SamplingStyle.SURFACE);
        List<Vector3d> second = sampler.getPoints(circle);
        assertNotSame(first, second, "Style change should invalidate cache");
    }

    @Test
    void cacheMiss_onShapeInvalidate() {
        List<Vector3d> first = sampler.getPoints(circle);
        circle.setRadius(2.0); // bumps version
        List<Vector3d> second = sampler.getPoints(circle);
        assertNotSame(first, second, "Shape geometry change should invalidate cache");
    }

    @Test
    void cacheHit_renderModifierAdded() {
        List<Vector3d> first = sampler.getPoints(circle);
        // Render modifiers don't affect geometry; cache should survive
        sampler.addModifier(new CountingRenderModifier());
        List<Vector3d> second = sampler.getPoints(circle);
        assertSame(first, second, "Adding a render modifier must not invalidate geometry cache");
    }

    @Test
    void cacheMiss_geoModifierAdded() {
        List<Vector3d> first = sampler.getPoints(circle);
        sampler.addModifier(new CountingGeoModifier()); // PreRenderPointModifier → calls markDirty
        List<Vector3d> second = sampler.getPoints(circle);
        assertNotSame(first, second, "Adding a geo modifier must invalidate cache");
    }

    // ----- Geometry modifier dispatch -----

    @Test
    void geoModifier_appliedToPoints() {
        // Taper from 0 to 0 collapses all XZ to zero
        sampler.addModifier(new TaperModifier(0.0, 0.0));
        List<Vector3d> points = sampler.getPoints(circle);
        for (Vector3d p : points) {
            assertEquals(0.0, p.x, 1e-9, "TaperModifier(0,0) should zero all X");
            assertEquals(0.0, p.z, 1e-9, "TaperModifier(0,0) should zero all Z");
        }
    }

    @Test
    void geoModifier_notCalledDuringRender() {
        CountingGeoModifier geoMod = new CountingGeoModifier();
        sampler.addModifier(geoMod);

        int pointCount = sampler.getPoints(circle).size();
        int afterGetPoints = geoMod.modifyCount;
        assertEquals(pointCount, afterGetPoints, "Geo modifier should be called once per point in getPoints()");

        // render() should NOT re-invoke the geo modifier
        CountingRenderer renderer = new CountingRenderer();
        sampler.render(circle, circle.getOrientation(), renderer);
        assertEquals(afterGetPoints, geoMod.modifyCount, "Geo modifier must not run again during render()");
    }

    // ----- Render loop -----

    @Test
    void render_callsBeginAndEnd() {
        CountingRenderer renderer = new CountingRenderer();
        sampler.render(circle, circle.getOrientation(), renderer);
        assertEquals(1, renderer.beginCount);
        assertEquals(1, renderer.endCount);
    }

    @Test
    void render_callsRenderPointForEachPoint() {
        CountingRenderer renderer = new CountingRenderer();
        sampler.render(circle, circle.getOrientation(), renderer);
        int pointCount = sampler.getPoints(circle).size();
        assertEquals(pointCount, renderer.renderCount,
                "renderPoint() should be called exactly once per point");
    }

    @Test
    void render_preparesModifier() {
        CountingRenderModifier renderMod = new CountingRenderModifier();
        sampler.addModifier(renderMod);
        CountingRenderer renderer = new CountingRenderer();

        sampler.render(circle, circle.getOrientation(), renderer);
        assertEquals(1, renderMod.prepareCount, "Render modifier prepare() must be called once per render()");

        sampler.render(circle, circle.getOrientation(), renderer);
        assertEquals(2, renderMod.prepareCount, "Prepare must be called once per render() invocation");
    }

    // ----- Context type validation -----

    @Test
    void render_rejectsIncompatibleModifier() {
        // SubContextModifier requires SubContext, but renderer provides plain PointContext
        sampler.addModifier(new SubContextModifier());
        CountingRenderer renderer = new CountingRenderer(); // provides PointContext
        assertThrows(IllegalStateException.class,
                () -> sampler.render(circle, circle.getOrientation(), renderer),
                "Should throw when modifier context type is not assignable from renderer context type");
    }

    @Test
    void render_acceptsCompatibleModifier() {
        sampler.addModifier(new CountingRenderModifier()); // contextType=PointContext
        CountingRenderer renderer = new CountingRenderer(); // provides PointContext
        assertDoesNotThrow(() -> sampler.render(circle, circle.getOrientation(), renderer));
    }

    @Test
    void render_acceptsSubtypeContext() {
        // Modifier needs PointContext; renderer provides SubContext (which IS a PointContext)
        sampler.addModifier(new CountingRenderModifier()); // contextType=PointContext
        SubContextRenderer renderer = new SubContextRenderer(); // provides SubContext
        assertDoesNotThrow(() -> sampler.render(circle, circle.getOrientation(), renderer));
    }

    // ----- Transform pipeline -----

    @Test
    void getPoints_appliesScale() {
        circle.setScale(2.0);
        List<Vector3d> points = sampler.getPoints(circle, new Quaterniond());
        for (Vector3d p : points) {
            double dist = Math.sqrt(p.x * p.x + p.z * p.z);
            assertEquals(2.0, dist, 1e-6, "Scale=2 on radius=1 circle → points at distance 2");
        }
    }

    @Test
    void getPoints_appliesOffset() {
        double offsetX = 5.0;
        circle.setOffset(new Vector3d(offsetX, 0, 0));

        // Get points without offset for comparison
        Circle baseCircle = new Circle(1.0);
        DefaultPointSampler baseSampler = new DefaultPointSampler();
        baseSampler.setDensity(0.5);
        List<Vector3d> base = new ArrayList<>(baseSampler.getPoints(baseCircle, new Quaterniond()));

        List<Vector3d> offset = sampler.getPoints(circle, new Quaterniond());
        assertEquals(base.size(), offset.size());
        for (int i = 0; i < base.size(); i++) {
            assertEquals(base.get(i).x + offsetX, offset.get(i).x, 1e-6,
                    "Offset X should shift all points");
        }
    }

    // ----- Auto-density (max-points) -----

    @Test
    void autoDensity_capsPointCount() {
        // Fresh sampler — no explicit density. Large circle should be capped.
        Circle large = new Circle(10.0);
        DefaultPointSampler fresh = new DefaultPointSampler();
        // Default cap is 10 000; default density (0.25) on radius-10 circle = ~628 points — well under cap.
        // Use a tiny cap so the test doesn't depend on default density math.
        fresh.setMaxPoints(100);
        List<Vector3d> points = fresh.getPoints(large);
        // Allow a 5% overshoot: computeDensity is an approximation and loop arithmetic
        // can produce 1-2 extra points due to floating-point step accumulation.
        assertTrue(points.size() <= 105,
                "Auto-density should cap point count near maxPoints (≤105), got " + points.size());
    }

    @Test
    void autoDensity_defaultCapIsApplied() {
        // A very large circle with default sampler (no explicit density, default cap = 10 000).
        Circle huge = new Circle(1000.0);
        DefaultPointSampler fresh = new DefaultPointSampler();
        assertFalse(fresh.isDensityExplicit());
        List<Vector3d> points = fresh.getPoints(huge);
        assertTrue(points.size() <= 10_500,
                "Default 10 000 cap should be respected (≤10 500 allowing FP overshoot), got " + points.size());
    }

    @Test
    void explicitDensity_overridesAutoCap() {
        Circle large = new Circle(10.0);
        DefaultPointSampler s = new DefaultPointSampler();
        s.setMaxPoints(50);           // very tight auto cap
        s.setDensity(0.5);            // explicit density
        assertTrue(s.isDensityExplicit());

        List<Vector3d> points = s.getPoints(large);
        // With radius 10 and density 0.5, we expect ~125 points (2π*10 / (1/0.5)).
        // The auto cap of 50 must NOT be applied.
        assertTrue(points.size() > 50,
                "Explicit density must not be limited by maxPoints, got " + points.size());
    }

    @Test
    void setMaxPoints_resetsToAutoMode() {
        Circle large = new Circle(10.0);
        DefaultPointSampler s = new DefaultPointSampler();
        s.setDensity(0.5);             // explicit
        assertTrue(s.isDensityExplicit());

        s.setMaxPoints(50);            // resets to auto
        assertFalse(s.isDensityExplicit(), "setMaxPoints should reset densityExplicit to false");

        List<Vector3d> points = s.getPoints(large);
        assertTrue(points.size() <= 53,
                "After setMaxPoints auto-mode should be re-enabled, got " + points.size());
    }

    @Test
    void setMaxPoints_clampedToOne() {
        DefaultPointSampler s = new DefaultPointSampler();
        s.setMaxPoints(0);
        assertEquals(1, s.getMaxPoints(), "setMaxPoints(0) should clamp to 1");
        s.setMaxPoints(-5);
        assertEquals(1, s.getMaxPoints(), "setMaxPoints(-5) should clamp to 1");
    }

    @Test
    void autoDensity_cacheInvalidatesOnShapeChange() {
        Circle large = new Circle(10.0);
        DefaultPointSampler s = new DefaultPointSampler();
        s.setMaxPoints(200);

        List<Vector3d> first = s.getPoints(large);
        large.setRadius(20.0); // geometry change
        List<Vector3d> second = s.getPoints(large);
        assertNotSame(first, second, "Cache must invalidate when shape geometry changes in auto-density mode");
        assertTrue(second.size() <= 210,
                "After geometry change auto-density should still cap near maxPoints, got " + second.size());
    }
}
