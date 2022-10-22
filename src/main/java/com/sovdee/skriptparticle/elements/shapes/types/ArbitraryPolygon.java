package com.sovdee.skriptparticle.elements.shapes.types;

import com.sovdee.skriptparticle.util.MathUtil;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;


public class ArbitraryPolygon extends Shape {

    private List<Vector> vertices;
    private double height;

    // assume all vertices are on the same plane, y = 0
    public ArbitraryPolygon(List<Vector> vertices, double height) {
        super();
        this.vertices = vertices;
        this.height = height;
    }

    @Override
    public List<org.bukkit.util.Vector> generateOutline() {
        this.points = MathUtil.connectPoints(vertices, particleDensity);
        points.addAll(MathUtil.calculateLine(vertices.get(0), vertices.get(vertices.size() - 1), particleDensity));
        List<Vector> upperPoints = new ArrayList<>();
        for (Vector v : points) {
            upperPoints.add(new Vector(v.getX(), height, v.getZ()));
        }
        points.addAll(upperPoints);
        for (Vector v : vertices) {
            points.addAll(MathUtil.calculateLine(v, new Vector(v.getX(), height, v.getZ()), particleDensity));
        }

        return points;
    }

    @Override
    public Shape particleCount(int count) {
        double perimeter = 0;
        for (int i = 0; i < vertices.size() - 1; i++) {
            perimeter += vertices.get(i).distance(vertices.get(i + 1));
        }
        perimeter += vertices.get(0).distance(vertices.get(vertices.size() - 1));
        perimeter *= 2;
        perimeter += vertices.size() * height;
        particleDensity = perimeter / count;
        return this;
    }

    @Override
    public Shape clone() {
        ArbitraryPolygon clone = new ArbitraryPolygon(vertices, height);
        this.copyTo(clone);
        return clone;
    }
}
