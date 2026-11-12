package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for converting between Bukkit Vectors and JOML Vector3d.
 */
public class VectorConversion {

    public static Vector toBukkit(Vector3d v) {
        return Vector.fromJOML(v);
    }

    public static Vector3d toJOML(Vector v) {
        return v.toVector3d();
    }

    public static List<Vector> toBukkit(List<Vector3d> points) {
        List<Vector> result = new ArrayList<>();
        for (Vector3d v : points) {
            result.add(toBukkit(v));
        }
        return result;
    }

    public static List<Vector3d> toJOML(List<Vector> points) {
        List<Vector3d> result = new ArrayList<>();
        for (Vector v : points) {
            result.add(toJOML(v));
        }
        return result;
    }
}
