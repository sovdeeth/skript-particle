package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point based on its XZ distance from the origin (center to edge in the horizontal plane).
 */
public class RadialGradientModifier extends AbstractGradientModifier {

    public RadialGradientModifier(Color center, Color edge) {
        super(center, edge);
    }

    public RadialGradientModifier(List<ColorStop> stops) {
        super(stops);
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(point.normalizedRadius());
    }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new RadialGradientModifier(List.of());
    }
}
