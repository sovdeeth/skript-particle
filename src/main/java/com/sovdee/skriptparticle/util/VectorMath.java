package com.sovdee.skriptparticle.util;

import org.bukkit.util.Vector;

public class VectorMath {
    public static final Vector defaultNormal = new Vector(0, 1, 0);

    public static RotationValue getRotationValues(Vector normal) {
        // ensure normal is normalized, then get cross prod and angle from dot prod
        normal.normalize();
        Vector cross = defaultNormal.getCrossProduct(normal);
        double angle = Math.acos(defaultNormal.dot(normal));
        return new RotationValue(cross, angle);
    }

    public static class RotationValue {
        public Vector cross;
        public double angle;

        public RotationValue(Vector cross, double angle){
            this.cross = cross;
            this.angle = angle;
        }

        public Vector getCross() {
            return cross;
        }

        public double getAngle() {
            return angle;
        }
    }
}
