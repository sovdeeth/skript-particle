package com.sovdee.shapes.modifiers;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.Cuboid;
import com.sovdee.shapes.shapes.Helix;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.shapes.Sphere;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The inputs every scale/twist/wave modifier and every gradient is driven by.
 */
class StandardInputTest {

    private static PointContext at(ShapeBounds bounds, double x, double y, double z) {
        PointContext ctx = new PointContext();
        ctx.bounds = bounds;
        ctx.x = x;
        ctx.y = y;
        ctx.z = z;
        return ctx;
    }

    private static final ShapeBounds BOX = ShapeBounds.compute(List.of(
            new Vector3d(-2, -1, 0), new Vector3d(2, 3, 4)));

    @Test
    void xyz_mapTheBoundsOntoZeroToOne() {
        assertEquals(0.0, StandardInput.X.sample(at(BOX, -2, 0, 0)), 1e-12);
        assertEquals(1.0, StandardInput.X.sample(at(BOX, 2, 0, 0)), 1e-12);
        assertEquals(0.5, StandardInput.X.sample(at(BOX, 0, 0, 0)), 1e-12);
        assertEquals(0.0, StandardInput.Y.sample(at(BOX, 0, -1, 0)), 1e-12);
        assertEquals(1.0, StandardInput.Y.sample(at(BOX, 0, 3, 0)), 1e-12);
        assertEquals(0.25, StandardInput.Y.sample(at(BOX, 0, 0, 0)), 1e-12);
        assertEquals(0.0, StandardInput.Z.sample(at(BOX, 0, 0, 0)), 1e-12);
        assertEquals(1.0, StandardInput.Z.sample(at(BOX, 0, 0, 4)), 1e-12);
    }

    @Test
    void xyz_eachIgnoreTheOtherAxes() {
        assertEquals(StandardInput.X.sample(at(BOX, 1, -1, 0)), StandardInput.X.sample(at(BOX, 1, 3, 4)), 1e-12);
        assertEquals(StandardInput.Y.sample(at(BOX, -2, 1, 0)), StandardInput.Y.sample(at(BOX, 2, 1, 4)), 1e-12);
        assertEquals(StandardInput.Z.sample(at(BOX, -2, -1, 2)), StandardInput.Z.sample(at(BOX, 2, 3, 2)), 1e-12);
    }

    @Test
    void t_followsTheDrawOrder() {
        PointContext ctx = at(BOX, 0, 0, 0);
        ctx.totalPoints = 10;
        ctx.index = 0;
        assertEquals(0.0, StandardInput.T.sample(ctx), 1e-12);
        ctx.index = 5;
        assertEquals(0.5, StandardInput.T.sample(ctx), 1e-12);
        // index / totalPoints: the last point stops just short of 1, so periodic inputs don't repeat the first point
        ctx.index = 9;
        assertEquals(0.9, StandardInput.T.sample(ctx), 1e-12);
    }

    @Test
    void t_isZeroWithoutAPointCount() {
        PointContext ctx = at(BOX, 0, 0, 0);
        ctx.totalPoints = 0;
        ctx.index = 3;
        assertEquals(0.0, StandardInput.T.sample(ctx), 1e-12);
    }

    @Test
    void radius_isTheHorizontalDistanceFromTheYAxis() {
        ShapeBounds bounds = ShapeBounds.compute(List.of(new Vector3d(3, 0, 4), new Vector3d(0, 10, 0)));
        assertEquals(0.0, StandardInput.RADIUS.sample(at(bounds, 0, 10, 0)), 1e-12);
        assertEquals(1.0, StandardInput.RADIUS.sample(at(bounds, 3, 0, 4)), 1e-12);
        assertEquals(0.5, StandardInput.RADIUS.sample(at(bounds, 0, 7, 2.5)), 1e-12, "y must not affect the radius");
    }

    @Test
    void spherical_isTheDistanceFromTheOrigin() {
        ShapeBounds bounds = ShapeBounds.compute(List.of(new Vector3d(0, 0, 0), new Vector3d(2, 2, 1)));
        assertEquals(0.0, StandardInput.SPHERICAL.sample(at(bounds, 0, 0, 0)), 1e-12);
        assertEquals(1.0, StandardInput.SPHERICAL.sample(at(bounds, 2, 2, 1)), 1e-12);
        assertEquals(1.0 / 3, StandardInput.SPHERICAL.sample(at(bounds, 0, 1, 0)), 1e-12);
    }

    @Test
    void angle_goesAroundTheYAxisStartingAtPlusX() {
        ShapeBounds bounds = ShapeBounds.compute(List.of(new Vector3d(-1, 0, -1), new Vector3d(1, 0, 1)));
        assertEquals(0.0, StandardInput.ANGLE.sample(at(bounds, 1, 0, 0)), 1e-12);
        assertEquals(0.25, StandardInput.ANGLE.sample(at(bounds, 0, 0, 1)), 1e-12);
        assertEquals(0.5, StandardInput.ANGLE.sample(at(bounds, -1, 0, 0)), 1e-12);
        assertEquals(0.75, StandardInput.ANGLE.sample(at(bounds, 0, 0, -1)), 1e-12);
        assertEquals(StandardInput.ANGLE.sample(at(bounds, 1, 0, 1)), StandardInput.ANGLE.sample(at(bounds, 5, 9, 5)), 1e-12,
                "only the direction in the XZ plane matters");
    }

    @Test
    void everyInput_staysWithinZeroToOneOnRealShapes() {
        Shape sphere = new Sphere(2);
        sphere.getPointSampler().setStyle(SamplingStyle.FILL);
        Shape cuboid = new Cuboid(3, 1, 5);
        cuboid.setOffset(new Vector3d(10, -4, 2));
        cuboid.setOrientation(new Quaterniond().rotateXYZ(0.4, 1.1, 0.2));
        Shape helix = new Helix(2, 6, 0.5);
        for (Shape shape : List.of(sphere, cuboid, helix)) {
            List<Vector3d> points = shape.getPointSampler().getPoints(shape);
            ShapeBounds bounds = ShapeBounds.compute(points);
            for (int i = 0; i < points.size(); i++) {
                Vector3d p = points.get(i);
                PointContext ctx = at(bounds, p.x, p.y, p.z);
                ctx.index = i;
                ctx.totalPoints = points.size();
                for (StandardInput input : StandardInput.values()) {
                    double v = input.sample(ctx);
                    assertTrue(v >= -1e-9 && v <= 1 + 1e-9,
                            input + " on " + shape.getClass().getSimpleName() + " gave " + v + " at " + p);
                }
            }
        }
    }

    @Test
    void flatBounds_doNotDivideByZero() {
        ShapeBounds flat = ShapeBounds.compute(List.of(new Vector3d(-1, 2, -1), new Vector3d(1, 2, 1)));
        for (StandardInput input : StandardInput.values()) {
            double v = input.sample(at(flat, 0.5, 2, 0.5));
            assertTrue(Double.isFinite(v), input + " gave " + v + " for a flat shape");
        }
        ShapeBounds single = ShapeBounds.compute(List.of(new Vector3d()));
        for (StandardInput input : StandardInput.values())
            assertTrue(Double.isFinite(input.sample(at(single, 0, 0, 0))), input + " for a single point at the origin");
    }

    @Test
    void inputHash_isDistinctPerInput() {
        Set<Integer> hashes = new HashSet<>();
        for (StandardInput input : StandardInput.values())
            hashes.add(input.inputHash());
        assertEquals(StandardInput.values().length, hashes.size());
    }
}
