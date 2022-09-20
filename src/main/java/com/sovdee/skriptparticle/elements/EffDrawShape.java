package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.log.ErrorQuality;
import ch.njol.util.Kleenean;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.List;

public class EffDrawShape extends Effect {

    static {
        Skript.registerEffect(EffDrawShape.class,
                "draw [shape[s]] %shapes% [location:[centered] at %-location%] [particle:(with|using) %-particlebuilder%]");
    }

    Expression<Shape> shapeExpr;
    Expression<Location> locationExpr;
    Expression<ParticleBuilder> particleExpr;

    @Override
    protected void execute(Event event) {
        Shape[] shapes = shapeExpr.getAll(event);
        if (shapes.length == 0) return;

        ParticleBuilder particle = null;
        if (particleExpr != null) {
            particle = particleExpr.getSingle(event);
            if (particle == null) return;
        }

        Location location = locationExpr != null ? locationExpr.getSingle(event) : null;

        for (Shape shape : shapes) {
            if (shape == null) continue;

            if (particle == null) {
                if (shape.getParticle() != null) {
                    particle = shape.getParticle();
                } else {
                    particle = new ParticleBuilder(Particle.FLAME).count(0);
                }
            }

            if (location == null){
                location = shape.getCenter();
                if (location == null) {
                    Skript.error("Shape has no location. Please use the 'centered at' draw syntax or define the shape with a location.", ErrorQuality.SEMANTIC_ERROR);
                    return;
                }
            }

            List<Location> locs = shape.getLocations(location);
            for (Location loc : locs) {
                particle.location(loc).receivers(loc.getWorld().getPlayers()).spawn();
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "Draw shape " + shapeExpr.getSingle(event);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) exprs[0];
        if (parseResult.hasTag("location"))
            locationExpr = (Expression<Location>) exprs[1];
        if (parseResult.hasTag("particle"))
            particleExpr = (Expression<ParticleBuilder>) exprs[2];
        return true;
    }
}
