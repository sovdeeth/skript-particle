package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Ellipsoid extends AbstractShape implements LWHShape {

    private static final Quaterniond XY_ROTATION = new Quaterniond().rotateX(Math.PI / 2);
    private static final Quaterniond ZY_ROTATION = new Quaterniond().rotateZ(Math.PI / 2);
    protected double xRadius;
    protected double yRadius;
    protected double zRadius;

    public Ellipsoid(double xRadius, double yRadius, double zRadius) {
        super();
        this.xRadius = Math.max(xRadius, Shape.EPSILON);
        this.yRadius = Math.max(yRadius, Shape.EPSILON);
        this.zRadius = Math.max(zRadius, Shape.EPSILON);
    }

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
