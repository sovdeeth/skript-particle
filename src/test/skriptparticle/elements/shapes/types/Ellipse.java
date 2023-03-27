package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.elements.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.List;

public class Ellipse extends Shape {

    private double radiusX;
    private double radiusZ;

    public Ellipse(double radiusX, double radiusZ) {
        super();
        this.radiusX = radiusX;
        this.radiusZ = radiusZ;
    }

    @Override
    public List<Vector> generateOutline() {
        return MathUtil.calculateEllipse(radiusX, radiusZ, particleDensity, 2*Math.PI);
    }

    @Override
    public List<Vector> generateSurface() {
        HashSet<Vector> points = new HashSet<>();
        int steps = (int) Math.round(radiusX / particleDensity);
        double r;
        for (double i = 1; i <= steps; i += 1){
            r = i / steps;
            points.addAll(MathUtil.calculateEllipse(radiusX * r, radiusZ * r, particleDensity, 2*Math.PI));
        }
        this.points = points.stream().toList();
        return this.points;
    }

    @Override
    public Shape particleCount(int count) {
        switch (style) {
            case OUTLINE:
                // this is so fucking cringe
                double h = (radiusX - radiusZ) * (radiusX - radiusZ) / ((radiusX + radiusZ) + (radiusX + radiusZ));
                double circumferenceXY = Math.PI * (radiusX + radiusZ) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                this.particleDensity = circumferenceXY / count;
                break;
            case SURFACE, FILL:
                this.particleDensity = Math.sqrt((Math.PI * radiusX * radiusZ) / count);
                break;
        }
        return this;
    }

    @Override
    public Shape clone() {
        Ellipse ellipse = new Ellipse(radiusX, radiusZ);
        this.copyTo(ellipse);
        return ellipse;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Ellipse.class, "ellipse")
                .user("ellipses?")
                .name("Ellipse")
                .description("An ellipse shape.")
                .examples("ellipse")
                .serializer(new Serializer<>() {
                    @Override
                    public Fields serialize(Ellipse ellipse) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radiusX", ellipse.radiusX);
                        fields.putPrimitive("radiusZ", ellipse.radiusZ);
                        ellipse.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Ellipse deserialize(Fields fields) throws StreamCorruptedException {
                        double radiusX = fields.getPrimitive("radiusX", Double.class);
                        double radiusZ = fields.getPrimitive("radiusZ", Double.class);
                        Ellipse ellipse = new Ellipse(radiusX, radiusZ);
                        Shape.deserialize(fields, ellipse);
                        return ellipse;
                    }

                    @Override
                    public void deserialize(Ellipse ellipse, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
