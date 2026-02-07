package com.sovdee.skriptparticles.shapes;

import com.sovdee.shapes.BezierCurve;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.util.Point;
import org.bukkit.Location;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A bezier curve that supports dynamic (entity-following) control points via the plugin's Point type.
 */
public class DynamicBezierCurve extends BezierCurve {

    private final Point<?> start;
    private final Point<?> end;
    private final List<Point<?>> dynamicControlPoints;

    public DynamicBezierCurve(Point<?> start, Point<?> end, List<Point<?>> controlPoints) {
        super(evaluatePoints(start, end, controlPoints));
        this.start = start;
        this.end = end;
        this.dynamicControlPoints = new ArrayList<>(controlPoints);
    }

    private static List<Vector3d> evaluatePoints(Point<?> start, Point<?> end, List<Point<?>> controlPoints) {
        List<Vector3d> result = new ArrayList<>();
        Location origin = start.getLocation();
        result.add(toVector3d(start.getVector(origin)));
        for (Point<?> cp : controlPoints)
            result.add(toVector3d(cp.getVector(origin)));
        result.add(toVector3d(end.getVector(origin)));
        return result;
    }

    private static Vector3d toVector3d(org.bukkit.util.Vector v) {
        return new Vector3d(v.getX(), v.getY(), v.getZ());
    }

    @Override
    public Set<Vector3d> getPoints(Quaterniond orientation) {
        Set<Vector3d> points = super.getPoints(orientation);
        this.setNeedsUpdate(true);
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        // Re-evaluate dynamic control points before generating
        this.setControlPoints(evaluatePoints(start, end, dynamicControlPoints));
        super.generateOutline(points);
    }

    public Point<?> getStart() { return start; }
    public Point<?> getEnd() { return end; }
    public List<Point<?>> getDynamicControlPoints() { return dynamicControlPoints; }

    @Override
    public Shape clone() {
        DynamicBezierCurve curve = new DynamicBezierCurve(start, end, dynamicControlPoints);
        return this.copyTo(curve);
    }
}
