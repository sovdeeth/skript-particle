package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.DefaultPointSampler;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of {@link Shape} providing spatial transform, versioning,
 * and a default {@link PointSampler}.
 */
public abstract class AbstractShape implements Shape {

    private final Quaterniond orientation;
    private double scale;
    private Vector3d offset;
    private boolean dynamic = false;
    private long version = 0;
    private PointSampler pointSampler;

    /**
     * Initialises a new shape with an identity orientation, scale {@code 1.0}, zero offset,
     * and a fresh {@link DefaultPointSampler}. Subclasses must call {@code super()} before
     * setting dimension-specific fields.
     */
    public AbstractShape() {
        this.orientation = new Quaterniond();
        this.scale = 1;
        this.offset = new Vector3d(0, 0, 0);
        this.pointSampler = new DefaultPointSampler();
    }

    // --- Spatial transform ---

    /**
     * {@inheritDoc}
     * Returns a defensive copy so callers cannot mutate the internal quaternion.
     */
    @Override
    public Quaterniond getOrientation() {
        return new Quaterniond(orientation);
    }

    /**
     * {@inheritDoc}
     * The value is copied into the internal quaternion; the argument is not retained.
     */
    @Override
    public void setOrientation(Quaterniond orientation) {
        this.orientation.set(orientation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getScale() { return scale; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setScale(double scale) {
        this.scale = scale;
    }

    /**
     * {@inheritDoc}
     * Returns a defensive copy so callers cannot mutate the internal offset.
     */
    @Override
    public Vector3d getOffset() {
        return new Vector3d(offset);
    }

    /**
     * {@inheritDoc}
     * The provided vector is stored directly (not copied); callers should not mutate it afterward.
     */
    @Override
    public void setOffset(Vector3d offset) {
        this.offset = offset;
    }

    // --- Oriented axes ---

    /**
     * {@inheritDoc}
     * Computed by rotating {@code (1, 0, 0)} with the internal orientation quaternion.
     */
    @Override
    public Vector3d getRelativeXAxis() {
        return orientation.transform(new Vector3d(1, 0, 0));
    }

    /**
     * {@inheritDoc}
     * Computed by rotating {@code (0, 1, 0)} with the internal orientation quaternion.
     */
    @Override
    public Vector3d getRelativeYAxis() {
        return orientation.transform(new Vector3d(0, 1, 0));
    }

    /**
     * {@inheritDoc}
     * Computed by rotating {@code (0, 0, 1)} with the internal orientation quaternion.
     */
    @Override
    public Vector3d getRelativeZAxis() {
        return orientation.transform(new Vector3d(0, 0, 1));
    }

    // --- Change detection ---

    /**
     * {@inheritDoc}
     * The counter starts at {@code 0} and is incremented each time {@link #invalidate()} is called.
     */
    @Override
    public long getVersion() { return version; }

    /**
     * Increments the version counter, signaling that geometry has changed.
     * Call from dimension setters.
     */
    protected void invalidate() {
        version++;
    }

    // --- Dynamic support ---

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDynamic() { return dynamic; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }

    // --- Point generation defaults ---

    /**
     * Default surface implementation that delegates to {@link #generateOutline(List, double)}.
     * Subclasses with a meaningful surface representation (e.g. {@link Circle}, {@link Sphere})
     * should override this method.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        generateOutline(points, density);
    }

    /**
     * Default filled implementation that delegates to {@link #generateSurface(List, double)}.
     * Subclasses that can meaningfully fill their volume (e.g. {@link Circle}, {@link Sphere})
     * should override this method.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired spacing between points
     */
    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        generateSurface(points, density);
    }

    /**
     * Generic fallback implementation that always returns a density of {@code 0.25}.
     * Subclasses should override this with a formula appropriate to their geometry so that
     * {@link PointSampler#setParticleCount(Shape, int)} produces accurate results.
     *
     * @param style            the {@link SamplingStyle} being used (OUTLINE, SURFACE, or FILL)
     * @param targetPointCount the desired number of points
     * @return {@code 0.25} as a safe, conservative default
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        // Generic fallback — subclasses should override for accuracy
        return 0.25;
    }

    // --- Geometry query ---

    /**
     * {@inheritDoc}
     * Concrete subclasses must implement the containment test appropriate to their geometry.
     */
    @Override
    public abstract boolean contains(Vector3d point);

    // --- PointSampler ---

    /**
     * {@inheritDoc}
     */
    @Override
    public PointSampler getPointSampler() { return pointSampler; }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPointSampler(PointSampler sampler) { this.pointSampler = sampler; }

    // --- Vertical fill helper ---

    /**
     * Duplicates an existing slice of {@code points} at successive Y offsets to fill a vertical
     * column of the given {@code height}. The Y step is derived from {@code density} so that
     * layer spacing is as uniform as possible.
     * <p>
     * The method reads all points in the range {@code [startIndex, points.size())} at the time
     * of the call and appends copies translated by {@code y = density, 2*density, ...} up to
     * (but not including) {@code height}.
     *
     * @param points     the list of points to extend; the existing slice is treated as the base layer
     * @param startIndex the index of the first point in the base layer
     * @param height     the total vertical extent to fill; must be positive
     * @param density    the desired spacing between vertical layers
     */
    protected static void fillVertically(List<Vector3d> points, int startIndex, double height, double density) {
        int baseSize = points.size() - startIndex;
        double heightStep = height / Math.round(height / density);
        if (points instanceof ArrayList<?> pointList) {
            pointList.ensureCapacity(points.size() + baseSize * (int) ((height - heightStep) / heightStep + 1));
        }
        for (double y = heightStep; y < height; y += heightStep) {
            for (int i = startIndex; i < startIndex + baseSize; i++) {
                Vector3d v = points.get(i);
                points.add(new Vector3d(v.x, y, v.z));
            }
        }
    }

    // --- Replication ---

    /**
     * {@inheritDoc}
     * Concrete subclasses must create a new instance with the same dimension-specific state and
     * then call {@link #copyTo(Shape)} to propagate the base transform fields.
     */
    @Override
    public abstract Shape clone();

    /**
     * {@inheritDoc}
     * Copies orientation, scale, offset, dynamic flag, and a clone of the point sampler into
     * {@code shape}. Dimension-specific fields are not touched.
     *
     * @param shape the target shape; must not be null
     * @return {@code shape}, for chaining convenience
     */
    @Override
    public Shape copyTo(Shape shape) {
        shape.setOrientation(new Quaterniond(this.orientation));
        shape.setScale(this.scale);
        shape.setOffset(new Vector3d(this.offset));
        shape.setDynamic(this.dynamic);

        shape.setPointSampler(this.pointSampler.clone());

        return shape;
    }
}
