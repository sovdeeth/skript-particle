package com.sovdee.skriptparticles.elements.expressions.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.util.Direction;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.particles.Particle;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.List;

@Name("Draw Particle - Skript-Particles")
@Description({
        "Draws a particle at a location. Syntax inspired by SkBee, so bee careful not to get the two confused.",
        "If you run into conflicts with SkBee, which you shouldn't, please report it immediately."
})
@Examples({
        "draw flame particle at player",
        "draw 10 of dust particle using dustOption(red, 1) at player for player",
})
@Since("1.0.0")
public class EffDrawParticle extends Effect {

    static {
        Skript.registerEffect(EffDrawParticle.class, "draw %customparticles% %directions% %locations% [(for|to) %-players%]");
    }

    private Expression<Particle> particles;
    private Expression<Location> locations;
    private Expression<Player> players;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        particles = (Expression<Particle>) exprs[0];
        locations = Direction.combine((Expression<? extends Direction>) exprs[1], (Expression<? extends Location>) exprs[2]);
        players = (Expression<Player>) exprs[3];
        return true;
    }

    @Override
    protected void execute(Event event) {
        List<Player> recipients = null;
        if (players != null) {
            recipients = List.of(players.getArray(event));
        }
        for (Particle particle : particles.getArray(event)) {
            particle = particle.clone();
            if (recipients != null) {
                particle.receivers(recipients);
            }
            for (Location location : locations.getArray(event)) {
                particle.location(location).spawn();
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "draw " + particles.toString(event, debug) + " at " + locations.toString(event, debug) + " " + (players != null ? "to " + players.toString(event, debug) : "");
    }

}
