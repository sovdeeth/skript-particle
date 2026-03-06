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
        NONE
    }

    private final Mode mode;
    /**
     * Y axis in world space after orientation transform, cached in prepare().
     */
    private float yAxisX, yAxisY, yAxisZ;
    private boolean yAxisComputed = false;

    public MotionModifier(Mode mode) {
        this.mode = mode;
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
            default -> { return; }
        }

        rc.motionX = mx;
        rc.motionY = my;
        rc.motionZ = mz;
        rc.hasMotion = true;
    }

    @Override
    public int modifierHash() {
        return mode.ordinal();
    }

    public Mode getMode() {
        return mode;
    }

    @Override
    public MotionModifier clone() {
        return new MotionModifier(mode);
    }
}
