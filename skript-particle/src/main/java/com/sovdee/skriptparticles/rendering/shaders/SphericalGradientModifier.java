package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point based on its 3D distance from the origin (center to surface).
 * Suitable for volumetric/filled shapes like solid spheres and ellipsoids.
 */
public class SphericalGradientModifier extends AbstractGradientModifier {

    public SphericalGradientModifier(Color center, Color surface) {
        super(center, surface);
    }

    public SphericalGradientModifier(List<ColorStop> stops) {
        super(stops);
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(point.normalizedSpherical());
    }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new SphericalGradientModifier(List.of());
    }
}
