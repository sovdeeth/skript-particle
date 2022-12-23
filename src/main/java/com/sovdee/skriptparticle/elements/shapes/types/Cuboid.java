package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashSet;
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
    public Cuboid(double halfWidth, double halfHeight, double halfDepth, Vector cubeCenter) {
        super();
        this.halfWidth = halfWidth;
        this.halfHeight = halfHeight;
        this.halfDepth = halfDepth;
        this.cubeCenter = cubeCenter;
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
        xStep = 2 * halfWidth / Math.floor(2 * halfWidth / particleDensity);
        yStep = 2 * halfHeight / Math.floor(2 * halfHeight / particleDensity);
        zStep = 2 * halfDepth / Math.floor(2 * halfDepth / particleDensity);
    }

    @Override
    public List<Vector> generatePoints() {
        calculateSteps();
        return super.generatePoints();
    }

    @Override
    public List<Vector> generateOutline() {
        HashSet<Vector> points = new HashSet<>();
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
        this.points = new ArrayList<>(points);
        if (cubeCenter != null) {
            this.points.replaceAll(vector -> vector.add(cubeCenter));
        }
        return this.points;
    }

    @Override
    public List<Vector> generateSurface() {
        HashSet<Vector> points = new HashSet<>();
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
        this.points = new ArrayList<>(points);
        if (cubeCenter != null) {
            this.points.replaceAll(vector -> vector.add(cubeCenter));
        }
        return this.points;
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
        points = generatePoints();
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
    static {
        Classes.registerClass(new ClassInfo<>(Cuboid.class, "cuboid")
                .user("cuboids?")
                    .name("Cuboid")
                    .description("Represents a cuboid particle shape.")
                    .examples("on load:", "\tset {_cuboid} to a cuboid ...")
                    .serializer(new Serializer<>() {

            @Override
            public Fields serialize(Cuboid cuboid) {
                Fields fields = new Fields();
                fields.putPrimitive("half-height", cuboid.halfHeight);
                fields.putPrimitive("half-width", cuboid.halfWidth);
                fields.putPrimitive("half-depth", cuboid.halfDepth);
                fields.putObject("center", cuboid.cubeCenter);
                cuboid.serialize(fields);
                return fields;
            }

            @Override
            public Cuboid deserialize(Fields fields) throws StreamCorruptedException {
                double halfHeight = fields.getPrimitive("half-height", double.class);
                double halfWidth = fields.getPrimitive("half-width", double.class);
                double halfDepth = fields.getPrimitive("half-depth", double.class);
                Vector cubeCenter = fields.getObject("center", Vector.class);
                Cuboid cuboid = new Cuboid(halfWidth, halfHeight, halfDepth, cubeCenter);
                Shape.deserialize(fields, cuboid);
                return cuboid;
            }

            @Override
            public void deserialize(Cuboid cuboid, Fields fields) throws StreamCorruptedException, NotSerializableException
            {
                assert false;
            }

            @Override
            public boolean mustSyncDeserialization() {
                return false;
            }

            @Override
            protected boolean canBeInstantiated() {
                return false;
            }

        }));

    }
}
