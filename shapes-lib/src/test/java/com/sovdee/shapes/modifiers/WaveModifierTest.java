package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveModifierTest {

    private PointContext ctxWithIndex(double x, double y, double z, int index, int total) {
        ShapeBounds bounds = ShapeBounds.compute(List.of(new Vector3d(x, y, z)));
        PointContext c = new PointContext();
        c.bounds = bounds;
        c.x = x; c.y = y; c.z = z;
        c.index = index;
        c.totalPoints = total;
        return c;
    }

    private PointContext ctxT(int index, int total) {
        return ctxWithIndex(0, 0, 0, index, total);
    }

    // frequency=1 means one full sine cycle over T=[0,1].
    // At T=0.25 with freq=1: sin(2π * 1 * 0.25) = sin(π/2) = 1

    @Test
    void displaceY_byT_quarterCycle() {
        // T=0.25, freq=1 → sin(2π * 0.25) = sin(π/2) = 1 → displacement = amplitude
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(5, 20); // T = 5/20 = 0.25
        mod.modify(c);
        assertEquals(Math.sin(2 * Math.PI * 0.25), c.y, 1e-9);
    }

    @Test
    void displaceY_byT_halfCycle() {
        // T=0.5, freq=1 → sin(2π * 0.5) = sin(π) ≈ 0
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(10, 20); // T = 0.5
        mod.modify(c);
        assertEquals(Math.sin(2 * Math.PI * 0.5), c.y, 1e-9);
    }

    @Test
    void displaceY_byT_twoCycles() {
        // T=0.25, freq=2 → sin(2π * 2 * 0.25) = sin(π) ≈ 0
        WaveModifier mod = new WaveModifier(1.0, 2.0, 0.0, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(5, 20); // T = 0.25
        mod.modify(c);
        assertEquals(Math.sin(2 * Math.PI * 2 * 0.25), c.y, 1e-9);
    }

    @Test
    void displaceByT_zeroTotalPoints() {
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(0, 0); // totalPoints=0, T=0 → sin(0) = 0
        assertDoesNotThrow(() -> mod.modify(c));
        assertEquals(0.0, c.y, 1e-9, "T=0 when totalPoints=0 → zero displacement");
    }

    @Test
    void zeroAmplitude() {
        WaveModifier mod = new WaveModifier(0.0, 3.0, 0.0, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(5, 20);
        double origY = c.y;
        mod.modify(c);
        assertEquals(origY, c.y, 1e-9, "Zero amplitude → no displacement");
    }

    @Test
    void zeroFrequency_withPhase() {
        // freq=0: sin(0 * T + phase) = sin(phase); T is irrelevant
        double phase = Math.PI / 6; // sin(PI/6) = 0.5
        WaveModifier mod = new WaveModifier(2.0, 0.0, phase, StandardInput.T, SpatialAxis.Y);
        PointContext c = ctxT(15, 20); // T = 0.75, irrelevant since freq=0
        mod.modify(c);
        assertEquals(2.0 * Math.sin(phase), c.y, 1e-9);
    }

    @Test
    void phaseShift_producesDistinctDisplacement() {
        WaveModifier mod1 = new WaveModifier(1.0, 1.0, 0.0, StandardInput.T, SpatialAxis.Y);
        WaveModifier mod2 = new WaveModifier(1.0, 1.0, Math.PI / 4, StandardInput.T, SpatialAxis.Y);
        PointContext c1 = ctxT(5, 20);
        PointContext c2 = ctxT(5, 20);
        mod1.modify(c1);
        mod2.modify(c2);
        assertNotEquals(c1.y, c2.y, "Different phase → different displacement");
    }

    @ParameterizedTest
    @EnumSource(SpatialAxis.class)
    void allOutputAxes_nonOutputAxesUnchanged(SpatialAxis outputAxis) {
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, StandardInput.T, outputAxis);
        PointContext c = ctxT(5, 20); // T=0.25 → non-zero displacement
        double origX = c.x, origY = c.y, origZ = c.z;
        mod.modify(c);
        if (outputAxis != SpatialAxis.X) assertEquals(origX, c.x, 1e-9, "X unchanged when output != X");
        if (outputAxis != SpatialAxis.Y) assertEquals(origY, c.y, 1e-9, "Y unchanged when output != Y");
        if (outputAxis != SpatialAxis.Z) assertEquals(origZ, c.z, 1e-9, "Z unchanged when output != Z");
    }

    @Test
    void modifierHashCoversAllParams() {
        WaveModifier base = new WaveModifier(1.0, 2.0, 0.5, StandardInput.X, SpatialAxis.Y);
        assertNotEquals(base.modifierHash(),
                new WaveModifier(9.0, 2.0, 0.5, StandardInput.X, SpatialAxis.Y).modifierHash(),
                "Different amplitude");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 9.0, 0.5, StandardInput.X, SpatialAxis.Y).modifierHash(),
                "Different frequency");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 9.0, StandardInput.X, SpatialAxis.Y).modifierHash(),
                "Different phase");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, StandardInput.Z, SpatialAxis.Y).modifierHash(),
                "Different input");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, StandardInput.X, SpatialAxis.Z).modifierHash(),
                "Different output axis");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, StandardInput.T, SpatialAxis.Y).modifierHash(),
                "T input differs from X");
    }

    @Test
    void cloneIsIndependent() {
        WaveModifier original = new WaveModifier(1.0, 2.0, 0.0, StandardInput.X, SpatialAxis.Y);
        WaveModifier copy = original.clone();
        copy.setAmplitude(99.0);
        assertEquals(1.0, original.getAmplitude(), "Mutating clone must not affect original");
    }
}
