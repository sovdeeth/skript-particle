package com.sovdee.skriptparticle.util;

import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;

import static java.lang.Math.*;

public class Quaternion implements Cloneable {
    public static double degreesToRadians = PI / 180.0;
    public static double radiansToDegrees = 180.0 / PI;
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
        double halfAngle = angle / 2;
        double sin = Math.sin(halfAngle);
        this.w = Math.cos(halfAngle);
        this.x = axis.getX() * sin;
        this.y = axis.getY() * sin;
        this.z = axis.getZ() * sin;
        return this.normalize();
    }
    
    public Quaternion set(Quaternion quaternion) {
        this.w = quaternion.w;
        this.x = quaternion.x;
        this.y = quaternion.y;
        this.z = quaternion.z;
        return this.normalize();
    }
    
    public Quaternion setEulerAngles (double yaw, double pitch, double roll) {
        return setEulerAnglesRad(yaw * degreesToRadians, pitch * degreesToRadians,
                roll * degreesToRadians);
    }
    
    public Quaternion setEulerAnglesRad (double yaw, double pitch, double roll) {
        final double hr = roll * 0.5d;
        final double shr = Math.sin(hr);
        final double chr = Math.cos(hr);
        final double hp = pitch * 0.5d;
        final double shp = Math.sin(hp);
        final double chp = Math.cos(hp);
        final double hy = yaw * 0.5d;
        final double shy = Math.sin(hy);
        final double chy = Math.cos(hy);
        final double chy_shp = chy * shp;
        final double shy_chp = shy * chp;
        final double chy_chp = chy * chp;
        final double shy_shp = shy * shp;

        x = (chy_shp * chr) + (shy_chp * shr); // cos(yaw/2) * sin(pitch/2) * cos(roll/2) + sin(yaw/2) * cos(pitch/2) * sin(roll/2)
        y = (shy_chp * chr) - (chy_shp * shr); // sin(yaw/2) * cos(pitch/2) * cos(roll/2) - cos(yaw/2) * sin(pitch/2) * sin(roll/2)
        z = (chy_chp * shr) - (shy_shp * chr); // cos(yaw/2) * cos(pitch/2) * sin(roll/2) - sin(yaw/2) * sin(pitch/2) * cos(roll/2)
        w = (chy_chp * chr) + (shy_shp * shr); // cos(yaw/2) * cos(pitch/2) * cos(roll/2) + sin(yaw/2) * sin(pitch/2) * sin(roll/2)
        return this.normalize();
    }

    public int getGimbalPole () {
        final double t = y * x + z * w;
        return t > 0.499f ? 1 : (t < -0.499f ? -1 : 0);
    }
    
    public double getRollRad () {
        final int pole = getGimbalPole();
        return pole == 0 ? atan2(2f * (w * z + y * x), 1f - 2f * (x * x + z * z))
                : (double)pole * 2f * atan2(y, w);
    }
    
    public double getRoll () {
        return getRollRad() * radiansToDegrees;
    }
    
    public double getPitchRad () {
        final int pole = getGimbalPole();
        return pole == 0 ? (double)Math.asin(clamp(2f * (w * x - z * y))) : (double)pole * PI * 0.5f;
    }

    private double clamp(double v) {
        return Math.max(-1.0, Math.min(1.0, v));
    }

    public double getPitch () {
        return getPitchRad() * radiansToDegrees;
    }
    
    public double getYawRad () {
        return getGimbalPole() == 0 ? atan2(2f * (y * w + x * z), 1f - 2f * (y * y + x * x)) : 0f;
    }
    
    public double getYaw () {
        return getYawRad() * radiansToDegrees;
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
        double dot = from.dot(to);
        double mag = sqrt(from.lengthSquared() * to.lengthSquared());
        double angle = dot / mag + 1;
        if (angle < 0.0000001 && angle > -0.0000001) {
            Vector v = from.getCrossProduct(vectorForCross);
            return new Quaternion(0, v.getX(), v.getY(), v.getZ());
        }

        Vector a = from.getCrossProduct(to);
        double w = dot + mag;
        return new Quaternion(w, a.getX(), a.getY(), a.getZ()).normalize();
    }

    public static Quaternion rotationToVector(Vector to) {
         double dot = to.getY();
         double mag = to.length();
         double angle = dot / mag + 1;
         if (angle < 0.0000001 && angle > -0.0000001)
            return new Quaternion(0, 0, 0, 1);
         return new Quaternion(dot + mag, to.getZ(), 0, -1 * to.getX()).normalize();
    }

}
