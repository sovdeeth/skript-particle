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

    /**
     * Checks whether a point is inside a closed polygon on the XZ plane, using ray casting.
     * The y components of the vertices are ignored.
     */
    public static boolean isInPolygonXZ(List<Vector3d> vertices, double x, double z) {
        boolean inside = false;
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = vertices.get(i).x, zi = vertices.get(i).z;
            double xj = vertices.get(j).x, zj = vertices.get(j).z;
            if ((zi > z) != (zj > z) && x < (xj - xi) * (z - zi) / (zj - zi) + xi)
                inside = !inside;
        }
        return inside;
    }

    /**
     * Checks whether a point on the XZ plane is within {@code tolerance} of the outline of a closed polygon.
     * The y components of the vertices are ignored.
     */
    public static boolean isNearPolygonXZ(List<Vector3d> vertices, double x, double z, double tolerance) {
        Vector3d point = new Vector3d(x, 0, z);
        Vector3d a = new Vector3d();
        Vector3d b = new Vector3d();
        int n = vertices.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            a.set(vertices.get(i).x, 0, vertices.get(i).z);
            b.set(vertices.get(j).x, 0, vertices.get(j).z);
            if (distanceToSegmentSquared(point, a, b) <= tolerance * tolerance)
                return true;
        }
        return false;
    }

    /**
     * @return the squared distance between a point and the line segment from {@code a} to {@code b}
     */
    public static double distanceToSegmentSquared(Vector3d point, Vector3d a, Vector3d b) {
        double abx = b.x - a.x, aby = b.y - a.y, abz = b.z - a.z;
        double lengthSquared = abx * abx + aby * aby + abz * abz;
        double t = 0;
        if (lengthSquared > 0) {
            t = ((point.x - a.x) * abx + (point.y - a.y) * aby + (point.z - a.z) * abz) / lengthSquared;
            t = Math.clamp(t, 0.0, 1.0);
        }
        double dx = a.x + abx * t - point.x;
        double dy = a.y + aby * t - point.y;
        double dz = a.z + abz * t - point.z;
        return dx * dx + dy * dy + dz * dz;
    }

        private record Key(long x, long y, long z) {}
}
