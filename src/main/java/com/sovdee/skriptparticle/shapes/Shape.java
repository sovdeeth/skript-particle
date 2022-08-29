package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import com.sovdee.skriptparticle.util.VectorMath;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public abstract class Shape{
    List<Vector> points;
    private Vector normal;
    private Vector backupNormal;
    private double rotation;
    private Particle particle;

    protected boolean needsUpdate;

    public Shape() {
        this.points = new ArrayList<>();
        this.normal = new Vector(0, 1, 0);
        this.backupNormal = normal.clone();
        this.rotation = 0.0;
        this.needsUpdate = true;
    }

    public abstract List<Vector> generatePoints();

    public abstract Shape clone();

    public void updatePoints(){
        if (needsUpdate || !backupNormal.equals(normal.normalize())) {
            points = this.getRotatedPoints(this.generatePoints());
            needsUpdate = false;
            backupNormal = normal.clone();
        }
    }

    public List<Vector> getRotatedPoints(List<Vector> points){
        VectorMath.RotationValue rotationValue = VectorMath.getRotationValues(normal);
        for (Vector point : points) {
            point.rotateAroundAxis(rotationValue.cross, rotationValue.angle);
            point.rotateAroundAxis(normal, rotation);
        }
        return points;
    }

    public Location[] getLocations(Location center){
        // only recalculate if the shape has been rotated since last calculation
        updatePoints();
        // offset center by vectors to get locations
        Location[] locations = new Location[points.size()];
        for(int i = 0; i < points.size(); i++){
            locations[i] = center.clone().add(points.get(i));
        }
        return locations;
    }

    public List<Vector> getPoints() {
        this.updatePoints();
        return points;
    }

    public void setNormal(Vector normal){
        this.normal = normal.clone().normalize();
        needsUpdate = true;
    }

    public Vector getNormal(){
        return normal;
    }

    public Vector getBackupNormal() {
        return backupNormal;
    }

    public void setRotation(double rotation){
        this.rotation = rotation;
        needsUpdate = true;
    }

    public double getRotation(){
        return rotation;
    }

    public void setParticle(@Nullable Particle particle) {
        this.particle = particle;
    }

    public Particle getParticle() {
        return particle;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public String toString(){
        return "Shape with " + getPoints().size() + " points";
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
                        return "shape:" + shape.getParticle() + ",vector(" + shape.getNormal().getX() + "," + shape.getNormal().getY() + "," + shape.getNormal().getZ() + ")," + shape.getRotation();
                    }
                })
        );

        Converters.registerConverter(Shape.class, Circle.class, (shape) -> {
            if (shape instanceof Circle) {
                return (Circle) shape;
            }
            return null;
        });

        Converters.registerConverter(Shape.class, Line.class, (shape) -> {
            if (shape instanceof Line) {
                return (Line) shape;
            }
            return null;
        });

        Converters.registerConverter(Shape.class, ComplexShape.class, (shape) -> {
            if (shape instanceof ComplexShape) {
                return (ComplexShape) shape;
            }
            return null;
        });
    }
}
