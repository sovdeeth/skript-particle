package com.sovdee.skriptparticles.particles;


import org.bukkit.util.Vector;

public enum ParticleMotion {
    CLOCKWISE,
    COUNTERCLOCKWISE,
    INWARDS,
    OUTWARDS,
    NONE;

    private static final Vector DEFAULT_MOTION = new Vector(0, 0, 0);

    public Vector getMotionVector(Vector axis, Vector point) {
        return switch (this) {
            case NONE -> DEFAULT_MOTION.clone();
            case CLOCKWISE -> getAntiClockwiseMotion(axis, point).multiply(-1);
            case COUNTERCLOCKWISE -> getAntiClockwiseMotion(axis, point);
            case INWARDS -> getOutwardsMotion(point).multiply(-1);
            case OUTWARDS -> getOutwardsMotion(point);
        };
    }

    private Vector getAntiClockwiseMotion(Vector axis, Vector point) {
        return axis.getCrossProduct(point).normalize();
    }

    private Vector getOutwardsMotion(Vector point) {
        return point.clone().normalize();
    }
}
