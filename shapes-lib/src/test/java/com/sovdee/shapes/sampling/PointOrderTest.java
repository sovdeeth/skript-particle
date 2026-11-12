package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.shapes.shapes.Circle;
import com.sovdee.shapes.shapes.Helix;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.shapes.Sphere;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PointOrderTest {

    private static void assertOrdered(List<Vector3d> points, Comparator<Vector3d> order, String message) {
        for (int i = 1; i < points.size(); i++)
            assertTrue(order.compare(points.get(i - 1), points.get(i)) <= 0, message + " at " + i);
    }

    /** The points before rotation, scale and offset, in the order the sampler returned them. */
    private static List<Vector3d> local(Shape shape) {
        Quaterniond inverse = new Quaterniond(shape.getOrientation()).invert();
        List<Vector3d> out = new ArrayList<>();
        for (Vector3d p : shape.getPointSampler().getPoints(shape)) {
            Vector3d v = new Vector3d(p).sub(shape.getOffset()).div(shape.getScale());
            out.add(inverse.transform(v));
        }
        return out;
    }

    @Test
    void along_sortsByEveryOrderableInput() {
        Shape sphere = new Sphere(2);
        sphere.getPointSampler().setStyle(SamplingStyle.FILL);
        for (StandardInput input : StandardInput.values()) {
            if (input == StandardInput.T)
                continue;
            Comparator<Vector3d> order = PointOrder.along(input);
            sphere.getPointSampler().setOrdering(order);
            assertOrdered(sphere.getPointSampler().getPoints(sphere), order, input.toString());
        }
    }

    @Test
    void along_drawOrderIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> PointOrder.along(StandardInput.T));
    }

    @Test
    void reversedOrder_isTheExactReverse() {
        Shape helix = new Helix(1, 4, 1);
        helix.getPointSampler().setOrdering(PointOrder.along(StandardInput.Y));
        List<Vector3d> up = new ArrayList<>(helix.getPointSampler().getPoints(helix));
        helix.getPointSampler().setOrdering(PointOrder.along(StandardInput.Y).reversed());
        List<Vector3d> down = helix.getPointSampler().getPoints(helix);
        assertEquals(up.getFirst().y, down.getLast().y, 1e-9);
        assertEquals(up.getLast().y, down.getFirst().y, 1e-9);
    }

    @Test
    void angle_startsAtPlusXAndTurnsTowardPlusZ() {
        Comparator<Vector3d> angle = PointOrder.along(StandardInput.ANGLE);
        List<Vector3d> points = new ArrayList<>(List.of(
                new Vector3d(0, 0, -1), new Vector3d(-1, 0, 0), new Vector3d(0, 0, 1), new Vector3d(1, 0, 0)));
        points.sort(angle);
        assertEquals(List.of(new Vector3d(1, 0, 0), new Vector3d(0, 0, 1), new Vector3d(-1, 0, 0), new Vector3d(0, 0, -1)), points);
    }

    @Test
    void orderIsAppliedInLocalSpace() {
        // a helix lying on its side should still be drawn from its own bottom to its own top
        Shape helix = new Helix(1, 4, 1);
        helix.setOrientation(new Quaterniond().rotateZ(Math.PI / 2));
        helix.setOffset(new Vector3d(5, 0, 0));
        helix.getPointSampler().setOrdering(PointOrder.along(StandardInput.Y));
        assertOrdered(local(helix), PointOrder.along(StandardInput.Y), "local y");
    }

    @Test
    void random_isShuffledButTheSameEveryTime() {
        Shape circle = new Circle(5);
        List<Vector3d> generated = new ArrayList<>(circle.getPointSampler().getPoints(circle));
        circle.getPointSampler().setOrdering(PointOrder.RANDOM);
        List<Vector3d> first = new ArrayList<>(circle.getPointSampler().getPoints(circle));
        assertNotEquals(generated, first, "random order should differ from the generated order");
        assertEquals(generated.size(), first.size());

        Shape again = new Circle(5);
        again.getPointSampler().setOrdering(PointOrder.RANDOM);
        assertEquals(first, again.getPointSampler().getPoints(again), "the same shape should be shuffled the same way");
    }

    @Test
    void diagonal_goesFromTheLowestToTheHighestCorner() {
        Shape sphere = new Sphere(1);
        sphere.getPointSampler().setOrdering(PointOrder.DIAGONAL);
        List<Vector3d> points = sphere.getPointSampler().getPoints(sphere);
        Vector3d first = points.getFirst(), last = points.getLast();
        assertTrue(first.x + first.y + first.z < last.x + last.y + last.z);
    }
}
