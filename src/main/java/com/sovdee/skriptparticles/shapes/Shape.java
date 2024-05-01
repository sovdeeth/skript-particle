package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.nullness.qual.RequiresNonNull;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Represents a shape that can be drawn with particles.
 * Shapes should have a style, which determines how they are drawn.
 * They should also have an orientation, a scale, and an offset which are applied when drawing.
 */
public interface Shape extends Cloneable {
    /**
     * Gets the points for the shape.
     * Attempts to use cached points if possible, by checking if the shape has been modified. Updates the cached points if necessary.
     * Uses the shape's orientation to rotate the shape.
     *
     * @return A set of points that make up the shape.
     */
    Set<Vector> getPoints();

    /**
     * Sets the points for the shape.
     *
     * @param points The points to use.
     */
    void setPoints(Set<Vector> points);

    /**
     * Gets the points for the shape.
     * Attempts to use cached points if possible, by checking if the shape has been modified. Updates the cached points if necessary.
     * Uses the given orientation to rotate the shape.
     *
     * @param orientation The orientation of the shape.
     * @return A set of points that make up the shape.
     */
    Set<Vector> getPoints(Quaternion orientation);

    /**
     * Generates the points for the shape. Depends on the set style.
     * No orientation, offset, or scale is applied.
     */
    @Contract(pure = true)
    default void generatePoints(Set<Vector> points) {
        getStyle().generatePoints(this, points);
    }

    /**
     * Generates the points for the shape, if the style is set to OUTLINE.
     *
     * @param points A set that will be filled with the points that make up the shape.
     */
    @Contract(pure = true)
    void generateOutline(Set<Vector> points);

    /**
     * Generates the points for the shape, if the style is set to SURFACE.
     * Default implementation is to return the same as generateOutline().
     *
     * @param points A set that will be filled with the points that make up the shape.
     */
    @Contract(pure = true)
    void generateSurface(Set<Vector> points);

    /**
     * Generates the points for the shape, if the style is set to FILL.
     * Default implementation is to return the same as generateSurface().
     *
     * @param points A set that will be filled with the points that make up the shape.
     */
    @Contract(pure = true)
    void generateFilled(Set<Vector> points);


    /**
     * Draws a shape using the default location, orientation, and particle.
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     *
     * @param recipients The players to draw the shape for.
     */
    @RequiresNonNull("location")
    void draw(Collection<Player> recipients);

    /**
     * Draws a shape at a location, using the default orientation and particle.
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     *
     * @param location The location to draw the shape at.
     * @param recipients The players to draw the shape for.
     */
    void draw(DynamicLocation location, Collection<Player> recipients);

    /**
     * Draws a shape at a location, given a starting orientation and a particle to use.
     * Caches the last orientation used to draw the shape, so that it can be updated if the orientation changes.
     *
     * @param location The location to draw the shape at.
     * @param baseOrientation The base orientation to draw the shape with. This is applied before the shape's own orientation.
     * @param particle The particle to draw the shape with.
     * @param recipients The players to draw the shape for.
     */
    void draw(DynamicLocation location, Quaternion baseOrientation, Particle particle, Collection<Player> recipients);

    /**
     * Draws a shape at a location, using the default orientation and particle.
     * The provided consumer is run before the shape is drawn, allowing for the shape to be modified before drawing.
     * This method should not be called by a complex shape, but only by the user via EffSecDrawShape.
     *
     * @param location The location to draw the shape at.
     * @param consumer A consumer that is run before the shape is drawn.
     * @param recipients The players to draw the shape for.
     */
    void draw(DynamicLocation location, Consumer<Shape> consumer, Collection<Player> recipients);

    /**
     * @return the relative X axis of the shape, either using its default orientation or the last orientation used to draw the shape.
     */
    Vector getRelativeXAxis(boolean useLastOrientation);

    /**
     * @return the relative Y axis of the shape, either using its default orientation or the last orientation used to draw the shape.
     */
    Vector getRelativeYAxis(boolean useLastOrientation);

    /**
     * @return the relative Z axis of the shape, either using its default orientation or the last orientation used to draw the shape.
     */
    Vector getRelativeZAxis(boolean useLastOrientation);

    /**
     * Sets whether the shape will draw its local axes.
     *
     * @param show Whether the shape should draw its local axes.
     */
    void showLocalAxes(boolean show);

    /**
     * @return whether the shape will draw its local axes.
     */
    boolean showLocalAxes();

    /**
     * Sets whether the shape will draw its global axes.
     *
     * @param show Whether the shape should draw its global axes.
     */
    void showGlobalAxes(boolean show);

    /**
     * @return whether the shape will draw its global axes.
     */
    boolean showGlobalAxes();

    /**
     * @return the last location the shape was drawn at.
     */
    @Nullable
    DynamicLocation getLastLocation();

    /**
     * @return the style of the shape.
     */
    Style getStyle();

    /**
     * Sets the style of the shape. Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param style The style to use.
     */
    void setStyle(Style style);

    /**
     * @return a clone of the orientation of the shape.
     */
    Quaternion getOrientation();

    /**
     * Sets the orientation of the shape. Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param orientation The orientation to use.
     */
    void setOrientation(Quaternionf orientation);

    /**
     * @return the scale of the shape.
     */
    double getScale();

    /**
     * Sets the scale of the shape. Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param scale The scale to use.
     */
    void setScale(double scale);

    /**
     * @return the offset of the shape.
     */
    Vector getOffset();

    /**
     * Sets the offset of the shape. Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param offset The offset vector to use.
     */
    void setOffset(Vector offset);

    /**
     * @return the default location of the shape.
     */
    @Nullable
    DynamicLocation getLocation();

    /**
     * Sets the default location of the shape. This is used as a fallback if the shape is drawn without a given location.
     *
     * @param location The location to use.
     */
    void setLocation(DynamicLocation location);

    /**
     * @return the UUID of the shape. Used for uniqueness during serialization.
     */
    UUID getUUID();

    /**
     * @return a clone of the particle of the shape.
     */
    Particle getParticle();

    /**
     * Sets the particle of the shape.
     *
     * @param particle The particle to use.
     */
    void setParticle(Particle particle);

    /**
     * @return The comparator used to order the particles for drawing
     */
    @Nullable Comparator<Vector> getOrdering();

    /**
     * Sets the comparator to be used to order the particles for drawing.
     * Using a null value means the particles will be drawn in the order they are calculated.
     *
     * @param comparator the Comparator to use.
     */
    void setOrdering(@Nullable Comparator<Vector> comparator);


    /**
     * @return the particle density of the shape.
     */
    double getParticleDensity();

    /**
     * Sets the particle density of the shape. Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param particleDensity The particle density to use, in meters per particle. Must be greater than 0.
     */
    void setParticleDensity(double particleDensity);

    /**
     * @return the number of points that the shape has.
     */
    int getParticleCount();

    /**
     * Sets the number of points that the shape should have. Will not always be accurate, but should be close.
     * Ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param particleCount The number of points to use, ideally. Must be greater than 0.
     */
    void setParticleCount(int particleCount);

    /**
     * @return whether the shape needs an update.
     */
    boolean needsUpdate();

    /**
     * Marks the shape as needing an update or not.
     * A value of true ensures that the shape will be updated upon the next getPoints() call.
     *
     * @param needsUpdate Whether the shape needs an update.
     */
    void setNeedsUpdate(boolean needsUpdate);

    /**
     * @return the duration of time it takes to draw the whole shape, in milliseconds. Default is 0.
     */
    long getAnimationDuration();

    /**
     * Sets the duration of time it takes to draw the whole shape, in milliseconds.
     * A value of 0 causes all points to be drawn at once.
     *
     * @param animationDuration how long it takes to draw the whole shape, in nanoseconds. Points will be drawn in 10ms intervals, so values below 10ms will be treated as 10ms.
     */
    void setAnimationDuration(long animationDuration);

    /**
     * @return a deep copy of the shape.
     */
    @Contract("-> new")
    Shape clone();

    /**
     * Used for deeply copying the shape's properties to a new shape. Intended to be used in the clone() method.
     *
     * @param shape The shape to copy the properties to.
     * @return the updated new shape.
     */
    @Contract("_ -> param1")
    Shape copyTo(Shape shape);

    /**
     * Gets the physical state of a shape, represented by its style, orientation, scale, offset, and particle density.
     * Used for checking if a shape has changed since the last time it was drawn.
     * @return A state object that represents the shape's physical state.
     */
    @Contract("-> new")
    State getState();

    /**
     * Gets the physical state of a shape, but with a custom orientation.
     *
     * @param orientation The orientation to use for the state.
     * @return A state object that represents the shape's physical state.
     */
    @Contract("_ -> new")
    State getState(Quaternion orientation);

    /**
     * Sets the last state of the shape. Used for checking if a shape has changed since the last time it was drawn.
     *
     * @param state The state to set.
     */
    void setLastState(State state);


    /**
     * The style of a shape, which determines how it is drawn.
     * OUTLINE: Draws the shape as a wireframe.
     * SURFACE: Draws the shape as a surface.
     * FILL: Draws the shape as a solid.
     */
    enum Style {
        OUTLINE((shape, points) -> shape.generateOutline(points)),
        SURFACE((shape, points) -> shape.generateSurface(points)),
        FILL((shape, points) -> shape.generateFilled(points));

        private final BiConsumer<Shape, Set<Vector>> generatePoints;

        Style(BiConsumer<Shape, Set<Vector>> generatePoints) {
            this.generatePoints = generatePoints;
        }

        /**
         * Puts the points of the shape, generated using the correct style, into the points parameter.
         */
        public void generatePoints(Shape shape, Set<Vector> points) {
            generatePoints.accept(shape, points);
        }

        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * A state object that represents the physical state of a shape.
     * Used for checking if a shape has changed since the last time it was drawn.
     */
    record State(Style style, int orientationHash, double scale, int offsetHash, double particleDensity) {

        /**
         * @return whether the state is equal to another state.
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
