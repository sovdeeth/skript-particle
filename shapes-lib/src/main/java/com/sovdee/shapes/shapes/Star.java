package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    // The point count is the source of truth; the angle between points is always derived from it.
    private int starPoints;
    private double angle;

    public Star(double innerRadius, double outerRadius, double angle) {
        super();
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
        double points = Math.round(Math.PI * 2 / Math.clamp(angle, Shape.EPSILON, Math.PI));
        setPointCount((int) Math.clamp(points, 2, 100_000));
    }

    /**
     * Creates a star with the given number of points.
     *
     * @param innerRadius the radius of the inner vertices; clamped to at least {@link Shape#EPSILON}
     * @param outerRadius the radius of the outer vertices; clamped to at least {@link Shape#EPSILON}
     * @param starPoints  the number of points on the star; at least 2
     */
    public Star(double innerRadius, double outerRadius, int starPoints) {
        super();
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
        setPointCount(starPoints);
    }

    private void setPointCount(int starPoints) {
        this.starPoints = Math.max(starPoints, 2);
        this.angle = Math.PI * 2 / this.starPoints;
    }

    private static void calculateStar(List<Vector3d> points, double innerRadius, double outerRadius, int starPoints, double density) {
        double angle = Math.PI * 2 / starPoints;
        Vector3d outerVertex = new Vector3d(outerRadius, 0, 0);
        Vector3d innerVertex = new Vector3d(innerRadius, 0, 0);
        // walk the outline once: outer vertex, inner vertex, next outer vertex...
        // each edge leaves out its end point, so every vertex appears exactly once.
        for (int point = 0; point < starPoints; point++) {
            Vector3d outer = VectorUtil.rotateAroundY(new Vector3d(outerVertex), point * angle);
            Vector3d inner = VectorUtil.rotateAroundY(new Vector3d(innerVertex), (point + 0.5) * angle);
            Vector3d nextOuter = VectorUtil.rotateAroundY(new Vector3d(outerVertex), (point + 1) * angle);
            Line.calculateLine(points, outer, inner, density, false);
            Line.calculateLine(points, inner, nextOuter, density, false);
        }
    }

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateStar(points, innerRadius, outerRadius, starPoints, density);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        // concentric stars, shrinking both radii until the smaller one reaches the center
        double minRadius = Math.min(innerRadius, outerRadius);
        int rings = segments(minRadius, density);
        for (int ring = 0; ring < rings; ring++) {
            double r = minRadius * ring / rings;
            calculateStar(points, innerRadius - r, outerRadius - r, starPoints, density);
        }
        points.add(new Vector3d(0, 0, 0));
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        // neighbouring inner and outer vertices are half a point-angle apart
        double sideLength = Math.sqrt(innerRadius * innerRadius + outerRadius * outerRadius
                - 2 * innerRadius * outerRadius * Math.cos(angle / 2));
        double perimeter = sideLength * starPoints * 2;
        return switch (style) {
            case OUTLINE -> perimeter / count;
            case SURFACE, FILL -> {
                // The surface is drawn as concentric outlines one density apart, so the point count is roughly
                // (perimeter / density) * (rings / 2), with rings = minRadius / density.
                double minRadius = Math.min(innerRadius, outerRadius);
                double maxRadius = Math.max(innerRadius, outerRadius);
                // the outlines shrink by minRadius in total; average their perimeter over that range
                double innerPerimeter = (maxRadius - minRadius) * starPoints * 2;
                double averagePerimeter = (perimeter + innerPerimeter) / 2;
                yield Math.sqrt(averagePerimeter * minRadius / count);
            }
        };
    }

    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        if (Math.abs(point.y) > tolerance) return false;
        // the outline as a polygon: outer and inner vertices in turn
        List<Vector3d> outline = new ArrayList<>(starPoints * 2);
        for (int i = 0; i < starPoints; i++) {
            outline.add(VectorUtil.rotateAroundY(new Vector3d(outerRadius, 0, 0), i * angle));
            outline.add(VectorUtil.rotateAroundY(new Vector3d(innerRadius, 0, 0), (i + 0.5) * angle));
        }
        return VectorUtil.isInPolygonXZ(outline, point.x, point.z)
                || VectorUtil.isNearPolygonXZ(outline, point.x, point.z, tolerance);
    }

    public double getInnerRadius() { return innerRadius; }

    public void setInnerRadius(double innerRadius) {
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        invalidate();
    }

    public double getOuterRadius() { return outerRadius; }

    public void setOuterRadius(double outerRadius) {
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
        invalidate();
    }

    public int getStarPoints() {
        return starPoints;
    }

    public void setStarPoints(int starPoints) {
        setPointCount(starPoints);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Star(innerRadius, outerRadius, starPoints));
    }
}
