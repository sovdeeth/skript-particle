package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.elements.shapes.Shape;
import com.sovdee.skriptparticles.shapes.RadialShape;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class Helix extends Shape implements RadialShape {

    private double radius;
    private double height;
    private double slope;
    private int direction = 1;

    public Helix(double radius, double height, double slope) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
    }

    public Helix(double radius, double height, double slope, int direction) {
        super();
        this.radius = radius;
        this.height = height;
        this.slope = slope;
        this.direction = direction;
    }

    @Override
    public List<Vector> generateOutline() {
        points = new ArrayList<>();
        calculateHelix(radius);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        points = new ArrayList<>();
        for (double r = radius; r > 0; r -= particleDensity){
            calculateHelix(r);
        }
        return points;
    }

    private void calculateHelix(double radius) {
        if (radius <= 0 || height <= 0) {
            return;
        }
        double loops = Math.abs(height / slope);
        double length = slope * slope + radius * radius;
        double stepSize = particleDensity / length;
        for (double t = 0; t < loops; t += stepSize) {
            double x = radius * Math.cos(direction * t);
            double z = radius * Math.sin(direction * t);
            points.add(new Vector(x, t*slope, z));
        }
    }

    @Override
    public Shape particleCount(int count) {
        particleDensity = switch (style) {
            case OUTLINE -> (Math.sqrt(slope * slope + radius * radius) * (height / slope) / count);
            case FILL, SURFACE -> Math.sqrt(slope * slope + radius * radius * (height / slope) / count);
        };
        points = generatePoints();
        return this;
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public Shape radius(double radius) {
        this.radius = radius;
        return this;
    }

    public double height() {
        return height;
    }

    public Shape height(double height) {
        this.height = height;
        return this;
    }

    public double slope() {
        return slope;
    }

    public Shape slope(double slope) {
        this.slope = slope;
        return this;
    }

    public int rotation() {
        return direction;
    }

    public Shape rotation(int rotation) {
        this.direction = rotation;
        return this;
    }

    @Override
    public Shape clone() {
        Helix clone = new Helix(radius, height, slope, direction);
        this.copyTo(clone);
        return clone;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Helix.class, "helix")
                .user("heli(x|ce)s?")
                .name("Helix")
                .description("An ellipsoid shape.")
                .examples("ellipsoid")
                .serializer(new Serializer<>() {
                    @Override
                    public Fields serialize(Helix helix) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", helix.radius);
                        fields.putPrimitive("height", helix.height);
                        fields.putPrimitive("slope", helix.slope);
                        fields.putPrimitive("direction", helix.direction);
                        helix.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Helix deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        double height = fields.getPrimitive("height", Double.class);
                        double slope = fields.getPrimitive("slope", Double.class);
                        int direction = fields.getPrimitive("direction", Integer.class);
                        Helix helix = new Helix(radius, height, slope, direction);
                        Shape.deserialize(fields, helix);
                        return helix;
                    }

                    @Override
                    public void deserialize(Helix helix, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
