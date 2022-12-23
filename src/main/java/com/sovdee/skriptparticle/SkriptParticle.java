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
    // SIMPLE
        // Properties
    // MODERATE
        // proper particle density for regular polyhedra
        // Particle motion (vector, inwards, outwards)
        // Particle section
            // create a new %particle% [particle]:
            //- count: int
            //- offset: vector
            //- velocity: vector or inwards/outwards (exclusive w/ offset & count)
            //- extra: double
            //- data: dustOptions, item, etc. (take skbee code)
            //- - if particle is dust, allow "color: color" and "size: double"
            //- force: boolean
    // HARD/LONG
        // Surface fill for arbitrary polygons
        // DOCUMENTATION

    // Eventually:
    // Better interface use
    // everything for bezier curves
    // Arbitrary 3d shapes w/ points
    // Shapes defined by equations
    //  - Chladni patterns?



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
