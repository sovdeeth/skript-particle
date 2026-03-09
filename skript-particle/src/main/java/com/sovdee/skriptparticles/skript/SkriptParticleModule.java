package com.sovdee.skriptparticles.skript;

import com.sovdee.skriptparticles.skript.drawing.DrawingModule;
import com.sovdee.skriptparticles.skript.shaders.ShadersModule;
import com.sovdee.skriptparticles.skript.shapes.ShapesModule;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;

import java.util.List;

/**
 * Holds all Skript syntax for skript-particle.
 */
public class SkriptParticleModule extends HierarchicalAddonModule {

    public SkriptParticleModule() {
        super();
    }

    @Override
    public Iterable<AddonModule> children() {
        return List.of(
            new ShapesModule(this),
            new DrawingModule(this),
            new ShadersModule(this)
        );
    }

    @Override
    public String name() {
        return "skript-particle";
    }

}
