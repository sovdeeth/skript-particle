package com.sovdee.shapes;

import com.sovdee.shapes.util.MathUtil;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class IrregularPolygon extends AbstractShape implements LWHShape {

    private final List<Vector3d> vertices;
    private double height;

    public IrregularPolygon(Collection<Vector3d> vertices) {
        super();
        if (vertices.size() < 3)
            throw new IllegalArgumentException("A polygon must have at least 3 vertices.");
        setBounds(vertices);
        this.vertices = flattenVertices(vertices);
    }

    public IrregularPolygon(Collection<Vector3d> vertices, double height) {
        this(vertices);
        this.height = Math.max(height, 0);
    }

    private List<Vector3d> flattenVertices(Collection<Vector3d> vertices) {
        List<Vector3d> flattened = new ArrayList<>();
        for (Vector3d v : vertices) {
            flattened.add(new Vector3d(v.x, 0, v.z));
        }
        return flattened;
    }

    private void setBounds(Collection<Vector3d> vertices) {
        double low = Double.MAX_VALUE;
        double high = -Double.MAX_VALUE;
        for (Vector3d v : vertices) {
            if (v.y < low) low = v.y;
            if (v.y > high) high = v.y;
        }
        this.height = high - low;
    }

    @Override
    public void generateOutline(Set<Vector3d> points) {
        double particleDensity = this.getParticleDensity();
        points.addAll(MathUtil.connectPoints(vertices, particleDensity));
        points.addAll(MathUtil.calculateLine(vertices.get(0), vertices.get(vertices.size() - 1), particleDensity));
        if (height != 0) {
            Set<Vector3d> upperPoints = new LinkedHashSet<>();
            for (Vector3d v : points) {
                upperPoints.add(new Vector3d(v.x, height, v.z));
            }
            points.addAll(upperPoints);
            for (Vector3d v : vertices) {
                points.addAll(MathUtil.calculateLine(v, new Vector3d(v.x, height, v.z), particleDensity));
            }
        }
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
    public double getLength() { return 0; }

    @Override
    public void setLength(double length) { }

    @Override
    public double getWidth() { return 0; }

    @Override
    public void setWidth(double width) { }

    @Override
    public double getHeight() { return height; }

    @Override
    public void setHeight(double height) {
        this.height = Math.max(height, 0);
        this.setNeedsUpdate(true);
    }

    @Override
    public Shape clone() {
        return this.copyTo(new IrregularPolygon(vertices, height));
    }
}
