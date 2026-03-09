package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * A 3-D ellipsoid defined by three independent semi-axis radii along the X, Y, and Z axes.
 * <br>
 * Outline sampling produces the three principal great-circle ellipses (XZ, XY, YZ planes).
 * Surface sampling generates a latitude-strip decomposition: the longest horizontal cross-section
 * ellipse (XZ or XY/YZ depending on which radius is larger) is used as the profile, and rings
 * at each latitude are computed from the ellipsoid equation. Filled sampling nests concentric
 * scaled ellipsoids to fill the interior.
 * <br>
 * A point {@code (x, y, z)} is inside the ellipsoid when
 * {@code (x/xRadius)² + (y/yRadius)² + (z/zRadius)² ≤ 1}.
 */
public class Ellipsoid extends AbstractShape implements LWHShape {

    private static final Quaterniond XY_ROTATION = new Quaterniond().rotateX(Math.PI / 2);
    private static final Quaterniond ZY_ROTATION = new Quaterniond().rotateZ(Math.PI / 2);
    protected double xRadius;
    protected double yRadius;
    protected double zRadius;

    /**
     * Constructs an ellipsoid with the given semi-axis lengths. Each radius is clamped to at
     * least {@link Shape#EPSILON} to prevent degenerate geometry.
     *
     * @param xRadius semi-axis length along the X axis
     * @param yRadius semi-axis length along the Y axis
     * @param zRadius semi-axis length along the Z axis
     */
    public Ellipsoid(double xRadius, double yRadius, double zRadius) {
        super();
        this.xRadius = Math.max(xRadius, Shape.EPSILON);
        this.yRadius = Math.max(yRadius, Shape.EPSILON);
        this.zRadius = Math.max(zRadius, Shape.EPSILON);
    }

    /**
     * Generates the wireframe outline of the ellipsoid: three full great-circle ellipses in the
     * XZ (horizontal), XY, and YZ planes. The XY and YZ ellipses are rotated into position using
     * pre-computed quaternion constants so they lie correctly in their respective planes.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points along each ellipse
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        Ellipse.calculateEllipse(points, xRadius, zRadius, density, 2 * Math.PI);
        int start = points.size();
        Ellipse.calculateEllipse(points, xRadius, yRadius, density, 2 * Math.PI);
        VectorUtil.transform(XY_ROTATION, points, start);
        start = points.size();
        Ellipse.calculateEllipse(points, yRadius, zRadius, density, 2 * Math.PI);
        VectorUtil.transform(ZY_ROTATION, points, start);
    }

    /**
     * Generates points covering the surface of the ellipsoid using a latitude-strip approach.
     * The longest in-plane cross-section ellipse (XY or YZ depending on whether {@code xRadius >
     * zRadius}) is used as the profile from which latitude angles are derived. At each latitude
     * a horizontal ring scaled by {@code cos(θ)} is added, along with its mirror below the equator.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        List<Vector3d> ellipse = new ArrayList<>();
        if (xRadius > zRadius) {
            Ellipse.calculateEllipse(ellipse, xRadius, yRadius, density, 2 * Math.PI);
            VectorUtil.transform(XY_ROTATION, ellipse);
        } else {
            Ellipse.calculateEllipse(ellipse, yRadius, zRadius, density, 2 * Math.PI);
            VectorUtil.transform(ZY_ROTATION, ellipse);
        }
        generateEllipsoid(points, ellipse, 1, density);
    }

    /**
     * Generates points filling the interior of the ellipsoid by nesting concentric scaled copies
     * of the surface, stepping the scale factor from 1 down to 0. Each scaled shell is generated
     * by the same latitude-strip method used in {@link #generateSurface}, applied to a proportionally
     * scaled version of the radii.
     *
     * @param points  the list to which generated points are appended
     * @param density the desired spacing between adjacent points
     */
    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        double radius = Math.max(xRadius, zRadius);
        int steps = (int) Math.round(radius / density);
        List<Vector3d> ellipse = new ArrayList<>();
        for (int i = steps; i > 0; i--) {
            double r = (i / (double) steps);
            ellipse.clear();
            if (xRadius > zRadius) {
                Ellipse.calculateEllipse(ellipse, xRadius * r, yRadius * r, density, 2 * Math.PI);
                VectorUtil.transform(XY_ROTATION, ellipse);
            } else {
                Ellipse.calculateEllipse(ellipse, yRadius * r, zRadius * r, density, 2 * Math.PI);
                VectorUtil.transform(ZY_ROTATION, ellipse);
            }
            generateEllipsoid(points, ellipse, r, density);
        }
    }

    private void generateEllipsoid(List<Vector3d> points, List<Vector3d> ellipse, double radius, double density) {
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++) {
            double y = ellipse.get(i).y;
            double theta = Math.asin(y / (yRadius * radius));
            // Add ring points, setting y in-place and adding mirrored copies
            int ringStart = points.size();
            Ellipse.calculateEllipse(points, radius * xRadius * Math.cos(theta), radius * zRadius * Math.cos(theta), density, 2 * Math.PI);
            int ringEnd = points.size();
            for (int j = ringStart; j < ringEnd; j++) {
                Vector3d v = points.get(j);
                v.y = y;
                if (Math.abs(y) > EPSILON) {
                    points.add(new Vector3d(v.x, -y, v.z));
                }
            }
        }
        Ellipse.calculateEllipse(points, radius * xRadius, radius * zRadius, density, 2 * Math.PI);
    }

    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        return switch (style) {
            case OUTLINE -> {
                double h = (xRadius - yRadius) * (xRadius - yRadius) / ((xRadius + yRadius) + (xRadius + yRadius));
                double circumferenceXY = Math.PI * (xRadius + yRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXZ = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (yRadius - zRadius) * (yRadius - zRadius) / ((yRadius + zRadius) + (yRadius + zRadius));
                double circumferenceYZ = Math.PI * (yRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                yield (circumferenceXY + circumferenceXZ + circumferenceYZ) / count;
            }
            case SURFACE -> {
                double surfaceArea = 4 * Math.PI * Math.pow((Math.pow(xRadius * yRadius, 1.6) + Math.pow(xRadius * zRadius, 1.6) + Math.pow(zRadius * yRadius, 1.6)) / 3, 1 / 1.6);
                yield Math.sqrt(surfaceArea / count);
            }
            case FILL -> {
                double volume = 4 / 3.0 * Math.PI * xRadius * yRadius * zRadius;
                yield Math.cbrt(volume / count);
            }
        };
    }

    @Override
    public boolean contains(Vector3d point) {
        double nx = point.x / xRadius;
        double ny = point.y / yRadius;
        double nz = point.z / zRadius;
        return nx * nx + ny * ny + nz * nz <= 1;
    }

    @Override
    public double getLength() { return xRadius * 2; }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getWidth() { return zRadius * 2; }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public double getHeight() { return yRadius * 2; }

    @Override
    public void setHeight(double height) {
        yRadius = Math.max(height / 2, Shape.EPSILON);
        invalidate();
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipsoid(xRadius, yRadius, zRadius));
    }
}
