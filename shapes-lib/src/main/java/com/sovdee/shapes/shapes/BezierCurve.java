package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A bezier curve defined by control points as Vector3d.
 * For dynamic (entity-following) bezier curves, use the plugin-side wrapper.
 */
public class BezierCurve extends AbstractShape {

    private List<Vector3d> controlPoints;
    private Supplier<List<Vector3d>> controlPointsSupplier;

    public BezierCurve(List<Vector3d> controlPoints) {
        super();
        if (controlPoints.size() < 2)
            throw new IllegalArgumentException("A bezier curve must have at least 2 control points.");
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    public BezierCurve(Supplier<List<Vector3d>> controlPointsSupplier) {
        super();
        this.controlPointsSupplier = controlPointsSupplier;
        List<Vector3d> pts = controlPointsSupplier.get();
        if (pts.size() < 2)
            throw new IllegalArgumentException("A bezier curve must have at least 2 control points.");
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : pts)
            this.controlPoints.add(new Vector3d(cp));
        setDynamic(true);
    }

    public BezierCurve(BezierCurve curve) {
        super();
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : curve.controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        if (controlPointsSupplier != null) {
            List<Vector3d> pts = controlPointsSupplier.get();
            this.controlPoints = new ArrayList<>();
            for (Vector3d cp : pts)
                this.controlPoints.add(new Vector3d(cp));
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

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return estimateLength() / count;
    }

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

    public List<Vector3d> getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(List<Vector3d> controlPoints) {
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
        invalidate();
    }

    public Supplier<List<Vector3d>> getControlPointsSupplier() {
        return controlPointsSupplier;
    }

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
