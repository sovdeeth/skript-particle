package com.sovdee.shapes.util;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.List;

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

}
