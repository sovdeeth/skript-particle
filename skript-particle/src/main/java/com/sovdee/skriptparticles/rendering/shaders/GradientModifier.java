package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.HasInput;
import com.sovdee.shapes.modifiers.NormalizedInput;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle;

import java.util.List;

/**
 * Colors each point by sampling a {@link NormalizedInput} and looking up the result
 * in a pre-built colour gradient LUT.
 */
public class GradientModifier extends AbstractGradientModifier implements HasInput {

    private NormalizedInput input;

    public GradientModifier(Color from, Color to, NormalizedInput input) {
        super(from, to);
        this.input = input;
    }

    public GradientModifier(List<ColorStop> stops, NormalizedInput input) {
        super(stops);
        this.input = input;
    }

    @Override
    public void modify(ParticleRenderContext point) {
        point.particle = Particle.DUST;
        point.data = dustLookup(input.sample(point));
    }

    @Override
    public NormalizedInput getInput() {
        return input;
    }

    @Override
    public void setInput(NormalizedInput input) {
        this.input = input;
    }

    @Override
    public int modifierHash() {
        int hash = input.inputHash();
        for (ColorStop s : stops) {
            hash = 31 * hash + Double.hashCode(s.position());
            hash = 31 * hash + s.color().hashCode();
        }
        hash = 31 * hash + easing.easingHash();
        return hash;
    }

    @Override
    protected AbstractGradientModifier createInstance() {
        return new GradientModifier(List.of(), input);
    }
}
