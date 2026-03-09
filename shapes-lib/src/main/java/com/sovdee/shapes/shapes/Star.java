package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.List;

public class Star extends AbstractShape {

    private double innerRadius;
    private double outerRadius;
    private double angle;

    public Star(double innerRadius, double outerRadius, double angle) {
        super();
        this.innerRadius = Math.max(innerRadius, Shape.EPSILON);
        this.outerRadius = Math.max(outerRadius, Shape.EPSILON);
        this.angle = Math.clamp(angle, Shape.EPSILON, Math.PI);
    }

    private static void calculateStar(List<Vector3d> points, double innerRadius, double outerRadius, double angle, double density) {
        Vector3d outerVertex = new Vector3d(outerRadius, 0, 0);
        Vector3d innerVertex = new Vector3d(innerRadius, 0, 0);
        for (double theta = 0; theta < 2 * Math.PI; theta += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(outerVertex), theta);
            Line.calculateLine(points, currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta + angle / 2), density);
            // Second line from same vertex - skip duplicate first point
            int before = points.size();
            Line.calculateLine(points, currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta - angle / 2), density);
            if (points.size() > before) {
                points.remove(before);
            }
        }
    }

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateStar(points, innerRadius, outerRadius, angle, density);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        double minRadius = Math.min(innerRadius, outerRadius);
        for (double r = 0; r < minRadius; r += density) {
            calculateStar(points, innerRadius - r, outerRadius - r, angle, density);
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        double sideLength = Math.sqrt(Math.pow(innerRadius, 2) + Math.pow(outerRadius, 2) - 2 * innerRadius * outerRadius * Math.cos(angle));
        double perimeter = sideLength * getStarPoints() * 2;
        return perimeter / count;
    }

    @Override
    public boolean contains(Vector3d point) {
        if (Math.abs(point.y) > EPSILON) return false;
        // Check if within outer radius bounding circle
        double dist = Math.sqrt(point.x * point.x + point.z * point.z);
        return dist <= Math.max(innerRadius, outerRadius);
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
        return (int) (Math.PI * 2 / angle);
    }

    public void setStarPoints(int starPoints) {
        starPoints = Math.max(starPoints, 2);
        this.angle = Math.PI * 2 / starPoints;
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Star(innerRadius, outerRadius, angle));
    }
}
