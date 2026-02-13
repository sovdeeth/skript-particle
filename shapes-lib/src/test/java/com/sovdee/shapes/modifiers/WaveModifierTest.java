package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WaveModifierTest {

    private PointContext ctx(double x, double y, double z) {
        ShapeBounds bounds = ShapeBounds.compute(List.of(new Vector3d(x, y, z)));
        PointContext c = new PointContext();
        c.bounds = bounds;
        c.x = x; c.y = y; c.z = z;
        return c;
    }

    private PointContext ctxWithIndex(double x, double y, double z, int index, int total) {
        PointContext c = ctx(x, y, z);
        c.index = index;
        c.totalPoints = total;
        return c;
    }

    @Test
    void displaceY_byX() {
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, SampleAxis.X, WaveModifier.Axis.Y);
        PointContext c = ctx(Math.PI / 2, 0, 0);
        mod.modify(c);
        assertEquals(1.0, c.y, 1e-9, "y should be displaced by sin(PI/2)=1");
        assertEquals(Math.PI / 2, c.x, 1e-9, "x unchanged");
    }

    @Test
    void displaceZ_byY() {
        WaveModifier mod = new WaveModifier(2.0, 1.0, 0.0, SampleAxis.Y, WaveModifier.Axis.Z);
        PointContext c = ctx(0, Math.PI / 2, 5);
        mod.modify(c);
        assertEquals(5.0 + 2.0, c.z, 1e-9, "z should increase by 2*sin(PI/2)=2");
    }

    @Test
    void displaceY_byT() {
        // T = index/total. With index=5, total=20: T=0.25, sin(2*PI*0.25)=sin(PI/2)=1
        WaveModifier mod = new WaveModifier(1.0, 2 * Math.PI, 0.0, SampleAxis.T, WaveModifier.Axis.Y);
        PointContext c = ctxWithIndex(0, 0, 0, 5, 20); // T = 0.25
        mod.modify(c);
        assertEquals(Math.sin(2 * Math.PI * 0.25), c.y, 1e-9);
    }

    @Test
    void displaceByT_zeroTotalPoints() {
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, SampleAxis.T, WaveModifier.Axis.Y);
        PointContext c = ctxWithIndex(0, 0, 0, 0, 0); // totalPoints=0, should not throw
        assertDoesNotThrow(() -> mod.modify(c));
        assertEquals(0.0, c.y, 1e-9, "T=0 when totalPoints=0");
    }

    @Test
    void zeroAmplitude() {
        WaveModifier mod = new WaveModifier(0.0, 3.0, 0.0, SampleAxis.X, WaveModifier.Axis.Y);
        PointContext c = ctx(1, 1, 1);
        double origY = c.y;
        mod.modify(c);
        assertEquals(origY, c.y, 1e-9, "Zero amplitude → no displacement");
    }

    @Test
    void zeroFrequency() {
        // amplitude*sin(0*x + phase) = amplitude*sin(phase)
        double phase = Math.PI / 6; // sin(PI/6) = 0.5
        WaveModifier mod = new WaveModifier(2.0, 0.0, phase, SampleAxis.X, WaveModifier.Axis.Y);
        PointContext c = ctx(100, 0, 0); // large X irrelevant when freq=0
        mod.modify(c);
        assertEquals(2.0 * Math.sin(phase), c.y, 1e-9);
    }

    @Test
    void phaseShift() {
        WaveModifier mod1 = new WaveModifier(1.0, 1.0, 0.0, SampleAxis.X, WaveModifier.Axis.Y);
        WaveModifier mod2 = new WaveModifier(1.0, 1.0, Math.PI / 4, SampleAxis.X, WaveModifier.Axis.Y);
        PointContext c1 = ctx(1, 0, 0);
        PointContext c2 = ctx(1, 0, 0);
        mod1.modify(c1);
        mod2.modify(c2);
        assertNotEquals(c1.y, c2.y, "Different phase → different displacement");
    }

    @ParameterizedTest
    @EnumSource(WaveModifier.Axis.class)
    void allOutputAxes_nonOutputAxesUnchanged(WaveModifier.Axis outputAxis) {
        WaveModifier mod = new WaveModifier(1.0, 1.0, 0.0, SampleAxis.X, outputAxis);
        PointContext c = ctx(Math.PI / 2, 2, 3);
        double origX = c.x, origY = c.y, origZ = c.z;
        mod.modify(c);
        if (outputAxis != WaveModifier.Axis.X) assertEquals(origX, c.x, 1e-9, "X unchanged when output != X");
        if (outputAxis != WaveModifier.Axis.Y) assertEquals(origY, c.y, 1e-9, "Y unchanged when output != Y");
        if (outputAxis != WaveModifier.Axis.Z) assertEquals(origZ, c.z, 1e-9, "Z unchanged when output != Z");
    }

    @Test
    void modifierHashCoversAllParams() {
        WaveModifier base = new WaveModifier(1.0, 2.0, 0.5, SampleAxis.X, WaveModifier.Axis.Y);
        assertNotEquals(base.modifierHash(),
                new WaveModifier(9.0, 2.0, 0.5, SampleAxis.X, WaveModifier.Axis.Y).modifierHash(),
                "Different amplitude");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 9.0, 0.5, SampleAxis.X, WaveModifier.Axis.Y).modifierHash(),
                "Different frequency");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 9.0, SampleAxis.X, WaveModifier.Axis.Y).modifierHash(),
                "Different phase");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, SampleAxis.Z, WaveModifier.Axis.Y).modifierHash(),
                "Different input axis");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, SampleAxis.X, WaveModifier.Axis.Z).modifierHash(),
                "Different output axis");
        assertNotEquals(base.modifierHash(),
                new WaveModifier(1.0, 2.0, 0.5, SampleAxis.T, WaveModifier.Axis.Y).modifierHash(),
                "T input axis differs from X");
    }

    @Test
    void cloneIsIndependent() {
        WaveModifier original = new WaveModifier(1.0, 2.0, 0.0, SampleAxis.X, WaveModifier.Axis.Y);
        WaveModifier copy = original.clone();
        copy.setAmplitude(99.0);
        assertEquals(1.0, original.getAmplitude(), "Mutating clone must not affect original");
    }
}
