package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Heart extends AbstractShape implements LWHShape {

    private double length;
    private double width;
    private double eccentricity;

    public Heart(double length, double width, double eccentricity) {
        super();
        this.length = Math.max(length, Shape.EPSILON);
        this.width = Math.max(width, Shape.EPSILON);
        this.eccentricity = Math.max(eccentricity, 1);
    }

    private static void calculateHeart(List<Vector3d> points, double length, double width, double eccentricity, double density) {
        int steps = Math.max(segments(Math.PI * 2 * (width + length) * 3 / 4, density), 3);
        if (points instanceof ArrayList<?> al)
            al.ensureCapacity(points.size() + steps);
        for (int i = 0; i < steps; i++) {
            points.add(heartPoint(length, width, eccentricity, Math.PI * 2 * i / steps));
        }
    }

    /**
     * @return the point on the heart curve at the given parametric angle
     */
    private static Vector3d heartPoint(double length, double width, double eccentricity, double theta) {
        double x = width * Math.pow(Math.sin(theta), 3);
        double y = length * (Math.cos(theta) - 1 / eccentricity * Math.cos(2 * theta) - 1.0 / 6 * Math.cos(3 * theta) - 1.0 / 16 * Math.cos(4 * theta));
        return new Vector3d(x, 0, y);
    }

    /**
     * Traces the outline of this heart as a closed polygon.
     *
     * @param resolution the number of vertices
     */
    List<Vector3d> outlinePolygon(int resolution) {
        List<Vector3d> polygon = new ArrayList<>(resolution);
        for (int i = 0; i < resolution; i++) {
            polygon.add(heartPoint(length / 2, width / 2, eccentricity, Math.PI * 2 * i / resolution));
        }
        return polygon;
    }

    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        calculateHeart(points, length / 2, width / 2, eccentricity, density);
    }

    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        for (double w = width, l = length; w > 0 && l > 0; w -= density * 1.5, l -= density * 1.5) {
            calculateHeart(points, l / 2, w / 2, eccentricity, density);
        }
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return switch (style) {
            // the outline is stepped by angle, at this many points per unit of density
            case OUTLINE -> Math.PI * 2 * (width / 2 + length / 2) * 3 / 4 / count;
            case SURFACE, FILL -> {
                // The surface is drawn as concentric outlines 1.5 densities apart, shrinking to nothing,
                // so on average each has half the points of the outermost one.
                double outline = Math.PI * 2 * (width / 2 + length / 2) * 3 / 4;
                double rings = Math.min(width, length) / 1.5;
                yield Math.sqrt(outline * rings / 2 / count);
            }
        };
    }

    @Override
    protected boolean containsLocal(Vector3d point, double tolerance) {
        if (Math.abs(point.y) > tolerance) return false;
        // Approximate: test against the outline traced as a polygon
        List<Vector3d> outline = outlinePolygon(256);
        return VectorUtil.isInPolygonXZ(outline, point.x, point.z)
                || VectorUtil.isNearPolygonXZ(outline, point.x, point.z, tolerance);
    }

    @Override
    public double getHeight() { return 0; }

    @Override
    public void setHeight(double height) { }

    @Override
    public double getWidth() { return width; }

    @Override
    public void setWidth(double width) {
        this.width = Math.max(width, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getLength() { return length; }

    @Override
    public void setLength(double length) {
        this.length = Math.max(length, Shape.EPSILON);
        invalidate();
    }

    public double getEccentricity() { return eccentricity; }

    public void setEccentricity(double eccentricity) {
        this.eccentricity = Math.max(1, eccentricity);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Heart(length, width, eccentricity));
    }
}
