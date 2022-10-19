package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.List;

public class Rectangle extends Shape {
    private double halfWidth;
    private double halfLength;

    public Rectangle(double length, double width){
        this.halfWidth = width / 2;
        this.halfLength = length / 2;
    }
    @Override
    public List<Vector> generateOutline() {
        this.points = MathUtil.calculateRectOutline(halfLength, halfWidth, particleDensity);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = MathUtil.calculateRectSurface(halfLength, halfWidth, particleDensity);
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
