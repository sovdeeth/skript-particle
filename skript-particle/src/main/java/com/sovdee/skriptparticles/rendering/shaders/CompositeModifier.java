package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;

import java.util.ArrayList;
import java.util.List;

/**
 * Chains multiple {@link PointModifier}s. Each modifier runs on the same {@link PointContext}
 * in order — later modifiers see and can further modify earlier modifiers' output.
 * No temporary objects are allocated; all modifiers share the same {@code PointContext}.
 */
public class CompositeModifier<Context extends PointContext> implements PointModifier<Context> {

    private final List<PointModifier<Context>> modifiers;

    public CompositeModifier() {
        this.modifiers = new ArrayList<>();
    }

    public CompositeModifier(List<PointModifier<Context>> modifiers) {
        this.modifiers = new ArrayList<>(modifiers);
    }

    @Override
    public void prepare(ShapeBounds bounds) {
        for (PointModifier<Context> modifier : modifiers) {
            modifier.prepare(bounds);
        }
    }

    @Override
    public void modify(Context point) {
        for (PointModifier<Context> modifier : modifiers) {
            modifier.modify(point);
        }
    }

    @Override
    public Class<? extends PointContext> contextType() {
        Class<? extends PointContext> result = PointContext.class;
        for (PointModifier<Context> mod : modifiers) {
            Class<? extends PointContext> t = mod.contextType();
            if (result.isAssignableFrom(t)) result = t;
        }
        return result;
    }

    @Override
    public int modifierHash() {
        int hash = 1;
        for (PointModifier<Context> mod : modifiers) {
            hash = 31 * hash + mod.modifierHash();
        }
        return hash;
    }

    public void addModifier(PointModifier<Context> modifier) {
        modifiers.add(modifier);
    }

    public boolean removeModifier(PointModifier<Context> modifier) {
        return modifiers.remove(modifier);
    }

    public List<PointModifier<Context>> getModifiers() {
        return modifiers;
    }

    public boolean isEmpty() {
        return modifiers.isEmpty();
    }

    @Override
    public CompositeModifier<Context> clone() {
        List<PointModifier<Context>> cloned = new ArrayList<>(modifiers.size());
        for (PointModifier<Context> m : modifiers) {
            cloned.add(m.clone());
        }
        return new CompositeModifier<>(cloned);
    }
}
