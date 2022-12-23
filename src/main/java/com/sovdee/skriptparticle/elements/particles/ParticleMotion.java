package com.sovdee.skriptparticle.elements.particles;


import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.util.Vector;

public class ParticleMotion {

    private Vector defaultMotion;
    private Motion motion;

    public ParticleMotion() {
        this.defaultMotion = new Vector(0, 0, 0);
        this.motion = Motion.NONE;
    }

    public ParticleMotion(Vector defaultMotion) {
        this.defaultMotion = defaultMotion;
        this.motion = Motion.NONE;
    }

    public ParticleMotion(Motion motion, Vector defaultMotion) {
        this.defaultMotion = defaultMotion;
        this.motion = motion;
    }

    public Vector getDefaultMotion() {
        return defaultMotion;
    }

    public void setDefaultMotion(Vector defaultMotion) {
        this.defaultMotion = defaultMotion;
    }

    public Motion getMotion() {
        return motion;
    }

    public void setMotion(Motion motion) {
        this.motion = motion;
    }

    public Vector getMotionVector(Shape shape, Vector point) {
        return switch (motion) {
            case NONE -> defaultMotion;
            case CLOCKWISE -> getClockwiseMotion(shape, point);
            case COUNTERCLOCKWISE -> getClockwiseMotion(shape, point).multiply(-1);
            case INWARDS -> getOutwardsMotion(point).multiply(-1);
            case OUTWARDS -> getOutwardsMotion(point);
        };
    }

    private Vector getClockwiseMotion(Shape shape, Vector point) {
        Vector normal = shape.relativeYAxis();
        return normal.getCrossProduct(point);
    }

    private Vector getOutwardsMotion(Vector point) {
        return point.clone();
    }

    public enum Motion {
        CLOCKWISE, COUNTERCLOCKWISE, INWARDS, OUTWARDS, NONE;
    }
}
