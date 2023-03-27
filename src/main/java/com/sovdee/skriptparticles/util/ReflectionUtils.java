package com.sovdee.skriptparticles.util;

import ch.njol.skript.Skript;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*
 * Thanks to ShaneBee at SkBee for the original code.
 * This is meant to fill the gap when SkBee isn't installed.
 */

public class ReflectionUtils {
    private static final String VERSION = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
    private static final boolean NEW_NMS = Skript.isRunningMinecraft(1, 17);
    private static final boolean DEBUG = false; //SkBee.getPlugin().getPluginConfig().SETTINGS_DEBUG;

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

    public static Class<?> getNMSClass(String nmsClass, String nmsPackage) {
        try {
            if (NEW_NMS) {
                return Class.forName(nmsPackage + "." + nmsClass);
            } else {
                return Class.forName("net.minecraft.server." + VERSION + nmsClass);
            }
        } catch (ClassNotFoundException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object getNMSHandle(Entity entity) {
        try {
            Method getHandle = entity.getClass().getMethod("getHandle");
            return getHandle.invoke(entity);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            if (DEBUG) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static Object getField(String field, Class<?> clazz, Object object) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            return f.get(object);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    public static void setField(String field, Class<?> clazz, Object object, Object toSet) {
        try {
            Field f = clazz.getDeclaredField(field);
            f.setAccessible(true);
            f.set(object, toSet);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public static void setField(String field, Object object, Object toSet) {
        try {
            Field f = object.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(object, toSet);
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException ex) {
            if (DEBUG) {
                ex.printStackTrace();
            }
        }
    }

    public static Object getConnection(Player player)
            throws SecurityException, NoSuchMethodException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Field conField = getHandle(player).getClass().getField("playerConnection");
        return conField.get(getHandle(player));
    }

    public static Object getHandle(Player player)
            throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method getHandle = player.getClass().getMethod("getHandle");
        return getHandle.invoke(player);
    }

    /**
     * Check if a class and method exist
     *
     * @param className  Class to check
     * @param methodName Method to check
     * @return True if both class and method exist
     */
    public static boolean methodExists(String className, String methodName) {
        if (Skript.classExists(className)) {
            try {
                Class<?> clazz = Class.forName(className);
                if (Skript.methodExists(clazz, methodName)) {
                    return true;
                }
            } catch (ClassNotFoundException ignore) {
            }
        }
        return false;
    }

    public static class ReflectionConstants {

        public static String MINECRAFT_KEY_GET_KEY_METHOD = get("getKey", "a", "a");
        public static String TAG_VISITOR_VISIT_METHOD = get("a", "a", "a");
        public static String ENTITY_NO_CLIP_FIELD = get("P", "Q", "Q");
        public static String NMS_SCOREBOARD_TEAM_SET_PREFIX_METHOD = get("setPrefix", "b", "b");
        public static String NMS_SCOREBOARD_TEAM_SET_SUFFIX_METHOD = get("setSuffix", "c", "c");
        public static String NMS_ITEMSTACK_GET_HOVER_NAME_METHOD = get("getName", "v", "v");
        public static String NMS_CHAT_MESSAGE_GET_KEY_METHOD = get("getKey", "i", "i");

        private static String get(String v117, String v118, String v119) {
            if (Skript.isRunningMinecraft(1, 19)) {
                return v119;
            } else if (Skript.isRunningMinecraft(1, 18)) {
                return v118;
            } else if (Skript.isRunningMinecraft(1, 17)) {
                return v117;
            }
            throw new IllegalArgumentException("Unknown Version");
        }

    }
}
