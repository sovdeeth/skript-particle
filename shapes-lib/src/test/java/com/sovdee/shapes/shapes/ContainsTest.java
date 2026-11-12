package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link Shape#contains(Vector3d)}: that it follows the shape's transform, and that it tests
 * against each shape's actual volume.
 */
class ContainsTest {

    private static Vector3d v(double x, double y, double z) {
        return new Vector3d(x, y, z);
    }

    /** Shapes with a volume or area, which contain every point of their filled form. */
    private static List<AbstractShape> solidShapes() {
        List<AbstractShape> shapes = new ArrayList<>();
        shapes.add(new Circle(3));
        shapes.add(new Circle(3, 2));
        shapes.add(new Arc(3, Math.PI / 2));
        shapes.add(new Arc(3, 2, 4.0));
        shapes.add(new Ellipse(4, 1));
        shapes.add(new Ellipse(4, 1, 2));
        shapes.add(new EllipticalArc(4, 2, 2, 2.0));
        shapes.add(new Ellipsoid(4, 1, 2));
        shapes.add(new Sphere(3));
        shapes.add(new SphericalCap(3, 1.0));
        shapes.add(new RegularPolygon(6, 3.0));
        shapes.add(new RegularPolygon(5, 3.0, 2.0));
        shapes.add(new Rectangle(4, 3, Rectangle.Plane.XY));
        shapes.add(new Rectangle(v(1, 0, 1), v(4, 0, 3), Rectangle.Plane.XZ));
        shapes.add(new Cuboid(3, 2, 1));
        shapes.add(new Cuboid(v(10, 10, 10), v(12, 13, 14)));
        shapes.add(new Star(1, 3, 5));
        shapes.add(new RegularPolyhedron(3, 4));
        shapes.add(new RegularPolyhedron(3, 8));
        shapes.add(new RegularPolyhedron(3, 12));
        shapes.add(new RegularPolyhedron(3, 20));
        shapes.add(new Heart(3, 4, 3));
        return shapes;
    }

    /** Shapes that are only a curve, which contain every point of their outline. */
    private static List<AbstractShape> curves() {
        List<AbstractShape> shapes = new ArrayList<>();
        shapes.add(new Line(v(1, 2, 3), v(-4, 0, 2)));
        shapes.add(new Helix(2, 5, 0.5));
        shapes.add(new BezierCurve(List.of(v(0, 0, 0), v(1, 2, 0), v(3, 0, 1))));
        shapes.add(new IrregularPolygon(List.of(v(0, 0, 0), v(2, 0, 0), v(1, 0, 2))));
        return shapes;
    }

    private static void transform(Shape shape) {
        shape.setOrientation(new Quaterniond().rotateXYZ(0.7, -1.3, 2.1));
        shape.setScale(1.7);
        shape.setOffset(v(40, -25, 60));
    }

    private static void assertContainsItsOwnPoints(AbstractShape shape, SamplingStyle style) {
        PointSampler sampler = shape.getPointSampler();
        sampler.setStyle(style);
        sampler.setDensity(0.2);
        String name = shape.getClass().getSimpleName() + " " + style;
        List<Vector3d> points = sampler.getPoints(shape);
        assertFalse(points.isEmpty(), name);
        for (Vector3d point : points)
            assertTrue(shape.contains(point, 1e-3), name + " should contain its own point " + point);
    }

    // ----- Every shape -----

    @Test
    void shapes_containTheirOwnPoints() {
        for (AbstractShape shape : solidShapes())
            assertContainsItsOwnPoints(shape, SamplingStyle.FILL);
        for (AbstractShape shape : curves())
            assertContainsItsOwnPoints(shape, SamplingStyle.OUTLINE);
    }

    @Test
    void shapes_containTheirOwnPoints_whenTransformed() {
        for (AbstractShape shape : solidShapes()) {
            transform(shape);
            assertContainsItsOwnPoints(shape, SamplingStyle.FILL);
        }
        for (AbstractShape shape : curves()) {
            transform(shape);
            assertContainsItsOwnPoints(shape, SamplingStyle.OUTLINE);
        }
    }

    @Test
    void shapes_doNotContainDistantPoints() {
        List<AbstractShape> shapes = solidShapes();
        shapes.addAll(curves());
        for (AbstractShape shape : shapes) {
            String name = shape.getClass().getSimpleName();
            assertFalse(shape.contains(v(500, 500, 500)), name);
            transform(shape);
            // where the untransformed shape was
            assertFalse(shape.contains(v(0.1, 0.05, 0.1)), name + " should have moved with its offset");
        }
    }

    // ----- Transform -----

    @Test
    void contains_followsOffset() {
        Sphere sphere = new Sphere(1);
        sphere.setOffset(v(10, 0, 0));
        assertTrue(sphere.contains(v(10, 0, 0)));
        assertTrue(sphere.contains(v(10.9, 0, 0)));
        assertFalse(sphere.contains(v(0, 0, 0)));
    }

    @Test
    void contains_followsScale() {
        Sphere sphere = new Sphere(1);
        sphere.setScale(3);
        assertTrue(sphere.contains(v(2.9, 0, 0)));
        assertFalse(sphere.contains(v(3.1, 0, 0)));
    }

    @Test
    void contains_followsOrientation() {
        Cuboid cuboid = new Cuboid(10, 1, 1); // long along x
        assertTrue(cuboid.contains(v(4, 0, 0)));
        assertFalse(cuboid.contains(v(0, 4, 0)));
        cuboid.setOrientation(new Quaterniond().rotateZ(Math.PI / 2)); // now long along y
        assertFalse(cuboid.contains(v(4, 0, 0)));
        assertTrue(cuboid.contains(v(0, 4, 0)));
    }

    // ----- Actual volumes, not bounding shapes -----

    @Test
    void regularPolygon_usesItsEdges_onAllSides() {
        RegularPolygon square = new RegularPolygon(4, 1.0); // vertices on the axes
        for (int sx = -1; sx <= 1; sx += 2) {
            for (int sz = -1; sz <= 1; sz += 2) {
                assertTrue(square.contains(v(0.4 * sx, 0, 0.4 * sz)), "inside, quadrant " + sx + "," + sz);
                // inside the circumscribed circle, but beyond the edge
                assertFalse(square.contains(v(0.6 * sx, 0, 0.6 * sz)), "outside, quadrant " + sx + "," + sz);
            }
        }
        assertTrue(square.contains(v(0.99, 0, 0)));
        assertFalse(square.contains(v(0.4, 0.5, 0.4)), "a flat polygon has no height");
    }

    @Test
    void star_excludesTheGapsBetweenItsPoints() {
        Star star = new Star(1, 3, 5);
        assertTrue(star.contains(v(2.9, 0, 0)), "the tip of a point");
        assertTrue(star.contains(v(0, 0, 0)));
        // halfway between two points, an inner vertex sits at radius 1
        double between = Math.PI / 5;
        assertFalse(star.contains(v(2 * Math.cos(between), 0, -2 * Math.sin(between))), "the gap between two points");
        assertTrue(star.contains(v(0.9 * Math.cos(between), 0, -0.9 * Math.sin(between))));
    }

    @Test
    void polyhedron_usesItsFaces() {
        double radius = 3;
        RegularPolyhedron octahedron = new RegularPolyhedron(radius, 8);
        double inscribed = radius / Math.sqrt(3);
        assertTrue(octahedron.contains(v(0, 0, 0)));
        assertFalse(octahedron.contains(v(1, 1, 1).normalize().mul(radius * 1.05)), "beyond the circumscribed sphere");

        // Measure the volume by sampling the cube around it. It must be larger than the inscribed sphere and
        // smaller than the circumscribed one: an octahedron of circumradius r has volume (4/3) r^3,
        // which is exactly 1/6 of a cube of side 2r.
        java.util.Random random = new java.util.Random(7);
        int samples = 20_000, insideSphere = 0, insideShape = 0, insideInscribed = 0;
        for (int i = 0; i < samples; i++) {
            Vector3d point = v(random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1, random.nextDouble() * 2 - 1).mul(radius);
            if (point.length() <= radius) insideSphere++;
            if (point.length() <= inscribed) insideInscribed++;
            if (octahedron.contains(point)) insideShape++;
        }
        assertTrue(insideShape > insideInscribed && insideShape < insideSphere,
                insideInscribed + " < " + insideShape + " < " + insideSphere);
        assertEquals(samples / 6.0, insideShape, samples * 0.02);
    }

    @Test
    void sphericalCap_isASector() {
        SphericalCap cap = new SphericalCap(2, Math.PI / 4);
        assertTrue(cap.contains(v(0, 1.9, 0)));
        assertTrue(cap.contains(v(0.5, 1, 0)));
        assertFalse(cap.contains(v(1.5, 1, 0)), "outside the cone");
        assertFalse(cap.contains(v(0, -1, 0)), "the opposite side");
    }

    @Test
    void arc_isLimitedToItsAngle() {
        Arc quarter = new Arc(2, Math.PI / 2);
        assertTrue(quarter.contains(v(1, 0, 1)));
        assertFalse(quarter.contains(v(-1, 0, 1)));
        assertFalse(quarter.contains(v(1, 0, -1)));
    }

    @Test
    void cuboidAndRectangle_fromCorners_containTheirCenter() {
        assertTrue(new Cuboid(v(10, 10, 10), v(12, 12, 12)).contains(v(11, 11, 11)));
        assertFalse(new Cuboid(v(10, 10, 10), v(12, 12, 12)).contains(v(0, 0, 0)));
        assertTrue(new Rectangle(v(10, 0, 10), v(12, 0, 12), Rectangle.Plane.XZ).contains(v(11, 0, 11)));
        assertFalse(new Rectangle(v(10, 0, 10), v(12, 0, 12), Rectangle.Plane.XZ).contains(v(0, 0, 0)));
    }

    @Test
    void curves_useTheTolerance() {
        Line line = new Line(v(0, 0, 0), v(10, 0, 0));
        assertTrue(line.contains(v(5, 0, 0)));
        assertFalse(line.contains(v(5, 0.01, 0)));
        assertTrue(line.contains(v(5, 0.01, 0), 0.05));
        assertFalse(line.contains(v(11, 0, 0), 0.05));
    }
}
