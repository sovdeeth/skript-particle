package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Cuboid extends AbstractShape implements LWHShape {

    private double halfLength;
    private double halfWidth;
    private double halfHeight;
    private double lengthStep;
    private double widthStep;
    private double heightStep;
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
        this.halfLength = Math.abs(positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfWidth = Math.abs(positiveCorner.getZ() - negativeCorner.getZ()) / 2;
        this.halfHeight = Math.abs(positiveCorner.getY() - negativeCorner.getY()) / 2;
        centerOffset = positiveCorner.clone().add(negativeCorner).multiply(0.5);
        calculateSteps();
    }
    
    public Cuboid(DynamicLocation negativeCorner, DynamicLocation positiveCorner){
        super();
        Location negative = negativeCorner.getLocation();
        Location positive = positiveCorner.getLocation();
        if (negativeCorner.isDynamic() || positiveCorner.isDynamic()) {
            this.negativeCorner = negativeCorner.clone();
            this.positiveCorner = positiveCorner.clone();
            isDynamic = true;
        } else {
            this.halfLength = Math.abs(positive.getX() - negative.getX()) / 2;
            this.halfWidth = Math.abs(positive.getZ() - negative.getZ()) / 2;
            this.halfHeight = Math.abs(positive.getY() - negative.getY()) / 2;
        }
        location = new DynamicLocation(negative.clone().add(positive.subtract(negative).toVector().multiply(0.5)));
        calculateSteps();
    }

    private void calculateSteps() {
        widthStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        lengthStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
        heightStep = 2 * halfHeight / Math.round(2 * halfHeight / particleDensity);
    }

    @Override
    public Set<Vector> generateOutline() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            points.add(new Vector(x, -halfHeight, -halfWidth));
            points.add(new Vector(x, -halfHeight, halfWidth));
            points.add(new Vector(x, halfHeight, -halfWidth));
            points.add(new Vector(x, halfHeight, halfWidth));
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            points.add(new Vector(-halfLength, y, -halfWidth));
            points.add(new Vector(-halfLength, y, halfWidth));
            points.add(new Vector(halfLength, y, -halfWidth));
            points.add(new Vector(halfLength, y, halfWidth));
        }
        for (double z = -halfWidth + widthStep; z < halfWidth; z += widthStep) {
            points.add(new Vector(-halfLength, -halfHeight, z));
            points.add(new Vector(-halfLength, halfHeight, z));
            points.add(new Vector(halfLength, -halfHeight, z));
            points.add(new Vector(halfLength, halfHeight, z));
        }
        return points;
    }

    @Override
    public Set<Vector> generateSurface() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector(x, -halfHeight, z));
                points.add(new Vector(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
            for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                points.add(new Vector(-halfLength, y, z));
                points.add(new Vector(halfLength, y, z));
            }
        }
        for (double x = -halfLength + lengthStep; x < halfLength; x += lengthStep) {
            for (double y = -halfHeight + heightStep; y < halfHeight; y += heightStep) {
                points.add(new Vector(x, y, -halfWidth));
                points.add(new Vector(x, y, halfWidth));
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generateFilled() {
        HashSet<Vector> points = new HashSet<>();
        for (double x = -halfLength; x <= halfLength; x += lengthStep) {
            for (double y = -halfHeight; y <= halfHeight; y += heightStep) {
                for (double z = -halfWidth; z <= halfWidth; z += widthStep) {
                    points.add(new Vector(x, y, z));
                }
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            Location negative = negativeCorner.getLocation();
            Location positive = positiveCorner.getLocation();
            this.halfLength = Math.abs(positive.getX() - negative.getX()) / 2;
            this.halfWidth = Math.abs(positive.getZ() - negative.getZ()) / 2;
            this.halfHeight = Math.abs(positive.getY() - negative.getY()) / 2;
            location = new DynamicLocation(negative.clone().add(positive.subtract(negative).toVector().multiply(0.5)));
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
        this.needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        this.halfWidth = width / 2;
        this.needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
        this.halfHeight = height / 2;
        this.needsUpdate = true;
    }

    @Override
    public Shape clone() {
        Cuboid cuboid = (isDynamic ? new Cuboid(negativeCorner, positiveCorner): new Cuboid(getLength(), getWidth(), getHeight()));
        cuboid.isDynamic = isDynamic;
        return this.copyTo(cuboid);
    }

}
