package com.sovdee.shapes;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Represents a geometric shape defined by a set of points.
 * Shapes have a style, orientation, scale, and offset which affect point generation.
 */
public interface Shape extends Cloneable {

    /**
     * Gets the points for the shape using the shape's own orientation.
     * Uses cached points if the shape has not been modified.
     *
     * @return A set of points that make up the shape.
     */
    Set<Vector3d> getPoints();

    /**
     * Sets the points for the shape.
     *
     * @param points The points to use.
     */
    void setPoints(Set<Vector3d> points);

    /**
     * Gets the points for the shape using the given orientation.
     * Uses cached points if the shape has not been modified.
     *
     * @param orientation The orientation to apply.
     * @return A set of points that make up the shape.
     */
    Set<Vector3d> getPoints(Quaterniond orientation);

    /**
     * Generates the points for the shape based on the current style.
     * No orientation, offset, or scale is applied.
     */
    default void generatePoints(Set<Vector3d> points) {
        getStyle().generatePoints(this, points);
    }

    /**
     * Generates the outline points for the shape.
     *
     * @param points A set that will be filled with the points.
     */
    void generateOutline(Set<Vector3d> points);

    /**
     * Generates the surface points for the shape.
     * Default implementation returns the same as generateOutline().
     *
     * @param points A set that will be filled with the points.
     */
    void generateSurface(Set<Vector3d> points);

    /**
     * Generates the filled points for the shape.
     * Default implementation returns the same as generateSurface().
     *
     * @param points A set that will be filled with the points.
     */
    void generateFilled(Set<Vector3d> points);

    /**
     * @return the relative X axis of the shape.
     */
    Vector3d getRelativeXAxis(boolean useLastOrientation);

    /**
     * @return the relative Y axis of the shape.
     */
    Vector3d getRelativeYAxis(boolean useLastOrientation);

    /**
     * @return the relative Z axis of the shape.
     */
    Vector3d getRelativeZAxis(boolean useLastOrientation);

    /**
     * @return the style of the shape.
     */
    Style getStyle();

    /**
     * Sets the style of the shape.
     *
     * @param style The style to use.
     */
    void setStyle(Style style);

    /**
     * @return a clone of the orientation of the shape.
     */
    Quaterniond getOrientation();

    /**
     * Sets the orientation of the shape.
     *
     * @param orientation The orientation to use.
     */
    void setOrientation(Quaterniond orientation);

    /**
     * @return the scale of the shape.
     */
    double getScale();

    /**
     * Sets the scale of the shape.
     *
     * @param scale The scale to use.
     */
    void setScale(double scale);

    /**
     * @return the offset of the shape.
     */
    Vector3d getOffset();

    /**
     * Sets the offset of the shape.
     *
     * @param offset The offset vector to use.
     */
    void setOffset(Vector3d offset);

    /**
     * @return the UUID of the shape.
     */
    UUID getUUID();

    /**
     * @return The comparator used to order points, or null for default ordering.
     */
    Comparator<Vector3d> getOrdering();

    /**
     * Sets the comparator to order the points.
     *
     * @param comparator the Comparator to use, or null for default ordering.
     */
    void setOrdering(Comparator<Vector3d> comparator);

    /**
     * @return the particle density of the shape.
     */
    double getParticleDensity();

    /**
     * Sets the particle density of the shape.
     *
     * @param particleDensity The density in meters per particle. Must be greater than 0.
     */
    void setParticleDensity(double particleDensity);

    /**
     * @return the number of points in the shape.
     */
    int getParticleCount();

    /**
     * Sets the approximate number of points for the shape.
     *
     * @param particleCount The target number of points. Must be greater than 0.
     */
    void setParticleCount(int particleCount);

    /**
     * @return the draw context attached to this shape, or null if none.
     */
    DrawContext getDrawContext();

    /**
     * Sets the draw context for this shape.
     *
     * @param drawContext The draw context to attach.
     */
    void setDrawContext(DrawContext drawContext);

    /**
     * @return whether the shape is dynamic (always regenerates points).
     */
    boolean isDynamic();

    /**
     * Sets whether the shape is dynamic.
     * Dynamic shapes always regenerate points, bypassing state caching.
     *
     * @param dynamic Whether the shape is dynamic.
     */
    void setDynamic(boolean dynamic);

    /**
     * @return whether the shape needs a point update.
     */
    boolean needsUpdate();

    /**
     * Marks the shape as needing an update or not.
     *
     * @param needsUpdate Whether the shape needs an update.
     */
    void setNeedsUpdate(boolean needsUpdate);

    /**
     * @return a deep copy of the shape.
     */
    Shape clone();

    /**
     * Copies the shape's properties to a new shape.
     *
     * @param shape The shape to copy properties to.
     * @return the updated shape.
     */
    Shape copyTo(Shape shape);

    /**
     * Gets the physical state of the shape for change detection.
     *
     * @return A state object representing the shape's current state.
     */
    State getState();

    /**
     * Gets the physical state with a custom orientation.
     *
     * @param orientation The orientation to use for the state.
     * @return A state object representing the shape's state.
     */
    State getState(Quaterniond orientation);

    /**
     * Sets the last state of the shape for change detection.
     *
     * @param state The state to set.
     */
    void setLastState(State state);


    /**
     * The style of a shape, which determines how it is drawn.
     */
    enum Style {
        OUTLINE((shape, points) -> shape.generateOutline(points)),
        SURFACE((shape, points) -> shape.generateSurface(points)),
        FILL((shape, points) -> shape.generateFilled(points));

        private final BiConsumer<Shape, Set<Vector3d>> generatePoints;

        Style(BiConsumer<Shape, Set<Vector3d>> generatePoints) {
            this.generatePoints = generatePoints;
        }

        public void generatePoints(Shape shape, Set<Vector3d> points) {
            generatePoints.accept(shape, points);
        }

        public String toString() {
            return name().toLowerCase();
        }
    }

    /**
     * A state object for checking if a shape has changed.
     */
    record State(Style style, int orientationHash, double scale, int offsetHash, double particleDensity) {
        public boolean equals(State state) {
            return state.style() == style &&
                    state.orientationHash() == orientationHash &&
                    state.scale() == scale &&
                    state.offsetHash() == offsetHash &&
                    state.particleDensity() == particleDensity;
        }
    }
}
