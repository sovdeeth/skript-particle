package com.sovdee.skriptparticles.util;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.Set;

/*
 * Thanks to ShaneBee at SkBee for the original code.
 */
public class ParticleUtil {
    private static final ParticleBuilder Y_AXIS = new ParticleBuilder(Particle.DUST).data(new DustOptions(DyeColor.LIME.getColor(), 0.5f));
    private static final ParticleBuilder X_AXIS = new ParticleBuilder(Particle.DUST).data(new DustOptions(DyeColor.RED.getColor(), 0.5f));
    private static final ParticleBuilder Z_AXIS = new ParticleBuilder(Particle.DUST).data(new DustOptions(DyeColor.BLUE.getColor(), 0.5f));

    public static com.sovdee.skriptparticles.particles.Particle getDefaultParticle() {
        return (com.sovdee.skriptparticles.particles.Particle) new com.sovdee.skriptparticles.particles.Particle(Particle.FLAME).count(1).extra(0);
    }

    public static void drawAxes(Location location, Quaternion orientation, Collection<Player> recipients) {
        Set<Vector> yAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(0, 1, 0), 0.2);
        Set<Vector> xAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(1, 0, 0), 0.2);
        Set<Vector> zAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(0, 0, 1), 0.2);

        yAxis = orientation.transform(yAxis);
        xAxis = orientation.transform(xAxis);
        zAxis = orientation.transform(zAxis);

        Y_AXIS.receivers(recipients);
        X_AXIS.receivers(recipients);
        Z_AXIS.receivers(recipients);

        for (Vector vector : yAxis) {
            Y_AXIS.location(location.clone().add(vector)).spawn();
        }

        for (Vector vector : xAxis) {
            X_AXIS.location(location.clone().add(vector)).spawn();
        }

        for (Vector vector : zAxis) {
            Z_AXIS.location(location.clone().add(vector)).spawn();
        }
    }
}
