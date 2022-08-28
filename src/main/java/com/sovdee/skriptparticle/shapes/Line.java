package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;

public class Line extends Shape {

    private Vector start;
    private Vector end;
    private double stepSize = 0.25;

    private Location startLocation;

    public Line(Vector start, Vector end) {
        super();
        this.start = start;
        this.end = end;
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Vector start, Vector end, double stepSize) {
        super();
        this.start = start;
        this.end = end;
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Location startLoc, Location endLoc) {
        super();
        setStartLocation(startLoc);
        this.end = endLoc.toVector().subtract(startLoc.toVector());
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Location startLoc, Location endLoc, double stepSize) {
        super();
        setStartLocation(startLoc);
        this.end = endLoc.toVector().subtract(startLoc.toVector());
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Vector direction, double length) {
        super();
        this.start = new Vector(0, 0, 0);
        this.end = direction.normalize().multiply(length);
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Vector direction, double length, double stepSize) {
        super();
        this.start = new Vector(0, 0, 0);
        this.end = direction.normalize().multiply(length);
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Vector start, Vector end, double stepSize, Vector normal, double rotation) {
        super();
        this.start = start;
        this.end = end;
        this.setNormal(normal);
        this.setRotation(rotation);
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public Line(Location startLoc, Vector end, double stepSize, Vector normal, double rotation) {
        super();
        this.setStartLocation(startLoc);
        this.end = end;
        this.setNormal(normal);
        this.setRotation(rotation);
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    @Override
    public List<Vector> generatePoints() {
        points = new ArrayList<>();

        Vector direction = end.clone().subtract(start);
        double length = direction.length();
        direction.normalize().multiply(stepSize);

        for (double i = 0; i < (length / stepSize); i++) {
            points.add(start.clone().add(direction.clone().multiply(i)));
        }
        return points;
    }

    @Override
    public Shape clone() {
        return new Line(start, end, stepSize, getNormal(), getRotation());
    }

    public Vector getStart() {
        return start;
    }

    public void setStart(Vector start) {
        this.start = start;
    }

    public Vector getEnd() {
        return end;
    }

    public void setEnd(Vector end) {
        this.end = end;
    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
        this.start = new Vector(0, 0, 0);
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public String toString(){
        return "Line from " + (this.startLocation == null ? start : startLocation) + " to " + this.end + " and stepSize " + this.stepSize + " and normal " + this.getNormal() + " and rotation " + this.getRotation();
    }

    static {
        Classes.registerClass(new ClassInfo<>(Line.class, "line")
                .user("lines?")
                .name("Line")
                .description("Represents a line particle shape.")
                .examples("on load:", "\tset {_line} to a line from vector(0,0,0) to vector(1,1,1)", "\tset {_line2} to a line from player's head to player's target location with step size of 0.5")
                .parser(new Parser<>() {
                    @Override
                    public Line parse(String input, ParseContext context) {
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return false;
                    }

                    @Override
                    public String toString(Line o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(Line line) {
                        return "line:" + line.getStart().toString() + "," + line.getEnd().toString() + "," + line.getStepSize() + "," + line.getNormal() + "," + line.getRotation() ;
                    }
                })
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Line line) {
                        Fields fields = new Fields();
                        fields.putObject("start", line.getStart());
                        fields.putObject("end", line.getEnd());
                        fields.putPrimitive("stepSize", line.getStepSize());
                        fields.putObject("normal", line.getNormal());
                        fields.putPrimitive("rotation", line.getRotation());
                        return fields;
                    }

                    @Override
                    public Line deserialize(Fields fields) throws StreamCorruptedException {
                        return new Line(fields.getObject("start", Vector.class), fields.getObject("end", Vector.class), fields.getPrimitive("stepSize", Double.class), fields.getObject("normal", Vector.class), fields.getPrimitive("rotation", Double.class));
                    }

                    @Override
                    public void deserialize(Line line, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
