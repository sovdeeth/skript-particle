package com.sovdee.skriptparticle;

import ch.njol.skript.Skript;
import ch.njol.skript.SkriptAddon;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public class SkriptParticle extends JavaPlugin {

    SkriptParticle instance;
    SkriptAddon addon;

    // todo before alpha
    // classinfo for cuboid, helix, arbitrary polygons
    // Surface fill for arbitrary polygons
    // everything for bezier curves, ellipsoids, regular polyhedra
    // Particle motion (vector, inwards, outwards)
    // Properties
    // Better interface use
    // DOCUMENTATION



    @Override
    public void onEnable() {
        instance = this;
        addon = Skript.registerAddon(this);
        try {
            addon.loadClasses("com.sovdee.skriptparticle", "elements");
        } catch (IOException e) {
            e.printStackTrace();
        }
        Bukkit.getLogger().info("Skript-Particle has been enabled.");
    }

    public SkriptParticle getInstance() {
        return instance;
    }

    public SkriptAddon getAddonInstance() {
        return addon;
    }
}
