package com.sovdee.skriptparticles;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.util.Version;
import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.logging.Logger;

public class SkriptParticle extends JavaPlugin {

    private static SkriptParticle instance;
    private static SkriptAddon addon;
    private static Logger logger;


    // todo, next release
    // custom shapes
    // icosphere
    // expressions for particles
    // todo, later versions
    // beziers
    // better triangle filling (basically allow any 3d model)
    // gradients
    // text rendering

    @Nullable
    public static SkriptParticle getInstance() {
        return instance;
    }

    @Nullable
    public static SkriptAddon getAddonInstance() {
        return addon;
    }

    public static void info(String message) {
        if (logger == null)
            return;
        logger.info(message);
    }

    public static void warning(String message) {
        if (logger == null)
            return;
        logger.warning(message);
    }

    public static void severe(String message) {
        if (logger == null)
            return;
        logger.severe(message);
    }

    public static void debug(String message) {
        if (logger == null)
            return;
        if (Skript.debug()) {
            logger.info(message);
        }
    }

    @Override
    public void onEnable() {
        final PluginManager manager = this.getServer().getPluginManager();
        final Plugin skript = manager.getPlugin("Skript");
        logger = this.getLogger();
        if (skript == null || !skript.isEnabled()) {
            SkriptParticle.severe("Could not find Skript! Make sure you have it installed and that it properly loaded. Disabling...");
            manager.disablePlugin(this);
            return;
        } else if (Skript.getVersion().compareTo(new Version(2, 7, 0)) < 0) {
            SkriptParticle.severe("You are running an unsupported version of Skript. Please update to at least Skript 2.7.0. Disabling...");
            manager.disablePlugin(this);
            return;
        }
        instance = this;
        addon = Skript.registerAddon(this);
        addon.setLanguageFileDirectory("lang");
        try {
            addon.loadClasses("com.sovdee.skriptparticles");
        } catch (IOException error) {
            error.printStackTrace();
            manager.disablePlugin(this);
            return;
        }
        new Metrics(this, 18457);
        SkriptParticle.info("Successfully enabled skript-particle.");
    }

    @Override
    public void onDisable() {
        instance = null;
        addon = null;
    }
}
