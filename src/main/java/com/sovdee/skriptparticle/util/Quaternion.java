package com.sovdee.skriptparticle.util;

import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.List;

import static java.lang.Math.PI;

public class Quaternion implements Cloneable {
    public static final Quaternion IDENTITY = new Quaternion(1, 0, 0, 0);
    public static double degreesToRadians = PI / 180.0;
    public static double radiansToDegrees = 180.0 / PI;
    private static final Quaternion temp1 = new Quaternion(0,0,0,0);
    private static final Quaternion temp2 = new Quaternion(0,0,0,0);
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
        double length = vector.length();
        temp2.set(this);
        temp2.conjugate();
        temp2.multiplyLeft(temp1.set(0, vector.getX(), vector.getY(), vector.getZ()));
        temp2.multiplyLeft(this);

        vector.setX(temp2.x);
        vector.setY(temp2.y);
        vector.setZ(temp2.z);
        return vector.multiply(length);
    }

    public Vector transform(List<Vector> vector) {
        for (Vector v : vector) {
            transform(v);
        }
        return null;
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
        return this.normalize();
    }

    // a = b * a
    public Quaternion multiplyLeft (Quaternion other) {
        final double newX = other.w * this.x + other.x * this.w + other.y * this.z - other.z * this.y; //
        final double newY = other.w * this.y + other.y * this.w + other.z * this.x - other.x * this.z;
        final double newZ = other.w * this.z + other.z * this.w + other.x * this.y - other.y * this.x;
        final double newW = other.w * this.w - other.x * this.x - other.y * this.y - other.z * this.z;
        this.x = newX;
        this.y = newY;
        this.z = newZ;
        this.w = newW;
        return this.normalize();
    }

    public Quaternion clone() {
        return new Quaternion(w, x, y, z);
    }

    public Quaternion set(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
        
        return this.normalize();
    }

    public Quaternion set(Vector axis, double angle) {
        Vector axisNorm = axis.clone().normalize();
        double halfAngle = angle / 2;
        double sin = Math.sin(halfAngle);
        this.w = Math.cos(halfAngle);
        this.x = axisNorm.getX() * sin;
        this.y = axisNorm.getY() * sin;
        this.z = axisNorm.getZ() * sin;
        return this;
    }
    
    public Quaternion set(Quaternion quaternion) {
        this.w = quaternion.w;
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        return this.normalize();
    }

    public Vector getAxis() {
        double s_squared = 1 - w * w; // assuming quaternion normalised then w is less than 1, so term always positive.
        if (s_squared < 0.001) { // test to avoid divide by zero, s is always positive due to sqrt
            // if s close to zero then direction of axis not important
            return new Vector(1, 0, 0); // if it is important that axis is normalised then replace with x=1; y=z=0;
        }
        double s = Math.sqrt(s_squared); // normalise s
        return new Vector(x / s, y / s, z / s);
    }

    public double getAngle() {
        double angle = 2 * Math.acos(w);
        return angle;
    }

    public boolean equals (Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Quaternion)) {
            return false;
        }
        Quaternion other = (Quaternion)obj;
        return (Double.doubleToLongBits(w) == Double.doubleToLongBits(other.w))
                && (Double.doubleToLongBits(x) == Double.doubleToLongBits(other.x))
                && (Double.doubleToLongBits(y) == Double.doubleToLongBits(other.y))
                && (Double.doubleToLongBits(z) == Double.doubleToLongBits(other.z));
    }

    public String toString() {
        return "Quaternion{w=" + w + ", x=" + x + ", y=" + y + ", z=" + z + "}";
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

    private static Vector vectorForCross = Vector.getRandom().normalize();
    public static Quaternion rotationFromVectorToVector(Vector from, Vector to) {
        Vector fromNorm = from.clone().normalize();
        Vector toNorm = to.clone().normalize();
        double dot = fromNorm.dot(toNorm);
        double angle = dot + 1;
        if (Double.isNaN(angle) || (angle < 0.00001 && angle > -0.00001)) {
            Vector v = from.getCrossProduct(vectorForCross);
            return new Quaternion(0, v.getX(), v.getY(), v.getZ());
        }

        Vector a = from.getCrossProduct(to);
        return new Quaternion(dot, a.getX(), a.getY(), a.getZ()).normalize();
    }

    public static Quaternion rotationToVector(Vector to) {
         to = to.clone().normalize();
         double dot = to.getY();
         double angle = dot + 1;
         if (Double.isNaN(angle) || (angle < 0.00001 && angle > -0.00001))
            return new Quaternion(1, 0, 0, 0);
         return new Quaternion(angle, to.getZ(), 0, -1 * to.getX()).normalize();
    }

}
