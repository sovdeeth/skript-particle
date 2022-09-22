package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComplexShape extends Shape {

    private List<Shape> shapes = new ArrayList<>();
    private List<Line> axes = new ArrayList<>();
    public ComplexShape() {
        super();
    }

    @Override
    public List<Vector> generatePoints() {
        List<Vector> points = new ArrayList<>();
        for (Shape shape : shapes) {
            points.addAll(shape.positionedPoints());
        }
        return points;
    }

    @Override
    public void draw(Location location, ParticleBuilder particle, @Nullable Shape... parents) {
        List<Shape> newParents = new ArrayList<>();
        if (parents != null) {
            newParents.addAll(List.of(parents));
        }
        newParents.add(this);
        // override particles if necessary
        if (particle.equals(this.particle)){
            for (Shape shape : shapes) {
                shape.draw(location, newParents.toArray(new Shape[0]));
            }
        } else {
            for (Shape shape : shapes) {
                shape.draw(location, particle, newParents.toArray(new Shape[0]));
            }
        }
        if (showLocalAxes) {
            drawLocalAxes(location, parents);
        }
        if (showGlobalAxes) {
            drawGlobalAxes(location, parents);
        }
    }

    @Override
    public List<Location> locations(Location center) {
        return super.locations(center);
    }

    @Override
    public int particleCount() {
        int count = 0;
        for (Shape shape : shapes) {
            count += shape.particleCount();
        }
        return count;
    }

    @Override
    public Shape particleCount(int count) {
        return this;
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
