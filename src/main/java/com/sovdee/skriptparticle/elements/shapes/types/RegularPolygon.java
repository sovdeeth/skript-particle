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

public class RegularPolygon extends Shape implements RadialShape{
    protected double sideLength;
    protected int sides;
    protected double radius;
    protected double angle;

    public RegularPolygon(int sides, double sideLength) {
        super();
        this.sides = sides;
        this.sideLength = sideLength;
        this.angle = 2*Math.PI/sides;
        this.radius = sideLength/(2*Math.sin(angle/2));
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

    @Override
    public List<Vector> generateOutline() {
        this.points = MathUtil.calculateRegularPolygon(radius, angle, particleDensity);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = new ArrayList<>();
        for (double subRadius = radius; subRadius > 0; subRadius -= particleDensity) {
            this.points.addAll(MathUtil.calculateRegularPolygon(subRadius, angle, particleDensity));
        }
        return points;
    }

    @Override
    public Shape particleCount(int count) {
        particleDensity = switch (style){
            case OUTLINE -> sideLength * sides / count;
            case SURFACE,FILL -> Math.sqrt(sides * radius * radius * Math.sin(angle) / 2 / count);
        };
        return this;
    }

    @Override
    public Shape clone() {
        RegularPolygon regularPolygon = new RegularPolygon(sides, sideLength);
        this.copyTo(regularPolygon);
        return regularPolygon;
    }

    static {
        Classes.registerClass(new ClassInfo<>(RegularPolygon.class, "regularpolygon")
                .user("regularpolygons?")
                .name("Regular Polygon")
                .description("Represents a regular polygon particle shape.")
                .examples("on load:", "\tset {_rp} to a regular polygon with 4 sides and side length of 2")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(RegularPolygon polygon) {
                        Fields fields = new Fields();
                        fields.putPrimitive("sides", polygon.sides);
                        fields.putPrimitive("sideLength", polygon.sideLength);
                        polygon.serialize(fields);
                        return fields;
                    }

                    @Override
                    public RegularPolygon deserialize(Fields fields) throws StreamCorruptedException {
                        int sides = fields.getPrimitive("sides", int.class);
                        double sideLength = fields.getPrimitive("sideLength", double.class);
                        RegularPolygon polygon = new RegularPolygon(sides, sideLength);
                        Shape.deserialize(fields, polygon);
                        return polygon;
                    }

                    @Override
                    public void deserialize(RegularPolygon polygon, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
