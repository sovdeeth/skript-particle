package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A regular polyhedron shape. This shape is defined by a center point and a radius, as well as the number of faces.
 * The four regular polyhedra are stored as static constant arrays of rotations, each representing a face of the polyhedron.
 */
public class RegularPolyhedron extends AbstractShape implements RadialShape, PolyShape {

    private static final Quaternion[] TETRAHEDRON_FACES = {
            new Quaternion(1.0, 0.0, 0.0, 0),
            new Quaternion(-0.5, -0.0, -0.288675134594813, 0.816496580927726),
            new Quaternion(0.5, 0.0, -0.288675134594813, 0.816496580927726),
            new Quaternion(0.0, 0.0, 0.5773502691896258, 0.816496580927726)
    };
    private static final Quaternion[] OCTAHEDRON_FACES = {
            new Quaternion(0.0, 0.0, 0.45970084338098305, 0.8880738339771153),
            new Quaternion(0.3250575836718681, 0.6279630301995544, 0.32505758367186816, 0.6279630301995545),
            new Quaternion(0.45970084338098305, 0.8880738339771153, 0, 0),
            new Quaternion(0.32505758367186816, 0.6279630301995545, -0.3250575836718681, -0.6279630301995544),
            new Quaternion(0.0, 0.0, 0.8880738339771153, -0.45970084338098316),
            new Quaternion(0.6279630301995544, -0.3250575836718682, 0.6279630301995545, -0.3250575836718682),
            new Quaternion(0.8880738339771153, -0.45970084338098316, 0, 0),
            new Quaternion(0.6279630301995545, -0.3250575836718682, -0.6279630301995544, 0.3250575836718682)
    };
    private static final Quaternion[] ICOSAHEDRON_FACES = {
            new Quaternion(1.0, 0.0, 0.0, 0),
            new Quaternion(0.0, 1.0, 0.0, 0),
            new Quaternion(0.3090169943749475, 0.0, 0.17841104488654494, 0.9341723589627158),
            new Quaternion(-0.5, 0.8090169943749475, 0.288675134594813, 0.110264089708268),
            new Quaternion(0, 0.8090169943749475, 0.5773502691896256, -0.110264089708268),
            new Quaternion(0.3090169943749475, 0.0, -0.1784110448865451, -0.9341723589627157),
            new Quaternion(0.5, -0.8090169943749475, 0.288675134594813, 0.110264089708268),
            new Quaternion(0, 0.8090169943749475, -0.5773502691896261, 0.110264089708268),
            new Quaternion(0.0, 0.0, 0.35682208977309, -0.9341723589627157),
            new Quaternion(-0.5, -0.8090169943749475, -0.288675134594813, -0.110264089708268),
            new Quaternion(0.5, 0.8090169943749475, -0.288675134594813, -0.110264089708268),
            new Quaternion(-0.8090169943749475, -0.0, -0.46708617948135794, 0.35682208977309),
            new Quaternion(0.3090169943749475, 0.5, -0.7557613140761709, -0.288675134594813),
            new Quaternion(0.8090169943749475, 0.0, -0.46708617948135794, 0.35682208977309),
            new Quaternion(-0.5, -0.5, -0.6454972243679027, 0.288675134594813),
            new Quaternion(0.0, 0.0, 0.9341723589627157, 0.35682208977309),
            new Quaternion(-0.8090169943749475, 0.5, 0.110264089708268, -0.288675134594813)
    };
    private static final Quaternion[] DODECAHEDRON_FACES = {
            new Quaternion(0.0, 0.3090169943749475, 0.0, 0.9510565162951536),
            new Quaternion(-0.3090169943749475, 0, 0.9510565162951536, 0),
            new Quaternion(0.0, 0.0, 0.8506508083520399, 0.5257311121191337),
            new Quaternion(0.0, 0.0, 0.5257311121191337, -0.8506508083520399),
            new Quaternion(0.5, 0.3090169943749475, 0.6881909602355868, 0.42532540417602),
            new Quaternion(0.3090169943749475, -0.5, 0.42532540417602, -0.6881909602355868),
            new Quaternion(0.8090169943749475, 0.5, 0.2628655560595668, 0.1624598481164532),
            new Quaternion(0.5, -0.8090169943749475, 0.1624598481164532, -0.2628655560595668),
            new Quaternion(0.8090169943749475, 0.5, -0.2628655560595668, -0.1624598481164532),
            new Quaternion(0.5, -0.8090169943749475, -0.1624598481164532, 0.2628655560595668),
            new Quaternion(0.5, 0.3090169943749475, -0.6881909602355868, -0.42532540417602),
            new Quaternion(0.3090169943749475, -0.5, -0.42532540417602, 0.6881909602355868)
    };
    private double radius;
    private int faces;

    /**
     * Generates a regular polyhedron with the given radius and number of faces.
     *
     * @param radius the radius of the polyhedron. Must be greater than 0.
     * @param faces the number of faces of the polyhedron. Must be 4, 8, 12, or 20.
     */
    public RegularPolyhedron(double radius, int faces) {
        super();
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.faces = switch (faces) {
            case 4, 8, 12, 20 -> faces;
            default -> 4;
        };
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        return switch (faces) {
            case 4 -> generatePolyhedron(TETRAHEDRON_FACES, radius);
            case 8 -> generatePolyhedron(OCTAHEDRON_FACES, radius);
            case 20 -> generatePolyhedron(ICOSAHEDRON_FACES, radius);
            case 12 -> generatePolyhedron(DODECAHEDRON_FACES, radius);
            default -> new HashSet<>();
        };
    }

    @Override
    public @NotNull Set<Vector> generateFilled() {
        Set<Vector> points = new LinkedHashSet<>();
        double step = radius / Math.round(radius / this.getParticleDensity());
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
                return points;
        }
        return points;
    }

    /**
     * Generates a polyhedron from the given set of face rotations and a radius.
     *
     * @param rotations the rotations of the faces of the polyhedron
     * @param radius the radius of the polyhedron
     * @return a set of vectors representing the polyhedron
     */
    @Contract(pure = true, value = "_, _ -> new")
    private Set<Vector> generatePolyhedron(Quaternion[] rotations, double radius) {
        Set<Vector> points = new LinkedHashSet<>();
        int sides = this.faces == 12 ? 5 : 3;
        // todo: get rid of magic numbers
        double sideLength = switch (faces) {
            case 4 -> radius / 0.6123724356957945;
            case 8 -> radius / 0.7071067811865;
            case 12 -> radius / 1.401258538;
            case 20 -> radius / 0.9510565162951535;
            default -> 0.0;
        };
        double inscribedRadius = switch (this.faces) {
            case 4 -> sideLength / 4.89897948556;
            case 8 -> sideLength * 0.408248290;
            case 12 -> sideLength * 1.113516364;
            case 20 -> sideLength * 0.7557613141;
            default -> 1;
        };
        Vector offset = new Vector(0, inscribedRadius, 0);
        double faceRadius = sideLength / (2 * Math.sin(Math.PI / sides));
        Style style = this.getStyle();
        for (Quaternion rotation : rotations) {
            Set<Vector> facePoints = new LinkedHashSet<>(switch (style) {
                case OUTLINE -> generateFaceOutline(sides, faceRadius);
                case FILL, SURFACE -> generateFaceSurface(sides, faceRadius);
            });
            facePoints.forEach(point -> rotation.transform(point.add(offset)));
            points.addAll(facePoints);
        }
        return points;
    }

    /**
     * Generates the outline of a face of the polyhedron. The face is a regular polygon with the given number of sides.
     * @param sides the number of sides of the face
     * @param radius the radius of the face
     * @return a set of vectors representing the outline of the face
     */
    @Contract(pure = true, value = "_, _ -> new")
    private Set<Vector> generateFaceOutline(int sides, double radius) {
        return new LinkedHashSet<>(MathUtil.calculateRegularPolygon(radius, 2 * Math.PI / sides, this.getParticleDensity(), true));
    }

    /**
     * Generates the surface of a face of the polyhedron. The face is a regular polygon with the given number of sides.
     * @param sides the number of sides of the face
     * @param radius the radius of the face
     * @return a set of vectors representing the surface of the face
     */
    @Contract(pure = true, value = "_, _ -> new")
    private Set<Vector> generateFaceSurface(int sides, double radius) {
        HashSet<Vector> facePoints = new LinkedHashSet<>();
        double particleDensity = this.getParticleDensity();
        double apothem = radius * Math.cos(Math.PI / sides);
        double radiusStep = radius / Math.round(apothem / particleDensity);
        for (double subRadius = radius; subRadius > 0; subRadius -= radiusStep) {
            facePoints.addAll(MathUtil.calculateRegularPolygon(subRadius, 2 * Math.PI / sides, particleDensity, false));
        }
        facePoints.add(new Vector(0, 0, 0));
        return facePoints;
    }

    @Override
    public void setParticleCount(int particleCount) {
        // intentionally left blank
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new RegularPolyhedron(radius, faces));
    }

    @Override
    public int getSides() {
        return faces;
    }

    @Override
    public void setSides(int sides) {
        switch (sides) {
            case 4, 8, 12, 20 -> this.faces = sides;
            default -> {
                // intentionally does not throw an exception
                return;
            }
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getSideLength() {
        return switch (faces) {
            case 4 -> radius / 0.6123724356957945;
            case 8 -> radius / 0.7071067811865;
            case 12 -> radius / 1.401258538;
            case 20 -> radius / 0.9510565162951535;
            default -> 0.0;
        };
    }

    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, MathUtil.EPSILON);
        switch (faces) {
            case 4 -> this.radius = sideLength * 0.6123724356957945;
            case 8 -> this.radius = sideLength * 0.7071067811865;
            case 12 -> this.radius = sideLength * 1.401258538;
            case 20 -> this.radius = sideLength * 0.9510565162951535;
            default -> {
                return;
            }
        }
        this.setNeedsUpdate(true);
    }

    @Override
    public double getRadius() {
        return radius;
    }

    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, MathUtil.EPSILON);
        this.setNeedsUpdate(true);
    }
}
