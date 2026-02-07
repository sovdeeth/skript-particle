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
    public static List<Vector3d> transform(Quaterniond quaternion, List<Vector3d> vectors) {
        vectors.replaceAll(v -> quaternion.transform(v));
        return vectors;
    }

    /**
     * Transforms a set of vectors using a quaternion, returning a new set.
     */
    public static Set<Vector3d> transform(Quaterniond quaternion, Set<Vector3d> vectors) {
        Set<Vector3d> newVectors = new HashSet<>();
        for (Vector3d vector : vectors) {
            newVectors.add(quaternion.transform(new Vector3d(vector)));
        }
        return newVectors;
    }
}
