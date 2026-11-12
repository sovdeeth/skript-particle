package com.sovdee.skriptparticles.rendering;

import org.bukkit.Color;
import org.bukkit.Particle.DustOptions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * One context is reused for every point of a draw, so {@link ParticleRenderContext#reset()} is what
 * stops one point's modifier output from leaking into the next point.
 */
class ParticleRenderContextTest {

    @Test
    void startsWithTheDrawnParticle() {
        DustOptions dust = new DustOptions(Color.RED, 2f);
        Particle particle = new Particle(org.bukkit.Particle.DUST);
        particle.data(dust);
        ParticleRenderContext ctx = new ParticleRenderContext(particle);
        assertEquals(org.bukkit.Particle.DUST, ctx.particle);
        assertSame(dust, ctx.data);
        assertSame(dust, ctx.defaultData());
        assertTrue(ctx.isDefaultParticle());
        assertTrue(ctx.visible);
        assertFalse(ctx.hasMotion);
    }

    @Test
    void isDefaultParticle_tracksChanges() {
        ParticleRenderContext ctx = new ParticleRenderContext(new Particle(org.bukkit.Particle.FLAME));
        ctx.particle = org.bukkit.Particle.DUST;
        assertFalse(ctx.isDefaultParticle());
        ctx.reset();
        assertTrue(ctx.isDefaultParticle());
        ctx.data = new DustOptions(Color.BLUE, 1f);
        assertFalse(ctx.isDefaultParticle(), "changed data alone also counts");
    }

    @Test
    void reset_clearsEverythingAModifierCanWrite() {
        ParticleRenderContext ctx = new ParticleRenderContext(new Particle(org.bukkit.Particle.FLAME));
        ctx.particle = org.bukkit.Particle.DUST;
        ctx.data = new DustOptions(Color.GREEN, 3f);
        ctx.visible = false;
        ctx.displacementX = 1;
        ctx.displacementY = 2;
        ctx.displacementZ = 3;
        ctx.motionX = 4;
        ctx.motionY = 5;
        ctx.motionZ = 6;
        ctx.hasMotion = true;

        ctx.reset();

        assertEquals(org.bukkit.Particle.FLAME, ctx.particle);
        assertNull(ctx.data);
        assertTrue(ctx.isDefaultParticle());
        assertTrue(ctx.visible);
        assertEquals(0, ctx.displacementX);
        assertEquals(0, ctx.displacementY);
        assertEquals(0, ctx.displacementZ);
        assertEquals(0, ctx.motionX);
        assertEquals(0, ctx.motionY);
        assertEquals(0, ctx.motionZ);
        assertFalse(ctx.hasMotion);
    }
}
