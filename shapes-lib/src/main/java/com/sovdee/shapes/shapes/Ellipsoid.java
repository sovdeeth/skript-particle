package com.sovdee.shapes.shapes;

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
    public void generateOutline(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        points.addAll(Ellipse.calculateEllipse(xRadius, zRadius, particleDensity, 2 * Math.PI));
        points.addAll(VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius, yRadius, particleDensity, 2 * Math.PI)));
        points.addAll(VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius, zRadius, particleDensity, 2 * Math.PI)));
    }

    @Override
    public void generateSurface(Set<Vector3d> points) {
        List<Vector3d> ellipse;
        double particleDensity = this.getParticleDensity();
        if (xRadius > zRadius) {
            ellipse = VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius, yRadius, particleDensity, 2 * Math.PI));
        } else {
            ellipse = VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius, zRadius, particleDensity, 2 * Math.PI));
        }
        points.addAll(generateEllipsoid(ellipse, 1));
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        List<Vector3d> ellipse;
        double radius = Math.max(xRadius, zRadius);
        double particleDensity = this.getParticleDensity();
        int steps = (int) Math.round(radius / particleDensity);
        for (int i = steps; i > 0; i--) {
            double r = (i / (double) steps);
            if (xRadius > zRadius) {
                ellipse = VectorUtil.transform(XY_ROTATION, Ellipse.calculateEllipse(xRadius * r, yRadius * r, particleDensity, 2 * Math.PI));
            } else {
                ellipse = VectorUtil.transform(ZY_ROTATION, Ellipse.calculateEllipse(yRadius * r, zRadius * r, particleDensity, 2 * Math.PI));
            }
            points.addAll(generateEllipsoid(ellipse, r));
        }
    }

    private Set<Vector3d> generateEllipsoid(List<Vector3d> ellipse, double radius) {
        Set<Vector3d> points = new LinkedHashSet<>();
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++) {
            double y = ellipse.get(i).y;
            double theta = Math.asin(y / (yRadius * radius));
            for (Vector3d v2 : Ellipse.calculateEllipse(radius * xRadius * Math.cos(theta), radius * zRadius * Math.cos(theta), this.getParticleDensity(), 2 * Math.PI)) {
                points.add(new Vector3d(v2.x, y, v2.z));
                points.add(new Vector3d(v2.x, -y, v2.z));
            }
        }
        points.addAll(Ellipse.calculateEllipse(radius * xRadius, radius * zRadius, this.getParticleDensity(), 2 * Math.PI));
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        switch (this.getStyle()) {
            case OUTLINE -> {
                double h = (xRadius - yRadius) * (xRadius - yRadius) / ((xRadius + yRadius) + (xRadius + yRadius));
                double circumferenceXY = Math.PI * (xRadius + yRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXZ = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (yRadius - zRadius) * (yRadius - zRadius) / ((yRadius + zRadius) + (yRadius + zRadius));
                double circumferenceYZ = Math.PI * (yRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.setParticleDensity((circumferenceXY + circumferenceXZ + circumferenceYZ) / particleCount);
            }
            case SURFACE -> {
                double surfaceArea = 4 * Math.PI * Math.pow((Math.pow(xRadius * yRadius, 1.6) + Math.pow(xRadius * zRadius, 1.6) + Math.pow(zRadius * yRadius, 1.6)) / 3, 1 / 1.6);
                this.setParticleDensity(Math.sqrt(surfaceArea / particleCount));
            }
            case FILL -> {
                double volume = 4 / 3.0 * Math.PI * xRadius * yRadius * zRadius;
                this.setParticleDensity(Math.cbrt(volume / particleCount));
            }
        }
    }

    @Override
    public double getLength() { return xRadius * 2; }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() { return zRadius * 2; }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() { return yRadius * 2; }

    @Override
    public void setHeight(double height) {
        yRadius = Math.max(height / 2, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipsoid(xRadius, yRadius, zRadius));
    }
}
