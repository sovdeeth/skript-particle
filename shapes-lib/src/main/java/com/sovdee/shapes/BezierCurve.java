package com.sovdee.shapes;

import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A bezier curve defined by control points as Vector3d.
 * For dynamic (entity-following) bezier curves, use the plugin-side wrapper.
 */
public class BezierCurve extends AbstractShape {

    private List<Vector3d> controlPoints;

    /**
     * Creates a bezier curve from the given control points.
     * Must have at least 2 control points (start and end).
     *
     * @param controlPoints the control points, including start and end
     */
    public BezierCurve(List<Vector3d> controlPoints) {
        super();
        if (controlPoints.size() < 2)
            throw new IllegalArgumentException("A bezier curve must have at least 2 control points.");
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    public BezierCurve(BezierCurve curve) {
        super();
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : curve.controlPoints)
            this.controlPoints.add(new Vector3d(cp));
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        int steps = (int) (estimateLength() / getParticleDensity());

        for (double step = 0; step < steps; step++) {
            double t = step / steps;
            double nt = 1 - t;
            List<Vector3d> tempCP = new ArrayList<>();
            for (Vector3d cp : controlPoints)
                tempCP.add(new Vector3d(cp));
            while (tempCP.size() > 1) {
                for (int i = 0; i < tempCP.size() - 1; i++)
                    tempCP.set(i, new Vector3d(tempCP.get(i)).mul(nt).add(new Vector3d(tempCP.get(i + 1)).mul(t)));
                tempCP.remove(tempCP.size() - 1);
            }
            points.add(tempCP.get(0));
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
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(estimateLength() / particleCount);
        this.setNeedsUpdate(true);
    }

    public List<Vector3d> getControlPoints() {
        return controlPoints;
    }

    public void setControlPoints(List<Vector3d> controlPoints) {
        this.controlPoints = new ArrayList<>();
        for (Vector3d cp : controlPoints)
            this.controlPoints.add(new Vector3d(cp));
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new BezierCurve(this));
    }
}
