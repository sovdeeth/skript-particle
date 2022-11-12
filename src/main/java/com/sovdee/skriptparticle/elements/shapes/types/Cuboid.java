package com.sovdee.skriptparticle.elements.shapes.types;

import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Cuboid extends Shape {

    private double halfWidth;
    private double halfHeight;
    private double halfDepth;
    private Vector cubeCenter;

    private double xStep = 1.0;
    private double yStep = 1.0;
    private double zStep = 1.0;

    public Cuboid(double halfWidth, double halfHeight, double halfDepth) {
        super();
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
        calculateSteps();
    }

    public Cuboid(@NotNull Vector negativeCorner, @NotNull Vector positiveCorner) {
        super();
        this.cubeCenter = negativeCorner.clone().add(positiveCorner.clone().subtract(negativeCorner).multiply(0.5));
        this.halfWidth = (positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfHeight = (positiveCorner.getY() - negativeCorner.getY()) / 2;
        this.halfDepth = (positiveCorner.getZ() - negativeCorner.getZ()) / 2;
        calculateSteps();
    }

    public Cuboid(@NotNull Location negativeCorner, @NotNull Location positiveCorner) {
        super();
        this.center(negativeCorner.clone().add(positiveCorner.clone().subtract(negativeCorner).toVector().multiply(0.5)));
        this.halfWidth = (positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfHeight = (positiveCorner.getY() - negativeCorner.getY()) / 2;
        this.halfDepth = (positiveCorner.getZ() - negativeCorner.getZ()) / 2;
        calculateSteps();
    }

    private void calculateSteps() {
        xStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        yStep = 2 * halfHeight / Math.round(2 * halfHeight / particleDensity);
        zStep = 2 * halfDepth / Math.round(2 * halfDepth / particleDensity);
    }

    @Override
    public List<Vector> generateOutline() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            points.add(new Vector(x, -halfHeight, -halfDepth));
            points.add(new Vector(x, -halfHeight, halfDepth));
            points.add(new Vector(x, halfHeight, -halfDepth));
            points.add(new Vector(x, halfHeight, halfDepth));
        }
        for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
            points.add(new Vector(-halfWidth, y, -halfDepth));
            points.add(new Vector(-halfWidth, y, halfDepth));
            points.add(new Vector(halfWidth, y, -halfDepth));
            points.add(new Vector(halfWidth, y, halfDepth));
        }
        for (double z = -halfDepth + zStep; z < halfDepth; z += zStep) {
            points.add(new Vector(-halfWidth, -halfHeight, z));
            points.add(new Vector(-halfWidth, halfHeight, z));
            points.add(new Vector(halfWidth, -halfHeight, z));
            points.add(new Vector(halfWidth, halfHeight, z));
        }
        if (cubeCenter != null) {
            points.replaceAll(vector -> vector.add(cubeCenter));
        }
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double z = -halfDepth; z <= halfDepth; z += zStep) {
                points.add(new Vector(x, -halfHeight, z));
                points.add(new Vector(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
            for (double z = -halfDepth; z <= halfDepth; z += zStep) {
                points.add(new Vector(-halfWidth, y, z));
                points.add(new Vector(halfWidth, y, z));
            }
        }
        for (double x = -halfWidth + xStep; x < halfWidth; x += xStep) {
            for (double y = -halfHeight + yStep; y < halfHeight; y += yStep) {
                points.add(new Vector(x, y, -halfDepth));
                points.add(new Vector(x, y, halfDepth));
            }
        }
        if (cubeCenter != null) {
            points.replaceAll(vector -> vector.add(cubeCenter));
        }
        return points;
    }

    @Override
    public List<Vector> generateFilled() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double y = -halfHeight; y <= halfHeight; y += yStep) {
                for (double z = -halfDepth; z <= halfDepth; z += zStep) {
                    points.add(new Vector(x, y, z));
                }
            }
        }
        if (cubeCenter != null) {
            points.replaceAll(vector -> vector.add(cubeCenter));
        }
        return points;
    }

    @Override
    public Shape particleCount(int count) {
        particleDensity = switch (style) {
            case OUTLINE -> 8 * (halfDepth + halfHeight + halfWidth) / count;
            case SURFACE -> Math.sqrt(8 * (halfDepth * halfHeight + halfDepth * halfWidth + halfHeight * halfWidth) / count);
            case FILL -> Math.cbrt(8 * halfDepth * halfHeight * halfWidth / count);
        };
        calculateSteps();
        return this;
    }

    public double getWidth() {
        return halfWidth * 2;
    }

    public void setWidth(double width) {
        this.halfWidth = width / 2;
        calculateSteps();
    }

    public double getHeight() {
        return halfHeight * 2;
    }

    public void setHeight(double height) {
        this.halfHeight = height / 2;
        calculateSteps();
    }

    public double getDepth() {
        return halfDepth * 2;
    }

    public void setDepth(double depth) {
        this.halfDepth = depth / 2;
        calculateSteps();
    }

    @Override
    public Shape clone() {
        Cuboid cuboid = new Cuboid(halfWidth, halfHeight, halfDepth);
        this.copyTo(cuboid);
        return cuboid;
    }
}
