package com.sovdee.skriptparticles.rendering;

import com.sovdee.shapes.sampling.DrawContext;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.jetbrains.annotations.Nullable;

/**
 * Plugin-side rendering metadata attached to library shapes via {@link DrawContext}.
 * Holds particle, location, animation, and debug axis state.
 * Render modifiers live on the shape's {@link com.sovdee.shapes.sampling.PointSampler}.
 */
public class DrawData implements DrawContext {

    private Particle particle;
    private @Nullable DynamicLocation location;
    private @Nullable DynamicLocation lastLocation;
    private final Quaternion lastOrientation;
    private long animationDuration = 0;
    private boolean drawLocalAxes = false;
    private boolean drawGlobalAxes = false;
    /**
     * Set to true after a large-point-count warning has been emitted for this shape.
     */
    private boolean largeSizeWarned = false;

    /**
     * Creates a new {@code DrawData} with default settings: a FLAME particle with zero extra
     * speed, no location, and an identity last-orientation quaternion.
     */
    public DrawData() {
        this.particle = new Particle(org.bukkit.Particle.FLAME).extra(0);
        this.lastOrientation = Quaternion.IDENTITY.clone();
    }

    /**
     * Gets the DrawData attached to a shape's PointSampler, creating and attaching one if missing.
     */
    public static DrawData of(Shape shape) {
        DrawContext ctx = shape.getPointSampler().getDrawContext();
        if (ctx instanceof DrawData dd) return dd;
        DrawData dd = new DrawData();
        shape.getPointSampler().setDrawContext(dd);
        return dd;
    }

    // ---- Particle ----

    /**
     * Returns a clone of the particle stored in this draw data. Callers may modify the returned
     * instance without affecting the stored particle.
     *
     * @return a cloned copy of the particle
     */
    public Particle getParticle() {
        return particle.clone();
    }

    /**
     * Returns the raw (uncloned) particle stored in this draw data. Modifications to the returned
     * instance will directly affect the stored particle.
     *
     * @return the stored {@link Particle} instance
     */
    public Particle getParticleRaw() {
        return particle;
    }

    /**
     * Replaces the stored particle with the given instance.
     *
     * @param particle the new particle to store
     */
    public void setParticle(Particle particle) {
        this.particle = particle;
    }

    // ---- Location ----

    /**
     * Returns a clone of the target draw location, or {@code null} if no location has been set.
     *
     * @return a cloned {@link DynamicLocation}, or {@code null}
     */
    @Nullable
    public DynamicLocation getLocation() {
        if (location == null) return null;
        return location.clone();
    }

    /**
     * Sets the target draw location for this shape.
     *
     * @param location the {@link DynamicLocation} to draw at
     */
    public void setLocation(DynamicLocation location) {
        this.location = location;
    }

    /**
     * Returns the location that was active during the most recent draw call, or {@code null} if
     * the shape has not yet been drawn.
     *
     * @return the last draw location, or {@code null}
     */
    @Nullable
    public DynamicLocation getLastLocation() {
        return lastLocation;
    }

    /**
     * Records the location used during the most recent draw call. Called internally by
     * {@link DrawManager}.
     *
     * @param lastLocation the location to store as the last draw location, or {@code null}
     */
    public void setLastLocation(@Nullable DynamicLocation lastLocation) {
        this.lastLocation = lastLocation;
    }

    // ---- Orientation ----

    /**
     * Returns the combined orientation quaternion that was used during the most recent draw call.
     * The returned instance is the live object; mutating it affects the stored value.
     *
     * @return the last draw orientation as a {@link Quaternion}
     */
    public Quaternion getLastOrientation() {
        return lastOrientation;
    }

    /**
     * Updates the stored last-orientation quaternion to match the given value.
     *
     * @param orientation the orientation to store
     */
    public void setLastOrientation(Quaternion orientation) {
        this.lastOrientation.set(orientation);
    }

    // ---- Animation ----

    /**
     * Returns the animation duration in milliseconds. A value of 0 means the shape is drawn
     * instantaneously rather than animated.
     *
     * @return the animation duration in milliseconds
     */
    public long getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Sets the animation duration in milliseconds. Set to 0 to disable animation.
     *
     * @param animationDuration the animation duration in milliseconds
     */
    public void setAnimationDuration(long animationDuration) {
        this.animationDuration = animationDuration;
    }

    // ---- Axes ----

    /**
     * Returns {@code true} if the shape's local orientation axes should be drawn as debug
     * particles after rendering the shape.
     *
     * @return {@code true} if local axes are drawn
     */
    public boolean showLocalAxes() {
        return drawLocalAxes;
    }

    /**
     * Sets whether the shape's local orientation axes should be drawn as debug particles.
     *
     * @param show {@code true} to draw local axes
     */
    public void showLocalAxes(boolean show) {
        this.drawLocalAxes = show;
    }

    /**
     * Returns {@code true} if the world-space global axes should be drawn as debug particles
     * at the shape's origin after rendering.
     *
     * @return {@code true} if global axes are drawn
     */
    public boolean showGlobalAxes() {
        return drawGlobalAxes;
    }

    /**
     * Sets whether the global (world-space) axes should be drawn as debug particles.
     *
     * @param show {@code true} to draw global axes
     */
    public void showGlobalAxes(boolean show) {
        this.drawGlobalAxes = show;
    }

    // ---- Warning flag ----

    /**
     * Returns {@code true} if a large-particle-count warning has already been emitted for this
     * shape during the current session. Used to prevent repeated warnings.
     *
     * @return {@code true} if the large-size warning has been emitted
     */
    public boolean isLargeSizeWarned() {
        return largeSizeWarned;
    }

    /**
     * Sets the large-size warning flag to suppress duplicate warnings.
     *
     * @param warned {@code true} to mark the warning as already emitted
     */
    public void setLargeSizeWarned(boolean warned) {
        this.largeSizeWarned = warned;
    }

    // ---- DrawContext ----

    /**
     * Creates a deep copy of this {@code DrawData}, duplicating the particle, location,
     * last location, last orientation, animation duration, and axis-draw flags. The
     * {@link #isLargeSizeWarned()} flag is not copied and resets to {@code false} in the clone.
     *
     * @return a new {@code DrawData} with the same settings as this instance
     */
    @Override
    public DrawData copy() {
        DrawData copy = new DrawData();
        copy.particle = this.particle.clone();
        if (this.location != null)
            copy.location = this.location.clone();
        if (this.lastLocation != null)
            copy.lastLocation = this.lastLocation.clone();
        copy.lastOrientation.set(this.lastOrientation);
        copy.animationDuration = this.animationDuration;
        copy.drawLocalAxes = this.drawLocalAxes;
        copy.drawGlobalAxes = this.drawGlobalAxes;
        return copy;
    }
}
