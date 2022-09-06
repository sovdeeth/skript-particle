package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class Circle extends Shape {

    private double radius;
    private double stepSize;
    public Circle (double radius){
        super();
        this.radius = radius;
        // default to 30 points per circle
        this.stepSize = Math.PI * 2 / 30;
        this.points = getRotatedPoints(generatePoints());
    }

    public Circle (double radius, double stepSize){
        super();
        this.radius = radius;
        this.stepSize = stepSize;
        this.points = getPoints();
    }

    public Circle (double radius, double stepSize, ShapePosition position){
        super();
        this.radius = radius;
        this.stepSize = stepSize;
        this.setShapePosition(position);
        this.points = getPoints();
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
        needsUpdate = true;
    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
        needsUpdate = true;
    }

    @Override
    public List<Vector> generatePoints() {
        this.points = new ArrayList<>();
        // simple flat circle, rotation is handled by the Shape class
        for (double theta = 0; theta < 2 * Math.PI; theta += this.stepSize) {
            points.add(new Vector(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    @Override
    public Shape clone() {
        Circle circle = new Circle(this.radius, this.stepSize, this.getShapePosition().clone());
        return circle;
    }

    public String toString(){
        return "Circle with radius " + this.radius + " and stepSize " + this.stepSize + " and normal " + this.getNormal() + " and rotation " + this.getRotation();
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
                        fields.putPrimitive("radius", circle.getRadius());
                        fields.putPrimitive("stepSize", circle.getStepSize());
                        circle.getShapePosition().serialize(fields);
                        return fields;
                    }

                    @Override
                    public Circle deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        double stepSize = fields.getPrimitive("stepSize", Double.class);
                        ShapePosition shapePosition = ShapePosition.deserialize(fields);
                        return new Circle(radius, stepSize, shapePosition);
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

        Converters.registerConverter(Circle.class, Shape.class, (circle) -> circle);

    }


}
