package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.List;

public class Arc extends Shape implements RadialShape {
    private double radius;
    private double cutoffAngle;

    public Arc(double radius, double cutoffAngle){
        super();
        this.radius = radius;
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public List<Vector> generateOutline() {
        this.points = MathUtil.calculateCircle(this.radius, this.particleDensity, this.cutoffAngle);
        return points;
    }

    @Override
    public List<Vector> generateSurface() {
        this.points = MathUtil.calculateDisc(this.radius, this.particleDensity, this.cutoffAngle);
        return points;
    }

    @Override
    public int particleCount() {
        return (int) (cutoffAngle * radius / particleDensity);
    }

    @Override
    public Shape particleCount(int count) {
        particleDensity = switch (style){
            case OUTLINE -> cutoffAngle * radius / count;
            case SURFACE,FILL -> Math.sqrt(0.5 * cutoffAngle * radius * radius / count);
        };
        return this;
    }

    public double radius() {
        return radius;
    }

    public Arc radius(double radius) {
        this.radius = radius;
        return this;
    }

    public double cutoffAngle() {
        return cutoffAngle;
    }

    public Arc cutoffAngle(double cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
        return this;
    }

    @Override
    public Shape clone() {
        Arc arc = new Arc(this.radius, this.cutoffAngle);
        this.copyTo(arc);
        return arc;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Arc.class, "arc")
                .user("arcs?")
                .name("Arc")
                .description("Represents an arc particle shape.")
                .examples("on load:", "\tset {_arc} to a arc with radius of 2 and angle of 45 degrees")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Arc arc) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", arc.radius);
                        fields.putPrimitive("angle", arc.cutoffAngle);
                        arc.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Arc deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        double angle = fields.getPrimitive("angle", Double.class);
                        Arc arc = new Arc(radius, angle);
                        Shape.deserialize(fields, arc);
                        return arc;
                    }

                    @Override
                    public void deserialize(Arc arc, Fields fields) {
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
