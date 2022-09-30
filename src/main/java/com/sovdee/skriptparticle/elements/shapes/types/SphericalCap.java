package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.skript.registrations.Converters;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.List;

public class SphericalCap extends Shape implements RadialShape {

    private double radius;
    private double cutoffAngle;

    public SphericalCap(double radius, double cutoffAngle) {
        super();
        this.radius = radius;
        this.cutoffAngle = cutoffAngle;
    }

    @Override
    public List<Vector> generateOutline() {
        return generateSurface();
    }

    @Override
    public List<Vector> generateSurface() {
        int pointCount = 4 * (int) (Math.PI * radius * radius / (particleDensity * particleDensity));
        this.points = MathUtil.calculateFibonacciSphere(pointCount, radius, cutoffAngle);
        return points;
    }

    public double radius() {
        return radius;
    }

    public SphericalCap radius(double radius) {
        this.radius = radius;
        return this;
    }

    public double cutoffAngle() {
        return cutoffAngle;
    }

    public SphericalCap cutoffAngle(double cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
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
        SphericalCap sphericalCap = new SphericalCap(radius, cutoffAngle);
        this.copyTo(sphericalCap);
        return sphericalCap;
    }

    public String toString(){
        return "Spherical cap with radius " + this.radius + " and cutoff angle " + this.cutoffAngle;
    }

    static {
        Classes.registerClass(new ClassInfo<>(SphericalCap.class, "sphericalcap")
                .user("sphericalcaps?")
                .name("Spherical Cap")
                .description("Represents a spherical cap particle shape (a section of the surface of a sphere.")
                .examples("on load:", "\tset {_sphere} to a spherical cap of radius 2 and cutoff angle 45")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(SphericalCap sphericalCap) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", sphericalCap.radius);
                        fields.putPrimitive("cutoffAngle", sphericalCap.cutoffAngle);
                        sphericalCap.serialize(fields);
                        return fields;
                    }

                    @Override
                    public SphericalCap deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        double cutoffAngle = fields.getPrimitive("cutoffAngle", Double.class);
                        SphericalCap sphericalCap = new SphericalCap(radius, cutoffAngle);
                        Shape.deserialize(fields, sphericalCap);
                        return sphericalCap;
                    }

                    @Override
                    public void deserialize(SphericalCap sphere, Fields fields) throws StreamCorruptedException, NotSerializableException {
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

        Converters.registerConverter(SphericalCap.class, Shape.class, (sphericalCap) -> sphericalCap);

    }
}
