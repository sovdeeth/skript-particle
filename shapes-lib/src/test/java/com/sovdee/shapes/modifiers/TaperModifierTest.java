package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TaperModifierTest {

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
    void tapersAtBottom() {
        TaperModifier mod = new TaperModifier(0.5, 2.0);
        PointContext c = ctx(1, 0, 1, ySpan());
        mod.modify(c);
        assertEquals(0.5, c.x, 1e-9, "At normalizedY=0, scale=startScale=0.5");
        assertEquals(0.5, c.z, 1e-9);
        assertEquals(0.0, c.y, 1e-9, "Y should be unchanged");
    }

    @Test
    void tapersAtTop() {
        TaperModifier mod = new TaperModifier(0.5, 2.0);
        PointContext c = ctx(1, 1, 1, ySpan());
        mod.modify(c);
        assertEquals(2.0, c.x, 1e-9, "At normalizedY=1, scale=endScale=2.0");
        assertEquals(2.0, c.z, 1e-9);
    }

    @Test
    void tapersAtMidpoint() {
        TaperModifier mod = new TaperModifier(0.0, 2.0);
        PointContext c = ctx(1, 0.5, 1, ySpan());
        mod.modify(c);
        double expected = 0.0 + 0.5 * (2.0 - 0.0); // lerp(0, 2, 0.5) = 1
        assertEquals(expected, c.x, 1e-9);
        assertEquals(expected, c.z, 1e-9);
    }

    @Test
    void xAndZScaled_yUnchanged() {
        TaperModifier mod = new TaperModifier(1.0, 3.0);
        PointContext c = ctx(2, 0.5, -3, ySpan());
        double origY = c.y;
        mod.modify(c);
        assertEquals(origY, c.y, 1e-9, "Y coordinate must not be changed by taper along Y");
    }

    @Test
    void xAxis_scaledYandZ_xUnchanged() {
        // Taper along X: scales Y and Z; X is unchanged
        TaperModifier mod = new TaperModifier(0.0, 2.0, SampleAxis.X);
        PointContext c = ctx(1, 1, 1, xSpan()); // normalizedX=1 → scale=2.0
        mod.modify(c);
        assertEquals(2.0, c.y, 1e-9, "Y scaled by endScale=2.0 at normalizedX=1");
        assertEquals(2.0, c.z, 1e-9, "Z scaled by endScale=2.0");
        assertEquals(1.0, c.x, 1e-9, "X must not change when axis=X");
    }

    @Test
    void zAxis_scaledXandY_zUnchanged() {
        // Taper along Z: scales X and Y; Z is unchanged
        List<Vector3d> zSpan = List.of(new Vector3d(0, 0, 0), new Vector3d(0, 0, 1));
        TaperModifier mod = new TaperModifier(1.0, 3.0, SampleAxis.Z);
        PointContext c = ctx(1, 1, 0, zSpan); // normalizedZ=0 → scale=1.0
        mod.modify(c);
        assertEquals(1.0, c.x, 1e-9, "X scaled by startScale=1.0");
        assertEquals(1.0, c.y, 1e-9, "Y scaled by startScale=1.0");
        assertEquals(0.0, c.z, 1e-9, "Z must not change when axis=Z");
    }

    @Test
    void modifierHashChangesWithParams() {
        TaperModifier a = new TaperModifier(1.0, 2.0);
        TaperModifier b = new TaperModifier(1.5, 2.0);
        assertNotEquals(a.modifierHash(), b.modifierHash(), "Different startScale");
    }

    @Test
    void modifierHashChangesWithAxis() {
        TaperModifier y = new TaperModifier(1.0, 2.0, SampleAxis.Y);
        TaperModifier x = new TaperModifier(1.0, 2.0, SampleAxis.X);
        TaperModifier t = new TaperModifier(1.0, 2.0, SampleAxis.T);
        assertNotEquals(y.modifierHash(), x.modifierHash(), "Y vs X axis");
        assertNotEquals(y.modifierHash(), t.modifierHash(), "Y vs T axis");
    }

    @Test
    void cloneIsIndependent() {
        TaperModifier original = new TaperModifier(1.0, 2.0);
        TaperModifier copy = original.clone();
        copy.setStartScale(99.0);
        assertEquals(1.0, original.getStartScale(), "Mutating clone must not affect original");
    }
}
