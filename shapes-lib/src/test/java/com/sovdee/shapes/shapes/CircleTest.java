package com.sovdee.shapes.shapes;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CircleTest {

    private static final double DENSITY = 0.25;

    @Test
    void calculateCircle_pointsOnRadius() {
        List<Vector3d> points = new ArrayList<>();
        Circle.calculateCircle(points, 1.0, DENSITY, 2 * Math.PI);
        assertFalse(points.isEmpty());
        for (Vector3d p : points) {
            double dist = Math.sqrt(p.x * p.x + p.z * p.z);
            assertEquals(1.0, dist, 1e-9, "All outline points should lie on the circle radius");
        }
    }

    @Test
    void calculateCircle_pointCount() {
        double radius = 2.0;
        List<Vector3d> points = new ArrayList<>();
        Circle.calculateCircle(points, radius, DENSITY, 2 * Math.PI);
        double expected = (2 * Math.PI) / (DENSITY / radius);
        // Allow ±5% tolerance due to floating-point step counting
        assertEquals(expected, points.size(), expected * 0.05 + 2);
    }

    @Test
    void calculateCircle_partialCutoff() {
        List<Vector3d> full = new ArrayList<>();
        List<Vector3d> half = new ArrayList<>();
        Circle.calculateCircle(full, 1.0, DENSITY, 2 * Math.PI);
        Circle.calculateCircle(half, 1.0, DENSITY, Math.PI);
        // Half cutoff should produce approximately half the points
        assertEquals(full.size(), half.size() * 2, (double) full.size() * 0.1 + 2);
    }

    @Test
    void calculateCircle_yIsZero() {
        List<Vector3d> points = new ArrayList<>();
        Circle.calculateCircle(points, 1.0, DENSITY, 2 * Math.PI);
        for (Vector3d p : points) {
            assertEquals(0.0, p.y, 1e-9, "All circle outline points should have Y=0");
        }
    }

    @Test
    void contains_insideRadius() {
        Circle c = new Circle(2.0);
        assertTrue(c.contains(new Vector3d(0.5, 0, 0)));
        assertTrue(c.contains(new Vector3d(0, 0, 1.9)));
    }

    @Test
    void contains_outsideRadius() {
        Circle c = new Circle(1.0);
        assertFalse(c.contains(new Vector3d(2.0, 0, 0)));
        assertFalse(c.contains(new Vector3d(1.0, 0, 1.0)));
    }

    @Test
    void contains_withHeight() {
        Circle c = new Circle(1.0, 2.0);
        assertTrue(c.contains(new Vector3d(0, 1, 0)), "Inside cylinder vertically");
        assertFalse(c.contains(new Vector3d(0, 3, 0)), "Above top of cylinder");
        assertFalse(c.contains(new Vector3d(0, -0.1, 0)), "Below bottom of cylinder");
    }

    @Test
    void setRadius_invalidatesVersion() {
        Circle c = new Circle(1.0);
        long v0 = c.getVersion();
        c.setRadius(2.0);
        assertTrue(c.getVersion() > v0, "setRadius() should increment the version");
    }
}
