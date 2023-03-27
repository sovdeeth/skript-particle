package com.sovdee.skriptparticles;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class SkriptParticle extends JavaPlugin {

    private static SkriptParticle instance;
    private static SkriptAddon addon;
    private static Logger logger;


    // todo before alpha
    // SIMPLE
        // Properties
    // MODERATE
        // proper particle density for regular polyhedra
        // Particle motion (vector, inwards, outwards)
        // Particle section
            // create a new %particle% [particle]:
            //- count: int
            //- offset: vector
            //- velocity: vector or inwards/outwards (exclusive w/ offset & count)
            //- extra: double
            //- data: dustOptions, item, etc. (take skbee code)
            //- - if particle is dust, allow "color: color" and "size: double"
            //- force: boolean
    // HARD/LONG
        // Surface fill for arbitrary polygons
        // DOCUMENTATION

    // Eventually:
    // Better interface use
    // everything for bezier curves
    // Arbitrary 3d shapes w/ points
    // Shapes defined by equations
    //  - Chladni patterns?



    @Override
    public void onEnable() {
        instance = this;
        addon = Skript.registerAddon(this);
        logger = this.getLogger();
        try {
            addon.loadClasses("com.sovdee.skriptparticles");
        } catch (IOException e) {
            e.printStackTrace();
        }
        SkriptParticle.info("Skript-Particle has been enabled.");

    }

    public static SkriptParticle getInstance() {
        return instance;
    }

    public static SkriptAddon getAddonInstance() {
        return addon;
    }

    public static void info(String message) {
        logger.info(message);
    }

    public static void warning(String message) {
        logger.warning(message);
    }

    public static void severe(String message) {
        logger.severe(message);
    }

    public static void debug(String message) {
        if (Skript.debug()) {
            logger.info(message);
        }
    }
}
