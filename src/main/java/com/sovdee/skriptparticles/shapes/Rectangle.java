package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Set;

public class Rectangle extends AbstractShape implements LWHShape {

    private double halfLength;
    private double halfWidth;
    private double xStep = 1.0;
    private double zStep = 1.0;
    private Vector centerOffset = new Vector(0, 0, 0);
    private DynamicLocation negativeCorner;
    private DynamicLocation positiveCorner;
    private boolean isDynamic = false;

    // TODO: Add xy and zy rectangles

    public Rectangle(double length, double width){
        super();
        this.halfWidth = width / 2;
        this.halfLength = length / 2;
        calculateSteps();
    }

    public Rectangle(Vector negativeCorner, Vector positiveCorner){
        super();
        this.halfWidth = Math.abs(positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfLength = Math.abs(positiveCorner.getZ() - negativeCorner.getZ()) / 2;
        centerOffset = positiveCorner.clone().add(negativeCorner).setY(0).multiply(0.5);
        calculateSteps();
    }

    public Rectangle(DynamicLocation negativeCorner, DynamicLocation positiveCorner){
        super();
        Location negative = negativeCorner.getLocation();
        Location positive = positiveCorner.getLocation();
        if (negativeCorner.isDynamic() || positiveCorner.isDynamic()) {
            this.negativeCorner = negativeCorner.clone();
            this.positiveCorner = positiveCorner.clone();
            isDynamic = true;
        } else {
            this.halfWidth = Math.abs(positive.getX() - negative.getX()) / 2;
            this.halfLength = Math.abs(positive.getZ() - negative.getZ()) / 2;
        }
        location = new DynamicLocation(negative.clone().add(positive.subtract(negative).toVector().setY(0).multiply(0.5)));
        calculateSteps();
    }

    /**
     * Calculates the nearest factor to particleDensity as step size for the x and z axis
     * Used to ensure the shape has a uniform density of particles
     */
    private void calculateSteps() {
        xStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        zStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
    }

    @Override
    public Set<Vector> generateOutline() {
        Set<Vector> points = new HashSet<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            points.add(new Vector(x, 0, -halfLength));
            points.add(new Vector(x, 0, halfLength));
        }
        for (double z = -halfLength + zStep; z < halfLength; z += zStep) {
            points.add(new Vector(-halfWidth, 0, z));
            points.add(new Vector(halfWidth, 0, z));
        }
        return points;
    }

    @Override
    public Set<Vector> generateSurface() {
        Set<Vector> points = new HashSet<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double z = -halfLength; z <= halfLength; z += zStep) {
                points.add(new Vector(x, 0, z));
            }
        }
        return points;
    }

    @Override
    public Set<Vector> generatePoints() {
        if (isDynamic) {
            Location pos = positiveCorner.getLocation();
            Location neg = negativeCorner.getLocation();
            this.halfWidth = Math.abs(pos.getX() - neg.getX()) / 2;
            this.halfLength = Math.abs(pos.getZ() - neg.getZ()) / 2;
            location = new DynamicLocation(neg.clone().add(pos.subtract(neg).toVector().setY(0).multiply(0.5)));
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
    public void setParticleCount(int count) {
        switch (style){
            case FILL, SURFACE -> particleDensity = Math.sqrt(4 * halfWidth * halfLength / count);
            case OUTLINE -> particleDensity = 4 * (halfWidth + halfLength) / count;
        };
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
        return 0;
    }

    @Override
    public void setLength(double length) {
        this.halfLength = Math.abs(length) / 2;
        this.needsUpdate = true;
    }

    @Override
    public void setWidth(double width) {
        this.halfWidth = Math.abs(width) / 2;
        this.needsUpdate = true;
    }

    @Override
    public void setHeight(double height) {
    }

    @Override
    public Shape clone() {
        Rectangle rectangle = (isDynamic ? new Rectangle(negativeCorner, positiveCorner): new Rectangle(this.getLength(), this.getWidth()));
        rectangle.isDynamic = this.isDynamic;
        return this.copyTo(rectangle);
    }

    @Override
    public String toString() {
        if (isDynamic)
            return "rectangle from " + negativeCorner.toString() + " to " + positiveCorner.toString();
        return "rectangle with length " + this.getLength() + " and width " + this.getWidth();
    }
}
