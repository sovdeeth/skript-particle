package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Cuboid extends AbstractShape implements LWHShape {

    private double halfLength;
    private double halfWidth;
    private double halfHeight;
    private double xStep;
    private double zStep;
    private double yStep;
    private Vector centerOffset = new Vector(0, 0, 0);
    private DynamicLocation negativeCorner;
    private DynamicLocation positiveCorner;
    private boolean isDynamic = false;

    public Cuboid(double length, double width, double height){
        super();
        this.halfWidth = width / 2;
        this.halfLength = length / 2;
        this.halfHeight = height / 2;
        calculateSteps();
    }
    
    public Cuboid(Vector negativeCorner, Vector positiveCorner){
        super();
        this.halfWidth = (positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfLength = (positiveCorner.getZ() - negativeCorner.getZ()) / 2;
        this.halfHeight = (positiveCorner.getY() - negativeCorner.getY()) / 2;
        centerOffset = positiveCorner.clone().add(negativeCorner).multiply(0.5);
        calculateSteps();
    }
    
    public Cuboid(DynamicLocation negativeCorner, DynamicLocation positiveCorner){
        super();
        if (negativeCorner.isDynamic() || positiveCorner.isDynamic()) {
            this.negativeCorner = negativeCorner.clone();
            this.positiveCorner = positiveCorner.clone();
            isDynamic = true;
        } else {
            this.halfWidth = (positiveCorner.getLocation().getX() - negativeCorner.getLocation().getX()) / 2;
            this.halfLength = (positiveCorner.getLocation().getZ() - negativeCorner.getLocation().getZ()) / 2;
            this.halfHeight = (positiveCorner.getLocation().getY() - negativeCorner.getLocation().getY()) / 2;
        }
        location = new DynamicLocation(negativeCorner.getLocation().add(positiveCorner.getLocation().subtract(negativeCorner.getLocation()).toVector().multiply(0.5)));
        calculateSteps();
    }

    private void calculateSteps() {
        xStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        zStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
        yStep = 2 * halfHeight / Math.round(2 * halfHeight / particleDensity);
    }

    @Override
    public Set<Vector> generateOutline() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            points.add(new Vector(x, -halfHeight, -halfLength));
            points.add(new Vector(x, -halfHeight, halfLength));
            points.add(new Vector(x, halfHeight, -halfLength));
            points.add(new Vector(x, halfHeight, halfLength));
        }
        for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
            points.add(new Vector(-halfWidth, y, -halfLength));
            points.add(new Vector(-halfWidth, y, halfLength));
            points.add(new Vector(halfWidth, y, -halfLength));
            points.add(new Vector(halfWidth, y, halfLength));
        }
        for (double z = -halfLength + zStep; z < halfLength; z += zStep) {
            points.add(new Vector(-halfWidth, -halfHeight, z));
            points.add(new Vector(-halfWidth, halfHeight, z));
            points.add(new Vector(halfWidth, -halfHeight, z));
            points.add(new Vector(halfWidth, halfHeight, z));
        }
        return points;
    }

    @Override
    public Set<Vector> generateSurface() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double z = -halfLength; z <= halfLength; z += zStep) {
                points.add(new Vector(x, -halfHeight, z));
                points.add(new Vector(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
            for (double z = -halfLength; z <= halfLength; z += zStep) {
                points.add(new Vector(-halfWidth, y, z));
                points.add(new Vector(halfWidth, y, z));
            }
        }
        for (double x = -halfWidth + xStep; x < halfWidth; x += xStep) {
            for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
                points.add(new Vector(x, y, -halfLength));
                points.add(new Vector(x, y, halfLength));
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generateFilled() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double y = -halfHeight; y <= halfHeight; y += yStep) {
                for (double z = -halfLength; z <= halfLength; z += zStep) {
                    points.add(new Vector(x, y, z));
                }
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            this.halfWidth = (positiveCorner.getLocation().getX() - negativeCorner.getLocation().getX()) / 2;
            this.halfLength = (positiveCorner.getLocation().getZ() - negativeCorner.getLocation().getZ()) / 2;
            this.halfHeight = (positiveCorner.getLocation().getY() - negativeCorner.getLocation().getY()) / 2;
            location = new DynamicLocation(negativeCorner.getLocation().add(positiveCorner.getLocation().subtract(negativeCorner.getLocation()).toVector().multiply(0.5)));
        }
        calculateSteps();
        Set<Vector> points = super.generatePoints();
        points.forEach(vector -> vector.add(centerOffset));
        return points;
    }

    // Ensure that the points are always needing to be updated if the start or end location is dynamic
    @Override
    public Set<Vector> getPoints(Quaternion orientation) {
        Set<Vector> points = super.getPoints(orientation);
        if (isDynamic)
            this.needsUpdate = true;
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleDensity = switch (style) {
            case OUTLINE -> 8 * (halfLength + halfHeight + halfWidth) / particleCount;
            case SURFACE -> Math.sqrt(8 * (halfLength * halfHeight + halfLength * halfWidth + halfHeight * halfWidth) / particleCount);
            case FILL -> Math.cbrt(8 * halfLength * halfHeight * halfWidth / particleCount);
        };
        calculateSteps();
        this.needsUpdate = true;
    }

    @Override
    public double getLength() {
        return halfLength * 2;
    }

    @Override
    public double getWidth() {
        return halfWidth * 2;
    }

    @Override
    public double getHeight() {
        return halfHeight * 2;
    }

    @Override
    public void setLength(double length) {
        this.halfLength = length / 2;
    }

    @Override
    public void setWidth(double width) {
        this.halfWidth = width / 2;
    }

    @Override
    public void setHeight(double height) {
        this.halfHeight = height / 2;
    }

    @Override
    public Shape clone() {
        Cuboid cuboid = (isDynamic ? new Cuboid(negativeCorner, positiveCorner): new Cuboid(getLength(), getWidth(), getHeight()));
        cuboid.isDynamic = isDynamic;
        return this.copyTo(cuboid);
    }

}
