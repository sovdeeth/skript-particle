package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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
    public void generateOutline(Set<Vector3d> points, double density) {
        points.addAll(Ellipse.calculateEllipse(xRadius, zRadius, density, 2 * Math.PI));
        points.addAll(VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius, yRadius, density, 2 * Math.PI)));
        points.addAll(VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius, zRadius, density, 2 * Math.PI)));
    }

    @Override
    public void generateSurface(Set<Vector3d> points, double density) {
        List<Vector3d> ellipse;
        if (xRadius > zRadius) {
            ellipse = VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius, yRadius, density, 2 * Math.PI));
        } else {
            ellipse = VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius, zRadius, density, 2 * Math.PI));
        }
        points.addAll(generateEllipsoid(ellipse, 1, density));
    }

    @Override
    public void generateFilled(Set<Vector3d> points, double density) {
        List<Vector3d> ellipse;
        double radius = Math.max(xRadius, zRadius);
        int steps = (int) Math.round(radius / density);
        for (int i = steps; i > 0; i--) {
            double r = (i / (double) steps);
            if (xRadius > zRadius) {
                ellipse = VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius * r, yRadius * r, density, 2 * Math.PI));
            } else {
                ellipse = VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius * r, zRadius * r, density, 2 * Math.PI));
            }
            points.addAll(generateEllipsoid(ellipse, r, density));
        }
    }

    private Set<Vector3d> generateEllipsoid(List<Vector3d> ellipse, double radius, double density) {
        Set<Vector3d> points = new LinkedHashSet<>();
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++) {
            double y = ellipse.get(i).y;
            double theta = Math.asin(y / (yRadius * radius));
            for (Vector3d v2 : Ellipse.calculateEllipse(radius * xRadius * Math.cos(theta), radius * zRadius * Math.cos(theta), density, 2 * Math.PI)) {
                points.add(new Vector3d(v2.x, y, v2.z));
                points.add(new Vector3d(v2.x, -y, v2.z));
            }
        }
        points.addAll(Ellipse.calculateEllipse(radius * xRadius, radius * zRadius, density, 2 * Math.PI));
        return points;
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
