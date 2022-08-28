package com.sovdee.skriptparticle.shapes;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Circle extends Shape {

    private double radius;
    private double stepSize;
    public Circle (double radius){
        super();
        this.radius = radius;
        // default to 30 points per circle
        this.stepSize = Math.PI * 2 / 30;
        this.points = getRotatedPoints(generatePoints());
    }

    public Circle (double radius, double stepSize){
        super();
        this.radius = radius;
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public Circle(double radius, Vector normal, double rotation) {
        super();
        this.radius = radius;
        this.setNormal(normal);
        this.setRotation(rotation);
        // default to 30 points per circle
        this.stepSize = Math.PI * 2 / 30;
        this.points = getRotatedPoints(generatePoints());
    }

    public Circle(double radius, double stepSize, Vector normal, double rotation) {
        super();
        this.radius = radius;
        this.setNormal(normal);
        this.setRotation(rotation);
        this.stepSize = stepSize;
        this.points = getRotatedPoints(generatePoints());
    }

    public double getRadius() {
        return radius;
    }

    public void setRadius(double radius) {
        this.radius = radius;
    }

    public double getStepSize() {
        return stepSize;
    }

    public void setStepSize(double stepSize) {
        this.stepSize = stepSize;
    }

    @Override
    public List<Vector> generatePoints() {
        this.points = new ArrayList<>();
        // simple flat circle, rotation is handled by the Shape class
        for (double theta = 0; theta < 2 * Math.PI; theta += this.stepSize) {
            points.add(new Vector(Math.cos(theta) * radius, 0, Math.sin(theta) * radius));
        }
        return points;
    }

    @Override
    public Shape clone() {
        return new Circle(this.radius, this.stepSize, this.getNormal(), this.getRotation());
    }

    public String toString(){
        return "Circle with radius " + this.radius + " and stepSize " + this.stepSize + " and normal " + this.getNormal() + " and rotation " + this.getRotation();
    }

    static {
        Classes.registerClass(new ClassInfo<>(Circle.class, "circle")
                .user("circles?")
                .name("Circle")
                .description("Represents a circle particle shape.")
                .examples("on load:", "\tset {_circle} to a circle with radius of 2")
//                .defaultExpression(new EventValueExpression<>(Circle.class))
                .parser(new Parser<>() {
                    Pattern pattern = Pattern.compile("^circle:(-?\\d+\\.?\\d*),(\\d+\\.?\\d*),vector\\((-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*),(-?\\d+\\.?\\d*)\\),(-?\\d+\\.?\\d*)$", Pattern.CASE_INSENSITIVE);

                    @Override
                    public Circle parse(String input, ParseContext context) {
                        Matcher matcher = pattern.matcher(input);
                        if (matcher.matches()) {
                            return new Circle(Double.parseDouble(matcher.group(1)), Double.parseDouble(matcher.group(2)), new Vector(Double.parseDouble(matcher.group(3)), Double.parseDouble(matcher.group(4)), Double.parseDouble(matcher.group(5))), Double.parseDouble(matcher.group(6)));
                        }
                        return null;
                    }

                    @Override
                    public boolean canParse(ParseContext context) {
                        return true;
                    }

                    @Override
                    public String toString(Circle o, int flags) {
                        return o.toString();
                    }

                    @Override
                    public String toVariableNameString(Circle circle) {
                        return "circle:" + circle.getRadius() + "," + circle.getStepSize() + ",vector(" + circle.getNormal().getX() + "," + circle.getNormal().getY() + "," + circle.getNormal().getZ() + ")," + circle.getRotation();
                    }
                })
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Circle circle) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", circle.getRadius());
                        fields.putPrimitive("stepSize", circle.getStepSize());
                        fields.putObject("normal", circle.getNormal());
                        fields.putPrimitive("rotation", circle.getRotation());
                        return fields;
                    }

                    @Override
                    public Circle deserialize(Fields fields) throws StreamCorruptedException {
                        return new Circle(fields.getPrimitive("radius", Double.class), fields.getPrimitive("stepSize", Double.class), fields.getObject("normal", Vector.class), fields.getPrimitive("rotation", Double.class));
                    }

                    @Override
                    public void deserialize(Circle circle, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
