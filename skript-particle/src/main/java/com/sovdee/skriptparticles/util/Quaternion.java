package com.sovdee.skriptparticles.util;

import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Helper class for JOML's Quaternionf class
 * Adds methods to deal with Bukkit's Vector class instead of JOML's Vector3f class
 * Adds a method to transform a collection of vectors
 * Adds proper cloning
 */
public class Quaternion extends Quaternionf implements Cloneable {

    public static final Quaternion IDENTITY = new Quaternion(0, 0, 0, 1);

    public Quaternion() {
        super();
    }

    public Quaternion(double x, double y, double z, double w) {
        super((float) x, (float) y, (float) z, (float) w);
    }

    public Quaternion(float x, float y, float z, float w) {
        super(x, y, z, w);
    }

    public Quaternion(Vector axis, float angle) {
        super();
        this.setAngleAxis(angle, (float) axis.getX(), (float) axis.getY(), (float) axis.getZ());
    }

    public Quaternion(Quaternionf quaternion) {
        super();
        this.set(quaternion);
    }

    public Quaternion(Quaternion quaternion) {
        super();
        this.set(quaternion);
    }

    public List<Vector> transform(List<Vector> vectors) {
        vectors.forEach(this::transform);
        return vectors;
    }

    public Vector transform(Vector vector) {
        Vector3f vector3f = new Vector3f((float) vector.getX(), (float) vector.getY(), (float) vector.getZ());
        vector3f = this.transform(vector3f);
        return vector.setX(vector3f.x).setY(vector3f.y).setZ(vector3f.z);
    }

    public Quaternion rotationTo(Vector to) {
        Vector3f vector3f = new Vector3f((float) to.getX(), (float) to.getY(), (float) to.getZ());
        return (Quaternion) this.rotationTo(new Vector3f(0, 1, 0), vector3f);
    }

    public Quaternion rotationTo(Vector from, Vector to) {
        Vector3f vector3f = new Vector3f((float) from.getX(), (float) from.getY(), (float) from.getZ());
        Vector3f vector3f2 = new Vector3f((float) to.getX(), (float) to.getY(), (float) to.getZ());
        return (Quaternion) this.rotationTo(vector3f, vector3f2);
    }

    public Quaternion rotationAxis(float angle, Vector axis) {
        Vector3f vector3f = new Vector3f((float) axis.getX(), (float) axis.getY(), (float) axis.getZ());
        return (Quaternion) this.rotationAxis(angle, vector3f);
    }

    public Quaternion clone() {
        return new Quaternion(this);
    }
}
