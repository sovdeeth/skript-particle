package com.sovdee.skriptparticles.util;

import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.Nullable;

/*
 * Thanks to ShaneBee at SkBee for the original code.
 * This is meant to fill the gap when SkBee isn't installed.
 */

public class ReflectionUtils {
    private static final String CRAFTBUKKIT_PACKAGE = Bukkit.getServer().getClass().getPackage().getName();
    private static final boolean DEBUG = false; //SkBee.getPlugin().getPluginConfig().SETTINGS_DEBUG;

    @Nullable
    public static Class<?> getOBCClass(String obcClassString) {
        String name = CRAFTBUKKIT_PACKAGE + obcClassString;
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
