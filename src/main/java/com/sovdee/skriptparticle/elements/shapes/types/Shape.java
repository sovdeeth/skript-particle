package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.util.ParticleUtil;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
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
    protected ParticleBuilder particle = ParticleUtil.DEFAULT_PB;
    protected double particleDensity = 0.1; // 1 particle per 0.01 meters^2, approximately
    public boolean showLocalAxes = false;
    public boolean showGlobalAxes = false;
    protected static final List<Line> globalAxes = new ArrayList<>();

    public Shape() {
        this.points = new ArrayList<>();
        
        this.orientation = new Quaternion(1, 0, 0, 0);
        this.previousOrientation = new Quaternion(1, 0, 0, 0);
        
        this.scale = 1;
        this.offset = new Vector(0, 0, 0);
        this.center = null;
        
        this.uuid = UUID.randomUUID();
    }

    public void draw(Location location, @Nullable Shape... parents) {
        draw(location, particle, parents);
    }
    public void draw(Location location, ParticleBuilder particle, @Nullable Shape... parents) {
        List<Vector> points = positionedPoints();
        // rotate, scale, and offset points by parents' values recursively (from most immediate parent to least)
        if (parents != null) {
            for (int i = parents.length - 1; i >= 0; i--) {
                for (Vector point : points) {
                    parents[i].orientation().transform(point);
                    point.multiply(parents[i].scale()).add(parents[i].offset());
                }
            }
        }
        if (showLocalAxes) {
            drawLocalAxes(location, parents);
        }
        if (showGlobalAxes) {
            drawGlobalAxes(location, parents);
        }
        // draw points
        for (Vector point : points) {
            particle.location(location.clone().add(point)).spawn();
        }
    }



    public abstract List<Vector> generatePoints();

    public List<Vector> positionedPoints() {
        // ensure points exist
        if (points == null || points.isEmpty())
            points = generatePoints();

        // ensure points are up-to-date
        orientPoints();

        // scale and offset points
        ArrayList<Vector> newPoints = new ArrayList<>();
        for (Vector point : points)
            newPoints.add(point.clone().multiply(scale).add(offset));

        return newPoints;
    }

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

    public List<Location> locations(){ return locations(this.center); }

    public List<Location> locations(Location center){
        ArrayList<Location> locations = new ArrayList<>();
        for (Vector point : positionedPoints()) {
            locations.add(center.clone().add(point));
        }
        return locations;
    }

    public Shape particleDensity(double count){
        this.particleDensity = count;
        return this;
    }
    public double particleDensity() {
        return particleDensity;
    }

    public abstract int particleCount();
    public abstract Shape particleCount(int count);



    public Shape resetOrientation() {
        this.orientation.set(1, 0, 0, 0);
        this.previousOrientation.set(1, 0, 0, 0);
        this.points = positionedPoints();
        return this;
    }

    public abstract Shape clone();
    public Shape copyTo(Shape shape){
        shape.orientation(this.orientation)
                .previousOrientation(this.previousOrientation)
                .scale(this.scale)
                .offset(this.offset)
                .center(this.center)
                .particle(this.particle)
                .particleDensity(this.particleDensity);
        return shape;
    };





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

    public void drawLocalAxes(Location location, Shape... parents) {
        new Line(relativeXAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.RED, 0.5F)))
                .offset(offset)
                .draw(location, parents);
        new Line(relativeYAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.LIME, 0.5F)))
                .offset(offset)
                .draw(location, parents);
        new Line(relativeZAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.AQUA, 0.5F)))
                .offset(offset)
                .draw(location, parents);
    }

    public void drawGlobalAxes(Location location, Shape... parents) {
        Vector v = offset.clone();
        // rotate, scale, and offset points by parents' values recursively (from most immediate parent to least)
        if (parents != null) {
            for (int i = parents.length - 1; i >= 0; i--) {
                parents[i].orientation().transform(v);
                v.multiply(parents[i].scale()).add(parents[i].offset());
            }
        }

        for (Line axis : globalAxes) {
            axis.clone().draw(location.clone().add(v));
        }
    }
}
