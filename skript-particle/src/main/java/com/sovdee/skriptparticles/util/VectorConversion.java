package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility class for converting between Bukkit Vectors and JOML Vector3d.
 */
public class VectorConversion {

    public static Vector toBukkit(Vector3d v) {
        return new Vector(v.x, v.y, v.z);
    }

    public static Vector3d toJOML(Vector v) {
        return new Vector3d(v.getX(), v.getY(), v.getZ());
    }

    public static Set<Vector> toBukkit(Set<Vector3d> points) {
        Set<Vector> result = new LinkedHashSet<>();
        for (Vector3d v : points) {
            result.add(toBukkit(v));
        }
        return result;
    }

    public static Set<Vector3d> toJOML(Set<Vector> points) {
        Set<Vector3d> result = new LinkedHashSet<>();
        for (Vector v : points) {
            result.add(toJOML(v));
        }
        return result;
    }
}
