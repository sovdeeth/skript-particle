package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Shape implements Cloneable {
    protected List<Vector> points;
    protected Quaternion orientation;
    private Quaternion previousOrientation;
    protected double scale;
    protected Vector offset;
    protected Location center;
    private final UUID uuid;
    protected ParticleBuilder particle = new ParticleBuilder(Particle.FLAME).count(1).extra(0);
    protected double particleDensity = 0.1; // 1 particle per 0.01 meters^2, approximately

    /*
    * CHANGE TO BUILDER BASED SYSTEM
     */
    public Shape() {
        this.points = new ArrayList<>();
        
        this.orientation = new Quaternion(1, 0, 0, 0);
        this.previousOrientation = new Quaternion(1, 0, 0, 0);
        
        this.scale = 1;
        this.offset = new Vector(0, 0, 0);
        this.center = new Location(null, 0, 0, 0);
        
        this.uuid = UUID.randomUUID();
    }


    public abstract List<Vector> generatePoints();

    public Shape particleDensity(double count){
        this.particleDensity = count;
        return this;
    }
    public double particleDensity() {
        return particleDensity;
    }

    public abstract int particleCount();
    public abstract Shape particleCount(int count);

    public Shape orientPoints() {
        if (orientation.equals(previousOrientation)) {
            return this;
        }
        Quaternion rotation = orientation.clone().multiply(previousOrientation.conjugate());
        for (Vector point : points) {
            rotation.transform(point);
        }
        this.previousOrientation = this.orientation.clone();
        return this;
    }

    public Shape resetOrientation() {
        this.orientation.set(1, 0, 0, 0);
        this.previousOrientation.set(1, 0, 0, 0);
        this.points = calculatePoints();
        return this;
    }

    public abstract Shape clone();
    public Shape copyTo(Shape shape){
        shape.orientation(this.orientation)
                .previousOrientation(this.previousOrientation)
                .scale(this.scale)
                .offset(this.offset)
                .center(this.center)
                .particleDensity(this.particleDensity);
        return shape;
    };

    public List<Location> locations(){ return locations(this.center); }

    public List<Location> locations(Location center){
        ArrayList<Location> locations = new ArrayList<>();
        for (Vector point : calculatePoints()) {
            locations.add(center.clone().add(point));
        }
        return locations;
    }

    public List<Vector> calculatePoints() {
        // ensure points exist
        if (points == null || points.isEmpty()) {
            points = generatePoints();
        }
        // ensure points are up-to-date
        orientPoints();
        ArrayList<Vector> positionedPoints = new ArrayList<>();
        for (Vector point : points) {
            positionedPoints.add(point.clone().multiply(scale).add(offset));
        }
        return positionedPoints;
    }

    public Quaternion orientation() {
        return orientation;
    }

    public Shape orientation(Quaternion orientation) {
        this.orientation.set(orientation);
        return this;
    }

    public Quaternion previousOrientation() {
        return previousOrientation;
    }

    public Shape previousOrientation(Quaternion previousOrientation) {
        this.previousOrientation = previousOrientation;
        return this;
    }

    public Vector relativeYAxis() {
        return orientation().transform(new Vector(0, 1, 0)).normalize();
    }

    public Vector relativeXAxis() {
        return orientation().transform(new Vector(1, 0, 0));
    }

    public Vector relativeZAxis() {
        return orientation().transform(new Vector(0, 0, 1));
    }

    public double scale() {
        return scale;
    }

    public Shape scale(double scale) {
        this.scale = scale;
        return this;
    }

    public Vector offset() {
        return offset;
    }

    public Shape offset(Vector offset) {
        this.offset = offset;
        return this;
    }

    public Location center() {
        return center;
    }

    public Shape center(Location center) {
        this.center = center;
        return this;
    }

    public UUID uuid() {
        return uuid;
    }

    public ParticleBuilder particle() {
        return particle;
    }

    public Shape particle(ParticleBuilder particle) {
        this.particle = particle;
        return this;
    }


    public void serialize(Fields fields) {
        orientation.serialize(fields, "o");
        previousOrientation.serialize(fields, "po");
        fields.putPrimitive("scale", scale);
        fields.putObject("offset", offset);
        fields.putObject("center", center);
        fields.putPrimitive("density", particleDensity);
    }

    public static Shape deserialize(Fields fields, Shape shape) throws StreamCorruptedException {
        shape.orientation = Quaternion.deserialize(fields, "o");
        shape.previousOrientation = Quaternion.deserialize(fields, "po");
        shape.scale = fields.getPrimitive("scale", double.class);
        shape.offset = fields.getObject("offset", Vector.class);
        shape.center = fields.getObject("center", Location.class);
        shape.particleDensity = fields.getPrimitive("density", double.class);
        return shape;
    }




    static {
        Classes.registerClass(new ClassInfo<>(Shape.class, "shape")
                .user("shapes?")
                .name("Shape")
                .description("Represents an abstract particle shape. See various shapes for implementations. eg: circle, line, etc.")
                .parser(new Parser<>() {

                    @Override
                    public Shape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(Shape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(Shape shape) {
                        return "shape:" + shape.uuid();
                    }
                })
        );

        Converters.registerConverter(Shape.class, Circle.class, (shape) -> {
            if (shape instanceof Circle) {
                return (Circle) shape;
            }
            return null;
        });

        Converters.registerConverter(Shape.class, Line.class, (shape) -> {
            if (shape instanceof Line) {
                return (Line) shape;
            }
            return null;
        });

        Converters.registerConverter(Shape.class, ComplexShape.class, (shape) -> {
            if (shape instanceof ComplexShape) {
                return (ComplexShape) shape;
            }
            return null;
        });
    }
}
