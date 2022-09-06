package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import com.sovdee.skriptparticle.particles.CustomParticle;
import com.sovdee.skriptparticle.util.VectorMath;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Shape implements Cloneable {
    protected List<Vector> points;
    private ShapePosition shapePosition;
    private CustomParticle particle;
    private Location center;
    protected boolean needsUpdate;
    private final UUID uuid;

    public Shape() {
        this.points = new ArrayList<>();
        this.shapePosition = new ShapePosition();
        this.needsUpdate = true;
        this.center = null;
        this.uuid = UUID.randomUUID();
    }

    public abstract List<Vector> generatePoints();

    public abstract Shape clone();

    public void updatePoints(){
        if (needsUpdate || !getBackupNormal().equals(getNormal().normalize())) {
            // generate points, then rotate them to the correct orientation. Then offset them to the final correct position.
            // this means rotations are always around the center of the shape, and not the point at which it's drawn.

            // Creating a complex shape will rotate the points around the center of the shape, then offset them,
            // then rotate them around the center of the complex shape.
            points = this.getOffsetPoints(this.getRotatedPoints(this.generatePoints()));
            needsUpdate = false;
            shapePosition.updateBackupNormal();
        }
    }

    public List<Vector> getOffsetPoints(List<Vector> points){
        for (Vector point : points) {
            point.add(getOffset());
        }
        return points;
    }

    public List<Vector> getRotatedPoints(List<Vector> points){
        VectorMath.RotationValue rotationValue = VectorMath.getRotationValues(getNormal());
        for (Vector point : points) {
            point.rotateAroundAxis(rotationValue.cross, rotationValue.angle);
            point.rotateAroundAxis(getNormal(), getRotation());
        }
        return points;
    }

    public List<Location> getLocations(){
        return getLocations(center);
    }
    public List<Location> getLocations(Location center){
        // only recalculate if the shape has been rotated since last calculation
        updatePoints();
        // offset center by vectors to get locations
        List<Location> locations = new ArrayList<>();
        for (Vector point : points) {
            locations.add(center.clone().add(point));
        }
        return locations;
    }

    public List<Vector> getPoints() {
        this.updatePoints();
        return points;
    }

    public void setNormal(Vector normal){
        shapePosition.setNormal(normal.clone().normalize());
        needsUpdate = true;
    }

    public Vector getNormal(){
        return shapePosition.getNormal();
    }

    public Vector getBackupNormal() {
        return shapePosition.getBackupNormal();
    }

    public void setRotation(double rotation){
        shapePosition.setRotation(rotation);
        needsUpdate = true;
    }

    public double getRotation(){
        return shapePosition.getRotation();
    }

    public void setParticle(@Nullable CustomParticle particle) {
        this.particle = particle;
    }

    public CustomParticle getParticle() {
        return particle;
    }

    public boolean needsUpdate() {
        return needsUpdate;
    }

    public void setNeedsUpdate(boolean needsUpdate) {
        this.needsUpdate = needsUpdate;
    }

    public Location getCenter() {
        return center;
    }

    public void setCenter(Location center) {
        this.center = center;
    }

    public UUID getUUID() {
        return uuid;
    }

    public Vector getOffset() {
        return shapePosition.getOffsetVector();
    }

    public void setOffset(Vector offsetVector) {
        shapePosition.setOffsetVector(offsetVector);
        needsUpdate = true;
    }

    public ShapePosition getShapePosition() {
        return shapePosition;
    }

    public void setShapePosition(ShapePosition shapePosition) {
        this.shapePosition = shapePosition;
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
                        return "shape:" + shape.getUUID();
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
