package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.List;

public class Sphere extends Shape implements RadialShape {

    private double radius;

    public Sphere (double radius){
        super();
        this.style = Style.SURFACE;
        this.radius = radius;
    }

    @Override
    public List<Vector> generateOutline() {
        return this.generateSurface();
    }





    @Override
    public List<Vector> generateSurface() {
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        this.points = MathUtil.calculateFibonacciSphere(pointCount, radius);
        return points;
    }

    @Override
    public List<Vector> generateFilled() {
        this.points = generateSurface();
        int subSpheres = (int) (radius / particleDensity) - 1;
        double radiusStep = radius / subSpheres;
        for (int i = 1; i < subSpheres; i++) {
            double subRadius = i * radiusStep;
            int pointCount = 4 * (int) (Math.PI * subRadius * subRadius / (particleDensity * particleDensity));
            points.addAll(MathUtil.calculateFibonacciSphere(pointCount, subRadius));
        }
        return points;
    }



    public double radius() {
        return radius;
    }

    public Sphere radius(double radius) {
        this.radius = radius;
        return this;
    }

    @Override
    public int particleCount() {
        return 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
    }

    @Override
    public Shape particleCount(int count) {
        this.particleDensity = Math.sqrt(4 * Math.PI * radius * radius / count);
        return this;
    }

    @Override
    public Shape clone() {
        Sphere sphere = new Sphere(radius);
        this.copyTo(sphere);
        return sphere;
    }

    public String toString(){
        return "Sphere with radius " + this.radius;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Sphere.class, "sphere")
                .user("spheres?")
                .name("Sphere")
                .description("Represents a sphere particle shape.")
                .examples("on load:", "\tset {_sphere} to a sphere of radius 2")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(Sphere sphere) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", sphere.radius);
                        sphere.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Sphere deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        Sphere sphere = new Sphere(radius);
                        Shape.deserialize(fields, sphere);
                        return sphere;
                    }

                    @Override
                    public void deserialize(Sphere sphere, Fields fields) throws StreamCorruptedException, NotSerializableException {
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

        Converters.registerConverter(Sphere.class, Shape.class, (sphere) -> sphere);

    }
}
