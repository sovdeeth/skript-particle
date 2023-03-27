package com.sovdee.skriptparticles.util;

import ch.njol.util.StringUtils;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParticleUtil {
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

    public static com.sovdee.skriptparticles.particles.Particle getDefaultParticle() {
        return (com.sovdee.skriptparticles.particles.Particle) new com.sovdee.skriptparticles.particles.Particle(Particle.FLAME).count(1).extra(0);
    }

    private static final Map<String, org.bukkit.Particle> PARTICLES = new HashMap<>();

    // Load and map Minecraft particle names
    // Bukkit does not have any API for getting the Minecraft names of particles (how stupid)
    // This method fetches them from the server and maps them with the Bukkit particle enums
    static {
        Class<?> cbParticle = ReflectionUtils.getOBCClass("CraftParticle");
        Class<?> mcKey = ReflectionUtils.getNMSClass("MinecraftKey", "net.minecraft.resources");
        try {
            assert cbParticle != null;
            Field mc = cbParticle.getDeclaredField("minecraftKey");
            mc.setAccessible(true);
            Field pc = cbParticle.getDeclaredField("bukkit");
            pc.setAccessible(true);

            assert mcKey != null;
            Method getKey = mcKey.getMethod(ReflectionUtils.ReflectionConstants.MINECRAFT_KEY_GET_KEY_METHOD);
            getKey.setAccessible(true);

            for (Object enumConstant : cbParticle.getEnumConstants()) {
                String KEY = getKey.invoke(mc.get(enumConstant)).toString();
                Particle PARTICLE = ((Particle) pc.get(enumConstant));

                if (!PARTICLE.toString().contains("LEGACY")) {
                    PARTICLES.put(KEY, PARTICLE);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns a string for docs of all names of particles
     *
     * @return Names of all particles in one long string
     */
    public static String getNamesAsString() {
        List<String> names = new ArrayList<>();
        PARTICLES.forEach((s, particle) -> {
            String name = s;

            if (particle.getDataType() != Void.class) {
                name = name + " [" + getDataType(particle) + "]";
            }
            names.add(name);
        });
        Collections.sort(names);
        return StringUtils.join(names, ", ");
    }

    /**
     * Get the Minecraft name of a particle
     *
     * @param particle Particle to get name of
     * @return Minecraft name of particle
     */
    public static String getName(Particle particle) {
        for (String key : PARTICLES.keySet()) {
            if (PARTICLES.get(key) == particle) {
                return key;
            }
        }
        return null;
    }

    /**
     * Parse a particle by its Minecraft name
     *
     * @param key Minecraft name of particle
     * @return Bukkit particle from Minecraft name (null if not available)
     */
    @Nullable
    public static Particle parse(String key) {
        if (PARTICLES.containsKey(key)) {
            return PARTICLES.get(key);
        }
        return null;
    }

    private static String getDataType(Particle particle) {
        Class<?> dataType = particle.getDataType();
        if (dataType == ItemStack.class) {
            return "itemtype";
        } else if (dataType == Particle.DustOptions.class) {
            return "dust-option";
        } else if (dataType == BlockData.class) {
            return "blockdata/itemtype";
        } else if (dataType == Particle.DustTransition.class) {
            return "dust-transition";
        } else if (dataType == Vibration.class) {
            return "vibration";
        } else if (dataType == Integer.class) {
            return "number(int)";
        } else if (dataType == Float.class) {
            return "number(float)";
        }
        // For future particle data additions that haven't been added here yet
        return "UNKNOWN";
    }
}
