package com.sovdee.skriptparticles.shapes;

import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IrregularPolygon extends AbstractShape implements LWHShape {

    private final List<Vector> vertices;
    private double height;
    private double lowerBound;

    public IrregularPolygon(Collection<Vector> vertices) {
        super();
        setBounds(vertices);
        this.vertices = flattenVertices(vertices);
    }

    public IrregularPolygon(Collection<Vector> vertices, double height) {
        this(vertices);
        this.height = height;
    }

    private List<Vector> flattenVertices(Collection<Vector> vertices) {
        List<Vector> flattened = new ArrayList<>();
        for (Vector v : vertices) {
            flattened.add(v.clone().setY(0));
        }
        return flattened;
    }

    private void setBounds(Collection<Vector> vertices) {
        double low = 9999999;
        double high = -9999999;
        for (Vector v : vertices) {
            if (v.getY() < low) low = v.getY();
            if (v.getY() > high) high = v.getY();
        }
        this.lowerBound = low;
        this.height = high - low;
    }

    @Override
    public Set<Vector> generateOutline() {
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
        double perimeter = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            perimeter += vertices.get(i).distance(vertices.get(i + 1));
        }
        perimeter += vertices.get(0).distance(vertices.get(vertices.size() - 1));
        perimeter *= 2;
        perimeter += vertices.size() * height;
        particleDensity = perimeter / particleCount;
        needsUpdate = true;
    }

    @Override
    public double getLength() {
        return 0;
    }

    @Override
    public void setLength(double length) {
    }

    @Override
    public double getWidth() {
        return 0;
    }

    @Override
    public void setWidth(double width) {
    }

    @Override
    public double getHeight() {
        return height;
    }

    @Override
    public void setHeight(double height) {
        this.height = height;
        needsUpdate = true;
    }

    @Override
    public Shape clone() {
        return this.copyTo(new IrregularPolygon(vertices, height));
    }
}
