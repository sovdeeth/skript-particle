package com.sovdee.skriptparticles.util;

import ch.njol.skript.util.Direction;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DynamicLocation extends Location {

    private Entity entity;
    private Direction direction;

    public DynamicLocation(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public DynamicLocation(@NotNull Entity entity) {
        super(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
        this.entity = entity;
    }

    public DynamicLocation(@NotNull Location location) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
    }

    public DynamicLocation(@NotNull Location location, @Nullable Direction direction) {
        super(location.getWorld(), location.getX(), location.getY(), location.getZ());
        this.direction = direction;
    }

    public DynamicLocation(@NotNull Entity entity, @Nullable Direction direction) {
        super(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY(), entity.getLocation().getZ());
        this.entity = entity;
        this.direction = direction;
    }

    public DynamicLocation() {
        super(null, 0, 0, 0);
    }

    /*
     * Gets the current location of the dynamic location
     * If not dynamic, returns the location
     * If dynamic, returns the location of the entity
     * If direction is set, applies the direction to the location
     * @return the current location of the dynamic location
     */
    public Location getLocation() {
        Location location;
        if (entity != null) {
            location = entity.getLocation();
        } else {
            if (getWorld() == null) {
                return null;
            }
            location = this;
        }
        if (direction != null) {
            location = direction.getRelative(location);
        }
        return location;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    @Nullable
    public Entity getEntity() {
        return entity;
    }

    public void setSkriptDirection(Direction direction) {
        this.direction = direction;
    }

    @Nullable
    public Direction getSkriptDirection() {
        return direction;
    }

    public boolean isDynamic() {
        return entity != null;
    }

}
