package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;

@Name("Shape Locations")
@Description({
        "Returns all the locations that particles would be spawned at if the shape was centered at the given location.",
        "If you want the vectors relative to the center of the shape, use the 'shape points' expression."
})
@Examples({
        "set {_locations::*} to locations of (circle of radius 10) centered at player",
        "teleport player to random element of {_locations::*}",
        "",
        "# drawing the shape yourself: (skbee particle syntax)",
        "draw 1 dust using dustOptions(red, 1) at (locations of (circle of radius 10) centered at player)"
})
@Since("1.0.0")
public class ExprShapeLocations extends SimpleExpression<Location> {

    static {
        Skript.registerExpression(ExprShapeLocations.class, Location.class, ExpressionType.COMBINED, "[particle] locations of %shapes% [[centered] %direction% %location%]");
    }

    private Expression<Shape> shapeExpr;
    private Expression<Location> locationExpr;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) exprs[0];
        locationExpr = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<Location>) exprs[2]);
        return true;
    }

    @Override
    @Nullable
    protected Location[] get(Event event) {
        Shape[] shapes = shapeExpr.getAll(event);
        if (shapes.length == 0) return new Location[0];

        @Nullable Location center = locationExpr.getSingle(event);

        if (center == null) return new Location[0];


        ArrayList<Location> locations = new ArrayList<>();
        for (Shape shape : shapes) {
            locations.addAll(shape.getPoints().stream().map(point -> center.clone().add(point)).toList());
        }
        return locations.toArray(new Location[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NonNull Class<? extends Location> getReturnType() {
        return Location.class;
    }

    @Override
    public @NonNull String toString(@Nullable Event event, boolean debug) {
        return "Locations of shapes";
    }

}
