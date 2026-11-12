package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScalingModifierTest {

    private PointContext ctx(double x, double y, double z, List<Vector3d> allPoints) {
        ShapeBounds bounds = ShapeBounds.compute(allPoints);
        PointContext c = new PointContext();
        c.bounds = bounds;
        c.x = x; c.y = y; c.z = z;
        return c;
    }

    /** Points spanning Y=[0,1] so normalizedY works predictably. */
    private List<Vector3d> ySpan() {
        return List.of(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
    }

    /** Points spanning X=[0,1]. */
    private List<Vector3d> xSpan() {
        return List.of(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
    }

    @Test
    void scalesAtInputMin() {
        ScalingModifier mod = new ScalingModifier(0.5, 2.0, StandardInput.Y, ScaleAxes.XZ);
        PointContext c = ctx(1, 0, 1, ySpan());
        mod.modify(c);
        assertEquals(0.5, c.x, 1e-9, "At normalizedY=0, scale=startScale=0.5");
        assertEquals(0.5, c.z, 1e-9);
        assertEquals(0.0, c.y, 1e-9, "Y should be unchanged");
    }

    @Test
    void scalesAtInputMax() {
        ScalingModifier mod = new ScalingModifier(0.5, 2.0, StandardInput.Y, ScaleAxes.XZ);
        PointContext c = ctx(1, 1, 1, ySpan());
        mod.modify(c);
        assertEquals(2.0, c.x, 1e-9, "At normalizedY=1, scale=endScale=2.0");
        assertEquals(2.0, c.z, 1e-9);
    }

    @Test
    void scalesAtMidpoint() {
        ScalingModifier mod = new ScalingModifier(0.0, 2.0, StandardInput.Y, ScaleAxes.XZ);
        PointContext c = ctx(1, 0.5, 1, ySpan());
        mod.modify(c);
        double expected = 0.0 + 0.5 * (2.0 - 0.0); // lerp(0, 2, 0.5) = 1
        assertEquals(expected, c.x, 1e-9);
        assertEquals(expected, c.z, 1e-9);
    }

    @Test
    void xzAxes_yUnchanged() {
        ScalingModifier mod = new ScalingModifier(1.0, 3.0, StandardInput.Y, ScaleAxes.XZ);
        PointContext c = ctx(2, 0.5, -3, ySpan());
        double origY = c.y;
        mod.modify(c);
        assertEquals(origY, c.y, 1e-9, "Y coordinate must not be changed when scaling XZ");
    }

    @Test
    void xzAxes_drivenByX() {
        ScalingModifier mod = new ScalingModifier(0.0, 2.0, StandardInput.X, ScaleAxes.XZ);
        PointContext c = ctx(1, 1, 1, xSpan()); // normalizedX=1 → scale=2.0
        mod.modify(c);
        assertEquals(2.0, c.x, 1e-9, "X scaled by endScale=2.0 at normalizedX=1");
        assertEquals(2.0, c.z, 1e-9, "Z scaled by endScale=2.0");
        assertEquals(1.0, c.y, 1e-9, "Y unchanged when scaling XZ");
    }

    @Test
    void xyzAxes_allScaled() {
        ScalingModifier mod = new ScalingModifier(2.0, 2.0, StandardInput.Y, ScaleAxes.XYZ);
        PointContext c = ctx(1, 0.5, 1, ySpan());
        mod.modify(c);
        assertEquals(2.0, c.x, 1e-9);
        assertEquals(1.0, c.y, 1e-9, "Y also scaled at 0.5 position");
        assertEquals(2.0, c.z, 1e-9);
    }

    @Test
    void yzAxes_xUnchanged() {
        ScalingModifier mod = new ScalingModifier(2.0, 2.0, StandardInput.Y, ScaleAxes.YZ);
        PointContext c = ctx(3, 0.5, 1, ySpan());
        mod.modify(c);
        assertEquals(3.0, c.x, 1e-9, "X unchanged when scaling YZ");
    }

    @Test
    void modifierHashChangesWithParams() {
        ScalingModifier a = new ScalingModifier(1.0, 2.0, StandardInput.Y, ScaleAxes.XZ);
        ScalingModifier b = new ScalingModifier(1.5, 2.0, StandardInput.Y, ScaleAxes.XZ);
        assertNotEquals(a.modifierHash(), b.modifierHash(), "Different startScale");
    }

    @Test
    void modifierHashChangesWithInput() {
        ScalingModifier y = new ScalingModifier(1.0, 2.0, StandardInput.Y, ScaleAxes.XZ);
        ScalingModifier x = new ScalingModifier(1.0, 2.0, StandardInput.X, ScaleAxes.XZ);
        ScalingModifier t = new ScalingModifier(1.0, 2.0, StandardInput.T, ScaleAxes.XZ);
        assertNotEquals(y.modifierHash(), x.modifierHash(), "Y vs X input");
        assertNotEquals(y.modifierHash(), t.modifierHash(), "Y vs T input");
    }

    @Test
    void modifierHashChangesWithAxes() {
        ScalingModifier xz = new ScalingModifier(1.0, 2.0, StandardInput.Y, ScaleAxes.XZ);
        ScalingModifier xyz = new ScalingModifier(1.0, 2.0, StandardInput.Y, ScaleAxes.XYZ);
        assertNotEquals(xz.modifierHash(), xyz.modifierHash(), "XZ vs XYZ axes");
    }

    @Test
    void cloneIsIndependent() {
        ScalingModifier original = new ScalingModifier(1.0, 2.0, StandardInput.Y, ScaleAxes.XZ);
        ScalingModifier copy = original.clone();
        copy.setStartScale(99.0);
        assertEquals(1.0, original.getStartScale(), "Mutating clone must not affect original");
    }
}
