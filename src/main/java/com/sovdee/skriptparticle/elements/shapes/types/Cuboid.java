package com.sovdee.skriptparticle.elements.shapes.types;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class Cuboid extends Shape {

    private double halfWidth;
    private double halfHeight;
    private double halfDepth;

    public Cuboid(double halfWidth, double halfHeight, double halfDepth) {
        super();
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
    }

    public Cuboid(Vector negativeCorner, Vector positiveCorner) {
        super();
        this.halfWidth = (positiveCorner.getX() - negativeCorner.getX()) / 2;
        this.halfHeight = (positiveCorner.getY() - negativeCorner.getY()) / 2;
        this.halfDepth = (positiveCorner.getZ() - negativeCorner.getZ()) / 2;
    }

    @Override
    public List<Vector> generateOutline() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x < halfWidth; x += particleDensity) {
            points.add(new Vector(x, -halfHeight, -halfDepth));
            points.add(new Vector(x, -halfHeight, halfDepth));
            points.add(new Vector(x, halfHeight, -halfDepth));
            points.add(new Vector(x, halfHeight, halfDepth));
        }
        for (double y = -halfHeight; y < halfHeight; y += particleDensity) {
            points.add(new Vector(-halfWidth, y, -halfDepth));
            points.add(new Vector(-halfWidth, y, halfDepth));
            points.add(new Vector(halfWidth, y, -halfDepth));
            points.add(new Vector(halfWidth, y, halfDepth));
        }
        for (double z = -halfDepth; z < halfDepth; z += particleDensity) {
            points.add(new Vector(-halfWidth, -halfHeight, z));
            points.add(new Vector(-halfWidth, halfHeight, z));
            points.add(new Vector(halfWidth, -halfHeight, z));
            points.add(new Vector(halfWidth, halfHeight, z));
        }
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x < halfWidth; x += particleDensity) {
            for (double z = -halfDepth; z < halfDepth; z += particleDensity) {
                points.add(new Vector(x, -halfHeight, z));
                points.add(new Vector(x, halfHeight, z));
            }
        }
        for (double y = -halfHeight; y < halfHeight; y += particleDensity) {
            for (double z = -halfDepth; z < halfDepth; z += particleDensity) {
                points.add(new Vector(-halfWidth, y, z));
                points.add(new Vector(halfWidth, y, z));
            }
        }
        for (double x = -halfWidth; x < halfWidth; x += particleDensity) {
            for (double y = -halfHeight; y < halfHeight; y += particleDensity) {
                points.add(new Vector(x, y, -halfDepth));
                points.add(new Vector(x, y, halfDepth));
            }
        }
        return points;
    }

    @Override
    public List<Vector> generateFilled() {
        points = new ArrayList<>();
        for (double x = -halfWidth; x < halfWidth; x += particleDensity) {
            for (double y = -halfHeight; y < halfHeight; y += particleDensity) {
                for (double z = -halfDepth; z < halfDepth; z += particleDensity) {
                    points.add(new Vector(x, y, z));
                }
            }
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
        return null;
    }

    public double getWidth() {
        return halfWidth * 2;
    }

    public void setWidth(double width) {
        this.halfWidth = width / 2;
    }

    public double getHeight() {
        return halfHeight * 2;
    }

    public void setHeight(double height) {
        this.halfHeight = height / 2;
    }

    public double getDepth() {
        return halfDepth * 2;
    }

    public void setDepth(double depth) {
        this.halfDepth = depth / 2;
    }

    @Override
    public Shape clone() {
        Cuboid cuboid = new Cuboid(halfWidth, halfHeight, halfDepth);
        this.copyTo(cuboid);
        return cuboid;
    }
}
