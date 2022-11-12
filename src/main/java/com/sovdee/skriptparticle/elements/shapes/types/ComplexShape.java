package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.elements.particles.ParticleGradient;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.util.Vector;

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
            points.addAll(shape.positionedPoints(Quaternion.IDENTITY));
        }
        return points;
    }

    @Override
    public List<Vector> generateOutline() {
        return generatePoints();
    }

    @Override
    public void draw(Location location, Quaternion parentOrientation, ParticleBuilder particle) {
        Quaternion newOrientation = parentOrientation.clone().multiply(orientation);
        Vector newOffset = parentOrientation.transform(offset.clone());
        Location newLocation = location.clone().add(newOffset);

        // update this object's particle if it's a gradient
        if (this.particle instanceof ParticleGradient) {
            // set orientation (basically re-orients back to world space, for easier calculation)
            ((ParticleGradient) this.particle).orientation(newOrientation.clone().conjugate());
            // set origin
            ((ParticleGradient) this.particle).origin(newLocation.clone());
        }

        // override particles if necessary.
        // if the particle is not being overwritten, and it's not a gradient, we'll leave as is.
        // if it is overwritten or a gradient, though, we'll force its use for all sub-shapes.
        if (particle.equals(this.particle) && !(this.particle instanceof ParticleGradient)){
            for (Shape shape : shapes) {
                shape.draw(newLocation, newOrientation);
            }
        } else {
            for (Shape shape : shapes) {
                shape.draw(newLocation, newOrientation, particle);
            }
        }

        // draw debug axes
        if (showLocalAxes) {
            drawLocalAxes(location, parentOrientation);
        }
        if (showGlobalAxes) {
            drawGlobalAxes(location);
        }
    }

    @Override
    public List<Location> locations(Location center, Quaternion orientation) {
        return super.locations(center, orientation);
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
        return "complex shape with " + shapes.size() + " shapes (total points: " + this.particleCount() + ").";
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
