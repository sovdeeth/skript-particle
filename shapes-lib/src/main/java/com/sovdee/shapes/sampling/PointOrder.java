package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.StandardInput;
import org.joml.Vector3d;

import java.util.Comparator;

/**
 * Common point orderings for {@link PointSampler#setOrdering(Comparator)}. Points are compared in the shape's
 * local space, so these follow the shape as it rotates.
 */
public final class PointOrder {

    private PointOrder() {}

    /** From lowest to highest {@code x + y + z}: one corner of the shape to the opposite one. */
    public static final Comparator<Vector3d> DIAGONAL = Comparator.comparingDouble(v -> v.x + v.y + v.z);

    /**
     * A shuffled order that is the same every time a shape is sampled, so animations don't flicker between
     * different orders. Points at the same position stay in their generated order.
     */
    public static final Comparator<Vector3d> RANDOM = Comparator.comparingLong(PointOrder::scramble);

    /**
     * Orders points from where the given input is lowest to where it is highest. Matches the definitions the
     * modifiers use: x/y/z along their axis, radius as the distance from the y axis, spherical as the distance
     * from the center, and angle counterclockwise around the y axis starting at +x.
     *
     * @throws IllegalArgumentException for {@link StandardInput#T}, which is the draw order itself
     */
    public static Comparator<Vector3d> along(StandardInput input) {
        return switch (input) {
            case X -> Comparator.comparingDouble(v -> v.x);
            case Y -> Comparator.comparingDouble(v -> v.y);
            case Z -> Comparator.comparingDouble(v -> v.z);
            case RADIUS -> Comparator.comparingDouble(v -> v.x * v.x + v.z * v.z);
            case SPHERICAL -> Comparator.comparingDouble(Vector3d::lengthSquared);
            case ANGLE -> Comparator.comparingDouble(PointOrder::angle);
            case T -> throw new IllegalArgumentException("points cannot be ordered by their own draw order");
        };
    }

    private static double angle(Vector3d v) {
        // same as PointContext#angle, before normalising
        double a = Math.atan2(v.z, v.x);
        return a < 0 ? a + 2 * Math.PI : a;
    }

    private static long scramble(Vector3d v) {
        long h = Double.doubleToLongBits(v.x);
        h = h * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(v.y);
        h = h * 0x9E3779B97F4A7C15L + Double.doubleToLongBits(v.z);
        // splitmix64 finaliser
        h ^= h >>> 30;
        h *= 0xBF58476D1CE4E5B9L;
        h ^= h >>> 27;
        h *= 0x94D049BB133111EBL;
        return h ^ (h >>> 31);
    }
}
