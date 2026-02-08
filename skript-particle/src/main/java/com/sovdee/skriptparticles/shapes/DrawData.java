package com.sovdee.skriptparticles.shapes;

import com.sovdee.shapes.DrawContext;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Plugin-side rendering metadata attached to library shapes via {@link DrawContext}.
 * Holds particle, location, animation, and debug axis state.
 */
public class DrawData implements DrawContext {

    private Particle particle;
    private @Nullable DynamicLocation location;
    private @Nullable DynamicLocation lastLocation;
    private final Quaternion lastOrientation;
    private long animationDuration = 0;
    private boolean drawLocalAxes = false;
    private boolean drawGlobalAxes = false;

    public DrawData() {
        this.particle = (Particle) new Particle(org.bukkit.Particle.FLAME).extra(0);
        this.lastOrientation = Quaternion.IDENTITY.clone();
    }

    /**
     * Gets the DrawData attached to a shape, creating and attaching one if missing.
     */
    public static DrawData of(com.sovdee.shapes.Shape shape) {
        DrawContext ctx = shape.getDrawContext();
        if (ctx instanceof DrawData dd) return dd;
        DrawData dd = new DrawData();
        shape.setDrawContext(dd);
        return dd;
    }

    // ---- Particle ----

    public Particle getParticle() { return particle.clone(); }

    public Particle getParticleRaw() { return particle; }

    public void setParticle(Particle particle) { this.particle = particle; }

    // ---- Location ----

    @Nullable
    public DynamicLocation getLocation() {
        if (location == null) return null;
        return location.clone();
    }

    public void setLocation(DynamicLocation location) { this.location = location; }

    @Nullable
    public DynamicLocation getLastLocation() { return lastLocation; }

    public void setLastLocation(@Nullable DynamicLocation lastLocation) { this.lastLocation = lastLocation; }

    // ---- Orientation ----

    public Quaternion getLastOrientation() { return lastOrientation; }

    public void setLastOrientation(Quaternion orientation) { this.lastOrientation.set(orientation); }

    // ---- Animation ----

    public long getAnimationDuration() { return animationDuration; }

    public void setAnimationDuration(long animationDuration) { this.animationDuration = animationDuration; }

    // ---- Axes ----

    public boolean showLocalAxes() { return drawLocalAxes; }

    public void showLocalAxes(boolean show) { this.drawLocalAxes = show; }

    public boolean showGlobalAxes() { return drawGlobalAxes; }

    public void showGlobalAxes(boolean show) { this.drawGlobalAxes = show; }

    // ---- DrawContext ----

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
