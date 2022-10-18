package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class Cylinder extends Circle implements RadialShape {
    private double height;

    public Cylinder(double radius, double height) {
        super(radius);
        this.height = height;
    }

    @Override
    public Circle particleCount(int count) {
        particleDensity = switch (style) {
            case OUTLINE -> Math.sqrt(Math.PI * 2 * radius * height / count);
            case SURFACE -> Math.sqrt((Math.PI * 2 * radius * height + 2 * Math.PI * radius * radius)/ count);
            case FILL -> Math.cbrt(Math.PI * radius * radius * height / count);
        };
        return this;
    }

    @Override
    public List<Vector> generateOutline() {
        this.points = new ArrayList<>();
        for (int i = 0; i < height; i += particleDensity) {
            List<Vector> circle = MathUtil.calculateCircle(radius, particleDensity, 2 * Math.PI);
            for (Vector vector : circle) {
                vector.setY(i);
            }
            points.addAll(circle);
        }
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = generateOutline();
        points.addAll(MathUtil.calculateDisc(radius - particleDensity, particleDensity, 2 * Math.PI));
        List<Vector> topDisc = MathUtil.calculateDisc(radius - particleDensity, particleDensity, 2 * Math.PI);
        for (Vector vector : topDisc) {
            vector.setY(height);
        }
        points.addAll(topDisc);
        return points;
    }

    @Override
    public List<Vector> generateFilled() {
        this.points = new ArrayList<>();
        for (int i = 0; i < height; i += particleDensity) {
            List<Vector> disc = MathUtil.calculateDisc(radius, particleDensity, 2 * Math.PI);
            for (Vector vector : disc) {
                vector.setY(i);
            }
            points.addAll(disc);
        }
        return points;
    }

    public double height() {
        return height;
    }

    public Cylinder height(double height) {
        this.height = height;
        return this;
    }

    @Override
    public Shape clone() {
        Cylinder cylinder = new Cylinder(this.radius, this.height);
        this.copyTo(cylinder);
        return cylinder;
    }

    public String toString(){
        return "cylinder with radius " + this.radius + " and height " + this.height;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Cylinder.class, "cylinder")
                .user("cylinders?")
                .name("Circle")
                .description("Represents a cylinder particle shape.")
                .examples("on load:", "\tset {_cylinder} to a cylinder with radius of 2 and height of 3")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Cylinder cylinder) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", cylinder.radius);
                        fields.putPrimitive("height", cylinder.height);
                        cylinder.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Cylinder deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        double height = fields.getPrimitive("height", Double.class);
                        Cylinder cylinder = new Cylinder(radius, height);
                        Shape.deserialize(fields, cylinder);
                        return cylinder;
                    }

                    @Override
                    public void deserialize(Cylinder cylinder, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
