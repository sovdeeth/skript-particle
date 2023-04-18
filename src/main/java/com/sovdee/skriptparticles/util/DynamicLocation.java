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

    @Nullable
    public static DynamicLocation fromLocationEntity(Object locationEntity) {
        if (locationEntity instanceof Location) {
            return new DynamicLocation((Location) locationEntity);
        } else if (locationEntity instanceof Entity) {
            return new DynamicLocation((Entity) locationEntity);
        }
        return null;
    }

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
        this.location = location;
        this.direction = direction;
    }

    public DynamicLocation(@NotNull Entity entity, @Nullable Direction direction) {
        this.entity = entity;
        this.direction = direction;
    }

    public DynamicLocation(@NotNull DynamicLocation dynamicLocation) {
        this.entity = dynamicLocation.entity;
        this.direction = dynamicLocation.direction;
        this.location = dynamicLocation.location;
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

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    @Nullable
    public Direction getDirection() {
        return direction;
    }

    public void setLocation(Location location) {
        this.location = location;
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
