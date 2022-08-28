package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import com.sovdee.skriptparticle.util.VectorMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public abstract class Shape{
    List<Vector> points;
    private Vector normal;
    private double rotation;
    private boolean isRotated;

    public Shape() {
        this.points = new ArrayList<>();
        this.normal = new Vector(0, 1, 0);
        this.rotation = 0.0;
        this.isRotated = false;
    }

    public abstract List<Vector> generatePoints();

    public abstract Shape clone();

    public List<Vector> getRotatedPoints(List<Vector> points){
        VectorMath.RotationValue rotationValue = VectorMath.getRotationValues(normal);
        for (Vector point : points) {
            point.rotateAroundAxis(rotationValue.cross, rotationValue.angle);
            point.rotateAroundAxis(this.normal, this.rotation);
        }
        // mark shape as rotated
        this.isRotated = true;
        return points;
    }

    public Location[] getLocations(Location center){
        // only recalculate if the shape has been rotated since last calculation
        if (!isRotated) {
            this.points = getRotatedPoints(generatePoints());
        }
        // offset center by vectors to get locations
        Location[] locations = new Location[points.size()];
        for(int i = 0; i < points.size(); i++){
            locations[i] = center.clone().add(points.get(i));
        }
        return locations;
    }

    public List<Vector> getPoints() {
        return points;
    }

    public void setNormal(Vector normal){
        this.normal = normal.normalize();
        // mark shape as needing to be rotated
        this.isRotated = false;
    }

    public Vector getNormal(){
        return normal;
    }

    public void setRotation(double rotation){
        this.rotation = rotation;
        // mark shape as needing to be rotated
        this.isRotated = false;
    }

    public double getRotation(){
        return rotation;
    }

    public String toString(){
        return "Shape with " + points.size() + " points";
    }

    static {
        Classes.registerClass(new ClassInfo<>(Shape.class, "shape")
                .user("shapes?")
                .name("Shape")
                .description("Represents an abstract particle shape. See various shapes for implementations. eg: circle, line, etc.")
                .parser(new Parser<>() {

                    @Override
                    public Circle parse(String input, ParseContext context) {
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
                        return "shape:vector(" + shape.getNormal().getX() + "," + shape.getNormal().getY() + "," + shape.getNormal().getZ() + ")," + shape.getRotation();
                    }
                })
        );

        Converters.registerConverter(Shape.class, Circle.class, (shape) -> {
            if (shape instanceof Circle) {
                return (Circle) shape;
            }
            return null;
        });
    }

}
