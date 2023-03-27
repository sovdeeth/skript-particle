package com.sovdee.skriptparticle.elements.shapes.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Serializer;
import ch.njol.skript.registrations.Classes;
import ch.njol.yggdrasil.Fields;
import com.sovdee.skriptparticle.elements.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import com.sovdee.skriptparticles.shapes.RadialShape;
import org.bukkit.util.Vector;

import java.io.StreamCorruptedException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class RegularPolyhedron extends Shape implements RadialShape {

    protected static final Quaternion[] TETRAHEDRON_FACES = {
            new Quaternion(0, 1.0, 0.0, 0.0),
            new Quaternion(0.816496580927726, -0.5, -0.0, -0.288675134594813),
            new Quaternion(0.816496580927726, 0.5, 0.0, -0.288675134594813),
            new Quaternion(0.816496580927726, 0.0, 0.0, 0.5773502691896258)
    };
    protected static final Quaternion[] OCTAHEDRON_FACES = {
            new Quaternion(0.8880738339771153, 0.0, 0.0, 0.45970084338098305),
            new Quaternion(0.6279630301995545, 0.3250575836718681, 0.6279630301995544, 0.32505758367186816),
            new Quaternion(0, 0.45970084338098305, 0.8880738339771153, 0),
            new Quaternion(-0.6279630301995544, 0.32505758367186816, 0.6279630301995545, -0.3250575836718681),
            new Quaternion(-0.45970084338098316, 0.0, 0.0, 0.8880738339771153),
            new Quaternion(-0.3250575836718682, 0.6279630301995544, -0.3250575836718682, 0.6279630301995545),
            new Quaternion(0, 0.8880738339771153, -0.45970084338098316, 0),
            new Quaternion(0.3250575836718682, 0.6279630301995545, -0.3250575836718682, -0.6279630301995544)
    };
    protected static final Quaternion[] ICOSAHEDRON_FACES = {
            new Quaternion(0, 1.0, 0.0, 0.0),
            new Quaternion(0, 0.0, 1.0, 0.0),
            new Quaternion(0.9341723589627158, 0.3090169943749475, 0.0, 0.17841104488654494),
            new Quaternion(0.110264089708268, -0.5, 0.8090169943749475, 0.288675134594813),
            new Quaternion(-0.110264089708268, 0, 0.8090169943749475, 0.5773502691896256),
            new Quaternion(-0.9341723589627157, 0.3090169943749475, 0.0, -0.1784110448865451),
            new Quaternion(0.110264089708268, 0.5, -0.8090169943749475, 0.288675134594813),
            new Quaternion(0.110264089708268, 0, 0.8090169943749475, -0.5773502691896261),
            new Quaternion(-0.9341723589627157, 0.0, 0.0, 0.35682208977309),
            new Quaternion(-0.110264089708268, -0.5, -0.8090169943749475, -0.288675134594813),
            new Quaternion(-0.110264089708268, 0.5, 0.8090169943749475, -0.288675134594813),
            new Quaternion(0.35682208977309, -0.8090169943749475, -0.0, -0.46708617948135794),
            new Quaternion(-0.288675134594813, 0.3090169943749475, 0.5, -0.7557613140761709),
            new Quaternion(0.35682208977309, 0.8090169943749475, 0.0, -0.46708617948135794),
            new Quaternion(0.288675134594813, -0.5, -0.5, -0.6454972243679027),
            new Quaternion(0.35682208977309, 0.0, 0.0, 0.9341723589627157),
            new Quaternion(-0.288675134594813, -0.8090169943749475, 0.5, 0.110264089708268)
    };
    protected static final Quaternion[] DODECAHEDRON_FACES = {
            new Quaternion(0.9510565162951536, 0.0, 0.3090169943749475, 0.0),
            new Quaternion(0, -0.3090169943749475, 0, 0.9510565162951536),
            new Quaternion(0.5257311121191337, 0.0, 0.0, 0.8506508083520399),
            new Quaternion(-0.8506508083520399, 0.0, 0.0, 0.5257311121191337),
            new Quaternion(0.42532540417602, 0.5, 0.3090169943749475, 0.6881909602355868),
            new Quaternion(-0.6881909602355868, 0.3090169943749475, -0.5, 0.42532540417602),
            new Quaternion(0.1624598481164532, 0.8090169943749475, 0.5, 0.2628655560595668),
            new Quaternion(-0.2628655560595668, 0.5, -0.8090169943749475, 0.1624598481164532),
            new Quaternion(-0.1624598481164532, 0.8090169943749475, 0.5, -0.2628655560595668),
            new Quaternion(0.2628655560595668, 0.5, -0.8090169943749475, -0.1624598481164532),
            new Quaternion(-0.42532540417602, 0.5, 0.3090169943749475, -0.6881909602355868),
            new Quaternion(0.6881909602355868, 0.3090169943749475, -0.5, -0.42532540417602)
    };
    private double radius;
    private int faces;

    public RegularPolyhedron(double radius, int faces) {
        super();
        this.radius = radius;
        this.faces = faces;
    }

    @Override
    public List<Vector> generateOutline() {
        this.points = switch(faces){
            case 4 -> generatePolyhedron(TETRAHEDRON_FACES, radius);
            case 8 -> generatePolyhedron(OCTAHEDRON_FACES, radius);
            case 20 -> generatePolyhedron(ICOSAHEDRON_FACES, radius);
            case 12 -> generatePolyhedron(DODECAHEDRON_FACES, radius);
            default -> throw new IllegalArgumentException("Invalid number of faces");
        };
        return points;
    }

    @Override
    public List<Vector> generateFilled() {
        points = new ArrayList<>();
        double step = radius / Math.round(radius / particleDensity);
        switch (faces) {
            case 4:
                for (double i = radius; i > 0; i -= step) {
                    points.addAll(generatePolyhedron(TETRAHEDRON_FACES, i));
                }
                break;
            case 8:
                for (double i = radius; i > 0; i -= step) {
                    points.addAll(generatePolyhedron(OCTAHEDRON_FACES, i));
                }
                break;
            case 12:
                for (double i = radius; i > 0; i -= step) {
                    points.addAll(generatePolyhedron(DODECAHEDRON_FACES, i));
                }
                break;
            case 20:
                for (double i = radius; i > 0; i -= step) {
                    points.addAll(generatePolyhedron(ICOSAHEDRON_FACES, i));
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid number of faces");
        }
        return points;
    }

    private List<Vector> generatePolyhedron(Quaternion[] rotations, double radius) {
        Set<Vector> points = new HashSet<>();
        int sides = this.faces == 12 ? 5 : 3;
        double sideLength = switch (this.faces) {
            case 4 -> radius / 0.6123724356957945;
            case 8 -> radius / 0.7071067811865;
            case 12 -> radius / 1.401258538;
            case 20 -> radius / 0.9510565162951535;
            default -> throw new IllegalArgumentException("Invalid number of faces");
        };
        double inscribedRadius = switch (this.faces) {
            case 4 -> sideLength / 4.89897948556;
            case 8 -> sideLength * 0.408248290;
            case 12 -> sideLength * 1.113516364;
            case 20 -> sideLength * 0.7557613141;
            default -> throw new IllegalArgumentException("Invalid number of faces");
        };
        Vector offset = new Vector(0, inscribedRadius, 0);
        double faceRadius = sideLength / (2 * Math.sin(Math.PI / sides));
        for (Quaternion rotation : rotations) {
            HashSet<Vector> facePoints = new HashSet<>(switch (style) {
                case Style.OUTLINE -> generateFaceOutline(sides, faceRadius);
                case Style.FILL, Style.SURFACE -> generateFaceSurface(sides, faceRadius);
            });
            facePoints.forEach(point -> rotation.transform(point.add(offset)));
            points.addAll(facePoints);
        }
        return points.stream().toList();
    }

    private Set<Vector> generateFaceOutline(int sides, double radius) {
        return new HashSet<>(MathUtil.calculateRegularPolygon(radius, 2 * Math.PI / sides, particleDensity));
    }

    private Set<Vector> generateFaceSurface(int sides, double radius){
        HashSet<Vector> facePoints = new HashSet<>();
        double apothem = radius * Math.cos(Math.PI/sides);
        double radiusStep = radius / Math.round(apothem/particleDensity);
        for (double subRadius = radius; subRadius > 0; subRadius -= radiusStep) {
            facePoints.addAll(MathUtil.calculateRegularPolygon(subRadius, 2 * Math.PI / sides, particleDensity));
        }
        facePoints.add(new Vector(0, 0, 0));
        return facePoints;
    }


    @Override
    public Shape particleCount(int count) {
        return null;
    }

    @Override
    public Shape clone() {
        RegularPolyhedron clone = new RegularPolyhedron(radius, faces);
        this.copyTo(clone);
        return clone;
    }

    @Override
    public double radius() {
        return radius;
    }

    @Override
    public Shape radius(double radius) {
        this.radius = Math.max(0, radius);
        return this;
    }

    static {
        Classes.registerClass(new ClassInfo<>(RegularPolyhedron.class, "polyhedron")
                .user("polyhedrons?")
                .name("Regular Polyhedron")
                .description("Represents a regular polyhedron (platonic solid) particle shape.")
                .examples("on load:", "\tset {_icosahedron} to an icosahedron of radius 3")
                .serializer(new Serializer<>() {

                    @Override
                    public Fields serialize(RegularPolyhedron regularPolyhedron) {
                        Fields fields = new Fields();
                        fields.putPrimitive("radius", regularPolyhedron.radius);
                        fields.putPrimitive("faces", regularPolyhedron.faces);
                        regularPolyhedron.serialize(fields);
                        return fields;
                    }

                    @Override
                    public RegularPolyhedron deserialize(Fields fields) throws StreamCorruptedException {
                        double radius = fields.getPrimitive("radius", Double.class);
                        int faces = fields.getPrimitive("faces", Integer.class);
                        RegularPolyhedron regularPolyhedron = new RegularPolyhedron(radius, faces);
                        Shape.deserialize(fields, regularPolyhedron);
                        return regularPolyhedron;
                    }

                    @Override
                    public void deserialize(RegularPolyhedron regularPolyhedron, Fields fields) {
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
