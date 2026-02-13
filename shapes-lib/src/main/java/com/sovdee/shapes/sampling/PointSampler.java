package com.sovdee.shapes.sampling;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.shapes.Shape;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Responsible for sampling points from a {@link Shape}'s geometry.
 * Manages sampling configuration (style, density, ordering) and caching.
 */
public interface PointSampler extends Cloneable {

    SamplingStyle getStyle();
    void setStyle(SamplingStyle style);

    double getDensity();
    void setDensity(double density);

    int getMaxPoints();
    void setMaxPoints(int maxPoints);
    boolean isDensityExplicit();

    Comparator<Vector3d> getOrdering();
    void setOrdering(Comparator<Vector3d> ordering);

    UUID getUUID();

    DrawContext getDrawContext();
    void setDrawContext(DrawContext context);

    /**
     * Samples points from the given shape using the shape's own orientation.
     */
    List<Vector3d> getPoints(Shape shape);

    /**
     * Samples points from the given shape using the given orientation.
     */
    List<Vector3d> getPoints(Shape shape, Quaterniond orientation);

    /**
     * Computes and sets the density to achieve approximately the given particle count.
     */
    default void setParticleCount(Shape shape, int count) {
        setDensity(shape.computeDensity(getStyle(), count));
    }

    // ---- Modifier management ----

    List<PointModifier<?>> getModifiers();

    void addModifier(PointModifier<?> modifier);

    void removeModifier(PointModifier<?> modifier);

    void clearModifiers();

    /**
     * Drives the full render loop: gets cached points, runs render modifiers, calls renderer.
     *
     * @param shape       the shape to render
     * @param orientation the orientation for point sampling
     * @param renderer    client-provided renderer
     */
    <Context extends PointContext>  void render(Shape shape, Quaterniond orientation, ShapeRenderer<Context> renderer);

    PointSampler clone();
}
