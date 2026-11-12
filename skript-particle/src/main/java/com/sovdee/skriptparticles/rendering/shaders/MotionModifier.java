package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.joml.Quaterniond;
import org.joml.Vector3d;

/**
 * Applies directional particle velocity based on the point's position relative to the shape.
 * Replaces the removed {@code ParticleMotion} enum.
 */
public class MotionModifier implements PointModifier<ParticleRenderContext> {

    public enum Mode {
        /**
         * Counterclockwise orbit around the shape's Y axis.
         */
        COUNTERCLOCKWISE,
        /**
         * Clockwise orbit around the shape's Y axis.
         */
        CLOCKWISE,
        /**
         * Velocity directed toward the origin.
         */
        INWARDS,
        /**
         * Velocity directed away from the origin.
         */
        OUTWARDS,
        /**
         * No velocity (zero motion).
         */
        NONE,
        /**
         * The same velocity for every point, in world space.
         */
        GLOBAL,
        /**
         * A velocity relative to each point's place around the shape: x outwards from the shape's y axis,
         * y along the shape's y axis, and z around it (positive is counterclockwise, like {@link #COUNTERCLOCKWISE}).
         */
        RELATIVE
    }

    private final Mode mode;
    // the velocity for GLOBAL and RELATIVE; unused by the other modes
    private final Vector3d velocity = new Vector3d();
    /**
     * Y axis in world space after orientation transform, cached in prepare().
     */
    private float yAxisX, yAxisY, yAxisZ;
    private boolean yAxisComputed = false;

    public MotionModifier(Mode mode) {
        this.mode = mode;
    }

    /**
     * @param mode     {@link Mode#GLOBAL} or {@link Mode#RELATIVE}
     * @param velocity the velocity; its length is the speed, multiplied by the particle's speed when drawn
     */
    public MotionModifier(Mode mode, Vector3d velocity) {
        this.mode = mode;
        this.velocity.set(velocity);
    }

    @Override
    public void prepare(ShapeBounds bounds) {
        // Reset so we re-compute on first ParticleRenderContext seen
        yAxisComputed = false;
    }

    @Override
    public Class<ParticleRenderContext> contextType() {
        return ParticleRenderContext.class;
    }

    @Override
    public void modify(ParticleRenderContext rc) {
        if (mode == Mode.NONE) return;

        // Lazily compute Y axis from orientation on first point
        if (!yAxisComputed) {
            if (rc.orientation != null) {
                Quaterniond q = rc.orientation;
                // float cast for the transform
                org.joml.Quaternionf qf = new org.joml.Quaternionf(
                        (float) q.x, (float) q.y, (float) q.z, (float) q.w);
                Vector3d y = qf.transform(new Vector3d(0, 1, 0));
                yAxisX = (float) y.x;
                yAxisY = (float) y.y;
                yAxisZ = (float) y.z;
            } else {
                yAxisX = 0; yAxisY = 1; yAxisZ = 0;
            }
            yAxisComputed = true;
        }

        float px = (float) rc.x;
        float py = (float) rc.y;
        float pz = (float) rc.z;

        float mx, my, mz;
        switch (mode) {
            case COUNTERCLOCKWISE -> {
                // cross(yAxis, point)
                mx = yAxisY * pz - yAxisZ * py;
                my = yAxisZ * px - yAxisX * pz;
                mz = yAxisX * py - yAxisY * px;
                float len = (float) Math.sqrt(mx * mx + my * my + mz * mz);
                if (len > 1e-6f) { mx /= len; my /= len; mz /= len; }
            }
            case CLOCKWISE -> {
                // -cross(yAxis, point)
                mx = -(yAxisY * pz - yAxisZ * py);
                my = -(yAxisZ * px - yAxisX * pz);
                mz = -(yAxisX * py - yAxisY * px);
                float len = (float) Math.sqrt(mx * mx + my * my + mz * mz);
                if (len > 1e-6f) { mx /= len; my /= len; mz /= len; }
            }
            case OUTWARDS -> {
                float len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 1e-6f) { mx = px / len; my = py / len; mz = pz / len; }
                else { mx = my = mz = 0; }
            }
            case INWARDS -> {
                float len = (float) Math.sqrt(px * px + py * py + pz * pz);
                if (len > 1e-6f) { mx = -px / len; my = -py / len; mz = -pz / len; }
                else { mx = my = mz = 0; }
            }
            case GLOBAL -> {
                mx = (float) velocity.x;
                my = (float) velocity.y;
                mz = (float) velocity.z;
            }
            case RELATIVE -> {
                // outwards: the point with its part along the y axis removed
                float along = px * yAxisX + py * yAxisY + pz * yAxisZ;
                float ox = px - along * yAxisX, oy = py - along * yAxisY, oz = pz - along * yAxisZ;
                float len = (float) Math.sqrt(ox * ox + oy * oy + oz * oz);
                if (len > 1e-6f) { ox /= len; oy /= len; oz /= len; }
                else { ox = oy = oz = 0; }
                // around: cross(yAxis, outwards), the counterclockwise direction
                float ax = yAxisY * oz - yAxisZ * oy;
                float ay = yAxisZ * ox - yAxisX * oz;
                float az = yAxisX * oy - yAxisY * ox;
                float vx = (float) velocity.x, vy = (float) velocity.y, vz = (float) velocity.z;
                mx = vx * ox + vy * yAxisX + vz * ax;
                my = vx * oy + vy * yAxisY + vz * ay;
                mz = vx * oz + vy * yAxisZ + vz * az;
            }
            default -> { return; }
        }

        rc.motionX = mx;
        rc.motionY = my;
        rc.motionZ = mz;
        rc.hasMotion = true;
    }

    @Override
    public int modifierHash() {
        return 31 * mode.ordinal() + velocity.hashCode();
    }

    public Mode getMode() {
        return mode;
    }

    /**
     * @return whether this modifier's motion comes from {@link #getVelocity()}
     */
    public boolean hasVelocity() {
        return mode == Mode.GLOBAL || mode == Mode.RELATIVE;
    }

    /**
     * @return a copy of the velocity used by {@link Mode#GLOBAL} and {@link Mode#RELATIVE}
     */
    public Vector3d getVelocity() {
        return new Vector3d(velocity);
    }

    public void setVelocity(Vector3d velocity) {
        this.velocity.set(velocity);
    }

    @Override
    public MotionModifier clone() {
        return new MotionModifier(mode, velocity);
    }
}
