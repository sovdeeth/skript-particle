package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Line;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class EffDrawShape extends Effect {

    static {
        Skript.registerEffect(EffDrawShape.class, "draw %shapes% (with|using) %particle% [centered] at %location%", "draw %lines% (with|using) %particle%");
    }

    Expression<Shape> shapeExpr;
    Expression<Location> locationExpr;
    Expression<Particle> particleExpr;

    private boolean useLineLocations;

    @Override
    protected void execute(Event e) {
        Shape[] shapes = shapeExpr.getAll(e);
        Particle particle = particleExpr.getSingle(e);
        if (shapes.length == 0 || particle == null) return;

        Location location = null;
        World world = null;

        if (!useLineLocations) {
            location = locationExpr.getSingle(e);
            if (location == null) return;
            world = location.getWorld();
        }

        for (Shape shape : shapes) {
            if (shape == null) continue;
            if (useLineLocations) {
                location = ((Line) shape).getStartLocation();
                if (location == null) {
                    Skript.error("Line has no start location. Please use the 'centered at' draw syntax or define the line with a start location.", ErrorQuality.SEMANTIC_ERROR);
                    return;
                };
                world = location.getWorld();
            }
            Location[] locs = shape.getLocations(location);
            for (Location loc : locs) {
                world.spawnParticle(particle, loc, 0, 0, 0, 0, 0);
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "Draw shape " + shapeExpr.getSingle(e) + " at " + locationExpr.getSingle(e) + " using " + particleExpr.getSingle(e);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) exprs[0];
        particleExpr = (Expression<Particle>) exprs[1];
        if (matchedPattern == 0)
            locationExpr = (Expression<Location>) exprs[2];
        else
            useLineLocations = true;

        return true;
    }
}
