package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.DefaultPointSampler;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

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

    public AbstractShape() {
        this.orientation = new Quaterniond();
        this.scale = 1;
        this.offset = new Vector3d(0, 0, 0);
        this.pointSampler = new DefaultPointSampler();
    }

    // --- Spatial transform ---

    @Override
    public Quaterniond getOrientation() {
        return new Quaterniond(orientation);
    }

    @Override
    public void setOrientation(Quaterniond orientation) {
        this.orientation.set(orientation);
    }

    @Override
    public double getScale() { return scale; }

    @Override
    public void setScale(double scale) {
        this.scale = scale;
    }

    @Override
    public Vector3d getOffset() {
        return new Vector3d(offset);
    }

    @Override
    public void setOffset(Vector3d offset) {
        this.offset = offset;
    }

    // --- Oriented axes ---

    @Override
    public Vector3d getRelativeXAxis() {
        return orientation.transform(new Vector3d(1, 0, 0));
    }

    @Override
    public Vector3d getRelativeYAxis() {
        return orientation.transform(new Vector3d(0, 1, 0));
    }

    @Override
    public Vector3d getRelativeZAxis() {
        return orientation.transform(new Vector3d(0, 0, 1));
    }

    // --- Change detection ---

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

    @Override
    public boolean isDynamic() { return dynamic; }

    @Override
    public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }

    // --- Point generation defaults ---

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        generateOutline(points, density);
    }

    @Override
    public void generateFilled(Set<Vector3d> points, double density) {
        generateSurface(points, density);
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        // Generic fallback — subclasses should override for accuracy
        return 0.25;
    }

    // --- Geometry query ---

    @Override
    public abstract boolean contains(Vector3d point);

    // --- PointSampler ---

    @Override
    public PointSampler getPointSampler() { return pointSampler; }

    @Override
    public void setPointSampler(PointSampler sampler) { this.pointSampler = sampler; }

    // --- Vertical fill helper ---

    protected static void fillVertically(Set<Vector3d> points, double height, double density) {
        Set<Vector3d> base = new LinkedHashSet<>(points);
        double heightStep = height / Math.round(height / density);
        for (double y = 0; y < height; y += heightStep) {
            for (Vector3d v : base) {
                points.add(new Vector3d(v.x, y, v.z));
            }
        }
    }

    // --- Replication ---

    @Override
    public abstract Shape clone();

    @Override
    public Shape copyTo(Shape shape) {
        shape.setOrientation(new Quaterniond(this.orientation));
        shape.setScale(this.scale);
        shape.setOffset(new Vector3d(this.offset));
        shape.setDynamic(this.dynamic);

        // Clone sampler config
        PointSampler srcSampler = this.pointSampler;
        PointSampler destSampler = shape.getPointSampler();
        destSampler.setStyle(srcSampler.getStyle());
        destSampler.setDensity(srcSampler.getDensity());
        destSampler.setOrdering(srcSampler.getOrdering());
        if (srcSampler.getDrawContext() != null)
            destSampler.setDrawContext(srcSampler.getDrawContext().copy());

        return shape;
    }
}
