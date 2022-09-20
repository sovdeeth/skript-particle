package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class ComplexShape extends Shape {

    private List<Shape> shapes = new ArrayList<>();
    public ComplexShape() {
        super();
    }

    @Override
    public List<Vector> calculatePoints(){
        // necessary to calculate sub-shape points for offset/scale
        points = generatePoints();
        // ensure orientation is up-to-date
        orientPoints();
        ArrayList<Vector> positionedPoints = new ArrayList<>();
        for (Vector point : points) {
            positionedPoints.add(point.clone().multiply(scale).add(offset));
        }
        return positionedPoints;
    }

    @Override
    public List<Vector> generatePoints() {
        List<Vector> points = new ArrayList<>();
        for (Shape shape : shapes) {
            for (Vector point : shape.calculatePoints()) {
                points.add(point.clone());
            }
        }
        return points;
    }

    @Override
    public Shape clone() {
        ComplexShape clone = new ComplexShape();
        this.copyTo(clone);
        for (Shape shape : shapes) {
            clone.shapes.add(shape.clone());
        }
        return clone;
    }

    public List<Shape> getShapes() {
        return shapes;
    }

    public void addShapes(Shape... shapes) {
        this.shapes.addAll(List.of(shapes));
    }

    public void removeShapes(Shape... shapes) {
        this.shapes.removeAll(List.of(shapes));
    }

    public void setShapes(Shape... shapes) {
        this.shapes = List.of(shapes);
    }

    public String toString() {
        return "complex shape with " + shapes.size() + " shapes (total points: " + points.size() + ").";
    }

    static {
        Classes.registerClass(new ClassInfo<>(ComplexShape.class, "complexshape")
                .user("complexshapes?")
                .name("Complex Shape")
                .description("Represents a complex particle shape. Consists of multiple shapes.")
                .parser(new Parser<>() {

                    @Override
                    public ComplexShape parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(ComplexShape o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(ComplexShape shape) {
                        return "complexshape:" + shape.uuid();
                    }
                })
        );
    }
}
