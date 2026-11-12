package com.sovdee.shapes.shapes;

import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the points each shape generates: counts, extents, duplicates, and density estimates.
 */
class ShapeGeometryTest {

    private static final double DENSITY = 0.25;

    private static List<Vector3d> sample(Shape shape, SamplingStyle style, double density) {
        PointSampler sampler = shape.getPointSampler();
        sampler.setStyle(style);
        sampler.setDensity(density);
        return sampler.getPoints(shape);
    }

    private static int distinct(List<Vector3d> points) {
        Set<String> seen = new HashSet<>();
        for (Vector3d v : points)
            seen.add(Math.round(v.x * 1e5) + "," + Math.round(v.y * 1e5) + "," + Math.round(v.z * 1e5));
        return seen.size();
    }

    private static List<Shape> allShapes() {
        return TestShapes.all();
    }

    // ----- Every shape -----

    @Test
    void everyShape_generatesFinitePoints() {
        for (Shape shape : allShapes()) {
            for (SamplingStyle style : SamplingStyle.values()) {
                List<Vector3d> points = sample(shape, style, DENSITY);
                assertFalse(points.isEmpty(), shape.getClass().getSimpleName() + " " + style + " generated no points");
                for (Vector3d point : points)
                    assertTrue(point.isFinite(), shape.getClass().getSimpleName() + " " + style + " generated " + point);
            }
        }
    }

    @Test
    void densityEstimate_isCloseForEveryShape() {
        int target = 500;
        for (Shape shape : allShapes()) {
            for (SamplingStyle style : SamplingStyle.values()) {
                double density = shape.computeDensity(style, target);
                String name = shape.getClass().getSimpleName() + " " + style;
                assertTrue(Double.isFinite(density) && density > 0.001, name + " estimated a density of " + density);
                int count = sample(shape, style, density).size();
                // an estimate only has to be in the right region; the sampler refines it from there
                assertTrue(count >= target / 4 && count <= target * 4,
                        name + " estimated " + density + " for " + target + " points, but that gives " + count);
            }
        }
    }

    @Test
    void setParticleCount_landsNearTheTargetForEveryShape() {
        int target = 500;
        for (Shape shape : allShapes()) {
            for (SamplingStyle style : SamplingStyle.values()) {
                PointSampler sampler = shape.getPointSampler();
                sampler.setStyle(style);
                sampler.resetDensity();
                sampler.setParticleCount(shape, target);
                int count = sampler.getPointCount(shape);
                assertTrue(count >= target * 3 / 4 && count <= target * 5 / 4,
                        shape.getClass().getSimpleName() + " " + style + " gave " + count + " points for a target of " + target);
            }
        }
    }

    @Test
    void outlines_haveNoDuplicatePoints() {
        // (an ellipsoid's three outline ellipses cross each other, so it is not included here)
        List<Shape> shapes = List.of(new Circle(3), new Circle(3, 2), new RegularPolygon(6, 3.0),
                new RegularPolygon(6, 3.0, 2.0), new Rectangle(4, 3, Rectangle.Plane.XZ), new Cuboid(3, 3, 3),
                new Star(1, 3, 5), new RegularPolyhedron(3, 4), new RegularPolyhedron(3, 8),
                new RegularPolyhedron(3, 12), new RegularPolyhedron(3, 20),
                new IrregularPolygon(List.of(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0), new Vector3d(1, 0, 2)), 2.0));
        for (Shape shape : shapes) {
            for (SamplingStyle style : SamplingStyle.values()) {
                List<Vector3d> points = sample(shape, style, DENSITY);
                assertEquals(points.size(), distinct(points),
                        shape.getClass().getSimpleName() + " " + style + " generated duplicate points");
            }
        }
    }

    // ----- Polygons and stars -----

    @Test
    void regularPolygon_keepsItsSideCount() {
        for (int sides = 3; sides <= 100; sides++) {
            assertEquals(sides, new RegularPolygon(sides, 1.0).getSides());
            RegularPolygon polygon = new RegularPolygon(3, 1.0);
            polygon.setSides(sides);
            assertEquals(sides, polygon.getSides());
            assertEquals(sides, ((RegularPolygon) polygon.clone()).getSides());
        }
    }

    @Test
    void star_keepsItsPointCount() {
        for (int points = 2; points <= 100; points++) {
            assertEquals(points, new Star(1, 2, points).getStarPoints());
            assertEquals(points, new Star(1, 2, Math.PI * 2 / points).getStarPoints());
            Star star = new Star(1, 2, 5);
            star.setStarPoints(points);
            assertEquals(points, ((Star) star.clone()).getStarPoints());
        }
    }

    @Test
    void regularPolygon_drawsEachSideOnce() {
        for (int sides = 3; sides <= 40; sides++) {
            RegularPolygon polygon = new RegularPolygon(sides, 5.0);
            List<Vector3d> points = sample(polygon, SamplingStyle.OUTLINE, DENSITY);
            int perSide = (int) Math.max(Math.round(polygon.getSideLength() / DENSITY), 1);
            assertEquals(sides * perSide, points.size(), sides + " sides");
            assertEquals(points.size(), distinct(points), sides + " sides");
        }
    }

    // ----- Extents -----

    @Test
    void solidSphere_isFilledOutToItsRadius() {
        for (double radius : new double[]{0.1, 0.4, 0.7, 1, 2, 5}) {
            List<Vector3d> points = sample(new Sphere(radius), SamplingStyle.FILL, DENSITY);
            assertFalse(points.isEmpty(), "a solid sphere of radius " + radius + " has no points");
            double furthest = 0;
            for (Vector3d point : points)
                furthest = Math.max(furthest, point.length());
            assertEquals(radius, furthest, 1e-9, "a solid sphere of radius " + radius + " should include its surface");
        }
    }

    @Test
    void shapesWithHeight_reachTheirTop() {
        List<LWHShape> shapes = List.of(new Circle(1, 1), new Circle(1, 10), new Ellipse(2, 1, 3),
                new RegularPolygon(5, 1.0, 1.0), new Arc(1, 2.5, Math.PI));
        for (LWHShape shape : shapes) {
            for (SamplingStyle style : SamplingStyle.values()) {
                // 0.3 does not divide these heights evenly
                ShapeBounds bounds = ShapeBounds.compute(sample(shape, style, 0.3));
                String name = shape.getClass().getSimpleName() + " " + style;
                assertEquals(shape.getHeight(), bounds.maxY(), 1e-9, name + " does not reach its top");
                assertEquals(0, bounds.minY(), 1e-9, name + " does not start at its base");
            }
        }
    }

    @Test
    void helix_spacesPointsByDensity() {
        for (double radius : new double[]{0.5, 1, 3, 8}) {
            List<Vector3d> points = sample(new Helix(radius, 5, 0.5), SamplingStyle.OUTLINE, DENSITY);
            double spacing = points.get(0).distance(points.get(1));
            assertEquals(DENSITY, spacing, DENSITY * 0.1, "radius " + radius);
        }
    }

    // ----- Polyhedra -----

    @Test
    void polyhedra_haveTheRightNumberOfVertices() {
        int[][] facesAndVertices = {{4, 4}, {8, 6}, {12, 20}, {20, 12}};
        for (int[] pair : facesAndVertices) {
            double radius = 3;
            List<Vector3d> points = sample(new RegularPolyhedron(radius, pair[0]), SamplingStyle.OUTLINE, DENSITY);
            int vertices = 0;
            for (Vector3d point : points) {
                assertTrue(point.length() <= radius + 1e-6, "a point lies outside the circumscribed sphere");
                if (Math.abs(point.length() - radius) < 1e-6)
                    vertices++;
            }
            assertEquals(pair[1], vertices, pair[0] + " faces");
        }
    }

    @Test
    void polyhedra_haveTheRightNumberOfEdges() {
        int[][] facesAndEdges = {{4, 6}, {8, 12}, {12, 30}, {20, 30}};
        for (int[] pair : facesAndEdges) {
            RegularPolyhedron polyhedron = new RegularPolyhedron(3, pair[0]);
            // at a density of half a side, every edge is its two vertices and one midpoint
            List<Vector3d> points = sample(polyhedron, SamplingStyle.OUTLINE, polyhedron.getSideLength() / 2);
            int vertices = switch (pair[0]) {
                case 4 -> 4;
                case 8 -> 6;
                case 12 -> 20;
                default -> 12;
            };
            assertEquals(vertices + pair[1], points.size(), pair[0] + " faces");
        }
    }

    // ----- Rectangles and cuboids -----

    @Test
    void cuboid_canBeGeneratedWithoutASampler() {
        List<Vector3d> points = new ArrayList<>();
        assertTimeoutPreemptively(Duration.ofSeconds(5), () -> new Cuboid(1, 1, 1).generateOutline(points, DENSITY));
        assertEquals(sample(new Cuboid(1, 1, 1), SamplingStyle.OUTLINE, DENSITY).size(), points.size());
    }

    @Test
    void rectangle_canBeGeneratedWithoutASampler() {
        List<Vector3d> points = new ArrayList<>();
        new Rectangle(4, 4, Rectangle.Plane.XZ).generateOutline(points, 0.1);
        assertEquals(sample(new Rectangle(4, 4, Rectangle.Plane.XZ), SamplingStyle.OUTLINE, 0.1).size(), points.size());
        assertEquals(160, points.size());
    }

    @Test
    void cuboid_fromCorners_sitsBetweenThem() {
        Vector3d a = new Vector3d(10, 20, 30);
        Vector3d b = new Vector3d(12, 23, 34);
        for (Shape cuboid : List.of(new Cuboid(a, b), new Cuboid(() -> a, () -> b))) {
            ShapeBounds bounds = ShapeBounds.compute(sample(cuboid, SamplingStyle.OUTLINE, DENSITY));
            assertEquals(10, bounds.minX(), 1e-9);
            assertEquals(12, bounds.maxX(), 1e-9);
            assertEquals(20, bounds.minY(), 1e-9);
            assertEquals(23, bounds.maxY(), 1e-9);
            assertEquals(30, bounds.minZ(), 1e-9);
            assertEquals(34, bounds.maxZ(), 1e-9);
        }
    }

    @Test
    void rectangle_fromCorners_sitsBetweenThem() {
        Vector3d a = new Vector3d(10, 0, 30);
        Vector3d b = new Vector3d(12, 0, 34);
        for (Shape rectangle : List.of(new Rectangle(a, b, Rectangle.Plane.XZ), new Rectangle(() -> a, () -> b, Rectangle.Plane.XZ))) {
            ShapeBounds bounds = ShapeBounds.compute(sample(rectangle, SamplingStyle.SURFACE, DENSITY));
            assertEquals(10, bounds.minX(), 1e-9);
            assertEquals(12, bounds.maxX(), 1e-9);
            assertEquals(30, bounds.minZ(), 1e-9);
            assertEquals(34, bounds.maxZ(), 1e-9);
        }
    }

    // ----- Dynamic shapes -----

    @Test
    void dynamicLine_isOnlyResampledWhenItMoves() {
        AtomicReference<Vector3d> end = new AtomicReference<>(new Vector3d(5, 0, 0));
        Line line = new Line(Vector3d::new, () -> new Vector3d(end.get()));
        line.getPointSampler().setDensity(DENSITY);

        List<Vector3d> first = line.getPointSampler().getPoints(line);
        assertSame(first, line.getPointSampler().getPoints(line), "an unmoved line should not be resampled");

        end.set(new Vector3d(0, 8, 0));
        List<Vector3d> moved = line.getPointSampler().getPoints(line);
        assertNotSame(first, moved, "a moved line must be resampled");
        assertEquals(8, moved.get(moved.size() - 1).y, 1e-9);
        assertEquals(8, line.getLength(), 1e-9);
    }

    @Test
    void dynamicShapes_cloneWithoutLosingTheirSuppliers() {
        AtomicReference<Vector3d> corner = new AtomicReference<>(new Vector3d(2, 2, 2));
        Cuboid cuboid = new Cuboid(Vector3d::new, () -> new Vector3d(corner.get()));
        cuboid.getPointSampler().setDensity(DENSITY);
        List<Vector3d> points = cuboid.getPointSampler().getPoints(cuboid);

        Shape copy = cuboid.clone();
        assertSame(points, copy.getPointSampler().getPoints(copy), "an unmoved clone should reuse the sampled points");

        corner.set(new Vector3d(4, 4, 4));
        ShapeBounds bounds = ShapeBounds.compute(copy.getPointSampler().getPoints(copy));
        assertEquals(4, bounds.maxX(), 1e-9, "the clone should still follow the suppliers");
    }

    @Test
    void dynamicBezier_followsItsSupplier() {
        AtomicReference<Vector3d> end = new AtomicReference<>(new Vector3d(3, 0, 0));
        BezierCurve curve = new BezierCurve(() -> List.of(new Vector3d(0, 0, 0), new Vector3d(1, 2, 0), new Vector3d(end.get())));
        curve.getPointSampler().setDensity(DENSITY);

        List<Vector3d> first = curve.getPointSampler().getPoints(curve);
        assertSame(first, curve.getPointSampler().getPoints(curve));
        assertEquals(3, first.get(first.size() - 1).x, 1e-9, "the curve should include its end point");

        end.set(new Vector3d(6, 0, 0));
        List<Vector3d> moved = curve.getPointSampler().getPoints(curve);
        assertEquals(6, moved.get(moved.size() - 1).x, 1e-9);
    }

    // ----- Transform -----

    @Test
    void setOffset_copiesTheVector() {
        Vector3d offset = new Vector3d(1, 0, 0);
        Circle circle = new Circle(1);
        circle.setOffset(offset);
        offset.x = 99;
        assertEquals(1, circle.getOffset().x, 1e-12);
    }
}
