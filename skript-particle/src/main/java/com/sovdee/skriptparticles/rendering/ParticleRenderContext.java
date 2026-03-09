package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.modifiers.PointContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;

/**
 * Plugin-side per-point context extending shapes-lib {@link PointContext} with
 * render-specific properties. Written by render modifiers, read by the renderer.
 *
 * <p>A single instance is allocated per draw call and reused across all points.
 * {@link #reset()} clears all render fields between points.
 */
public class ParticleRenderContext extends PointContext {

    // ---- Set once per draw call ----

    /**
     * Shape orientation in world space, used by motion modifiers to transform the local Y axis.
     */
    public @Nullable Quaterniond orientation;

    /**
     * Shape scale (informational, available to modifiers).
     */
    public double scale = 1.0;

    // ---- Written by render modifiers, read by the renderer ----

    /**
     * Particle type.
     */
    public org.bukkit.Particle particle;
    private final org.bukkit.Particle defaultParticle;

    /**
     * Particle data.
     */
    public Object data;
    private final Object defaultData;

    /**
     * Whether this point should be rendered at all.
     */
    public boolean visible = true;

    /**
     * Position displacement added to the world coordinates at render time.
     */
    public double displacementX, displacementY, displacementZ;

    /**
     * Directional motion override (used as particle velocity). Only applied when {@link #hasMotion}
     * is true.
     */
    public float motionX, motionY, motionZ;

    /**
     * Whether {@link #motionX}/{@link #motionY}/{@link #motionZ} should override the default motion.
     */
    public boolean hasMotion;

    public ParticleRenderContext(Particle particle) {
        this.defaultParticle = particle.particle();
        this.particle = defaultParticle;
        this.defaultData = particle.data();
        this.data = defaultData;
    }

    /**
     * Returns {@code true} if {@link #particle} and {@link #data} are still at their defaults.
     * Used by the renderer to skip re-computing NMS options when no modifier changed them.
     */
    public boolean isDefaultParticle() {
        return particle == defaultParticle && data == defaultData;
    }

    /**
     * Resets all render fields to their defaults before each point is processed.
     */
    @Override
    public void reset() {
        particle = defaultParticle;
        data = defaultData;
        visible = true;
        displacementX = 0;
        displacementY = 0;
        displacementZ = 0;
        motionX = 0;
        motionY = 0;
        motionZ = 0;
        hasMotion = false;
    }
}
