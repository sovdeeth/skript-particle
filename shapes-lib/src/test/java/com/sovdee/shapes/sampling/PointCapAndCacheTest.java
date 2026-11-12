package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ScaleAxes;
import com.sovdee.shapes.modifiers.ScalingModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.shapes.Circle;
import com.sovdee.shapes.shapes.Cuboid;
import com.sovdee.shapes.shapes.Rectangle;
import com.sovdee.shapes.shapes.Shape;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the point cap, particle count targeting, and how the cache behaves across clones.
 */
class PointCapAndCacheTest {

    /** A cuboid whose density estimate is a fixed value, regardless of the requested point count. */
    static class FixedEstimateCuboid extends Cuboid {
        private final double estimate;

        FixedEstimateCuboid(double size, double estimate) {
            super(size, size, size);
            this.estimate = estimate;
        }

        @Override
        public double computeDensity(SamplingStyle style, int targetPointCount) {
            return estimate;
        }
    }

    /** A rectangle whose density estimate is always zero, as if it had no measurable size. */
    static class ZeroEstimateRectangle extends Rectangle {
        ZeroEstimateRectangle(double length, double width) {
            super(length, width, Plane.XZ);
        }

        @Override
        public double computeDensity(SamplingStyle style, int targetPointCount) {
            return 0;
        }
    }

    /** A geometry modifier that records the bounds it was prepared with. */
    static class BoundsRecorder extends PointModifier.PreRenderPointModifier {
        double maxX = Double.NaN;

        @Override public void prepare(ShapeBounds bounds) { maxX = bounds.maxX(); }
        @Override public void modify(PointContext point) {}
        @Override public int modifierHash() { return 11; }
    }

    // ----- Cap -----

    @Test
    void defaultDensity_isUsedWhenUnderTheCap() {
        Circle small = new Circle(1.0);
        int count = small.getPointSampler().getPoints(small).size();
        // circumference 2π at the default 0.25 spacing is about 25 points; the cap of 10 000 must not be a target
        assertTrue(count >= 20 && count <= 30, "expected about 25 points, got " + count);
    }

    @Test
    void cap_holdsWhenTheEstimateIgnoresTheTarget() {
        // 40^3 at 0.25 spacing would be over 4 million points
        Shape shape = new FixedEstimateCuboid(40, 0.25);
        shape.getPointSampler().setStyle(SamplingStyle.FILL);
        int count = shape.getPointSampler().getPoints(shape).size();
        assertTrue(count <= 10_000, "cap exceeded: " + count);
        assertTrue(count >= 2_000, "cap should be approached, not wildly undershot: " + count);
    }

    @Test
    void cap_holdsWhenTheEstimateIsFarTooFine() {
        Shape shape = new FixedEstimateCuboid(40, 0.0001);
        shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        shape.getPointSampler().setMaxPoints(500);
        int count = shape.getPointSampler().getPoints(shape).size();
        assertTrue(count <= 500, "cap exceeded: " + count);
        assertTrue(count >= 100, "cap should be approached, not wildly undershot: " + count);
    }

    @Test
    void effectiveDensity_reportsTheDensityInUse() {
        Circle small = new Circle(1.0);
        assertEquals(DefaultPointSampler.DEFAULT_DENSITY, small.getPointSampler().getEffectiveDensity(small), 1e-12);

        Circle huge = new Circle(1000.0);
        assertTrue(huge.getPointSampler().getEffectiveDensity(huge) > DefaultPointSampler.DEFAULT_DENSITY,
                "a capped shape should report the coarser density it was sampled at");
        assertEquals(DefaultPointSampler.DEFAULT_DENSITY, huge.getPointSampler().getDensity(), 1e-12,
                "the base density itself should be unchanged");
    }

    @Test
    void resetDensity_restoresTheDefault() {
        DefaultPointSampler sampler = new DefaultPointSampler();
        sampler.setDensity(0.01);
        sampler.resetDensity();
        assertFalse(sampler.isDensityExplicit());
        assertEquals(DefaultPointSampler.DEFAULT_DENSITY, sampler.getDensity(), 1e-12);
    }

    // ----- Hard limit -----

    @Test
    void explicitDensity_isCappedAtTheHardLimit() {
        // a filled 12^3 cube at 1/12 spacing would be about 2.5 million points
        Cuboid cube = new Cuboid(12, 12, 12);
        DefaultPointSampler sampler = (DefaultPointSampler) cube.getPointSampler();
        sampler.setStyle(SamplingStyle.FILL);
        sampler.setDensity(1 / 12.0);
        int count = sampler.getPoints(cube).size();
        assertTrue(count <= DefaultPointSampler.HARD_MAX_POINTS, "hard limit exceeded: " + count);
        assertTrue(count >= DefaultPointSampler.HARD_MAX_POINTS / 4, "hard limit should be approached, not wildly undershot: " + count);
        assertTrue(sampler.isDensityExplicit(), "hitting the hard limit should not drop the explicit density");
        assertEquals(1 / 12.0, sampler.getDensity(), 1e-12, "the requested density itself should be unchanged");
        assertTrue(sampler.getEffectiveDensity(cube) > 1 / 12.0, "the density in use should be coarser than requested");
    }

    @Test
    void explicitDensity_isExactUnderTheHardLimit() {
        // a misleading estimate must not coarsen an explicit density that fits
        Shape shape = new FixedEstimateCuboid(10, 5);
        DefaultPointSampler sampler = (DefaultPointSampler) shape.getPointSampler();
        sampler.setStyle(SamplingStyle.FILL);
        sampler.setDensity(0.5); // 21^3 = 9 261 points
        assertEquals(0.5, sampler.getEffectiveDensity(shape), 1e-12);
        assertTrue(sampler.getPoints(shape).size() > 5_000, "explicit density should be used exactly");
    }

    @Test
    void explicitDensity_isCappedWhenTheEstimateIsWrong() {
        Shape shape = new FixedEstimateCuboid(40, 0.0001);
        shape.getPointSampler().setStyle(SamplingStyle.FILL);
        shape.getPointSampler().setDensity(0.1); // 401^3 is about 64 million points
        int count = shape.getPointSampler().getPoints(shape).size();
        assertTrue(count <= DefaultPointSampler.HARD_MAX_POINTS, "hard limit exceeded: " + count);
    }

    @Test
    void setParticleCount_isCappedAtTheHardLimit() {
        Cuboid cube = new Cuboid(12, 12, 12);
        cube.getPointSampler().setStyle(SamplingStyle.FILL);
        cube.getPointSampler().setParticleCount(cube, 1_000_000);
        int count = cube.getPointSampler().getPointCount(cube);
        assertTrue(count <= DefaultPointSampler.HARD_MAX_POINTS, "hard limit exceeded: " + count);
    }

    @Test
    void setMaxPoints_isClampedToTheHardLimit() {
        DefaultPointSampler sampler = new DefaultPointSampler();
        sampler.setMaxPoints(5_000_000);
        assertEquals(DefaultPointSampler.HARD_MAX_POINTS, sampler.getMaxPoints());
    }

    // ----- Particle count -----

    @Test
    void setParticleCount_correctsABadEstimate() {
        Shape shape = new FixedEstimateCuboid(10, 0.25); // the estimate alone would give 68 921 points
        shape.getPointSampler().setStyle(SamplingStyle.FILL);
        shape.getPointSampler().setParticleCount(shape, 1000);
        int count = shape.getPointSampler().getPointCount(shape);
        // a filled cube can only produce cubes of whole numbers: 729, 1000, 1331...
        assertTrue(count >= 729 && count <= 1331, "expected close to 1000 points, got " + count);
    }

    @Test
    void setParticleCount_survivesAZeroEstimate() {
        Shape shape = new ZeroEstimateRectangle(10, 10);
        shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        shape.getPointSampler().setParticleCount(shape, 500);
        assertTrue(shape.getPointSampler().getDensity() > 0.01,
                "a zero estimate must not become a near-zero density: " + shape.getPointSampler().getDensity());
        int count = shape.getPointSampler().getPointCount(shape);
        assertTrue(count >= 400 && count <= 600, "expected close to 500 points, got " + count);
    }

    // ----- Cache and clones -----

    @Test
    void points_areUnmodifiable() {
        Circle circle = new Circle(2.0);
        List<Vector3d> points = circle.getPointSampler().getPoints(circle);
        assertThrows(UnsupportedOperationException.class, () -> points.add(new Vector3d()));
        assertThrows(UnsupportedOperationException.class, points::clear);
    }

    @Test
    void clone_startsWithTheCachedPoints() {
        Circle circle = new Circle(2.0);
        List<Vector3d> points = circle.getPointSampler().getPoints(circle);
        Shape copy = circle.clone();
        assertSame(points, copy.getPointSampler().getPoints(copy), "a clone should not need to resample");
    }

    @Test
    void clone_hasItsOwnIdentity() {
        Circle circle = new Circle(2.0);
        Shape copy = circle.clone();
        assertNotEquals(circle.getPointSampler().getUUID(), copy.getPointSampler().getUUID());
    }

    @Test
    void clone_andOriginal_invalidateIndependently() {
        Circle circle = new Circle(2.0);
        List<Vector3d> points = circle.getPointSampler().getPoints(circle);
        Circle copy = (Circle) circle.clone();

        copy.setRadius(5.0);
        List<Vector3d> copyPoints = copy.getPointSampler().getPoints(copy);
        assertNotSame(points, copyPoints, "changing the clone must resample the clone");
        assertSame(points, circle.getPointSampler().getPoints(circle), "changing the clone must not affect the original");

        // changing the original must not make it reuse points sampled for the clone, or vice versa
        circle.setRadius(1.0);
        assertNotSame(points, circle.getPointSampler().getPoints(circle));
        assertSame(copyPoints, copy.getPointSampler().getPoints(copy));
    }

    // ----- Geometry modifiers -----

    @Test
    void geometryModifiers_seeTheBoundsLeftByEarlierModifiers() {
        Circle circle = new Circle(1.0);
        BoundsRecorder recorder = new BoundsRecorder();
        circle.getPointSampler().addModifier(new ScalingModifier(2.0, 2.0, StandardInput.Y, ScaleAxes.X));
        circle.getPointSampler().addModifier(recorder);
        circle.getPointSampler().getPoints(circle);
        assertEquals(2.0, recorder.maxX, 1e-9, "the second modifier should see the scaled shape");
    }
}
