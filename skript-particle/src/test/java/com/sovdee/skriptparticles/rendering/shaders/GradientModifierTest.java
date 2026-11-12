package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.skriptparticles.rendering.Particle;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Gradients are the most used render modifier and run once per point, every frame.
 */
class GradientModifierTest {

    private static final Color BLACK = Color.fromRGB(0, 0, 0);
    private static final Color WHITE = Color.fromRGB(255, 255, 255);
    /** x spans [0, 10], so StandardInput.X is x / 10. */
    private static final ShapeBounds BOUNDS = ShapeBounds.compute(List.of(new Vector3d(0, 0, 0), new Vector3d(10, 0, 0)));

    private static ParticleRenderContext context(Particle particle) {
        ParticleRenderContext ctx = new ParticleRenderContext(particle);
        ctx.bounds = BOUNDS;
        return ctx;
    }

    private static ParticleRenderContext flame() {
        return context(new Particle(org.bukkit.Particle.FLAME));
    }

    private static ParticleRenderContext dust(float size) {
        Particle particle = new Particle(org.bukkit.Particle.DUST);
        particle.data(new DustOptions(Color.RED, size));
        return context(particle);
    }

    private static DustOptions colorAt(GradientModifier gradient, ParticleRenderContext ctx, double x) {
        ctx.reset();
        ctx.x = x;
        gradient.modify(ctx);
        assertEquals(org.bukkit.Particle.DUST, ctx.particle);
        return assertInstanceOf(DustOptions.class, ctx.data);
    }

    /** The table has 256 entries, so a stop between two entries is only hit to within a step or so. */
    private static void assertNearColor(Color expected, Color actual) {
        assertTrue(Math.abs(expected.getRed() - actual.getRed()) <= 2
                        && Math.abs(expected.getGreen() - actual.getGreen()) <= 2
                        && Math.abs(expected.getBlue() - actual.getBlue()) <= 2,
                "expected about " + expected + " but was " + actual);
    }

    private static GradientModifier blackToWhite() {
        GradientModifier gradient = new GradientModifier(BLACK, WHITE, StandardInput.X);
        gradient.prepare(BOUNDS);
        return gradient;
    }

    @Test
    void colorsTheEndsWithTheEndColors() {
        GradientModifier gradient = blackToWhite();
        ParticleRenderContext ctx = flame();
        assertEquals(BLACK, colorAt(gradient, ctx, 0).getColor());
        assertEquals(WHITE, colorAt(gradient, ctx, 10).getColor());
        assertFalse(ctx.isDefaultParticle());
    }

    @Test
    void blendsAlongTheInput() {
        GradientModifier gradient = blackToWhite();
        ParticleRenderContext ctx = flame();
        int previous = -1;
        for (int x = 0; x <= 10; x++) {
            int red = colorAt(gradient, ctx, x).getColor().getRed();
            assertTrue(red > previous, "the gradient should get brighter along x, at x=" + x);
            previous = red;
        }
        // the table has 256 entries, so the midpoint lands within one step of the exact blend
        assertEquals(127.5, colorAt(gradient, ctx, 5).getColor().getRed(), 1.5);
    }

    @Test
    void appliesItsEasing() {
        GradientModifier gradient = blackToWhite();
        gradient.setEasing(EasingFunction.QUAD_IN);
        gradient.prepare(BOUNDS);
        // QUAD_IN(0.5) = 0.25
        assertEquals(255 * 0.25, colorAt(gradient, flame(), 5).getColor().getRed(), 1.5);
    }

    @Test
    void usesSizeOneForParticlesThatAreNotDust() {
        assertEquals(1f, colorAt(blackToWhite(), flame(), 3).getSize());
    }

    @Test
    void keepsTheSizeOfDustParticles() {
        GradientModifier gradient = blackToWhite();
        assertEquals(2.5f, colorAt(gradient, dust(2.5f), 3).getSize());
        // the table is rebuilt when a draw with another size reuses the gradient
        assertEquals(0.5f, colorAt(gradient, dust(0.5f), 3).getSize());
        assertEquals(WHITE, colorAt(gradient, dust(0.5f), 10).getColor());
    }

    @Test
    void reusesItsDustOptionsInsteadOfAllocatingPerPoint() {
        GradientModifier gradient = blackToWhite();
        ParticleRenderContext ctx = flame();
        assertSame(colorAt(gradient, ctx, 4), colorAt(gradient, ctx, 4));
    }

    @Test
    void worksWithoutPrepare() {
        GradientModifier gradient = new GradientModifier(BLACK, WHITE, StandardInput.X);
        assertEquals(WHITE, colorAt(gradient, flame(), 10).getColor());
    }

    @Test
    void addedStopsAreSortedAndUsedAfterPrepare() {
        GradientModifier gradient = blackToWhite();
        gradient.addStop(0.5, Color.RED);
        gradient.addStop(0.25, Color.BLUE);
        assertEquals(List.of(0.0, 0.25, 0.5, 1.0), gradient.getStops().stream().map(ColorStop::position).toList());
        gradient.prepare(BOUNDS);
        ParticleRenderContext ctx = flame();
        assertNearColor(Color.RED, colorAt(gradient, ctx, 5).getColor());
        assertNearColor(Color.BLUE, colorAt(gradient, ctx, 2.5).getColor());
    }

    @Test
    void multiStopConstructor() {
        GradientModifier gradient = new GradientModifier(List.of(
                new ColorStop(0, Color.RED), new ColorStop(0.5, Color.LIME), new ColorStop(1, Color.BLUE)), StandardInput.X);
        gradient.prepare(BOUNDS);
        ParticleRenderContext ctx = flame();
        assertEquals(Color.RED, colorAt(gradient, ctx, 0).getColor());
        assertNearColor(Color.LIME, colorAt(gradient, ctx, 5).getColor());
        assertEquals(Color.BLUE, colorAt(gradient, ctx, 10).getColor());
    }

    @Test
    void clone_isConfiguredTheSameButIndependent() {
        GradientModifier gradient = blackToWhite();
        gradient.setEasing(EasingFunction.SINE_OUT);
        GradientModifier copy = (GradientModifier) gradient.clone();
        assertNotSame(gradient, copy);
        assertEquals(gradient.modifierHash(), copy.modifierHash());
        assertEquals(gradient.getInput(), copy.getInput());
        assertEquals(gradient.getStops(), copy.getStops());
        copy.prepare(BOUNDS);
        ParticleRenderContext ctx = flame();
        assertEquals(colorAt(gradient, ctx, 3).getColor(), colorAt(copy, ctx, 3).getColor());

        copy.addStop(0.5, Color.RED);
        copy.setInput(StandardInput.Y);
        assertEquals(2, gradient.getStops().size(), "the original's stops must not change");
        assertEquals(StandardInput.X, gradient.getInput());
    }

    @Test
    void modifierHash_changesWithTheConfiguration() {
        int base = blackToWhite().modifierHash();
        GradientModifier stop = blackToWhite();
        stop.addStop(0.5, Color.RED);
        GradientModifier eased = blackToWhite();
        eased.setEasing(EasingFunction.CUBIC_IN);
        GradientModifier input = blackToWhite();
        input.setInput(StandardInput.RADIUS);
        GradientModifier colors = new GradientModifier(BLACK, Color.RED, StandardInput.X);
        assertNotEquals(base, stop.modifierHash());
        assertNotEquals(base, eased.modifierHash());
        assertNotEquals(base, input.modifierHash());
        assertNotEquals(base, colors.modifierHash());
        assertEquals(base, blackToWhite().modifierHash());
    }

    @Test
    void needsARenderContext() {
        assertEquals(ParticleRenderContext.class, blackToWhite().contextType());
    }
}
