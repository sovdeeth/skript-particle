package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point based on its angle around the Y axis (0 = +X, wraps around).
 */
public class AngularGradientModifier extends AbstractGradientModifier {

    public AngularGradientModifier(Color start, Color end) {
        super(start, end);
    }

    public AngularGradientModifier(List<ColorStop> stops) {
        super(stops);
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(point.angle());
    }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new AngularGradientModifier(List.of());
    }
}
