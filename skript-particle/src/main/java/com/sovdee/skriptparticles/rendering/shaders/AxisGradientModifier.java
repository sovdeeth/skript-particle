package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.SampleAxis;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point based on its position along a {@link SampleAxis} within the shape's bounds.
 * Supports X, Y, Z (spatial axes) and T (normalised draw order).
 */
public class AxisGradientModifier extends AbstractGradientModifier {

    private final SampleAxis axis;

    public AxisGradientModifier(Color from, Color to, SampleAxis axis) {
        super(from, to);
        this.axis = axis;
    }

    public AxisGradientModifier(List<ColorStop> stops, SampleAxis axis) {
        super(stops);
        this.axis = axis;
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(axis.sampleNormalized(point));
    }

    public SampleAxis getAxis() { return axis; }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new AxisGradientModifier(List.of(), axis);
    }
}
