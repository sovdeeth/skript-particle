package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.List;

public class Circle extends Shape implements RadialShape {

    protected double radius;

    public Circle (){
        super();
        this.radius = 1;
    }

    public Circle (double radius){
        super();
        this.radius = radius;
    }

    public double radius() {
        return radius;
    }

    public Circle radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public Circle particleCount(int count) {
        particleDensity = switch (style) {
            case OUTLINE -> Math.PI * 2 * radius / count;
            case SURFACE,FILL -> Math.sqrt(Math.PI * radius * radius / count);
        };
        points = generatePoints();
        return this;
    }

    @Override
    public List<Vector> generateOutline() {
        this.points = MathUtil.calculateCircle(this.radius, this.particleDensity, 2*Math.PI);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = MathUtil.calculateDisc(this.radius, this.particleDensity, 2*Math.PI);
        return points;
    }

    @Override
    public Shape clone() {
        Circle circle = new Circle(this.radius);
        this.copyTo(circle);
        return circle;
    }

    public String toString(){
        return "Circle with radius " + this.radius;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Circle.class, "circle")
                .user("circles?")
                .name("Circle")
                .description("Represents a circle particle shape.")
                .examples("on load:", "\tset {_circle} to a circle with radius of 2")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Circle circle) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", circle.radius);
                        circle.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Circle deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        Circle circle = new Circle(radius);
                        Shape.deserialize(fields, circle);
                        return circle;
                    }

                    @Override
                    public void deserialize(Circle circle, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
