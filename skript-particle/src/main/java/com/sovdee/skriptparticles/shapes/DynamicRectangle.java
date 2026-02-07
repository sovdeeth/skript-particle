package com.sovdee.skriptparticles.shapes;

import com.sovdee.shapes.Rectangle;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.Location;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Set;

/**
 * A rectangle that tracks dynamic locations (entities) for its corners.
 */
public class DynamicRectangle extends Rectangle {

    private final DynamicLocation negativeCorner;
    private final DynamicLocation positiveCorner;

    public DynamicRectangle(DynamicLocation cornerA, DynamicLocation cornerB, Plane plane) {
        super(computeLength(cornerA, cornerB, plane), computeWidth(cornerA, cornerB, plane), plane);
        this.negativeCorner = cornerA.clone();
        this.positiveCorner = cornerB.clone();
    }

    private static double computeLength(DynamicLocation a, DynamicLocation b, Plane plane) {
        Location al = a.getLocation();
        Location bl = b.getLocation();
        return switch (plane) {
            case XZ, XY -> Math.max(Math.abs(al.getX() - bl.getX()), 0.001);
            case YZ -> Math.max(Math.abs(al.getY() - bl.getY()), 0.001);
        };
    }

    private static double computeWidth(DynamicLocation a, DynamicLocation b, Plane plane) {
        Location al = a.getLocation();
        Location bl = b.getLocation();
        return switch (plane) {
            case XZ, YZ -> Math.max(Math.abs(al.getZ() - bl.getZ()), 0.001);
            case XY -> Math.max(Math.abs(al.getY() - bl.getY()), 0.001);
        };
    }

    @Override
    public Set<Vector3d> getPoints(Quaterniond orientation) {
        Set<Vector3d> points = super.getPoints(orientation);
        this.setNeedsUpdate(true);
        return points;
    }

    @Override
    public void generatePoints(Set<Vector3d> points) {
        Location neg = negativeCorner.getLocation();
        Location pos = positiveCorner.getLocation();
        Plane plane = getPlane();
        double length = switch (plane) {
            case XZ, XY -> Math.abs(neg.getX() - pos.getX());
            case YZ -> Math.abs(neg.getY() - pos.getY());
        };
        double width = switch (plane) {
            case XZ, YZ -> Math.abs(neg.getZ() - pos.getZ());
            case XY -> Math.abs(neg.getY() - pos.getY());
        };
        this.setLength(length);
        this.setWidth(width);
        super.generatePoints(points);
    }

    public DynamicLocation getNegativeCorner() { return negativeCorner; }
    public DynamicLocation getPositiveCorner() { return positiveCorner; }

    @Override
    public Shape clone() {
        DynamicRectangle rect = new DynamicRectangle(negativeCorner, positiveCorner, getPlane());
        return this.copyTo(rect);
    }
}
