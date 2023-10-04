package com.sovdee.skriptparticles.util;

import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.Nullable;

/*
 * Thanks to ShaneBee at SkBee for the original code.
 * This is meant to fill the gap when SkBee isn't installed.
 */

public class ReflectionUtils {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    private static final boolean DEBUG = false; //SkBee.getPlugin().getPluginConfig().SETTINGS_DEBUG;

    @Nullable
    public static Class<?> getOBCClass(String obcClassString) {
        String name = "org.bukkit.craftbukkit." + VERSION + obcClassString;
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
