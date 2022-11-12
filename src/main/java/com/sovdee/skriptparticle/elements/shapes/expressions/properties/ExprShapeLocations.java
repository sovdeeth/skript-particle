package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ExprShapeLocations extends SimpleExpression<Location> {

    static {
        Skript.registerExpression(ExprShapeLocations.class, Location.class, ExpressionType.COMBINED, "[particle] locations of %shapes% [[centered] at %-location%]");
    }

    private Expression<Shape> shapeExpr;
    private Expression<Location> locationExpr;

    @Override
    protected @Nullable Location[] get(Event event) {
        Shape[] shapes = shapeExpr.getAll(event);
        if (shapes.length == 0) return null;

        Location center = locationExpr != null ? locationExpr.getSingle(event) : null;

        ArrayList<Location> locations = new ArrayList<>();
        for (Shape shape : shapes) {
            if (shape == null) continue;
            if (center == null && shape.center() == null) {
                return null;
            } else if (center != null) {
                locations.addAll(shape.locations(center, Quaternion.IDENTITY));
            } else {
                locations.addAll(shape.locations());
            }
        }
        return locations.toArray(new Location[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "Locations of shapes";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) exprs[0];
        if (exprs.length > 1)
            locationExpr = (Expression<Location>) exprs[1];
        return true;
    }
}
