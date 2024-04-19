package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Point;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class BezierCurve extends AbstractShape {

    private Point<?> start;
    private Point<?> end;
    private List<Point<?>> controlPoints;

    private boolean isDynamic;

    /**
     * Creates a new line shape with the start point at the origin and the end point at the given vector.
     * The vector cannot be the origin.
     * @param end the end point of the line
     * @throws IllegalArgumentException if the end vector is the origin
     */

    public BezierCurve(Point<?> start, Point<?> end, List<Point<?>> controlPoints) {
        this.start = start;
        if (start.getType() != Vector.class)
            this.setLocation(start.getDynamicLocation());
        this.end = end;
        this.controlPoints = new ArrayList<>(controlPoints);
        isDynamic = true;
    }

    public BezierCurve(BezierCurve curve) {
        this.start = curve.getStart();
        this.end = curve.getEnd();
        this.controlPoints = new ArrayList<>(curve.getControlPoints());
        isDynamic = curve.isDynamic;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            // Ensure that the points are always needing to be updated if the start or end location is dynamic
            this.setNeedsUpdate(true);
        return points;
    }

    private List<Vector> evaluateControlPoints() {
        List<Vector> controlPoints = new ArrayList<>();
        @Nullable Location origin = start.getLocation();
        controlPoints.add(start.getVector(origin));
        for (Point<?> controlPoint : this.controlPoints)
            controlPoints.add(controlPoint.getVector(origin));
        controlPoints.add(end.getVector(origin));
        return controlPoints;
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> points = new LinkedHashSet<>();
        List<Vector> controlPoints = evaluateControlPoints();

        int steps = (int) (estimateLength(controlPoints) / getParticleDensity());

        for (double step = 0; step < steps; step++) {
            double t = step / steps;
            double nt = 1 - t;
            List<Vector> tempCP = new ArrayList<>(controlPoints);
            while (tempCP.size() > 1) {
                for (int i = 0; i < tempCP.size() - 1; i++)
                    tempCP.set(i, tempCP.get(i).clone().multiply(nt).add(tempCP.get(i + 1).clone().multiply(t)));
                tempCP.remove(tempCP.size()-1);
            }
            points.add(tempCP.get(0));
        }
        return points;
    }

    private double estimateLength() {
        return estimateLength(evaluateControlPoints());
    }
    private double estimateLength(List<Vector> controlPoints) {
        double dist = 0;
        for (int i = 0; i < controlPoints.size()-1; i++) {
            dist += controlPoints.get(i).distance(controlPoints.get(i+1));
        }
        return dist;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        this.setParticleDensity(estimateLength() / particleCount);
        this.setNeedsUpdate(true);
    }

    public Point<?> getStart() {
        return start;
    }

    public Point<?> getEnd() {
        return end;
    }

    public List<Point<?>> getControlPoints() {
        return controlPoints;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new BezierCurve(this));
    }
}
