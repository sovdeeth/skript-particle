package com.sovdee.shapes.shapes;

import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ScaleAxes;
import com.sovdee.shapes.modifiers.ScalingModifier;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every draw clones every shape it draws, so a clone has to be an exact, fully independent copy that can still
 * reuse the original's cached points.
 */
class ShapeCloneTest {

    private static String name(Shape shape) {
        return shape.getClass().getSimpleName();
    }

    private static List<Vector3d> copy(List<Vector3d> points) {
        List<Vector3d> out = new ArrayList<>(points.size());
        for (Vector3d p : points)
            out.add(new Vector3d(p));
        return out;
    }

    private static void assertSamePoints(List<Vector3d> expected, List<Vector3d> actual, String message) {
        assertEquals(expected.size(), actual.size(), message + ": point count");
        for (int i = 0; i < expected.size(); i++)
            assertTrue(expected.get(i).equals(actual.get(i), 1e-9), message + ": point " + i + " " + expected.get(i) + " vs " + actual.get(i));
    }

    /** A shape with a non-default transform and sampler setup, so that copying all of it is actually tested. */
    private static Shape configured(Shape shape) {
        shape.setOrientation(new Quaterniond().rotateXYZ(0.3, 0.7, -0.2));
        shape.setScale(1.5);
        shape.setOffset(new Vector3d(1, -2, 3));
        shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        shape.getPointSampler().setDensity(0.4);
        return shape;
    }

    @Test
    void clone_samplesTheSamePointsForEveryShape() {
        for (Shape shape : TestShapes.all()) {
            configured(shape);
            List<Vector3d> original = copy(shape.getPointSampler().getPoints(shape));
            Shape clone = shape.clone();
            assertSame(shape.getClass(), clone.getClass());
            assertSamePoints(original, clone.getPointSampler().getPoints(clone), name(shape));
        }
    }

    @Test
    void clone_matchesWhenItsCacheIsRegenerated() {
        // the clone must copy the shape's own dimensions, not only the cached points
        for (Shape shape : TestShapes.all()) {
            configured(shape);
            List<Vector3d> original = copy(shape.getPointSampler().getPoints(shape));
            Shape clone = shape.clone();
            if (clone.getPointSampler() instanceof com.sovdee.shapes.sampling.DefaultPointSampler sampler)
                sampler.markDirty();
            assertSamePoints(original, clone.getPointSampler().getPoints(clone), name(shape) + " (regenerated)");
        }
    }

    @Test
    void clone_reusesTheOriginalsCachedPoints() {
        for (Shape shape : TestShapes.all()) {
            List<Vector3d> points = shape.getPointSampler().getPoints(shape);
            Shape clone = shape.clone();
            assertSame(points, clone.getPointSampler().getPoints(clone),
                    name(shape) + ": an unchanged clone should not resample");
        }
    }

    @Test
    void clone_copiesTransformAndSamplerSettings() {
        Comparator<Vector3d> ordering = Comparator.comparingDouble(v -> v.y);
        for (Shape shape : TestShapes.all()) {
            configured(shape);
            shape.getPointSampler().setOrdering(ordering);
            Shape clone = shape.clone();
            String n = name(shape);
            assertTrue(shape.getOrientation().equals(clone.getOrientation(), 1e-12), n + " orientation");
            assertEquals(shape.getScale(), clone.getScale(), n + " scale");
            assertTrue(shape.getOffset().equals(clone.getOffset(), 1e-12), n + " offset");
            assertEquals(shape.isDynamic(), clone.isDynamic(), n + " dynamic");
            PointSampler a = shape.getPointSampler(), b = clone.getPointSampler();
            assertNotSame(a, b, n + " sampler must be its own");
            assertEquals(a.getStyle(), b.getStyle(), n + " style");
            assertEquals(a.getDensity(), b.getDensity(), n + " density");
            assertEquals(a.isDensityExplicit(), b.isDensityExplicit(), n + " explicit density");
            assertEquals(a.getMaxPoints(), b.getMaxPoints(), n + " max points");
            assertSame(ordering, b.getOrdering(), n + " ordering");
        }
    }

    @Test
    void changingTheClone_leavesTheOriginalAlone() {
        for (Shape shape : TestShapes.all()) {
            configured(shape);
            List<Vector3d> before = shape.getPointSampler().getPoints(shape);
            List<Vector3d> snapshot = copy(before);
            Quaterniond orientation = new Quaterniond(shape.getOrientation());
            Vector3d offset = new Vector3d(shape.getOffset());

            Shape clone = shape.clone();
            clone.getOrientation().rotateY(1.0);   // even mutating the returned instance must not leak
            clone.setOrientation(new Quaterniond().rotateX(1.2));
            clone.getOffset().add(5, 5, 5);
            clone.setOffset(new Vector3d(-4, 0, 0));
            clone.setScale(0.25);
            clone.getPointSampler().setStyle(SamplingStyle.OUTLINE);
            clone.getPointSampler().setDensity(0.9);
            clone.getPointSampler().getPoints(clone);

            String n = name(shape);
            assertTrue(orientation.equals(shape.getOrientation(), 1e-12), n + " orientation leaked");
            assertTrue(offset.equals(shape.getOffset(), 1e-12), n + " offset leaked");
            assertEquals(1.5, shape.getScale(), n + " scale leaked");
            assertEquals(SamplingStyle.SURFACE, shape.getPointSampler().getStyle(), n + " style leaked");
            assertEquals(0.4, shape.getPointSampler().getDensity(), n + " density leaked");
            assertSame(before, shape.getPointSampler().getPoints(shape), n + ": the original should still be cached");
            assertSamePoints(snapshot, shape.getPointSampler().getPoints(shape), n);
        }
    }

    @Test
    void changingTheOriginal_leavesTheCloneAlone() {
        for (Shape shape : TestShapes.all()) {
            configured(shape);
            Shape clone = shape.clone();
            List<Vector3d> cloned = copy(clone.getPointSampler().getPoints(clone));
            shape.setOrientation(new Quaterniond().rotateZ(2.0));
            shape.setScale(3);
            shape.getPointSampler().setDensity(0.8);
            shape.getPointSampler().getPoints(shape);
            assertSamePoints(cloned, clone.getPointSampler().getPoints(clone), name(shape));
        }
    }

    @Test
    void changingACloneDimension_leavesTheOriginalAlone() {
        Circle circle = new Circle(2);
        List<Vector3d> before = circle.getPointSampler().getPoints(circle);
        Circle clone = (Circle) circle.clone();
        clone.setRadius(5);
        assertEquals(2, circle.getRadius(), 1e-12);
        assertEquals(5, clone.getRadius(), 1e-12);
        assertSame(before, circle.getPointSampler().getPoints(circle), "the original should still be cached");
        double farthest = clone.getPointSampler().getPoints(clone).stream().mapToDouble(Vector3d::length).max().orElse(0);
        assertEquals(5, farthest, 1e-9, "the clone should be sampled at its new radius");

        Cuboid cuboid = new Cuboid(1, 2, 3);
        Cuboid cuboidClone = (Cuboid) cuboid.clone();
        cuboidClone.setLength(10);
        assertEquals(1, cuboid.getLength(), 1e-12);
    }

    @Test
    void clone_deepCopiesModifiers() {
        Circle circle = new Circle(2);
        ScalingModifier scale = new ScalingModifier(1, 0, StandardInput.Y, ScaleAxes.XZ);
        circle.getPointSampler().addModifier(scale);
        Circle clone = (Circle) circle.clone();

        List<PointModifier<?>> cloned = clone.getPointSampler().getModifiers();
        assertEquals(1, cloned.size());
        assertNotSame(scale, cloned.getFirst(), "modifiers must be copied, not shared");
        assertEquals(scale.modifierHash(), cloned.getFirst().modifierHash(), "the copy must be configured the same");

        clone.getPointSampler().addModifier(new ScalingModifier(0, 1, StandardInput.X, ScaleAxes.XYZ));
        assertEquals(1, circle.getPointSampler().getModifiers().size(), "adding to the clone must not change the original");
        clone.getPointSampler().clearModifiers();
        assertEquals(1, circle.getPointSampler().getModifiers().size(), "clearing the clone must not change the original");
    }

    @Test
    void cloneOfAClone_isStillIndependent() {
        Circle circle = new Circle(2);
        Circle first = (Circle) circle.clone();
        Circle second = (Circle) first.clone();
        second.setRadius(7);
        first.setRadius(4);
        assertEquals(2, circle.getRadius(), 1e-12);
        assertEquals(4, first.getRadius(), 1e-12);
        assertEquals(7, second.getRadius(), 1e-12);
    }
}
