package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ShapeBoundsTest {

    @Test
    void computeEmpty() {
        ShapeBounds b = ShapeBounds.compute(List.of());
        assertEquals(0.0, b.minX());
        assertEquals(0.0, b.maxX());
        assertEquals(0.0, b.invRangeX());
        assertEquals(0.0, b.invRangeY());
        assertEquals(0.0, b.invRangeZ());
        assertEquals(0.0, b.maxRadiusXZ());
    }

    @Test
    void computeSinglePoint() {
        ShapeBounds b = ShapeBounds.compute(List.of(new Vector3d(3, 5, 7)));
        assertEquals(3.0, b.minX());
        assertEquals(3.0, b.maxX());
        assertEquals(0.0, b.invRangeX(), "invRangeX should be 0 for zero-width extent");
        assertEquals(0.0, b.invRangeY());
        assertEquals(0.0, b.invRangeZ());
    }

    @Test
    void computeMultiplePoints() {
        List<Vector3d> pts = List.of(
                new Vector3d(-1, 0, 2),
                new Vector3d(3, 4, -2),
                new Vector3d(0, 2, 0)
        );
        ShapeBounds b = ShapeBounds.compute(pts);
        assertEquals(-1.0, b.minX());
        assertEquals(3.0, b.maxX());
        assertEquals(0.0, b.minY());
        assertEquals(4.0, b.maxY());
        assertEquals(-2.0, b.minZ());
        assertEquals(2.0, b.maxZ());
        assertEquals(4.0, b.width());
        assertEquals(4.0, b.height());
        assertEquals(4.0, b.length());
    }

    @Test
    void normalizeXYZ() {
        List<Vector3d> pts = List.of(new Vector3d(0, 0, 0), new Vector3d(2, 4, 6));
        ShapeBounds b = ShapeBounds.compute(pts);

        assertEquals(0.0, b.normalizeX(0.0), 1e-9);
        assertEquals(1.0, b.normalizeX(2.0), 1e-9);
        assertEquals(0.5, b.normalizeX(1.0), 1e-9);

        assertEquals(0.5, b.normalizeY(2.0), 1e-9);
        assertEquals(0.5, b.normalizeZ(3.0), 1e-9);
    }

    @Test
    void normalizeRadial() {
        // Points on a unit circle in XZ
        List<Vector3d> pts = List.of(
                new Vector3d(1, 0, 0),
                new Vector3d(-1, 0, 0),
                new Vector3d(0, 0, 1),
                new Vector3d(0, 0, -1)
        );
        ShapeBounds b = ShapeBounds.compute(pts);
        assertEquals(1.0, b.maxRadiusXZ(), 1e-9);
        // Point on the unit circle → normalized radial = 1
        assertEquals(1.0, b.normalizeRadial(1, 0), 1e-9);
        // Origin → normalized radial = 0
        assertEquals(0.0, b.normalizeRadial(0, 0), 1e-9);
        // Point at (1/√2, 1/√2) in XZ: radius = sqrt(0.5 + 0.5) = 1.0 → normalizes to 1.0
        assertEquals(1.0, b.normalizeRadial(1.0 / Math.sqrt(2), 1.0 / Math.sqrt(2)), 1e-6);
        // Point at half-radius on X axis
        assertEquals(0.5, b.normalizeRadial(0.5, 0), 1e-9);
    }

    @Test
    void zeroDimensionNormalization() {
        // Flat shape — all points at Y=5
        List<Vector3d> pts = List.of(
                new Vector3d(0, 5, 0),
                new Vector3d(1, 5, 0),
                new Vector3d(0, 5, 1)
        );
        ShapeBounds b = ShapeBounds.compute(pts);
        assertEquals(0.0, b.invRangeY(), "Flat shape should have invRangeY=0");
        // normalizeY should return 0 (no divide-by-zero)
        assertEquals(0.0, b.normalizeY(5.0), 1e-9);
        assertEquals(0.0, b.normalizeY(0.0), 1e-9);
    }

    @Test
    void maxRadiusXZ_ignoresY() {
        // (1, 100, 0) → XZ radius = 1; (0, -100, 1) → XZ radius = 1
        // A huge Y value does not contribute to the max XZ radius
        List<Vector3d> pts = List.of(
                new Vector3d(1, 100, 0),
                new Vector3d(0, -100, 1)
        );
        ShapeBounds b = ShapeBounds.compute(pts);
        assertEquals(1.0, b.maxRadiusXZ(), 1e-9, "Max XZ radius should only use X and Z components");

        // Additional check: a point with larger XZ distance should increase maxRadiusXZ
        List<Vector3d> pts2 = List.of(
                new Vector3d(3, 0, 4), // XZ radius = 5
                new Vector3d(0, 1000, 0)  // XZ radius = 0
        );
        ShapeBounds b2 = ShapeBounds.compute(pts2);
        assertEquals(5.0, b2.maxRadiusXZ(), 1e-9);
    }
}
