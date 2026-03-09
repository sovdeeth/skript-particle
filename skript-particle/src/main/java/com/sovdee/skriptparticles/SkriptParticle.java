package com.sovdee.skriptparticles;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import ch.njol.skript.bstats.bukkit.Metrics;
import ch.njol.skript.util.Version;
import com.sovdee.skriptparticles.skript.SkriptParticleModule;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

/**
 * Entry point for the skript-particle plugin. Extends {@link JavaPlugin} and acts as the
 * central hub for registering the Skript addon, loading all element classes, and providing
 * static logging helpers.
 * <br>
 * On enable, the plugin verifies that Skript 2.7.0 or later is present, registers itself as a
 * {@link SkriptAddon}, loads all Skript element classes under {@code com.sovdee.skriptparticles},
 * and initialises bStats metrics. The static {@link #getInstance()} and {@link #getAddonInstance()}
 * accessors return {@code null} after the plugin has been disabled.
 */
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

    /**
     * Returns the singleton instance of this plugin, or {@code null} if the plugin has not yet
     * been enabled or has already been disabled.
     *
     * @return the current {@link SkriptParticle} instance, or {@code null}
     */
    @Nullable
    public static SkriptParticle getInstance() {
        return instance;
    }

    /**
     * Returns the {@link SkriptAddon} registered for this plugin, or {@code null} if the plugin
     * has not yet been enabled or has already been disabled.
     *
     * @return the registered {@link SkriptAddon}, or {@code null}
     */
    @Nullable
    public static SkriptAddon getAddonInstance() {
        return addon;
    }

    /**
     * Logs an informational message to the plugin logger. Does nothing if the logger has not yet
     * been initialised (i.e. before {@link #onEnable()} has run).
     *
     * @param message the message to log
     */
    public static void info(String message) {
        if (logger == null)
            return;
        logger.info(message);
    }

    /**
     * Logs a warning message to the plugin logger. Does nothing if the logger has not yet been
     * initialised.
     *
     * @param message the message to log
     */
    public static void warning(String message) {
        if (logger == null)
            return;
        logger.warning(message);
    }

    /**
     * Logs a severe (error-level) message to the plugin logger. Does nothing if the logger has not
     * yet been initialised.
     *
     * @param message the message to log
     */
    public static void severe(String message) {
        if (logger == null)
            return;
        logger.severe(message);
    }

    /**
     * Logs a debug message at INFO level, but only when Skript's debug mode is active.
     * Does nothing if the logger has not yet been initialised.
     *
     * @param message the message to log
     */
    public static void debug(String message) {
        if (logger == null)
            return;
        if (Skript.debug()) {
            logger.info(message);
        }
    }

    /**
     * Initialises the plugin. Verifies that Skript 2.7.0+ is present, registers the
     * {@link SkriptAddon}, loads all element classes under {@code com.sovdee.skriptparticles},
     * and starts bStats metrics collection. Disables the plugin if any prerequisite check fails.
     */
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
        addon.loadModules(new SkriptParticleModule());
        new Metrics(this, 18457);
        SkriptParticle.info("Successfully enabled skript-particle.");
    }

    /**
     * Cleans up plugin state on shutdown. Clears the singleton {@link #instance} and
     * {@link #addon} references so they become {@code null} for the remainder of the JVM session.
     */
    @Override
    public void onDisable() {
        instance = null;
        addon = null;
    }
}
