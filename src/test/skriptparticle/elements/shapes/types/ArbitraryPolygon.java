package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.elements.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class ArbitraryPolygon extends Shape {

    private List<Vector> vertices;
    private double height;

    // assume all vertices are on the same plane, y = 0
    public ArbitraryPolygon(List<Vector> vertices, double height) {
        super();
        this.vertices = vertices;
        this.height = height;
    }

    @Override
    public List<org.bukkit.util.Vector> generateOutline() {
        Set<Vector> points = new HashSet<>(MathUtil.connectPoints(vertices, particleDensity));
        points.addAll(MathUtil.calculateLine(vertices.get(0), vertices.get(vertices.size() - 1), particleDensity));
        Set<Vector> upperPoints = new HashSet<>();
        for (Vector v : points) {
            upperPoints.add(new Vector(v.getX(), height, v.getZ()));
        }
        points.addAll(upperPoints);
        for (Vector v : vertices) {
            points.addAll(MathUtil.calculateLine(v, new Vector(v.getX(), height, v.getZ()), particleDensity));
        }

        return points.stream().toList();
    }

    @Override
    public Shape particleCount(int count) {
        double perimeter = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            perimeter += vertices.get(i).distance(vertices.get(i + 1));
        }
        perimeter += vertices.get(0).distance(vertices.get(vertices.size() - 1));
        perimeter *= 2;
        perimeter += vertices.size() * height;
        particleDensity = perimeter / count;
        points = generatePoints();
        return this;
    }

    @Override
    public Shape clone() {
        ArbitraryPolygon clone = new ArbitraryPolygon(vertices, height);
        this.copyTo(clone);
        return clone;
    }
    static {
        Classes.registerClass(new ClassInfo<>(ArbitraryPolygon.class, "arbitrarypolygon")
                .user("arbitrarypolygons?")
                .name("ArbitraryPolygon")
                .description("Represents an arbitrary polygon particle shape.")
                .examples("on load:", "\tset {_arbitrary-polygon} to an arbitrary polygon from points {_points::*}")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(ArbitraryPolygon polygon) {
                        Fields fields = new Fields();
                        fields.putObject("vertices", polygon.vertices);
                        fields.putPrimitive("height", polygon.height);
                        polygon.serialize(fields);
                        return fields;
                    }

                    @Override
                    public ArbitraryPolygon deserialize(Fields fields) throws StreamCorruptedException {
                        List<Vector> vertices = (List<Vector>) fields.getObject("vertices", List.class);
                        double height = fields.getPrimitive("height", double.class);
                        ArbitraryPolygon polygon = new ArbitraryPolygon(vertices, height);
                        Shape.deserialize(fields, polygon);
                        return polygon;
                    }

                    @Override
                    public void deserialize(ArbitraryPolygon polygon, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
