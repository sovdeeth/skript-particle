package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * An ellipsoid shape, with an x radius, y radius, and z radius.
 * All radii must be greater than 0.
 */
public class Ellipsoid extends AbstractShape implements LWHShape {

    private static final Quaternion XY_ROTATION = new Quaternion(new Vector(1, 0, 0), (float) (Math.PI / 2));
    private static final Quaternion ZY_ROTATION = new Quaternion(new Vector(0, 0, 1), (float) (Math.PI / 2));
    protected double xRadius;
    protected double yRadius;
    protected double zRadius;

    /**
     * Creates an ellipsoid with the given x radius, y radius, and z radius.
     * All radii must be greater than 0.
     * @param xRadius the x radius. Must be greater than 0.
     * @param yRadius the y radius. Must be greater than 0.
     * @param zRadius the z radius. Must be greater than 0.
     */
    public Ellipsoid(double xRadius, double yRadius, double zRadius) {
        super();
        this.xRadius = Math.max(xRadius, MathUtil.EPSILON);
        this.yRadius = Math.max(yRadius, MathUtil.EPSILON);
        this.zRadius = Math.max(zRadius, MathUtil.EPSILON);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        HashSet<Vector> points = new LinkedHashSet<>();
        double particleDensity = this.getParticleDensity();
        points.addAll(MathUtil.calculateEllipse(xRadius, zRadius, particleDensity, 2 * Math.PI));
        points.addAll(XY_ROTATION.transform(MathUtil.calculateEllipse(xRadius, yRadius, particleDensity, 2 * Math.PI)));
        points.addAll(ZY_ROTATION.transform(MathUtil.calculateEllipse(yRadius, zRadius, particleDensity, 2 * Math.PI)));
        return points;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateSurface() {
        List<Vector> ellipse;
        double particleDensity = this.getParticleDensity();
        if (xRadius > zRadius) {
            ellipse = XY_ROTATION.transform(MathUtil.calculateEllipse(xRadius, yRadius, particleDensity, 2 * Math.PI));
        } else {
            ellipse = ZY_ROTATION.transform(MathUtil.calculateEllipse(yRadius, zRadius, particleDensity, 2 * Math.PI));
        }
        return generateEllipsoid(ellipse, 1);
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateFilled() {
        Set<Vector> points = new LinkedHashSet<>();
        List<Vector> ellipse;
        double radius = Math.max(xRadius, zRadius);
        double particleDensity = this.getParticleDensity();
        int steps = (int) Math.round(radius / particleDensity);
        for (int i = steps; i > 0; i--) {
            double r = (i / (double) steps);
            if (xRadius > zRadius) {
                ellipse = XY_ROTATION.transform(MathUtil.calculateEllipse(xRadius * r, yRadius * r, particleDensity, 2 * Math.PI));
            } else {
                ellipse = ZY_ROTATION.transform(MathUtil.calculateEllipse(yRadius * r, zRadius * r, particleDensity, 2 * Math.PI));
            }
            points.addAll(generateEllipsoid(ellipse, r));
        }
        return points;
    }

    /**
     * Generates the point on an ellipsoid with the given elliptical cross-section and third radius.
     *
     * @param ellipse the elliptical cross-section.
     * @param radius the third radius. Must be greater than 0.
     * @return the points on the ellipsoid.
     */
    private Set<Vector> generateEllipsoid(List<Vector> ellipse, double radius) {
        Set<Vector> points = new LinkedHashSet<>();
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++) {
            double y = ellipse.get(i).getY();
            double theta = Math.asin(y / (yRadius * radius));
            for (Vector v2 : MathUtil.calculateEllipse(radius * xRadius * Math.cos(theta), radius * zRadius * Math.cos(theta), this.getParticleDensity(), 2 * Math.PI)) {
                points.add(new Vector(v2.getX(), y, v2.getZ()));
                points.add(new Vector(v2.getX(), -y, v2.getZ()));
            }
        }
        points.addAll(MathUtil.calculateEllipse(radius * xRadius, radius * zRadius, this.getParticleDensity(), 2 * Math.PI));
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        switch (this.getStyle()) {
            case OUTLINE -> {
                // this is so fucking cringe
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
    public double getLength() {
        return xRadius * 2;
    }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getWidth() {
        return zRadius * 2;
    }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getHeight() {
        return yRadius * 2;
    }

    @Override
    public void setHeight(double height) {
        yRadius = Math.max(height / 2, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new Ellipsoid(xRadius, yRadius, zRadius));
    }

}
