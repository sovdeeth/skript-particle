package com.sovdee.skriptparticles.shapes;

import com.sovdee.shapes.Cuboid;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.Location;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Set;

/**
 * A cuboid that tracks dynamic locations (entities) for its corners.
 */
public class DynamicCuboid extends Cuboid {

    private final DynamicLocation negativeCorner;
    private final DynamicLocation positiveCorner;

    public DynamicCuboid(DynamicLocation cornerA, DynamicLocation cornerB) {
        super(computeLength(cornerA, cornerB), computeWidth(cornerA, cornerB), computeHeight(cornerA, cornerB));
        this.negativeCorner = cornerA.clone();
        this.positiveCorner = cornerB.clone();
    }

    private static double computeLength(DynamicLocation a, DynamicLocation b) {
        return Math.max(Math.abs(b.getLocation().getX() - a.getLocation().getX()), 0.001);
    }

    private static double computeWidth(DynamicLocation a, DynamicLocation b) {
        return Math.max(Math.abs(b.getLocation().getZ() - a.getLocation().getZ()), 0.001);
    }

    private static double computeHeight(DynamicLocation a, DynamicLocation b) {
        return Math.max(Math.abs(b.getLocation().getY() - a.getLocation().getY()), 0.001);
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
        this.setLength(Math.abs(pos.getX() - neg.getX()));
        this.setWidth(Math.abs(pos.getZ() - neg.getZ()));
        this.setHeight(Math.abs(pos.getY() - neg.getY()));
        super.generatePoints(points);
    }

    public DynamicLocation getNegativeCorner() { return negativeCorner; }
    public DynamicLocation getPositiveCorner() { return positiveCorner; }

    @Override
    public Shape clone() {
        DynamicCuboid cuboid = new DynamicCuboid(negativeCorner, positiveCorner);
        return this.copyTo(cuboid);
    }
}
