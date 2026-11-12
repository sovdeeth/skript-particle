package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeModifierTest {

    /** Records the order it ran in and adds its id to x. */
    private static final class Step implements PointModifier<PointContext> {
        final int id;
        final List<String> log;

        Step(int id, List<String> log) {
            this.id = id;
            this.log = log;
        }

        @Override
        public void prepare(ShapeBounds bounds) {
            log.add("prepare " + id);
        }

        @Override
        public void modify(PointContext point) {
            log.add("modify " + id + " saw x=" + point.x);
            point.x += id;
        }

        @Override
        public int modifierHash() {
            return id;
        }

        @Override
        public Step clone() {
            return new Step(id, log);
        }
    }

    @Test
    void runsModifiersInOrder_eachSeeingThePreviousOutput() {
        List<String> log = new ArrayList<>();
        CompositeModifier<PointContext> composite = new CompositeModifier<>(List.of(new Step(1, log), new Step(10, log)));
        PointContext ctx = new PointContext();
        composite.prepare(ShapeBounds.compute(List.of(new Vector3d())));
        composite.modify(ctx);
        assertEquals(List.of("prepare 1", "prepare 10", "modify 1 saw x=0.0", "modify 10 saw x=1.0"), log);
        assertEquals(11, ctx.x);
    }

    @Test
    void contextType_isTheMostSpecificOneNeeded() {
        CompositeModifier<PointContext> composite = new CompositeModifier<>();
        assertEquals(PointContext.class, composite.contextType());
        composite.addModifier(new Step(1, new ArrayList<>()));
        assertEquals(PointContext.class, composite.contextType());

        @SuppressWarnings({"unchecked", "rawtypes"})
        PointModifier<PointContext> gradient = (PointModifier) new GradientModifier(Color.RED, Color.BLUE, StandardInput.X);
        composite.addModifier(gradient);
        assertEquals(ParticleRenderContext.class, composite.contextType(),
                "one render modifier makes the whole chain need a render context");
    }

    @Test
    void clone_copiesEveryModifier() {
        List<String> log = new ArrayList<>();
        Step a = new Step(1, log), b = new Step(2, log);
        CompositeModifier<PointContext> composite = new CompositeModifier<>(List.of(a, b));
        CompositeModifier<PointContext> copy = composite.clone();
        assertEquals(2, copy.getModifiers().size());
        assertNotSame(a, copy.getModifiers().get(0));
        assertNotSame(b, copy.getModifiers().get(1));
        assertEquals(composite.modifierHash(), copy.modifierHash());

        copy.addModifier(new Step(3, log));
        assertEquals(2, composite.getModifiers().size());
    }

    @Test
    void constructorCopiesTheList() {
        List<PointModifier<PointContext>> list = new ArrayList<>();
        list.add(new Step(1, new ArrayList<>()));
        CompositeModifier<PointContext> composite = new CompositeModifier<>(list);
        list.clear();
        assertFalse(composite.isEmpty());
    }

    @Test
    void modifierHash_dependsOnOrder() {
        List<String> log = new ArrayList<>();
        int ab = new CompositeModifier<>(List.<PointModifier<PointContext>>of(new Step(1, log), new Step(2, log))).modifierHash();
        int ba = new CompositeModifier<>(List.<PointModifier<PointContext>>of(new Step(2, log), new Step(1, log))).modifierHash();
        assertNotEquals(ab, ba);
    }

    @Test
    void addAndRemove() {
        CompositeModifier<PointContext> composite = new CompositeModifier<>();
        assertTrue(composite.isEmpty());
        Step step = new Step(1, new ArrayList<>());
        composite.addModifier(step);
        assertFalse(composite.isEmpty());
        assertTrue(composite.removeModifier(step));
        assertFalse(composite.removeModifier(step));
        assertTrue(composite.isEmpty());
    }
}
