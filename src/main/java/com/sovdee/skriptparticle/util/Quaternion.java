package com.sovdee.skriptparticle.util;

import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;

public class Quaternion implements Cloneable {
    private Quaternion temp1 = new Quaternion(0,0,0,0);
    private Quaternion temp2 = new Quaternion(0,0,0,0);
    private double w;
    private double x;
    private double y;
    private double z;

    public Quaternion(double w, double x, double y, double z) {
        this.set(w, x, y, z);
    }
    public Quaternion(Vector axis, double angle) {
        this.set(axis, angle);
    }

    public double squareLength() {
        return x * x + y * y + z * z + w * w;
    }
    
    public Quaternion normalize() {
        double len = squareLength();
        if (len != 0.0d && len != 1.0d) {
            len = Math.sqrt(len);
            w /= len;
            x /= len;
            y /= len;
            z /= len;
        }
        return this;
    }

    public Quaternion conjugate() {
        x = -x;
        y = -y;
        z = -z;
        return this;
    }
    
    public Vector transform(Vector vector) {
        temp2.set(this);
        temp2.conjugate();
        temp2.multiplyLeft(temp1.set(vector.getX(), vector.getY(), vector.getZ(), 0)).multiplyLeft(this);

        vector.setX(temp2.x);
        vector.setY(temp2.y);
        vector.setZ(temp2.z);
        return vector;
    }

    // a = a * b
    public Quaternion multiply(final Quaternion other) {
        final double newX = this.w * other.x + this.x * other.w + this.y * other.z - this.z * other.y;
        final double newY = this.w * other.y + this.y * other.w + this.z * other.x - this.x * other.z;
        final double newZ = this.w * other.z + this.z * other.w + this.x * other.y - this.y * other.x;
        final double newW = this.w * other.w - this.x * other.x - this.y * other.y - this.z * other.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    // a = a * b
    public Quaternion multiply (final double x, final double y, final double z, final double w) {
        final double newX = this.w * x + this.x * w + this.y * z - this.z * y;
        final double newY = this.w * y + this.y * w + this.z * x - this.x * z;
        final double newZ = this.w * z + this.z * w + this.x * y - this.y * x;
        final double newW = this.w * w - this.x * x - this.y * y - this.z * z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    // a = b * a
    public Quaternion multiplyLeft (Quaternion other) {
        final double newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y;
        final double newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
        final double newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
        final double newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    // a = b * a
    public Quaternion multiplyLeft (final double x, final double y, final double z, final double w) {
        final double newX = w * this.x + x * this.w + y * this.z - z * this.y;
        final double newY = w * this.y + y * this.w + z * this.x - x * this.z;
        final double newZ = w * this.z + z * this.w + x * this.y - y * this.x;
        final double newW = w * this.w - x * this.x - y * this.y - z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this;
    }

    public Quaternion add (Quaternion quaternion) {
        this.x += quaternion.x;
        this.y += quaternion.y;
        this.z += quaternion.z;
        this.w += quaternion.w;
        return this;
    }

    public Quaternion add (double qx, double qy, double qz, double qw) {
        this.x += qx;
        this.y += qy;
        this.z += qz;
        this.w += qw;
        return this;
    }

    public Quaternion clone() {
        return new Quaternion(w, x, y, z);
    }

    public Quaternion set(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    public Quaternion set(Vector axis, double angle) {
        double halfAngle = angle / 2;
        double sin = Math.sin(halfAngle);
        this.w = Math.cos(halfAngle);
        this.x = axis.getX() * sin;
        this.y = axis.getY() * sin;
        this.z = axis.getZ() * sin;
        return this;
    }
    
    public Quaternion set(Quaternion quaternion) {
        this.w = quaternion.w;
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        return this;
    }

    public void serialize(Fields fields) {
        serialize(fields, "");
    }
    public void serialize(Fields fields, String prefix) {
        fields.putPrimitive(prefix + "w", w);
        fields.putPrimitive(prefix + "x", x);
        fields.putPrimitive(prefix + "y", y);
        fields.putPrimitive(prefix + "z", z);
    }


    public static Quaternion deserialize(Fields fields) throws StreamCorruptedException {
        return deserialize(fields, "");
    }
    public static Quaternion deserialize(Fields fields, String prefix) throws StreamCorruptedException {
        double w = fields.getPrimitive(prefix + "w", Double.class);
        double x = fields.getPrimitive(prefix + "x", Double.class);
        double y = fields.getPrimitive(prefix + "y", Double.class);
        double z = fields.getPrimitive(prefix + "z", Double.class);
        return new Quaternion(w, x, y, z);
    }


}
