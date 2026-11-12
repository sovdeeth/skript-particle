package com.sovdee.shapes.util;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper methods for JOML Vector3d operations that mirror Bukkit Vector convenience methods.
 */
public class VectorUtil {

    /**
     * Rotates a vector around the Y axis by the given angle in radians.
     * Modifies and returns the same vector.
     *
     * @param v     the vector to rotate
     * @param angle the angle in radians
     * @return the rotated vector (same instance)
     */
    public static Vector3d rotateAroundY(Vector3d v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        double x = v.x * cos + v.z * sin;
        double z = -v.x * sin + v.z * cos;
        v.x = x;
        v.z = z;
        return v;
    }

    /**
     * Transforms a list of vectors using a quaternion, modifying them in place.
     */
    public static void transform(Quaterniond quaternion, List<Vector3d> vectors) {
        transform(quaternion, vectors, 0);
    }

    /**
     * Transforms vectors in a list from the given start index, modifying them in place.
     */
    public static void transform(Quaterniond quaternion, List<Vector3d> vectors, int fromIndex) {
        for (int i = fromIndex; i < vectors.size(); i++) {
            quaternion.transform(vectors.get(i));
        }
    }


    /**
     * Removes points from index {@code fromIndex} onwards that lie within roughly {@code precision} of an
     * earlier point in that same range. The first occurrence is kept, and the order of the remaining points
     * is unchanged.
     *
     * @param points    the list to remove duplicates from
     * @param fromIndex the index of the first point to consider
     * @param precision the size of the grid that points are snapped to when compared
     */
    public static void removeDuplicates(List<Vector3d> points, int fromIndex, double precision) {
        Set<Key> seen = new HashSet<>();
        int write = fromIndex;
        for (int read = fromIndex; read < points.size(); read++) {
            Vector3d v = points.get(read);
            Key key = new Key(Math.round(v.x / precision), Math.round(v.y / precision), Math.round(v.z / precision));
            if (seen.add(key))
                points.set(write++, v);
        }
        points.subList(write, points.size()).clear();
    }

    private record Key(long x, long y, long z) {}
}
