package com.sovdee.shapes.shapes;

import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * One instance of every shape type (and the notable variants of each), freshly constructed on every call.
 */
final class TestShapes {

    private TestShapes() {}

    static List<Shape> all() {
        List<Shape> shapes = new ArrayList<>();
        shapes.add(new Circle(3));
        shapes.add(new Circle(3, 2));
        shapes.add(new Arc(3, Math.PI / 2));
        shapes.add(new Arc(3, 2, Math.PI / 2));
        shapes.add(new Ellipse(4, 1));
        shapes.add(new Ellipse(4, 1, 2));
        shapes.add(new EllipticalArc(4, 2, 2, 2.0));
        shapes.add(new Ellipsoid(4, 1, 2));
        shapes.add(new Sphere(3));
        shapes.add(new SphericalCap(3, 1.0));
        shapes.add(new Helix(2, 5, 1 / (2 * Math.PI)));
        shapes.add(new RegularPolygon(6, 3.0));
        shapes.add(new RegularPolygon(6, 3.0, 2.0));
        shapes.add(new Rectangle(4, 3, Rectangle.Plane.XZ));
        shapes.add(new Cuboid(3, 3, 3));
        shapes.add(new Star(1, 3, 5));
        shapes.add(new Line(new Vector3d(5, 0, 0)));
        shapes.add(new RegularPolyhedron(3, 4));
        shapes.add(new RegularPolyhedron(3, 8));
        shapes.add(new RegularPolyhedron(3, 12));
        shapes.add(new RegularPolyhedron(3, 20));
        shapes.add(new Heart(3, 3, 3));
        shapes.add(new BezierCurve(List.of(new Vector3d(0, 0, 0), new Vector3d(1, 2, 0), new Vector3d(3, 0, 0))));
        shapes.add(new IrregularPolygon(List.of(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0), new Vector3d(1, 0, 2))));
        shapes.add(new IrregularPolygon(List.of(new Vector3d(0, 0, 0), new Vector3d(2, 0, 0), new Vector3d(1, 0, 2)), 2.0));
        return shapes;
    }
}
