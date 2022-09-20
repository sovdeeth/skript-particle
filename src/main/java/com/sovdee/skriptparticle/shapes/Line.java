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

    public Line() {
        super();
        this.start = new Vector(0, 0, 0);
        this.end = new Vector(0, 0, 0);
    }

    public Line(Vector start, Vector end) {
        super();
        this.start = start;
        this.end = end;
    }

    public Line(Location start, Location end) {
        super();
        this.center(start);
        this.start = new Vector(0, 0, 0);
        this.end = end.toVector().subtract(start.toVector());
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
        Line line = new Line(this.start, this.end).stepSize(this.stepSize);
        this.copyTo(line);
        return line;
    }

    public Vector start() {
        return start;
    }

    public Line start(Vector start) {
        this.start = start;
        return this;
    }

    public Vector end() {
        return end;
    }

    public Line end(Vector end) {
        this.end = end;
        return this;
    }

    public double stepSize() {
        return stepSize;
    }

    public Line stepSize(double stepSize) {
        this.stepSize = stepSize;
        return this;
    }





    public String toString(){
        return "Line from " + this.start + " to " + this.end + " and stepSize " + this.stepSize;
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
                        return "line:" + line.uuid() ;
                    }
                })
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Line line) {
                        Fields fields = new Fields();
                        fields.putObject("start", line.start);
                        fields.putObject("end", line.end);
                        fields.putPrimitive("stepSize", line.stepSize);
                        line.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Line deserialize(Fields fields) throws StreamCorruptedException {
                        Vector start = fields.getObject("start", Vector.class);
                        Vector end = fields.getObject("end", Vector.class);
                        double stepSize = fields.getPrimitive("stepSize", double.class);
                        Line line = new Line(start, end).stepSize(stepSize);
                        Shape.deserialize(fields, line);
                        return line;
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
