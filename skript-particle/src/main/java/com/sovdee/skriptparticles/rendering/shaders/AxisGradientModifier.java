package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.NormalizedInput;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point based on its position along a {@link NormalizedInput} within the shape's bounds.
 * Supports any normalized input — X, Y, Z, T (normalized draw order), RADIUS, SPHERICAL, or ANGLE.
 */
public class AxisGradientModifier extends AbstractGradientModifier {

    private final NormalizedInput input;

    public AxisGradientModifier(Color from, Color to, NormalizedInput input) {
        super(from, to);
        this.input = input;
    }

    public AxisGradientModifier(List<ColorStop> stops, NormalizedInput input) {
        super(stops);
        this.input = input;
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(input.sample(point));
    }

    public NormalizedInput getInput() {
        return input;
    }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new AxisGradientModifier(List.of(), input);
    }
}
