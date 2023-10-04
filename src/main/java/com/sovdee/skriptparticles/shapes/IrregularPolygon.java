package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A polygon with an arbitrary number of vertices. The polygon is assumed to be parallel to the xz plane.
 * The height of the polygon is the distance from the lowest vertex to the highest vertex, or the given height from the lowest vertex.
 */
public class IrregularPolygon extends AbstractShape implements LWHShape {

    private final List<Vector> vertices;
    private double height;

    /**
     * Creates a polygon with the given vertices. The polygon is assumed to be parallel to the xz plane.
     * The height of the polygon is the distance from the lowest vertex to the highest vertex.
     * @param vertices the vertices of the polygon. Must be greater than 2.
     * @throws IllegalArgumentException if the vertices are less than 3.
     */
    public IrregularPolygon(Collection<Vector> vertices) {
        super();
        if (vertices.size() < 3)
            throw new IllegalArgumentException("A polygon must have at least 3 vertices.");
        setBounds(vertices);
        this.vertices = flattenVertices(vertices);
    }

    /**
     * Creates a polygon with the given vertices and height. The polygon is assumed to be parallel to the xz plane.
     * @param vertices the vertices of the polygon. Must be greater than 2.
     * @param height the height of the polygon. Must be non-negative.
     * @throws IllegalArgumentException if the vertices are less than 3.
     */
    public IrregularPolygon(Collection<Vector> vertices, double height) {
        this(vertices);
        this.height = Math.max(height, 0);
    }

    /**
     * Flattens the vertices to the xz plane.
     * @param vertices the vertices to flatten. Does not modify the original vertices.
     * @return the flattened vertices.
     */
    @Contract(pure = true, value = "_ -> new")
    private List<Vector> flattenVertices(Collection<Vector> vertices) {
        List<Vector> flattened = new ArrayList<>();
        for (Vector v : vertices) {
            flattened.add(v.clone().setY(0));
        }
        return flattened;
    }

    /**
     * Sets the height of the polygon to the distance from the lowest vertex to the highest vertex.
     * @param vertices the vertices of the polygon.
     */
    private void setBounds(Collection<Vector> vertices) {
        double low = 9999999;
        double high = -9999999;
        for (Vector v : vertices) {
            if (v.getY() < low) low = v.getY();
            if (v.getY() > high) high = v.getY();
        }
        this.height = high - low;
    }

    @Override
    @Contract(pure = true)
    public Set<Vector> generateOutline() {
        double particleDensity = this.getParticleDensity();
        Set<Vector> points = new HashSet<>(MathUtil.connectPoints(vertices, particleDensity));
        points.addAll(MathUtil.calculateLine(vertices.get(0), vertices.get(vertices.size() - 1), particleDensity));
        if (height != 0) {
            Set<Vector> upperPoints = new HashSet<>();
            for (Vector v : points) {
                upperPoints.add(new Vector(v.getX(), height, v.getZ()));
            }
            points.addAll(upperPoints);
            for (Vector v : vertices) {
                points.addAll(MathUtil.calculateLine(v, new Vector(v.getX(), height, v.getZ()), particleDensity));
            }
        }
        return points;
    }

    @Override
    public void setParticleCount(int particleCount) {
        particleCount = Math.max(particleCount, 1);
        double perimeter = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            perimeter += vertices.get(i).distance(vertices.get(i + 1));
        }
        perimeter += vertices.get(0).distance(vertices.get(vertices.size() - 1));
        perimeter *= 2;
        perimeter += vertices.size() * height;
        this.setParticleDensity(perimeter / particleCount);
        this.setNeedsUpdate(true);
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public void setLength(double length) {
        // intentionally left blank
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
        // intentionally left blank
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    @Contract("-> new")
    public Shape clone() {
        return this.copyTo(new IrregularPolygon(vertices, height));
    }
}
