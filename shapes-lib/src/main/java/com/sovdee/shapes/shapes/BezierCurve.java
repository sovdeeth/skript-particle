package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * A bezier curve defined by control points as Vector3d.
 * For dynamic (entity-following) bezier curves, use the plugin-side wrapper.
 */
public class BezierCurve extends AbstractShape {

    private List<Vector3d> controlPoints;
    private Supplier<List<Vector3d>> controlPointsSupplier;

    /**
     * Creates a static Bezier curve from a fixed list of control points.
     * The points are deep-copied so subsequent mutation of the list has no effect.
     *
     * @param controlPoints the control points defining the curve; must contain at least 2 elements
     * @throws IllegalArgumentException if fewer than 2 control points are provided
     */
    public BezierCurve(List<Vector3d> controlPoints) {
        super();
        if (controlPoints.size() < 2)
            throw new IllegalArgumentException("A bezier curve must have at least 2 control points.");
        this.controlPoints = new ArrayList<>(controlPoints.size());
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    /**
     * Creates a dynamic Bezier curve whose control points are re-fetched from {@code controlPointsSupplier}
     * on every render call. The shape is automatically marked {@link #setDynamic(boolean) dynamic}.
     * The supplier is invoked once during construction to validate the initial point count.
     *
     * @param controlPointsSupplier a supplier that returns the live control points; must return
     *                              at least 2 points on every invocation
     * @throws IllegalArgumentException if the initial supplier result contains fewer than 2 points
     */
    public BezierCurve(Supplier<List<Vector3d>> controlPointsSupplier) {
        super();
        this.controlPointsSupplier = controlPointsSupplier;
        List<Vector3d> pts = controlPointsSupplier.get();
        if (pts.size() < 2)
            throw new IllegalArgumentException("A bezier curve must have at least 2 control points.");
        this.controlPoints = new ArrayList<>(pts);
        setDynamic(true);
    }

    /**
     * Copy constructor: creates a static Bezier curve with deep-copied control points from
     * {@code curve}. The supplier is not copied; the result is always a static curve.
     *
     * @param curve the source curve to copy control points from; must not be null
     */
    public BezierCurve(BezierCurve curve) {
        super();
        this.controlPoints = new ArrayList<>(curve.controlPoints.size());
        for (Vector3d cp : curve.controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    /**
     * Generates points along the Bezier curve using the de Casteljau algorithm and appends them
     * to {@code points}. If a control-points supplier is set it is invoked now to refresh the
     * control point list before sampling.
     * <p>
     * The number of steps is {@code estimateLength() / density}, so smaller {@code density} values
     * produce a finer approximation of the smooth curve.
     *
     * @param points  the list to append generated points to; must not be null
     * @param density the desired arc-length spacing between consecutive curve samples
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        if (controlPointsSupplier != null) {
            List<Vector3d> pts = controlPointsSupplier.get();
            this.controlPoints = new ArrayList<>(pts);
        }
        int steps = (int) (estimateLength() / density);
        int n = controlPoints.size();

        Vector3d[] temp = new Vector3d[n];
        for (int i = 0; i < n; i++)
            temp[i] = new Vector3d();

        for (int step = 0; step < steps; step++) {
            double t = (double) step / steps;
            double nt = 1 - t;
            for (int i = 0; i < n; i++)
                temp[i].set(controlPoints.get(i));
            for (int level = n - 1; level > 0; level--) {
                for (int i = 0; i < level; i++) {
                    temp[i].mul(nt).add(new Vector3d(temp[i + 1]).mul(t));
                }
            }
            points.add(new Vector3d(temp[0]));
        }
    }

    private double estimateLength() {
        double dist = 0;
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            dist += controlPoints.get(i).distance(controlPoints.get(i + 1));
        }
        return dist;
    }

    /**
     * Computes the density required to produce approximately {@code targetPointCount} points along
     * this curve. The result is {@code estimateLength() / targetPointCount}, where
     * {@code estimateLength()} is the sum of straight-line distances between consecutive control points.
     *
     * @param style            ignored; the Bezier curve has only one meaningful sampling mode
     * @param targetPointCount the desired number of curve samples; clamped to at least 1
     * @return the density value to pass to {@link #generateOutline(List, double)}
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return estimateLength() / count;
    }

    /**
     * Returns {@code true} if {@code point} lies approximately on the curve.
     * The test uses a high-density sampling of the curve (approximately one sample per {@code 0.1}
     * units of chord length) and checks whether any sampled point is within {@link Shape#EPSILON}
     * of {@code point}.
     *
     * @param point the point to test, in local coordinates
     * @return {@code true} if the point is within {@link Shape#EPSILON} of the curve
     */
    @Override
    public boolean contains(Vector3d point) {
        // Approximate: check distance to nearest sampled point
        int samples = Math.max((int) (estimateLength() / 0.1), 10);
        int n = controlPoints.size();
        Vector3d[] temp = new Vector3d[n];
        for (int i = 0; i < n; i++) temp[i] = new Vector3d();

        for (int step = 0; step <= samples; step++) {
            double t = (double) step / samples;
            double nt = 1 - t;
            for (int i = 0; i < n; i++) temp[i].set(controlPoints.get(i));
            for (int level = n - 1; level > 0; level--) {
                for (int i = 0; i < level; i++) {
                    temp[i].mul(nt).add(new Vector3d(temp[i + 1]).mul(t));
                }
            }
            if (point.distance(temp[0]) <= EPSILON) return true;
        }
        return false;
    }

    /**
     * Returns the current list of control points. For dynamic curves this reflects the most
     * recently fetched values from the supplier (i.e. after the last {@link #generateOutline} call).
     *
     * @return the live control point list; modifications affect the internal state directly
     */
    public List<Vector3d> getControlPoints() {
        return controlPoints;
    }

    /**
     * Replaces the control points of this curve with a deep copy of {@code controlPoints} and
     * invalidates the point cache. The supplier is not updated.
     *
     * @param controlPoints the new control points; must not be null or empty
     */
    public void setControlPoints(List<Vector3d> controlPoints) {
        this.controlPoints = new ArrayList<>(controlPoints.size());
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
        invalidate();
    }

    /**
     * Returns the supplier used to refresh control points on each render call, or {@code null}
     * if this is a static curve constructed from a fixed point list.
     *
     * @return the control-points supplier, or {@code null} for static curves
     */
    public Supplier<List<Vector3d>> getControlPointsSupplier() {
        return controlPointsSupplier;
    }

    /**
     * Returns a deep copy of this curve. If a control-points supplier is present the clone shares
     * the same supplier reference (suppliers are not themselves cloneable). Otherwise, control
     * points are deep-copied. Base transform state is copied via {@link #copyTo(Shape)}.
     *
     * @return a new {@link BezierCurve} with the same geometry and transform state
     */
    @Override
    public Shape clone() {
        BezierCurve clone;
        if (controlPointsSupplier != null) {
            clone = new BezierCurve(controlPointsSupplier);
        } else {
            clone = new BezierCurve(this);
        }
        return this.copyTo(clone);
    }
}
