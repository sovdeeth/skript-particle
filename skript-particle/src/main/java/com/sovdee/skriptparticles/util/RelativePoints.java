package com.sovdee.skriptparticles.util;

import org.bukkit.Location;
import org.joml.Vector3d;

import java.util.function.Supplier;

/**
 * Helpers for shapes that are defined by locations or entities in the world.
 * <p>
 * Shapes are always drawn relative to a location, so a shape defined by world positions is built from the
 * positions of its points relative to an anchor, and then drawn at that anchor.
 */
public final class RelativePoints {

    private RelativePoints() {}

    /**
     * @return the current position of {@code point}, relative to the current position of {@code anchor}
     */
    public static Vector3d offset(DynamicLocation anchor, DynamicLocation point) {
        Location anchorLocation = anchor.getLocation();
        Location pointLocation = point.getLocation();
        return new Vector3d(
                pointLocation.getX() - anchorLocation.getX(),
                pointLocation.getY() - anchorLocation.getY(),
                pointLocation.getZ() - anchorLocation.getZ());
    }

    /**
     * @return a supplier of the position of {@code point} relative to {@code anchor}, which follows both as they move
     */
    public static Supplier<Vector3d> supplier(DynamicLocation anchor, DynamicLocation point) {
        return () -> offset(anchor, point);
    }

    /**
     * @return a supplier of the anchor's own relative position, which is always the origin
     */
    public static Supplier<Vector3d> origin() {
        return Vector3d::new;
    }
}
