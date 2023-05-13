package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Ellipsoid extends AbstractShape implements LWHShape {

    protected double xRadius;
    protected double yRadius;
    protected double zRadius;


    private static final Quaternion XY_ROTATION = new Quaternion(new Vector(1,0,0), (float) (Math.PI / 2));
    private static final Quaternion ZY_ROTATION = new Quaternion(new Vector(0,0,1), (float) (Math.PI / 2));
    
    public Ellipsoid(double xRadius, double yRadius, double zRadius) {
        super();
        this.xRadius = xRadius;
        this.yRadius = yRadius;
        this.zRadius = zRadius;
    }

    @Override
    public Set<Vector> generateOutline() {
        HashSet<Vector> points = new HashSet<>();
        points.addAll(MathUtil.calculateEllipse(xRadius, zRadius, particleDensity, 2*Math.PI));
        points.addAll(XY_ROTATION.transform(MathUtil.calculateEllipse(xRadius, yRadius, particleDensity, 2*Math.PI)));
        points.addAll(ZY_ROTATION.transform(MathUtil.calculateEllipse(yRadius, zRadius, particleDensity, 2*Math.PI)));
        return points;
    }

    @Override
    public Set<Vector> generateSurface() {
        List<Vector> ellipse;
        if (xRadius > zRadius) {
            ellipse = XY_ROTATION.transform(MathUtil.calculateEllipse(xRadius, yRadius, particleDensity, 2 * Math.PI));
        } else {
            ellipse = ZY_ROTATION.transform(MathUtil.calculateEllipse(yRadius, zRadius, particleDensity, 2 * Math.PI));
        }
        return generateEllipsoid(ellipse, 1);
    }

    @Override
    public Set<Vector> generateFilled() {
        Set<Vector> points = new HashSet<>();
        List<Vector> ellipse;
        double radius = Math.max(xRadius, zRadius);
        int steps = (int) Math.round(radius / particleDensity);
        for (int i = steps; i > 0; i--){
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

    private Set<Vector> generateEllipsoid(List<Vector> ellipse, double r) {
        Set<Vector> points = new HashSet<>();
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++) {
            double y = ellipse.get(i).getY();
            double theta = Math.asin(y / (yRadius * r));
            for (Vector v2 : MathUtil.calculateEllipse(r * xRadius * Math.cos(theta), r * zRadius * Math.cos(theta), particleDensity, 2*Math.PI)){
                points.add(new Vector(v2.getX(), y, v2.getZ()));
                points.add(new Vector(v2.getX(), -y, v2.getZ()));
            }
        }
        points.addAll(MathUtil.calculateEllipse(r * xRadius, r * zRadius, particleDensity, 2*Math.PI));
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        switch (style) {
            case OUTLINE -> {
                // this is so fucking cringe
                double h = (xRadius - yRadius) * (xRadius - yRadius) / ((xRadius + yRadius) + (xRadius + yRadius));
                double circumferenceXY = Math.PI * (xRadius + yRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (xRadius - zRadius) * (xRadius - zRadius) / ((xRadius + zRadius) + (xRadius + zRadius));
                double circumferenceXZ = Math.PI * (xRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (yRadius - zRadius) * (yRadius - zRadius) / ((yRadius + zRadius) + (yRadius + zRadius));
                double circumferenceYZ = Math.PI * (yRadius + zRadius) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                particleDensity = (circumferenceXY + circumferenceXZ + circumferenceYZ) / particleCount;
            }
            case SURFACE -> {
                double surfaceArea = 4 * Math.PI * Math.pow((Math.pow(xRadius * yRadius, 1.6) + Math.pow(xRadius * zRadius, 1.6) + Math.pow(zRadius * yRadius, 1.6)) / 3, 1 / 1.6);
                this.particleDensity = Math.sqrt(surfaceArea / particleCount);
            }
            case FILL -> {
                double volume = 4 / 3.0 * Math.PI * xRadius * yRadius * zRadius;
                this.particleDensity = Math.cbrt(volume / particleCount);
            }
        }
    }

    @Override
    public double getLength() {
        return xRadius * 2;
    }

    @Override
    public double getWidth() {
        return zRadius * 2;
    }

    @Override
    public double getHeight() {
        return yRadius * 2;
    }

    @Override
    public void setLength(double length) {
        xRadius = Math.max(length / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        zRadius = Math.max(width / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        yRadius = Math.max(height / 2, MathUtil.EPSILON);
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new Ellipsoid(xRadius, yRadius, zRadius));
    }

}
