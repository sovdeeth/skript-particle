package com.sovdee.skriptparticles;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.util.logging.Logger;

public class SkriptParticle extends JavaPlugin {

    private static SkriptParticle instance;
    private static SkriptAddon addon;
    private static Logger logger;


    // todo before release
    // custom shapes
    // motion
    // icosphere
    // heart
    // stars
    // todo, later versions
    // beziers
    // better triangle filling (basically allow any 3d model)
    // gradients
    // text rendering
    // animation?


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
        int pluginId = 18457;
        Metrics metrics = new Metrics(this, pluginId);
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
