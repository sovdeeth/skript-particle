package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

public interface Shape extends Cloneable {
    /*
     * Gets the points for the shape.
     * Attempts to use cached points if possible, by checking if the shape has been modified.
     * Uses the shape's orientation to rotate the shape.
     * @return A set of points that make up the shape.
     */
    Set<Vector> getPoints();

    /*
     * Sets the points for the shape.
     */
    void setPoints(Set<Vector> points);

    /*
     * Gets the points for the shape.
     * Attempts to use cached points if possible, by checking if the shape has been modified.
     * Uses the given orientation to rotate the shape.
     * @param orientation The orientation of the shape.
     * @return A set of points that make up the shape.
     */
    Set<Vector> getPoints(Quaternion orientation);

    /*
     * Generates the points for the shape. Depends on the set style.
     * No orientation, offset, or scale is applied.
     * @return A set of points that make up the shape.
     */
    Set<Vector> generatePoints();

    /*
     * Generates the points for the shape, if the style is set to OUTLINE.
     * @return A set of points that make up the shape.
     */
    Set<Vector> generateOutline();

    /*
     * Generates the points for the shape, if the style is set to SURFACE.
     * Default implementation is to return the same as generateOutline().
     * @return A set of points that make up the shape.
     */
    Set<Vector> generateSurface();

    /*
     * Generates the points for the shape, if the style is set to FILL.
     * Default implementation is to return the same as generateSurface().
     * @return A set of points that make up the shape.
     */
    Set<Vector> generateFilled();

    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     */
    void draw(@Nullable Location location, @Nullable Quaternion baseOrientation, @Nullable Particle particle, @Nullable Collection<Player> recipients);

    /*
     * Draws a shape at a location, given a starting orientation and a particle to use
     * The provided consumer is run before the shape is drawn, allowing for the shape to be modified before drawing.
     * This method should not be called by a complex shape, but only by the user via EffSecDrawShape.
     */
    void draw(@Nullable Location location, Consumer<Shape> consumer, @Nullable Collection<Player> recipients);

    /*
     * @Returns the relative X axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    Vector getRelativeXAxis(boolean useLastOrientation);

    /*
     * @Returns the relative Y axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    Vector getRelativeYAxis(boolean useLastOrientation);

    /*
     * @Returns the relative Z axis of the shape, either using its default orientation, or the last orientation used to draw the shape.
     */
    Vector getRelativeZAxis(boolean useLastOrientation);

    /*
     * Sets whether the shape will draw its local axes.
     * @param show Whether the shape should draw its local axes.
     */
    void showLocalAxes(boolean show);

    /*
     * @Returns whether the shape will draw its local axes.
     */
    boolean showLocalAxes();

    /*
     * Sets whether the shape will draw its global axes.
     * @param show Whether the shape should draw its global axes.
     */
    void showGlobalAxes(boolean show);

    /*
     * @Returns whether the shape will draw its global axes.
     */
    boolean showGlobalAxes();

    /*
     * @Returns the last location used to draw the shape.
     */
    Location getLastLocation();

    /*
     * @Returns the style of the shape.
     */
    Style getStyle();

    /*
     * Sets the style of the shape. Marks the shape as needing an update.
     */
    void setStyle(Style style);

    /*
     * @Returns the orientation of the shape. Changes to the orientation will
     * cause the shape to update upon the next getPoints() call.
     */
    Quaternion getOrientation();

    /*
     * Sets the orientation of the shape. Marks the shape as needing an update.
     */
    void setOrientation(Quaternionf orientation);

    /*
     * @Returns the scale of the shape.
     */
    double getScale();

    /*
     * Sets the scale of the shape. Marks the shape as needing an update.
     */
    void setScale(double scale);

    /*
     * @Returns the offset of the shape.
     */
    Vector getOffset();

    /*
     * Sets the offset of the shape. Marks the shape as needing an update.
     */
    void setOffset(Vector offset);

    /*
     * @Returns the location of the shape.
     */
    DynamicLocation getLocation();

    /*
     * Sets the location of the shape. This is used as a fallback if the shape is drawn without a location.
     */
    void setLocation(DynamicLocation location);

    /*
     * @Returns the UUID of the shape. Used for uniqueness during serialization.
     */
    UUID getUUID();

    /*
     * @Returns the particle of the shape.
     */
    Particle getParticle();

    /*
     * Sets the particle of the shape.
     */
    void setParticle(Particle particle);

    /*
     * @Returns the particle density of the shape.
     */
    double getParticleDensity();

    /*
     * Sets the particle density of the shape. Marks the shape as needing an update.
     */
    void setParticleDensity(double particleDensity);

    /*
     * @Returns the number of points that the shape has.
     */
    int getParticleCount();

    /*
     * Sets the number of points that the shape should have. Will not always be accurate, but should be close.
     * Marks the shape as needing an update.
     */
    void setParticleCount(int particleCount);

    /*
     * @Returns whether the shape needs an update.
     */
    boolean needsUpdate();

    /*
     * Marks the shape as needing an update or not.
     */
    void setNeedsUpdate(boolean needsUpdate);

    /*
     * @Returns a copy of the shape.
     */
    Shape clone();

    /*
     * Used for copying the shape's properties to a new shape. Intended to be used in the clone() method.
     * @Returns the updated new shape.
     */
    Shape copyTo(Shape shape);

    /*
     * Gets the physical state of a shape, represented by its style, orientation, scale, offset, and particle density.
     * Used for checking if a shape has changed since the last time it was drawn.
     * @Returns A state object that represents the shape's physical state.
     */
    State getState();

    /*
     * Gets the physical state of a shape, but with a custom orientation.
     * @Param orientation The orientation to use for the state.
     * @Returns A state object that represents the shape's physical state.
     */
    State getState(Quaternion orientation);

    void setLastState(State state);

    enum Style {
        OUTLINE,
        SURFACE,
        FILL;

        public String toString() {
            return name().toLowerCase();
        }
    }

    record State(Style style, int orientationHash, double scale, int offsetHash, double particleDensity) {

        /*
         * @Returns whether the state is equal to another state.
         */
        public boolean equals(State state) {
            return state.style() == style &&
                    state.orientationHash() == orientationHash &&
                    state.scale() == scale &&
                    state.offsetHash() == offsetHash &&
                    state.particleDensity() == particleDensity;
        }
    }
}
