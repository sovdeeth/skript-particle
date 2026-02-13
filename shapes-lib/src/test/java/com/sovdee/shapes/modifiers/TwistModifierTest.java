package com.sovdee.shapes.modifiers;

import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TwistModifierTest {

    private PointContext ctx(double x, double y, double z, List<Vector3d> allPoints) {
        ShapeBounds bounds = ShapeBounds.compute(allPoints);
        PointContext c = new PointContext();
        c.bounds = bounds;
        c.x = x; c.y = y; c.z = z;
        return c;
    }

    private List<Vector3d> ySpan() {
        return List.of(new Vector3d(0, 0, 0), new Vector3d(0, 1, 0));
    }

    private List<Vector3d> xSpan() {
        return List.of(new Vector3d(0, 0, 0), new Vector3d(1, 0, 0));
    }

    // --- Default Y-axis tests ---

    @Test
    void noTwistAtBottom() {
        TwistModifier mod = new TwistModifier(Math.PI);
        PointContext c = ctx(1, 0, 0, ySpan());
        mod.modify(c);
        // angle = PI * 0 = 0 → identity rotation
        assertEquals(1.0, c.x, 1e-9);
        assertEquals(0.0, c.z, 1e-9);
    }

    @Test
    void fullTwistAtTop() {
        // totalAngle = 2π, normalizedY=1 → full 360° rotation → identity
        TwistModifier mod = new TwistModifier(2 * Math.PI);
        PointContext c = ctx(1, 1, 0, ySpan());
        mod.modify(c);
        assertEquals(1.0, c.x, 1e-9, "Full 2π twist should return to original X");
        assertEquals(0.0, c.z, 1e-9, "Full 2π twist should return to original Z");
    }

    @Test
    void quarterTwistAtMidpoint() {
        // totalAngle=π, normalizedY=0.5 → angle=π/2 (90°)
        // newX = x*cos - z*sin = 1*cos(π/2) - 0*sin(π/2) = 0
        // newZ = x*sin + z*cos = 1*sin(π/2) + 0*cos(π/2) = 1
        TwistModifier mod = new TwistModifier(Math.PI);
        PointContext c = ctx(1, 0.5, 0, ySpan());
        mod.modify(c);
        assertEquals(0.0, c.x, 1e-9);
        assertEquals(1.0, c.z, 1e-9);
    }

    @Test
    void yCoordinateUnchanged() {
        TwistModifier mod = new TwistModifier(Math.PI);
        PointContext c = ctx(1, 0.5, 0, ySpan());
        double origY = c.y;
        mod.modify(c);
        assertEquals(origY, c.y, 1e-9, "Twist along Y must not change Y");
    }

    // --- X-axis twist: rotates YZ plane, X unchanged ---

    @Test
    void xAxis_quarterTwist_rotatesYZ() {
        // totalAngle=π, normalizedX=0.5 → angle=π/2
        // point (0, 1, 0): newY = 1*cos(π/2) - 0*sin(π/2) = 0, newZ = 1*sin(π/2) + 0*cos(π/2) = 1
        TwistModifier mod = new TwistModifier(Math.PI, SampleAxis.X);
        PointContext c = ctx(0.5, 1, 0, xSpan());
        mod.modify(c);
        assertEquals(0.0, c.y, 1e-9, "Y rotated to 0");
        assertEquals(1.0, c.z, 1e-9, "Z rotated to 1");
        assertEquals(0.5, c.x, 1e-9, "X must not change when axis=X");
    }

    @Test
    void xAxis_xCoordinateUnchanged() {
        TwistModifier mod = new TwistModifier(Math.PI, SampleAxis.X);
        PointContext c = ctx(0.5, 1, 0, xSpan());
        double origX = c.x;
        mod.modify(c);
        assertEquals(origX, c.x, 1e-9, "Twist along X must not change X");
    }

    // --- Hash and clone ---

    @Test
    void modifierHashChangesWithAngle() {
        TwistModifier a = new TwistModifier(Math.PI);
        TwistModifier b = new TwistModifier(Math.PI / 2);
        assertNotEquals(a.modifierHash(), b.modifierHash());
    }

    @Test
    void modifierHashChangesWithAxis() {
        TwistModifier y = new TwistModifier(Math.PI, SampleAxis.Y);
        TwistModifier x = new TwistModifier(Math.PI, SampleAxis.X);
        TwistModifier t = new TwistModifier(Math.PI, SampleAxis.T);
        assertNotEquals(y.modifierHash(), x.modifierHash(), "Y vs X axis");
        assertNotEquals(y.modifierHash(), t.modifierHash(), "Y vs T axis");
    }

    @Test
    void cloneIsIndependent() {
        TwistModifier original = new TwistModifier(Math.PI);
        TwistModifier copy = original.clone();
        copy.setTotalAngle(99.0);
        assertEquals(Math.PI, original.getTotalAngle(), "Mutating clone must not affect original");
    }
}
