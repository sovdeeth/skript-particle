package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class Rectangle extends Shape {
    private double halfWidth;
    private double halfLength;

    private double xStep = 1.0;
    private double zStep = 1.0;

    public Rectangle(double length, double width){
        super();
        this.halfWidth = width / 2;
        this.halfLength = length / 2;
        calculateSteps();
    }


    private void calculateSteps() {
        xStep = 2 * halfWidth / Math.round(2 * halfWidth / particleDensity);
        zStep = 2 * halfLength / Math.round(2 * halfLength / particleDensity);
    }
    @Override
    public List<Vector> generateOutline() {
        this.points = new ArrayList<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            points.add(new Vector(x, 0, -halfLength));
            points.add(new Vector(x, 0, halfLength));
        }
        for (double z = -halfLength + zStep; z < halfLength; z += zStep) {
            points.add(new Vector(-halfWidth, 0, z));
            points.add(new Vector(halfWidth, 0, z));
        }
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = new ArrayList<>();
        for (double x = -halfWidth; x <= halfWidth; x += xStep) {
            for (double z = -halfLength; z <= halfLength; z += zStep) {
                points.add(new Vector(x, 0, z));
            }
        }
        return points;
    }

    @Override
    public Shape particleCount(int count) {
        switch (style){
            case FILL,SURFACE -> particleDensity = Math.sqrt(4 * halfWidth * halfLength / count);
            case OUTLINE -> particleDensity = 4 * (halfWidth + halfLength) / count;
        };
        return this;
    }

    @Override
    public Shape clone() {
        Rectangle rect = new Rectangle(this.halfLength * 2, this.halfWidth * 2);
        this.copyTo(rect);
        return rect;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Rectangle.class, "rectangle")
                .user("rectangles?")
                .name("Rectangle")
                .description("Represents a rectangle particle shape.")
                .examples("on load:", "\tset {_rectangle} to a rectangle with length of 2 and width of 3")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Rectangle rectangle) {
                        Fields fields = new Fields();
                        fields.putPrimitive("length", rectangle.halfLength);
                        fields.putPrimitive("width", rectangle.halfWidth);
                        rectangle.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Rectangle deserialize(Fields fields) throws StreamCorruptedException {
                        double length = fields.getPrimitive("length", Double.class);
                        double width = fields.getPrimitive("width", Double.class);
                        Rectangle rectangle = new Rectangle(length, width);
                        Shape.deserialize(fields, rectangle);
                        return rectangle;
                    }

                    @Override
                    public void deserialize(Rectangle rectangle, Fields fields) {
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
