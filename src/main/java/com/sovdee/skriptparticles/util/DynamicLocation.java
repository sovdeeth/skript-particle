package com.sovdee.skriptparticles.util;

import ch.njol.skript.util.Direction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A dynamic location is a location that can update over time
 * If the location is created with an entity, it will update to the entity's location
 * If the location is created with a location, it will update to the location
 * If the location has a direction, it will apply the direction to the location when getLocation() is called
 */
public class DynamicLocation {

    private Entity entity;
    private Location location;
    private Direction direction;
    public DynamicLocation(@NotNull Entity entity) {
        this(entity, null);
    }

    public DynamicLocation(@NotNull Location location) {
        this(location, null);
    }

    public DynamicLocation(@NotNull Location location, @Nullable Direction direction) {
        this.location = location.clone();
        this.direction = direction;
    }

    public DynamicLocation(@NotNull Entity entity, @Nullable Direction direction) {
        this.entity = entity;
        this.direction = direction;
    }

    public DynamicLocation(@NotNull DynamicLocation dynamicLocation) {
        this.entity = dynamicLocation.getEntity();
        this.direction = dynamicLocation.getDirection();
        this.location = dynamicLocation.getLocation();
    }

    /**
     * Creates a dynamic location with no entity or location
     * This is used for the default value of the dynamic location
     */
    public DynamicLocation() {
        this.location = null;
        this.entity = null;
        this.direction = null;
    }

    @Nullable
    public static DynamicLocation fromLocationEntity(Object locationEntity) {
        if (locationEntity instanceof Location location) {
            return new DynamicLocation(location);
        } else if (locationEntity instanceof Entity entity) {
            return new DynamicLocation(entity);
        }
        return null;
    }

    /*
     * Gets the current location of the dynamic location
     * If not dynamic, returns the location
     * If dynamic, returns the location of the entity
     * If direction is set, applies the direction to the location
     * If the location is null, returns origin in null world
     * @return the current location of the dynamic location
     */
    public Location getLocation() {
        Location location = this.location;

        if (entity != null)
            location = entity.getLocation();

        if (direction != null && location != null)
            location = direction.getRelative(location);

        if (location != null)
            location = location.clone();
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public boolean isDynamic() {
        return entity != null;
    }

    public DynamicLocation clone() {
        return new DynamicLocation(this);
    }

    @Override
    public String toString() {
        if (entity != null)
            return entity.toString();
        else if (location != null)
            return location.toString();
        else
            return "null location";
    }
}
