package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.util.MathUtil;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.util.Vector;

import java.io.NotSerializableException;
import java.io.StreamCorruptedException;
import java.util.HashSet;
import java.util.List;

public class Ellipsoid extends Shape {

    private double radiusX;
    private double radiusY;
    private double radiusZ;

    private static final Quaternion XY_ROTATION = new Quaternion(new Vector(1,0,0), Math.PI / 2);
    private static final Quaternion ZY_ROTATION = new Quaternion(new Vector(0,0,1), Math.PI / 2);

    public Ellipsoid(double radiusX, double radiusY, double radiusZ) {
        super();
        this.radiusX = radiusX;
        this.radiusY = radiusY;
        this.radiusZ = radiusZ;
    }

    @Override
    public List<Vector> generateOutline() {
        HashSet<Vector> points = new HashSet<>();
        points.addAll(MathUtil.calculateEllipse(radiusX, radiusZ, particleDensity, 2*Math.PI));
        points.addAll(XY_ROTATION.transform(MathUtil.calculateEllipse(radiusX, radiusY, particleDensity, 2*Math.PI)));
        points.addAll(ZY_ROTATION.transform(MathUtil.calculateEllipse(radiusY, radiusZ, particleDensity, 2*Math.PI)));
        return points.stream().toList();
    }

    @Override
    public List<Vector> generateSurface() {
        List<Vector> ellipse;
        if (radiusX > radiusZ)
             ellipse = XY_ROTATION.transform(MathUtil.calculateEllipse(radiusX, radiusY, particleDensity, 2*Math.PI));
        else
            ellipse = ZY_ROTATION.transform(MathUtil.calculateEllipse(radiusY, radiusZ, particleDensity, 2*Math.PI));
        HashSet<Vector> points = generateEllipsoid(ellipse, 1);
        return points.stream().toList();
    }

    @Override
    public List<Vector> generateFilled() {
        HashSet<Vector> points = new HashSet<>();
        List<Vector> ellipse;
        double radius = Math.max(radiusX, radiusZ);
        int steps = (int) Math.round(radius / particleDensity);
        for (int i = steps; i > 0; i--){
            double r = (i / (double) steps);
            if (radiusX > radiusZ)
                ellipse = XY_ROTATION.transform(MathUtil.calculateEllipse(radiusX * r, radiusY * r, particleDensity, 2*Math.PI));
            else
                ellipse = ZY_ROTATION.transform(MathUtil.calculateEllipse(radiusY * r, radiusZ * r, particleDensity, 2*Math.PI));
            points.addAll(generateEllipsoid(ellipse, r));
        }
        return points.stream().toList();
    }

    private HashSet<Vector> generateEllipsoid(List<Vector> ellipse, double r) {
        HashSet<Vector> points = new HashSet<>();
        for (int i = 0; i < Math.ceil(ellipse.size() / 4.0); i++){
            double y = ellipse.get(i).getY();
            double theta = Math.asin(y / (radiusY * r));
            for (Vector v2 : MathUtil.calculateEllipse(r * radiusX * Math.cos(theta), r * radiusZ * Math.cos(theta), particleDensity, 2*Math.PI)){
                points.add(new Vector(v2.getX(), y, v2.getZ()));
                points.add(new Vector(v2.getX(), -y, v2.getZ()));
            }
        }
        points.addAll(MathUtil.calculateEllipse(r * radiusX, r * radiusZ, particleDensity, 2*Math.PI));
        return points;
    }

    @Override
    public Shape particleCount(int count) {
        switch (style) {
            case OUTLINE:
                // this is so fucking cringe
                double h = (radiusX - radiusY) * (radiusX - radiusY) / ((radiusX + radiusY) + (radiusX + radiusY));
                double circumferenceXY = Math.PI * (radiusX + radiusY) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (radiusX - radiusZ) * (radiusX - radiusZ) / ((radiusX + radiusZ) + (radiusX + radiusZ));
                double circumferenceXZ = Math.PI * (radiusX + radiusZ) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                h = (radiusY - radiusZ) * (radiusY - radiusZ) / ((radiusY + radiusZ) + (radiusY + radiusZ));
                double circumferenceYZ = Math.PI * (radiusY + radiusZ) * (1 + (3 * h / (10 + Math.sqrt(4 - 3 * h))));
                particleDensity = (circumferenceXY + circumferenceXZ + circumferenceYZ) / count;
                break;
            case SURFACE:
                double surfaceArea = 4 * Math.PI * Math.pow((Math.pow(radiusX * radiusY, 1.6) + Math.pow(radiusX * radiusZ, 1.6) + Math.pow(radiusZ * radiusY, 1.6)) / 3, 1 / 1.6);
                this.particleDensity = Math.sqrt(surfaceArea / count);
                break;
            case FILL:
                double volume = 4 / 3.0 * Math.PI * radiusX * radiusY * radiusZ;
                this.particleDensity = Math.cbrt(volume / count);
                break;
        }
        points = generatePoints();
        return this;
    }

    @Override
    public Shape clone() {
        Ellipsoid clone = new Ellipsoid(radiusX, radiusY, radiusZ);
        this.copyTo(clone);
        return clone;
    }

    static {
        Classes.registerClass(new ClassInfo<>(Ellipsoid.class, "ellipsoid")
                .user("ellipsoids?")
                .name("Ellipsoid")
                .description("An ellipsoid shape.")
                .examples("ellipsoid")
                .serializer(new Serializer<>() {
                    @Override
                    public Fields serialize(Ellipsoid ellipsoid) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radiusX", ellipsoid.radiusX);
                        fields.putPrimitive("radiusY", ellipsoid.radiusY);
                        fields.putPrimitive("radiusZ", ellipsoid.radiusZ);
                        ellipsoid.serialize(fields);
                        return fields;
                    }

                    @Override
                    public Ellipsoid deserialize(Fields fields) throws StreamCorruptedException {
                        double radiusX = fields.getPrimitive("radiusX", Double.class);
                        double radiusY = fields.getPrimitive("radiusY", Double.class);
                        double radiusZ = fields.getPrimitive("radiusZ", Double.class);
                        Ellipsoid ellipsoid = new Ellipsoid(radiusX, radiusY, radiusZ);
                        Shape.deserialize(fields, ellipsoid);
                        return ellipsoid;
                    }

                    @Override
                    public void deserialize(Ellipsoid ellipsoid, Fields fields) throws StreamCorruptedException, NotSerializableException {
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
