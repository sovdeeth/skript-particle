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


    // todo before beta
    // Irregular Polygons
    // Regular Polyhedra
    // Fix Ellipse/Ellipsoid performance?
    // bstats
    // TEST EVERYTHING



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
