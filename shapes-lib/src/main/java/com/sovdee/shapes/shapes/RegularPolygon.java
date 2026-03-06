package com.sovdee.shapes.shapes;

import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.util.VectorUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * A regular polygon (or regular prism when {@code height > 0}) with equal-length sides and equal
 * interior angles, oriented in the XZ plane. The circumscribed circle has the given {@code radius},
 * and each side subtends an angle of {@code 2π / sides} at the centre.
 * <p>
 * When {@code height == 0} the shape is a flat polygon; when {@code height > 0} it becomes a
 * prism extruded along the Y axis. The shape implements {@link PolyShape}, {@link RadialShape},
 * and {@link LWHShape}; {@code length} and {@code width} from {@link LWHShape} always return
 * {@code 0} for this shape — only {@code height} is meaningful.
 * </p>
 * <p>
 * The minimum number of sides is 3 (triangle); the maximum interior angle per side is
 * {@code 2π/3} (i.e. a minimum of 3 sides is enforced when setting via angle).
 * </p>
 */
public class RegularPolygon extends AbstractShape implements PolyShape, RadialShape, LWHShape {

    private double angle;
    private double radius;
    private double height;

    /**
     * Constructs a flat regular polygon (height = 0) with the given number of sides and radius.
     *
     * @param sides  the number of sides; determines the vertex angle as {@code 2π / sides}
     * @param radius the circumscribed (outer) radius; clamped to at least {@link Shape#EPSILON}
     */
    public RegularPolygon(int sides, double radius) {
        this((Math.PI * 2) / sides, radius, 0);
    }

    /**
     * Constructs a flat regular polygon (height = 0) with an explicit vertex angle and radius.
     * The number of sides is derived as {@code floor(2π / angle)}.
     *
     * @param angle  the angular size of each side in radians; clamped to {@code [ε, 2π/3]}
     * @param radius the circumscribed (outer) radius; clamped to at least {@link Shape#EPSILON}
     */
    public RegularPolygon(double angle, double radius) {
        this(angle, radius, 0);
    }

    /**
     * Constructs a regular prism with the given number of sides, circumscribed radius, and height.
     * When {@code height == 0} this is equivalent to {@link #RegularPolygon(int, double)}.
     *
     * @param sides  the number of sides
     * @param radius the circumscribed (outer) radius; clamped to at least {@link Shape#EPSILON}
     * @param height the extrusion height along the Y axis; {@code 0} for a flat polygon
     */
    public RegularPolygon(int sides, double radius, double height) {
        this((Math.PI * 2) / sides, radius, height);
    }

    /**
     * Constructs a regular prism with an explicit vertex angle, radius, and height.
     * This is the canonical constructor; all other constructors delegate here.
     *
     * @param angle  the angular size of each side in radians; clamped to {@code [ε, 2π/3]}
     * @param radius the circumscribed (outer) radius; clamped to at least {@link Shape#EPSILON}
     * @param height the extrusion height along the Y axis; {@code 0} for a flat polygon
     */
    public RegularPolygon(double angle, double radius, double height) {
        super();
        this.angle = Math.clamp(angle, Shape.EPSILON, Math.PI * 2 / 3);
        this.radius = Math.max(radius, Shape.EPSILON);
        this.height = Math.max(height, 0);
    }

    // --- Static calculation methods ---

    /**
     * Samples points on (or filling) a regular polygon in the XZ plane.
     * <p>
     * When {@code wireframe} is {@code true} only the perimeter edges are sampled. When
     * {@code false}, concentric rings from the outer radius inward are filled (surface mode),
     * with a centre point added explicitly.
     * </p>
     * The apothem is {@code radius * cos(angle/2)}; the radial step between rings is
     * {@code radius / round(apothem / density)}.
     *
     * @param points    the list to which sampled points are appended
     * @param radius    the circumscribed radius of the polygon
     * @param angle     the angular size of each side in radians ({@code 2π / sides})
     * @param density   the approximate spacing between consecutive points
     * @param wireframe {@code true} to sample only the outer perimeter; {@code false} to fill
     */
    public static void calculateRegularPolygon(List<Vector3d> points, double radius, double angle, double density, boolean wireframe) {
        angle = Math.max(angle, Shape.EPSILON);

        double apothem = radius * Math.cos(angle / 2);
        double radiusStep = radius / Math.round(apothem / density);
        if (wireframe) {
            radiusStep = 2 * radius;
        } else {
            points.add(new Vector3d(0, 0, 0));
        }
        for (double subRadius = radius; subRadius >= 0; subRadius -= radiusStep) {
            Vector3d vertex = new Vector3d(subRadius, 0, 0);
            for (double i = 0; i < 2 * Math.PI; i += angle) {
                Line.calculateLine(points,
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i),
                        VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle),
                        density);
            }
        }
    }

    /**
     * Samples points on (or filling) a regular prism extruded along the Y axis.
     * <p>
     * For each side of the polygon, the bottom edge is sampled using
     * {@link Line#calculateLine(List, Vector3d, Vector3d, double)}, then either vertical edge
     * points are paired (wireframe) or filled columns are generated up to {@code height}.
     * Vertical pillar edges at each vertex are always included in wireframe mode.
     * </p>
     *
     * @param points    the list to which sampled points are appended
     * @param radius    the circumscribed radius of the prism's base
     * @param angle     the angular size of each side in radians ({@code 2π / sides})
     * @param height    the extrusion height along the Y axis
     * @param density   the approximate spacing between consecutive points
     * @param wireframe {@code true} to sample only the edges; {@code false} to fill the lateral faces
     */
    public static void calculateRegularPrism(List<Vector3d> points, double radius, double angle, double height, double density, boolean wireframe) {
        Vector3d vertex = new Vector3d(radius, 0, 0);
        // Need a temp list for the edge points since each spawns vertical points
        List<Vector3d> edgePoints = new ArrayList<>();
        for (double i = 0; i < 2 * Math.PI; i += angle) {
            Vector3d currentVertex = VectorUtil.rotateAroundY(new Vector3d(vertex), i);
            edgePoints.clear();
            Line.calculateLine(edgePoints, currentVertex, VectorUtil.rotateAroundY(new Vector3d(vertex), i + angle), density);
            for (Vector3d vector : edgePoints) {
                points.add(vector);
                if (wireframe) {
                    points.add(new Vector3d(vector.x, height, vector.z));
                } else {
                    Line.calculateLine(points, vector, new Vector3d(vector.x, height, vector.z), density);
                }
            }
            if (wireframe)
                Line.calculateLine(points, currentVertex, new Vector3d(currentVertex.x, height, currentVertex.z), density);
        }
    }

    // --- Generation methods ---

    /**
     * Generates outline (perimeter) points. For flat polygons this is the outer edge; for prisms
     * this includes both the top/bottom edges and the vertical pillar edges at each vertex.
     *
     * @param points  the list to which outline points are appended
     * @param density the approximate spacing between consecutive points
     */
    @Override
    public void generateOutline(List<Vector3d> points, double density) {
        if (height == 0)
            calculateRegularPolygon(points, this.radius, this.angle, density, true);
        else
            calculateRegularPrism(points, this.radius, this.angle, this.height, density, true);
    }

    /**
     * Generates surface points. For flat polygons this samples the filled face; for prisms this
     * samples the lateral faces (not the top/bottom caps).
     *
     * @param points  the list to which surface points are appended
     * @param density the approximate spacing between consecutive points
     */
    @Override
    public void generateSurface(List<Vector3d> points, double density) {
        if (height == 0)
            calculateRegularPolygon(points, this.radius, this.angle, density, false);
        else
            calculateRegularPrism(points, this.radius, this.angle, this.height, density, false);
    }

    /**
     * Generates filled interior points. For flat polygons this is identical to surface mode.
     * For prisms, a surface polygon is generated and then replicated vertically using
     * {@link AbstractShape#fillVertically} to fill the volume.
     *
     * @param points  the list to which filled points are appended
     * @param density the approximate spacing between consecutive points
     */
    @Override
    public void generateFilled(List<Vector3d> points, double density) {
        if (height == 0)
            generateSurface(points, density);
        else {
            int start = points.size();
            calculateRegularPolygon(points, this.radius, this.angle, density, false);
            fillVertically(points, start, height, density);
        }
    }

    /**
     * Computes the density needed to achieve {@code targetPointCount} sampled points.
     * Uses the following formulas (where {@code s = sides}, {@code r = radius},
     * {@code θ = angle}, {@code h = height}):
     * <ul>
     *   <li>OUTLINE (flat): {@code 2·s·r·sin(θ/2) / count}</li>
     *   <li>OUTLINE (prism): {@code (4·s·r·sin(θ/2) + h·s) / count}</li>
     *   <li>SURFACE (flat): {@code sqrt(s·r²·sin(θ)/2 / count)}</li>
     *   <li>SURFACE (prism): {@code (s·r²·sin(θ) + sideLength·s·h) / count}</li>
     *   <li>FILL: {@code s·r²·sin(θ)·h / count}</li>
     * </ul>
     *
     * @param style            determines which area/perimeter formula to apply
     * @param targetPointCount the desired number of points
     * @return the computed density
     */
    @Override
    public double computeDensity(SamplingStyle style, int targetPointCount) {
        int count = Math.max(targetPointCount, 1);
        int sides = getSides();
        return switch (style) {
            case OUTLINE -> {
                if (height == 0)
                    yield 2 * sides * radius * Math.sin(angle / 2) / count;
                yield (4 * sides * radius * Math.sin(angle / 2) + height * sides) / count;
            }
            case SURFACE -> {
                if (height == 0)
                    yield Math.sqrt(sides * radius * radius * Math.sin(angle) / 2 / count);
                yield (sides * radius * radius * Math.sin(angle) + getSideLength() * sides * height) / count;
            }
            case FILL -> (sides * radius * radius * Math.sin(angle) * height) / count;
        };
    }

    /**
     * Returns {@code true} if {@code point} lies within the polygon (or prism) in local
     * coordinates. For flat polygons the point must lie in the XZ plane (|y| &lt; ε). For prisms
     * the Y coordinate must be in {@code [0, height]}. The XZ containment check uses the apothem
     * and the angular position to test against the actual polygon boundary.
     *
     * @param point the point to test in local coordinates
     * @return {@code true} if the point is inside the polygon or prism
     */
    @Override
    public boolean contains(Vector3d point) {
        if (height > 0 && (point.y < 0 || point.y > height)) return false;
        if (height == 0 && Math.abs(point.y) > EPSILON) return false;
        // Check if point is within the polygon on XZ plane using inscribed radius
        double dist = Math.sqrt(point.x * point.x + point.z * point.z);
        double apothem = radius * Math.cos(angle / 2);
        return dist <= radius && dist <= apothem / Math.cos(Math.atan2(point.z, point.x) % angle - angle / 2);
    }

    /**
     * Returns the number of sides, computed as {@code floor(2π / angle)}.
     *
     * @return the number of sides of this polygon
     */
    @Override
    public int getSides() { return (int) (Math.PI * 2 / this.angle); }

    /**
     * Sets the number of sides by recomputing the vertex angle as {@code 2π / sides}.
     * Minimum of 3 sides is enforced. Invalidates the point cache.
     *
     * @param sides the desired number of sides; clamped to at least 3
     */
    @Override
    public void setSides(int sides) {
        this.angle = (Math.PI * 2) / Math.max(sides, 3);
        invalidate();
    }

    /**
     * Returns the edge length of each side, equal to {@code 2 * radius * sin(angle / 2)}.
     *
     * @return the side length
     */
    @Override
    public double getSideLength() { return this.radius * 2 * Math.sin(this.angle / 2); }

    /**
     * Sets the radius so that the edge length equals {@code sideLength}, keeping the current
     * angle (and thus side count) unchanged. Invalidates the point cache.
     *
     * @param sideLength the desired edge length; clamped to at least {@link Shape#EPSILON}
     */
    @Override
    public void setSideLength(double sideLength) {
        sideLength = Math.max(sideLength, Shape.EPSILON);
        this.radius = sideLength / (2 * Math.sin(this.angle / 2));
        this.radius = Math.max(radius, Shape.EPSILON);
        invalidate();
    }

    /**
     * Returns the circumscribed (outer) radius of the polygon.
     *
     * @return the radius
     */
    @Override
    public double getRadius() { return this.radius; }

    /**
     * Sets the circumscribed radius and invalidates the point cache.
     *
     * @param radius the new radius; clamped to at least {@link Shape#EPSILON}
     */
    @Override
    public void setRadius(double radius) {
        this.radius = Math.max(radius, Shape.EPSILON);
        invalidate();
    }

    /**
     * Always returns {@code 0}; length is not a distinct dimension of this shape.
     *
     * @return {@code 0}
     */
    @Override
    public double getLength() { return 0; }

    /**
     * No-op; length is not a distinct dimension of this shape.
     *
     * @param length ignored
     */
    @Override
    public void setLength(double length) { }

    /**
     * Always returns {@code 0}; width is not a distinct dimension of this shape.
     *
     * @return {@code 0}
     */
    @Override
    public double getWidth() { return 0; }

    /**
     * No-op; width is not a distinct dimension of this shape.
     *
     * @param width ignored
     */
    @Override
    public void setWidth(double width) { }

    /**
     * Returns the extrusion height of the prism along the Y axis, or {@code 0} for a flat polygon.
     *
     * @return the height
     */
    @Override
    public double getHeight() { return height; }

    /**
     * Sets the extrusion height and invalidates the point cache. A value of {@code 0} produces a
     * flat polygon; positive values produce a prism.
     *
     * @param height the new height; clamped to at least {@code 0}
     */
    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        invalidate();
    }

    /**
     * Returns a deep copy of this polygon, preserving angle, radius, height, and all inherited
     * {@link AbstractShape} state.
     *
     * @return a new {@link RegularPolygon} with identical configuration
     */
    @Override
    public Shape clone() {
        return this.copyTo(new RegularPolygon(angle, radius, height));
    }

    /**
     * Returns a human-readable description of this polygon including side count and radius.
     *
     * @return a string of the form {@code "regular polygon with <n> sides and radius <r>"}
     */
    @Override
    public String toString() {
        return "regular polygon with " + getSides() + " sides and radius " + getRadius();
    }
}
