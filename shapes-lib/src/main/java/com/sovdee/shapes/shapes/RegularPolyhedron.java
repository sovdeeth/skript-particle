package com.sovdee.shapes.shapes;

import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RegularPolyhedron extends AbstractShape implements RadialShape, PolyShape {

    private static final Quaterniond[] TETRAHEDRON_FACES = {
            new Quaterniond(1.0, 0.0, 0.0, 0),
            new Quaterniond(-0.5, -0.0, -0.288675134594813, 0.816496580927726),
            new Quaterniond(0.5, 0.0, -0.288675134594813, 0.816496580927726),
            new Quaterniond(0.0, 0.0, 0.5773502691896258, 0.816496580927726)
    };
    private static final Quaterniond[] OCTAHEDRON_FACES = {
            new Quaterniond(0.0, 0.0, 0.45970084338098305, 0.8880738339771153),
            new Quaterniond(0.3250575836718681, 0.6279630301995544, 0.32505758367186816, 0.6279630301995545),
            new Quaterniond(0.45970084338098305, 0.8880738339771153, 0, 0),
            new Quaterniond(0.32505758367186816, 0.6279630301995545, -0.3250575836718681, -0.6279630301995544),
            new Quaterniond(0.0, 0.0, 0.8880738339771153, -0.45970084338098316),
            new Quaterniond(0.6279630301995544, -0.3250575836718682, 0.6279630301995545, -0.3250575836718682),
            new Quaterniond(0.8880738339771153, -0.45970084338098316, 0, 0),
            new Quaterniond(0.6279630301995545, -0.3250575836718682, -0.6279630301995544, 0.3250575836718682)
    };
    private static final Quaterniond[] ICOSAHEDRON_FACES = {
            new Quaterniond(1.0, 0.0, 0.0, 0),
            new Quaterniond(0.0, 1.0, 0.0, 0),
            new Quaterniond(0.3090169943749475, 0.0, 0.17841104488654494, 0.9341723589627158),
            new Quaterniond(-0.5, 0.8090169943749475, 0.288675134594813, 0.110264089708268),
            new Quaterniond(0, 0.8090169943749475, 0.5773502691896256, -0.110264089708268),
            new Quaterniond(0.3090169943749475, 0.0, -0.1784110448865451, -0.9341723589627157),
            new Quaterniond(0.5, -0.8090169943749475, 0.288675134594813, 0.110264089708268),
            new Quaterniond(0, 0.8090169943749475, -0.5773502691896261, 0.110264089708268),
            new Quaterniond(0.0, 0.0, 0.35682208977309, -0.9341723589627157),
            new Quaterniond(-0.5, -0.8090169943749475, -0.288675134594813, -0.110264089708268),
            new Quaterniond(0.5, 0.8090169943749475, -0.288675134594813, -0.110264089708268),
            new Quaterniond(-0.8090169943749475, -0.0, -0.46708617948135794, 0.35682208977309),
            new Quaterniond(0.3090169943749475, 0.5, -0.7557613140761709, -0.288675134594813),
            new Quaterniond(0.8090169943749475, 0.0, -0.46708617948135794, 0.35682208977309),
            new Quaterniond(-0.5, -0.5, -0.6454972243679027, 0.288675134594813),
            new Quaterniond(0.0, 0.0, 0.9341723589627157, 0.35682208977309),
            new Quaterniond(-0.8090169943749475, 0.5, 0.110264089708268, -0.288675134594813)
    };
    private static final Quaterniond[] DODECAHEDRON_FACES = {
            new Quaterniond(0.0, 0.3090169943749475, 0.0, 0.9510565162951536),
            new Quaterniond(-0.3090169943749475, 0, 0.9510565162951536, 0),
            new Quaterniond(0.0, 0.0, 0.8506508083520399, 0.5257311121191337),
            new Quaterniond(0.0, 0.0, 0.5257311121191337, -0.8506508083520399),
            new Quaterniond(0.5, 0.3090169943749475, 0.6881909602355868, 0.42532540417602),
            new Quaterniond(0.3090169943749475, -0.5, 0.42532540417602, -0.6881909602355868),
            new Quaterniond(0.8090169943749475, 0.5, 0.2628655560595668, 0.1624598481164532),
            new Quaterniond(0.5, -0.8090169943749475, 0.1624598481164532, -0.2628655560595668),
            new Quaterniond(0.8090169943749475, 0.5, -0.2628655560595668, -0.1624598481164532),
            new Quaterniond(0.5, -0.8090169943749475, -0.1624598481164532, 0.2628655560595668),
            new Quaterniond(0.5, 0.3090169943749475, -0.6881909602355868, -0.42532540417602),
            new Quaterniond(0.3090169943749475, -0.5, -0.42532540417602, 0.6881909602355868)
    };
    // Radius-to-side-length conversion factors for each polyhedron type
    private static final double TETRA_R2SL = 0.6123724356957945;
    private static final double OCTA_R2SL = 0.7071067811865;
    private static final double DODECA_R2SL = 1.401258538;
    private static final double ICOSA_R2SL = 0.9510565162951535;

    // Side-length to inscribed-radius conversion factors
    private static final double TETRA_INSC = 1.0 / 4.89897948556;
    private static final double OCTA_INSC = 0.408248290;
    private static final double DODECA_INSC = 1.113516364;
    private static final double ICOSA_INSC = 0.7557613141;

    private double radius;
    private int faces;

    public RegularPolyhedron(double radius, int faces) {
        super();
        this.radius = Math.max(radius, Shape.EPSILON);
        this.faces = switch (faces) {
            case 4, 8, 12, 20 -> faces;
            default -> 4;
        };
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        points.addAll(switch (faces) {
            case 4 -> generatePolyhedron(TETRAHEDRON_FACES, radius);
            case 8 -> generatePolyhedron(OCTAHEDRON_FACES, radius);
            case 20 -> generatePolyhedron(ICOSAHEDRON_FACES, radius);
            case 12 -> generatePolyhedron(DODECAHEDRON_FACES, radius);
            default -> new HashSet<>();
        });
    }

    @Override
    public void generateFilled(Set<Vector3d> points) {
        double step = radius / Math.round(radius / this.getParticleDensity());
        switch (faces) {
            case 4:
                for (double i = radius; i > 0; i -= step)
                    points.addAll(generatePolyhedron(TETRAHEDRON_FACES, i));
                break;
            case 8:
                for (double i = radius; i > 0; i -= step)
                    points.addAll(generatePolyhedron(OCTAHEDRON_FACES, i));
                break;
            case 12:
                for (double i = radius; i > 0; i -= step)
                    points.addAll(generatePolyhedron(DODECAHEDRON_FACES, i));
                break;
            case 20:
                for (double i = radius; i > 0; i -= step)
                    points.addAll(generatePolyhedron(ICOSAHEDRON_FACES, i));
                break;
        }
    }

    private Set<Vector3d> generatePolyhedron(Quaterniond[] rotations, double radius) {
        Set<Vector3d> points = new LinkedHashSet<>();
        int sides = this.faces == 12 ? 5 : 3;
        double sideLength = switch (faces) {
            case 4 -> radius / TETRA_R2SL;
            case 8 -> radius / OCTA_R2SL;
            case 12 -> radius / DODECA_R2SL;
            case 20 -> radius / ICOSA_R2SL;
            default -> 0.0;
        };
        double inscribedRadius = switch (this.faces) {
            case 4 -> sideLength * TETRA_INSC;
            case 8 -> sideLength * OCTA_INSC;
            case 12 -> sideLength * DODECA_INSC;
            case 20 -> sideLength * ICOSA_INSC;
            default -> 1;
        };
        Vector3d offset = new Vector3d(0, inscribedRadius, 0);
        double faceRadius = sideLength / (2 * Math.sin(Math.PI / sides));
        Style style = this.getStyle();
        for (Quaterniond rotation : rotations) {
            Set<Vector3d> facePoints = new LinkedHashSet<>(switch (style) {
                case OUTLINE -> generateFaceOutline(sides, faceRadius);
                case FILL, SURFACE -> generateFaceSurface(sides, faceRadius);
            });
            facePoints.forEach(point -> rotation.transform(point.add(offset)));
            points.addAll(facePoints);
        }
        return points;
    }

    private Set<Vector3d> generateFaceOutline(int sides, double radius) {
        return new LinkedHashSet<>(RegularPolygon.calculateRegularPolygon(radius, 2 * Math.PI / sides, this.getParticleDensity(), true));
    }

    private Set<Vector3d> generateFaceSurface(int sides, double radius) {
        Set<Vector3d> facePoints = new LinkedHashSet<>();
        double particleDensity = this.getParticleDensity();
        double apothem = radius * Math.cos(Math.PI / sides);
        double radiusStep = radius / Math.round(apothem / particleDensity);
        for (double subRadius = radius; subRadius > 0; subRadius -= radiusStep) {
            facePoints.addAll(RegularPolygon.calculateRegularPolygon(subRadius, 2 * Math.PI / sides, particleDensity, false));
        }
        facePoints.add(new Vector3d(0, 0, 0));
        return facePoints;
    }

    @Override
    public void setParticleCount(int particleCount) { }

    @Override
    public Shape clone() {
        return this.copyTo(new RegularPolyhedron(radius, faces));
    }

    @Override
    public int getSides() { return faces; }

    @Override
    public void setSides(int sides) {
        switch (sides) {
            case 4, 8, 12, 20 -> this.faces = sides;
            default -> { return; }
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getSideLength() {
        return switch (faces) {
            case 4 -> radius / TETRA_R2SL;
            case 8 -> radius / OCTA_R2SL;
            case 12 -> radius / DODECA_R2SL;
            case 20 -> radius / ICOSA_R2SL;
            default -> 0.0;
        };
    }

    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, Shape.EPSILON);
        switch (faces) {
            case 4 -> this.radius = sideLength * TETRA_R2SL;
            case 8 -> this.radius = sideLength * OCTA_R2SL;
            case 12 -> this.radius = sideLength * DODECA_R2SL;
            case 20 -> this.radius = sideLength * ICOSA_R2SL;
            default -> { return; }
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() { return radius; }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, Shape.EPSILON);
        this.setNeedsUpdate(true);
    }
}
