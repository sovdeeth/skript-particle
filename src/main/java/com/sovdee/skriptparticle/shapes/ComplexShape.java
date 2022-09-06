package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.skriptparticle.particles.CustomParticle;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ComplexShape extends Shape {

    private List<Shape> shapes = new ArrayList<>();
    public ComplexShape() {
        super();
    }

    public ComplexShape(ShapePosition position, @Nullable CustomParticle particle, @Nullable Location center) {
        super();
        this.setShapePosition(position);
        setParticle(particle);
        setCenter(center);
    }

    @Override
    public void updatePoints(){
        for (Shape shape : shapes) {
            if (shape.needsUpdate || !shape.getBackupNormal().equals(shape.getNormal().normalize())) {
                this.needsUpdate = true;
                break;
            }
        }

        if (needsUpdate || !getBackupNormal().equals(getNormal().normalize())) {
            // generate points, then rotate them to the correct orientation. Then offset them to the final correct position.
            // this means rotations are always around the center of the shape, and not the point at which it's drawn.

            // Creating a complex shape will rotate the points around the center of the shape, then offset them,
            // then rotate them around the center of the complex shape.
            points = this.getOffsetPoints(this.getRotatedPoints(this.generatePoints()));
            needsUpdate = false;
            getShapePosition().updateBackupNormal();
        }
    }

    @Override
    public List<Vector> generatePoints() {
        List<Vector> points = new ArrayList<>();
        for (Shape shape : shapes) {
            points.addAll(shape.getPoints().stream().map((Vector::clone)).toList());
        }
        return points;
    }

    @Override
    public Shape clone() {
        ComplexShape clone = new ComplexShape(getShapePosition(), getParticle(), getCenter());
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
        return "complex shape with normal " + getNormal() + " and rotation " + getRotation() + " and " + shapes.size() + " shapes (total points: " + getPoints().size() + ").";
    }

    static {
        Classes.registerClass(new ClassInfo<>(ComplexShape.class, "complexshape")
                .user("complexshapes?")
                .name("Complex Shape")
                .description("Represents an abstract complex particle shape. See various shapes for implementations. eg: circle, line, etc.")
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
                        return "complexshape:" + shape.getUUID();
                    }
                })
        );
    }
}
