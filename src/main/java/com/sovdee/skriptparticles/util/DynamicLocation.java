package com.sovdee.skriptparticles.util;

import ch.njol.skript.util.Direction;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

/**
 * A dynamic location is a location that can update over time
 * If the location is created with an entity, it will update to the entity's location
 * If the location is created with a location, it will update to the location
 * If the location has a direction, it will apply the direction to the location when getLocation() is called
 */
public class DynamicLocation {

    private @Nullable Entity entity;
    private @Nullable Location location;
    private @Nullable Direction direction;

    /**
     * Creates a dynamic location with the given entity
     * @param entity the entity to create the dynamic location from
     */
    public DynamicLocation(Entity entity) {
        this.entity = entity;
    }

    /**
     * Creates a dynamic location with the given location
     * @param location the location to create the dynamic location from
     */
    public DynamicLocation(Location location) {
        this.location = location.clone();
    }

    /**
     * Creates a dynamic location with the given location and direction
     * @param location the location to create the dynamic location from
     * @param direction the direction to create the dynamic location from
     */
    public DynamicLocation(Location location, @Nullable Direction direction) {
        this.location = location.clone();
        this.direction = direction;
    }

    /**
     * Creates a dynamic location with the given entity and direction
     * @param entity the entity to create the dynamic location from
     * @param direction the direction to create the dynamic location from
     */
    public DynamicLocation(Entity entity, @Nullable Direction direction) {
        this.entity = entity;
        this.direction = direction;
    }

    /**
     * Creates a dynamic location with the given dynamic location. This will copy any {@link Location}, but not the {@link Entity} or {@link Direction}.
     * @param dynamicLocation the dynamic location to create the dynamic location from.
     */
    public DynamicLocation(DynamicLocation dynamicLocation) {
        this.entity = dynamicLocation.getEntity();
        this.direction = dynamicLocation.getDirection();
        this.location = dynamicLocation.getLocation();
    }

    /**
     * Creates a dynamic location with no entity or location.
     * This is used for the default value of the dynamic location.
     */
    public DynamicLocation() {
        this.location = null;
        this.entity = null;
        this.direction = null;
    }

    /**
     * Creates a dynamic location with the given location or entity.
     *
     * @param locationEntity the location or entity to create the dynamic location from
     * @return the dynamic location created with the location entity
     */
    @Nullable
    public static DynamicLocation fromLocationEntity(Object locationEntity) {
        if (locationEntity instanceof Location location) {
            return new DynamicLocation(location);
        } else if (locationEntity instanceof Entity entity) {
            return new DynamicLocation(entity);
        }
        return null;
    }

    /**
     * Gets the current location of the dynamic location
     * If not dynamic, returns the location
     * If dynamic, returns the location of the entity
     * If direction is set, applies the direction to the location
     * If the location is null, returns origin in null world
     * @return the current location of the dynamic location
     */
    public Location getLocation() {
        @Nullable Location location = this.location;

        if (entity != null)
            location = entity.getLocation();

        if (direction != null && location != null)
            location = direction.getRelative(location);

        return (location == null) ? new Location(null, 0, 0, 0) : location.clone();
    }

    /**
     * Sets the dynamic location to the given location.
     * If the dynamic location is already referencing an entity, the entity reference will be dropped.
     *
     * @param location the location to set the dynamic location to
     */
    public void setLocation(Location location) {
        this.location = location;
        this.entity = null;
    }

    /**
     * Gets the current entity of the dynamic location, or null if it is not set.
     *
     * @return the current entity of the dynamic location
     */
    @Nullable
    public Entity getEntity() {
        return entity;
    }

    /**
     * Sets the dynamic location to the given entity.
     * If the dynamic location is already referencing a location, the location reference will be dropped.
     *
     * @param entity the entity to set the dynamic location to
     */
    public void setEntity(Entity entity) {
        this.entity = entity;
        this.location = null;
    }

    /**
     * Gets the current direction of the dynamic location, or null if it is not set.
     *
     * @return the current direction of the dynamic location
     */
    @Nullable
    public Direction getDirection() {
        return direction;
    }

    /**
     * Sets the direction of the dynamic location.
     *
     * @param direction the direction to set the dynamic location to
     */
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    /**
     * Checks if the dynamic location is dynamic (tracking a moving entity).
     *
     * @return true if the dynamic location is dynamic, false otherwise
     */
    public boolean isDynamic() {
        return entity != null;
    }

    /**
     * Clones the dynamic location. This will copy any {@link Location}, but not the {@link Entity} or {@link Direction}.
     * @return the cloned dynamic location
     */
    @Contract(value = " -> new", pure = true)
    public DynamicLocation clone() {
        return new DynamicLocation(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof DynamicLocation dynamicLocation))
            return false;
        boolean matchingEntity = entity != null && dynamicLocation.entity != null && entity.equals(dynamicLocation.entity);
        boolean matchingLocation = location != null && dynamicLocation.location != null && location.equals(dynamicLocation.location);
        boolean matchingDirection = direction != null && dynamicLocation.direction != null && direction.equals(dynamicLocation.direction);
        if (matchingEntity || matchingLocation) {
            return matchingDirection || (direction == null && dynamicLocation.direction == null);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        if (entity != null)
            return entity.toString();
        else if (location != null)
            return location.toString();
        else
            return "DynamicLocation{null}";
    }

    public boolean isNull() {
        if (entity == null && location == null)
            return true;
        else if (entity != null && entity.isDead())
            return true;
        else return location != null && location.getWorld() == null;
    }
}
