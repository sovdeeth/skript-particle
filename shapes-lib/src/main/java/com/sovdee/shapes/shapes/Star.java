package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.Set;

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

    private static Set<Vector3d> calculateStar(double innerRadius, double outerRadius, double angle, double density) {
        Set<Vector3d> points = new LinkedHashSet<>();
        Vector3d outerVertex = new Vector3d(outerRadius, 0, 0);
        Vector3d innerVertex = new Vector3d(innerRadius, 0, 0);
        for (double theta = 0; theta < 2 * Math.PI; theta += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(outerVertex), theta);
            points.addAll(Line.calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta + angle / 2), density));
            points.addAll(Line.calculateLine(currentVertex, VectorUtil.rotateAroundY(new Vector3d(innerVertex), theta - angle / 2), density));
        }
        return points;
    }

    @Override
    public void generateOutline(Set<Vector3d> points, double density) {
        points.addAll(calculateStar(innerRadius, outerRadius, angle, density));
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        double minRadius = Math.min(innerRadius, outerRadius);
        for (double r = 0; r < minRadius; r += density) {
            points.addAll(calculateStar(innerRadius - r, outerRadius - r, angle, density));
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
