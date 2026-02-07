package com.sovdee.skriptparticles.shapes;

import com.sovdee.shapes.Line;
import com.sovdee.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.joml.Quaterniond;
import org.joml.Vector3d;

import java.util.Set;

/**
 * A line that tracks dynamic locations (entities).
 * Recalculates start/end vectors from DynamicLocations before each point generation.
 */
public class DynamicLine extends Line {

    private final DynamicLocation startLocation;
    private final DynamicLocation endLocation;

    public DynamicLine(DynamicLocation start, DynamicLocation end) {
        super(new Vector3d(0, 0, 0), toRelativeVector(start, end));
        this.startLocation = start.clone();
        this.endLocation = end.clone();
    }

    private static Vector3d toRelativeVector(DynamicLocation start, DynamicLocation end) {
        org.bukkit.util.Vector startVec = start.getLocation().toVector();
        org.bukkit.util.Vector endVec = end.getLocation().toVector();
        org.bukkit.util.Vector diff = endVec.subtract(startVec);
        return new Vector3d(diff.getX(), diff.getY(), diff.getZ());
    }

    @Override
    public Set<Vector3d> getPoints(Quaterniond orientation) {
        Set<Vector3d> points = super.getPoints(orientation);
        this.setNeedsUpdate(true);
        return points;
    }

    @Override
    public void generatePoints(Set<Vector3d> points) {
        Vector3d relativeEnd = toRelativeVector(startLocation, endLocation);
        this.setStart(new Vector3d(0, 0, 0));
        this.setEnd(relativeEnd);
        super.generatePoints(points);
    }

    public DynamicLocation getStartLocation() { return startLocation; }
    public DynamicLocation getEndLocation() { return endLocation; }

    @Override
    public Shape clone() {
        DynamicLine line = new DynamicLine(this.startLocation, this.endLocation);
        return this.copyTo(line);
    }
}
