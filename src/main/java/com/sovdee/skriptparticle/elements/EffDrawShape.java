package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.particles.CustomParticle;
import com.sovdee.skriptparticle.shapes.Line;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class EffDrawShape extends Effect {

    static {
        Skript.registerEffect(EffDrawShape.class,
                "draw [shape[s]] %shapes% [centered] at %location% [(with|using) %-customparticle%]",
                "draw [shape[s]] %lines% [(with|using) %-customparticle%]");
    }

    Expression<Shape> shapeExpr;
    Expression<Location> locationExpr;
    Expression<CustomParticle> particleExpr;

    private boolean useLineLocations;

    @Override
    protected void execute(Event e) {
        Shape[] shapes = shapeExpr.getAll(e);
        if (shapes.length == 0) return;

        CustomParticle particle = null;
        if (particleExpr != null) {
            particle = particleExpr.getSingle(e);
            if (particle == null) return;
        }

        Location location = null;
        World world = null;

        if (!useLineLocations) {
            location = locationExpr.getSingle(e);
            if (location == null) return;
            world = location.getWorld();
        }

        for (Shape shape : shapes) {
            if (shape == null) continue;

            if (particle == null) {
                if (shape.getParticle() != null) {
                    particle = shape.getParticle();
                } else {
                    particle = new CustomParticle(Particle.FLAME, 0);
                }
            }

            if (useLineLocations) {
                location = ((Line) shape).getStartLocation();
                if (location == null) {
                    Skript.error("Line has no start location. Please use the 'centered at' draw syntax or define the line with a start location.", ErrorQuality.SEMANTIC_ERROR);
                    return;
                };
            }
            Location[] locs = shape.getLocations(location);
            for (Location loc : locs) {
                particle.drawParticle(loc);
            }
        }
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "Draw shape " + shapeExpr.getSingle(e) + " at " + locationExpr.getSingle(e);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) exprs[0];
        if (matchedPattern == 0) {
            locationExpr = (Expression<Location>) exprs[1];
            particleExpr = (Expression<CustomParticle>) exprs[2];
        } else {
            useLineLocations = true;
            particleExpr = (Expression<CustomParticle>) exprs[1];
        }
        return true;
    }
}
