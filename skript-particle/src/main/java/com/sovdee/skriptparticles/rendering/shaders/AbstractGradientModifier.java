package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.Easable;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for gradient modifiers. Manages a list of {@link ColorStop}s and provides
 * a pre-built 256-entry LUT for zero-alloc per-point colour lookup.
 */
public abstract class AbstractGradientModifier implements PointModifier<ParticleRenderContext>, Easable {

    protected final List<ColorStop> stops = new ArrayList<>();
    protected EasingFunction easing = EasingFunction.LINEAR;
    /**
     * Pre-built LUT: 256 Color objects, one per 1/255 position step.
     */
    protected Color[] lut;
    /**
     * Pre-built DustOptions LUT, parallel to {@link #lut}. Built in {@link #buildLUT()}.
     */
    protected DustOptions[] dustLut;
    // the dust size the dust lookup table was built for
    private float dustLutSize = Float.NaN;

    protected AbstractGradientModifier() {}

    protected AbstractGradientModifier(Color from, Color to) {
        stops.add(new ColorStop(0.0, from));
        stops.add(new ColorStop(1.0, to));
    }

    protected AbstractGradientModifier(List<ColorStop> stops) {
        this.stops.addAll(stops);
    }

    public void addStop(ColorStop stop) {
        stops.add(stop);
        stops.sort(Comparator.comparingDouble(ColorStop::position));
    }

    public void addStop(double position, Color color) {
        addStop(new ColorStop(position, color));
    }

    public List<ColorStop> getStops() {
        return stops;
    }

    /**
     * Replaces every stop with the given colours, spaced evenly from 0 to 1. A single colour makes a solid gradient.
     *
     * @param colors the colours, in order; ignored if empty
     */
    public void setColors(List<Color> colors) {
        if (colors.isEmpty())
            return;
        stops.clear();
        stops.addAll(evenlySpaced(colors));
    }

    /**
     * @return stops for the given colours, spaced evenly from 0 to 1; a single colour is used at both ends
     */
    public static List<ColorStop> evenlySpaced(List<Color> colors) {
        List<ColorStop> result = new ArrayList<>(Math.max(2, colors.size()));
        if (colors.size() == 1) {
            result.add(new ColorStop(0.0, colors.getFirst()));
            result.add(new ColorStop(1.0, colors.getFirst()));
            return result;
        }
        for (int i = 0; i < colors.size(); i++)
            result.add(new ColorStop((double) i / (colors.size() - 1), colors.get(i)));
        return result;
    }

    /**
     * Builds the 256-entry LUT from the current stops.
     * Called in {@link #prepare(ShapeBounds)}.
     */
    protected void buildLUT() {
        stops.sort(Comparator.comparingDouble(ColorStop::position));
        lut = new Color[256];
        for (int i = 0; i < 256; i++) {
            lut[i] = ColorStop.interpolate(stops, i / 255.0);
        }
        // built on demand, once the dust size is known
        dustLut = null;
    }

    /**
     * Looks up the DustOptions LUT for {@code t ∈ [0, 1]}, applying this gradient's
     * {@link EasingFunction} before the lookup.
     */
    protected DustOptions dustLookup(double t) {
        return dustLookup(t, 1.0f);
    }

    /**
     * Looks up the dust options for a normalised input, with the given dust size.
     * The table of dust options is built on first use and reused while the size stays the same.
     *
     * @param t    the normalised input, before easing
     * @param size the size of the dust particles
     */
    protected DustOptions dustLookup(double t, float size) {
        if (dustLut == null || size != dustLutSize) {
            if (lut == null)
                buildLUT();
            DustOptions[] table = new DustOptions[256];
            for (int i = 0; i < 256; i++) {
                table[i] = new DustOptions(lut[i], size);
            }
            dustLut = table;
            dustLutSize = size;
        }
        int idx = (int) (easing.apply(t) * 255.0 + 0.5);
        if (idx < 0) idx = 0;
        if (idx > 255) idx = 255;
        return dustLut[idx];
    }

    @Override
    public EasingFunction getEasing() {
        return easing;
    }
    @Override
    public void setEasing(EasingFunction easing) {
        this.easing = easing;
    }

    @Override
    public void prepare(ShapeBounds bounds) {
        buildLUT();
    }

    @Override
    public Class<ParticleRenderContext> contextType() {
        return ParticleRenderContext.class;
    }

    @Override
    public int modifierHash() {
        int hash = getClass().getSimpleName().hashCode();
        for (ColorStop s : stops) {
            hash = 31 * hash + Double.hashCode(s.position());
            hash = 31 * hash + s.color().hashCode();
        }
        hash = 31 * hash + easing.easingHash();
        return hash;
    }

    protected abstract AbstractGradientModifier createInstance();

    @Override
    public AbstractGradientModifier clone() {
        AbstractGradientModifier copy = createInstance();
        copy.stops.addAll(stops); // ColorStop is a record, immutable
        copy.easing = easing;     // EasingFunction is immutable
        return copy;
    }
}
