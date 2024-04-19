package com.sovdee.skriptparticles.util;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Point<T> {

    private final T rawValue;
    private final Class<T> type;
    private boolean isDynamic;

    public Point(T value, Class<T> type) {
        this(value, type, false);
    }

    private Point(T value, Class<T> type, boolean isDynamic) {
        this.rawValue = value;
        this.type = type;
        this.isDynamic = isDynamic;
    }

    public static List<Point<?>> of(List<?> values) {
        List<Point<?>> points = new ArrayList<>();
        for (Object value : values) {
            points.add(of(value));
        }
        return points;
    }

    @Nullable
    @Contract("!null -> !null")
    public static Point<?> of(@Nullable Object value) {
        if (value == null)
            return null;

        if (value instanceof Vector v) {
            return Point.of(v);
        } else if (value instanceof Entity v) {
            return Point.of(v);
        } else if (value instanceof Location v) {
            return Point.of(v);
        } else if (value instanceof DynamicLocation v) {
            return Point.of(v);
        }
        assert false;
        return null;
    }

    public static Point<Vector> of(Vector value) {
        return new Point<>(value.clone(), Vector.class);
    }

    public static Point<Location> of(Location value) {
        return new Point<>(value.clone(), Location.class);
    }

    public static Point<Entity> of(Entity value) {
        return new Point<>(value, Entity.class, true);
    }

    public static Point<DynamicLocation> of(DynamicLocation value) {
        return new Point<>(value, DynamicLocation.class, value.isDynamic());
    }



    public DynamicLocation getDynamicLocation() {
        if (type == Vector.class)
            return new DynamicLocation();

        if (type == Location.class)
            return new DynamicLocation((Location) rawValue);

        if (type == Entity.class)
            return new DynamicLocation((Entity) rawValue);

        if (type == DynamicLocation.class)
            return new DynamicLocation((DynamicLocation) rawValue);

        assert false;
        return null;
    }

    @Nullable
    @Contract(pure = true)
    public Location getLocation() {
        return getLocation(null);
    }

    @Nullable
    @Contract(value = "!null -> new", pure = true)
    public Location getLocation(@Nullable Location origin) {
        if (type == Vector.class) {
            if (origin == null) return null;
            return origin.clone().add((Vector) rawValue);
        }

        if (type == Location.class)
            return ((Location) rawValue).clone();

        if (type == Entity.class)
            return ((Entity) rawValue).getLocation().clone();

        if (type == DynamicLocation.class)
            return ((DynamicLocation) rawValue).getLocation();

        assert false;
        return null;
    }

    @Contract(value = "_ -> new", pure = true)
    public Vector getVector(@Nullable Location origin) {
        if (type == Vector.class)
            return ((Vector) rawValue).clone();

        if (origin == null)
            origin = new Location(null, 0, 0, 0);

        if (type == Location.class)
            return ((Location) rawValue).toVector().subtract(origin.toVector());

        if (type == Entity.class)
            return ((Entity) rawValue).getLocation().toVector().subtract(origin.toVector());

        if (type == DynamicLocation.class)
            return ((DynamicLocation) rawValue).getLocation().toVector().subtract(origin.toVector());

        return new Vector(0, 0, 0);
    }

    public T getRawValue() {
        return rawValue;
    }

    public Class<T> getType() {
        return type;
    }

    public boolean isDynamic() {
        return isDynamic;
    }

}