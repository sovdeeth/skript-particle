package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.elements.particles.ParticleGradient;
import com.sovdee.skriptparticle.util.ParticleUtil;
import com.sovdee.skriptparticle.util.Quaternion;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Shape implements Cloneable {
    protected List<Vector> points;
    protected Style style;
    private Style previousStyle;
    protected Quaternion orientation;
    private Quaternion previousOrientation;
    protected double scale;
    protected Vector offset;
    protected Location center;
    private final UUID uuid;
    protected ParticleBuilder particle = ParticleUtil.DEFAULT_PB;
    protected double particleDensity = 0.35;
    public boolean showLocalAxes = false;
    public boolean showGlobalAxes = false;
    protected static final List<Line> globalAxes = new ArrayList<>();

    public Shape() {
        this.style = Style.OUTLINE;
        this.points = new ArrayList<>();
        
        this.orientation = new Quaternion(1, 0, 0, 0);
        this.previousOrientation = new Quaternion(1, 0, 0, 0);
        
        this.scale = 1;
        this.offset = new Vector(0, 0, 0);
        this.center = null;
        
        this.uuid = UUID.randomUUID();
    }

    public void draw(Location location) {
        draw(location, Quaternion.IDENTITY, particle);
    }

    public void draw(Location location, Quaternion orientation) {
        draw(location, orientation, particle);
    }

    public void draw(Location location, ParticleBuilder particle) {
        draw(location, Quaternion.IDENTITY, particle);
    }

    public void draw(Location location, Quaternion parentOrientation, ParticleBuilder particle) {
        if (this.points.isEmpty()) this.points = generatePoints();
        // get local point positions
        List<Vector> points = positionedPoints(parentOrientation);

        // update this object's particle if it's a gradient
        if (this.particle instanceof ParticleGradient) {
            // set orientation (basically re-orients back to world space, for easier calculation)
            ((ParticleGradient) this.particle).orientation(parentOrientation.clone().multiply(orientation).conjugate());
            // set origin
            ((ParticleGradient) this.particle).origin(location.clone().add(parentOrientation.transform(offset)));
        }

        // draw debug axes
        if (showLocalAxes) {
            drawLocalAxes(location, parentOrientation);
        }
        if (showGlobalAxes) {
            drawGlobalAxes(location);
        }

        // draw points
        for (Vector point : points) {
            particle.location(location.clone().add(point)).spawn();
        }
    }



    public List<Vector> generatePoints(){
        this.previousStyle = this.style;
        this.previousOrientation = Quaternion.IDENTITY;
        return switch (style) {
            case OUTLINE -> generateOutline();
            case SURFACE -> generateSurface();
            case FILL -> generateFilled();
        };
    }

    public abstract List<Vector> generateOutline();

    public List<Vector> generateSurface(){
        return generateOutline();
    };

    public List<Vector> generateFilled(){
        return generateSurface();
    };

    public List<Vector> positionedPoints(Quaternion parentOrientation) {
        // ensure points exist
        if (points == null || points.isEmpty() || previousStyle != style)
            points = generatePoints();

        // ensure points are up-to-date
        orientPoints(parentOrientation);

        // scale and offset points
        ArrayList<Vector> newPoints = new ArrayList<>();
        Vector transformedOffset = parentOrientation.transform(offset.clone());
        for (Vector point : points)
            newPoints.add(point.clone().multiply(scale).add(transformedOffset));

        return newPoints;
    }

    public Shape orientPoints(Quaternion parentOrientation) {
        Quaternion fullOrientation = parentOrientation.clone().multiply(orientation);
        if (fullOrientation.equals(previousOrientation)) {
            return this;
        }
        Quaternion rotation = fullOrientation.clone().multiply(previousOrientation.conjugate());
        for (Vector point : points) {
            rotation.transform(point);
        }
        this.previousOrientation = fullOrientation.clone();
        return this;
    }

    public List<Location> locations(){ return locations(this.center, Quaternion.IDENTITY); }

    public List<Location> locations(Location center, Quaternion orientation){
        ArrayList<Location> locations = new ArrayList<>();
        for (Vector point : positionedPoints(orientation)) {
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

    public int particleCount(){
        return points.size();
    };
    public abstract Shape particleCount(int count);



    public Shape resetOrientation() {
        this.orientation.set(1, 0, 0, 0);
        this.previousOrientation.set(1, 0, 0, 0);
        this.points = positionedPoints(Quaternion.IDENTITY);
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

    public Style style() {
        return style;
    }

    public Shape style(Style style) {
        this.style = style;
        return this;
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
        this.particle = particle != null ? particle : ParticleUtil.DEFAULT_PB;
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

//        Converters.registerConverter(Shape.class, Circle.class, (shape) -> {
//            if (shape instanceof Circle) {
//                return (Circle) shape;
//            }
//            return null;
//        });
//
//        Converters.registerConverter(Shape.class, Line.class, (shape) -> {
//            if (shape instanceof Line) {
//                return (Line) shape;
//            }
//            return null;
//        });
//
//        Converters.registerConverter(Shape.class, ComplexShape.class, (shape) -> {
//            if (shape instanceof ComplexShape) {
//                return (ComplexShape) shape;
//            }
//            return null;
//        });
    }

    public void drawLocalAxes(Location location, Quaternion parentOrientation) {
        new Line(relativeXAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.RED, 0.5F)))
                .offset(offset)
                .draw(location, parentOrientation);
        new Line(relativeYAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.LIME, 0.5F)))
                .offset(offset)
                .draw(location, parentOrientation);
        new Line(relativeZAxis())
                .particle(new ParticleBuilder(Particle.REDSTONE).data(new Particle.DustOptions(Color.AQUA, 0.5F)))
                .offset(offset)
                .draw(location, parentOrientation);
    }

    public void drawGlobalAxes(Location location) {
        for (Line axis : globalAxes) {
            axis.clone().draw(location);
        }
    }
}
