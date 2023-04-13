package com.sovdee.skriptparticles.util;

import ch.njol.skript.aliases.ItemType;
import ch.njol.util.StringUtils;
import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Particle.DustTransition;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParticleUtil {

    public static com.sovdee.skriptparticles.particles.Particle getDefaultParticle() {
        return (com.sovdee.skriptparticles.particles.Particle) new com.sovdee.skriptparticles.particles.Particle(Particle.FLAME).count(1).extra(0);
    }
    private static final Map<String, Particle> PARTICLES = new HashMap<>();
    private static final Map<Particle, String> PARTICLE_NAMES = new HashMap<>();

    // Load and map Minecraft particle names
    // Bukkit does not have any API for getting the Minecraft names of particles (how stupid)
    // This method fetches them from the server and maps them with the Bukkit particle enums
    static {
        Class<?> cbParticle = ReflectionUtils.getOBCClass("CraftParticle");
        try {
            assert cbParticle != null;
            Field bukkitParticleField = cbParticle.getDeclaredField("bukkit");
            bukkitParticleField.setAccessible(true);
            Field mcKeyField = cbParticle.getDeclaredField("minecraftKey");
            mcKeyField.setAccessible(true);

            for (Object enumConstant : cbParticle.getEnumConstants()) {
                String mcKey = mcKeyField.get(enumConstant).toString().replace("minecraft:", "");
                Particle bukkitParticle = (Particle) bukkitParticleField.get(enumConstant);

                if (!bukkitParticle.toString().contains("LEGACY")) {
                    PARTICLES.put(mcKey, bukkitParticle);
                    PARTICLE_NAMES.put(bukkitParticle, mcKey);
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
        return PARTICLE_NAMES.get(particle);
    }

    /**
     * Get a list of all available particles
     *
     * @return List of all available particles
     */
    public static List<Particle> getAvailableParticles() {
        return new ArrayList<>(PARTICLES.values());
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
        } else if (dataType == DustOptions.class) {
            return "dust-option";
        } else if (dataType == BlockData.class) {
            return "blockdata/itemtype";
        } else if (dataType == DustTransition.class) {
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
        } else if (dataType == DustOptions.class && data instanceof DustOptions) {
            return data;
        } else if (dataType == DustTransition.class && data instanceof DustTransition) {
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

    private static final ParticleBuilder Y_AXIS = new ParticleBuilder(Particle.REDSTONE).data(new DustOptions(DyeColor.LIME.getColor(), 0.5f));
    private static final ParticleBuilder X_AXIS = new ParticleBuilder(Particle.REDSTONE).data(new DustOptions(DyeColor.RED.getColor(), 0.5f));
    private static final ParticleBuilder Z_AXIS = new ParticleBuilder(Particle.REDSTONE).data(new DustOptions(DyeColor.BLUE.getColor(), 0.5f));
    public static void drawAxes(Location location, Quaternion orientation, Collection<Player> recipients) {
        Set<Vector> yAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(0, 1, 0), 0.2);
        Set<Vector> xAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(1, 0, 0), 0.2);
        Set<Vector> zAxis = MathUtil.calculateLine(new Vector(0, 0, 0), new Vector(0, 0, 1), 0.2);

        yAxis = orientation.transform(yAxis);
        xAxis = orientation.transform(xAxis);
        zAxis = orientation.transform(zAxis);

        Y_AXIS.receivers(recipients);
        X_AXIS.receivers(recipients);
        Z_AXIS.receivers(recipients);

        for (Vector vector : yAxis) {
            Y_AXIS.location(location.clone().add(vector)).spawn();
        }

        for (Vector vector : xAxis) {
            X_AXIS.location(location.clone().add(vector)).spawn();
        }

        for (Vector vector : zAxis) {
            Z_AXIS.location(location.clone().add(vector)).spawn();
        }
    }
}
