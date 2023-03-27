package com.sovdee.skriptparticle.util;

import ch.njol.skript.aliases.ItemType;
import ch.njol.util.StringUtils;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.*;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/*
 * Thanks to ShaneBee at SkBee for the original code.
 * This is meant to fill the gap when SkBee isn't installed.
 */

public class ParticleUtil {
    private ParticleUtil() {
    }



    public static void spawnParticle(@Nullable Player[] players, Particle particle, Location location, int count, Object data, Vector offset, double extra, boolean force) {
        Object particleData = getData(particle, data);
        if (particle.getDataType() != Void.class && particleData == null) return;

        double x = offset.getX();
        double y = offset.getY();
        double z = offset.getZ();
        if (players == null) {
            World world = location.getWorld();
            if (world == null) return;
            world.spawnParticle(particle, location, count, x, y, z, extra, particleData, force);
        } else {
            for (Player player : players) {
                assert player != null;
                player.spawnParticle(particle, location, count, x, y, z, extra, particleData);
            }
        }
    }

    private static Object getData(Particle particle, Object data) {
        Class<?> dataType = particle.getDataType();
        if (dataType == Void.class) {
            return null;
        } else if (dataType == Float.class && data instanceof Number number) {
            return number.floatValue();
        } else if (dataType == Integer.class && data instanceof Number number) {
            return number.intValue();
        } else if (dataType == ItemStack.class && data instanceof ItemType itemType) {
            return itemType.getRandom();
        } else if (dataType == Particle.DustOptions.class && data instanceof Particle.DustOptions) {
            return data;
        } else if (dataType == Particle.DustTransition.class && data instanceof Particle.DustTransition) {
            return data;
        } else if (dataType == Vibration.class && data instanceof Vibration) {
            return data;
        } else if (dataType == BlockData.class) {
            if (data instanceof BlockData) {
                return data;
            } else if (data instanceof ItemType itemType) {
                Material material = itemType.getMaterial();
                if (material.isBlock()) {
                    return material.createBlockData();
                }
            }
        }
        return null;
    }


    public static final ParticleBuilder DEFAULT_PB =  new ParticleBuilder(Particle.FLAME).count(1).extra(0);

    public static ParticleBuilder cloneBuilder(ParticleBuilder builder) {
        return new ParticleBuilder(builder.particle())
                .count(builder.count())
                .extra(builder.extra())
                .offset(builder.offsetX(), builder.offsetY(), builder.offsetZ())
                .data(builder.data())
                .force(builder.force())
                .receivers(builder.receivers())
                .source(builder.source());
    }
}
