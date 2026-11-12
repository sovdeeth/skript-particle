package com.sovdee.skriptparticles.rendering.shaders;

import com.sovdee.skriptparticles.rendering.Particle;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import com.sovdee.skriptparticles.rendering.shaders.MotionModifier.Mode;
import org.joml.Quaterniond;
import org.joml.Vector3d;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class MotionModifierTest {

    private static final double EPS = 1e-5;

    private static ParticleRenderContext context() {
        return new ParticleRenderContext(new Particle(org.bukkit.Particle.FLAME));
    }

    private static Vector3d motion(MotionModifier modifier, ParticleRenderContext ctx, double x, double y, double z) {
        ctx.reset();
        ctx.x = x;
        ctx.y = y;
        ctx.z = z;
        modifier.modify(ctx);
        return new Vector3d(ctx.motionX, ctx.motionY, ctx.motionZ);
    }

    private static Vector3d motion(Mode mode, double x, double y, double z) {
        MotionModifier modifier = new MotionModifier(mode);
        modifier.prepare(null);
        return motion(modifier, context(), x, y, z);
    }

    private static void assertVector(Vector3d expected, Vector3d actual) {
        assertTrue(expected.equals(actual, EPS), "expected " + expected + " but was " + actual);
    }

    @Test
    void outwards_pointsAwayFromTheCenter() {
        assertVector(new Vector3d(0.6, 0, 0.8), motion(Mode.OUTWARDS, 3, 0, 4));
        assertVector(new Vector3d(0, -1, 0), motion(Mode.OUTWARDS, 0, -7, 0));
    }

    @Test
    void inwards_pointsTowardTheCenter() {
        assertVector(new Vector3d(-0.6, 0, -0.8), motion(Mode.INWARDS, 3, 0, 4));
    }

    @Test
    void radialModes_atTheCenter_haveNoMotion() {
        assertVector(new Vector3d(), motion(Mode.OUTWARDS, 0, 0, 0));
        assertVector(new Vector3d(), motion(Mode.INWARDS, 0, 0, 0));
    }

    @Test
    void orbits_areUnitTangentsAroundTheYAxis() {
        double[][] points = {{1, 0, 0}, {0, 2, 3}, {-4, -1, 1}, {2.5, 7, -2.5}};
        for (Mode mode : new Mode[]{Mode.CLOCKWISE, Mode.COUNTERCLOCKWISE}) {
            for (double[] p : points) {
                Vector3d m = motion(mode, p[0], p[1], p[2]);
                assertEquals(1, m.length(), EPS, mode + " should be a unit vector");
                assertEquals(0, m.y, EPS, mode + " should stay horizontal");
                assertEquals(0, m.dot(p[0], 0, p[2]), EPS, mode + " should be perpendicular to the radius");
            }
        }
    }

    @Test
    void clockwise_isTheReverseOfCounterclockwise() {
        assertVector(motion(Mode.COUNTERCLOCKWISE, 2, 1, -3).negate(), motion(Mode.CLOCKWISE, 2, 1, -3));
    }

    @Test
    void counterclockwise_turnsFromPlusXTowardMinusZ() {
        // cross(+Y, +X) = -Z: counterclockwise when looking down from above in Minecraft's axes
        assertVector(new Vector3d(0, 0, -1), motion(Mode.COUNTERCLOCKWISE, 1, 0, 0));
        assertVector(new Vector3d(-1, 0, 0), motion(Mode.COUNTERCLOCKWISE, 0, 0, -1));
    }

    @Test
    void orbits_onTheAxis_haveNoMotion() {
        Vector3d m = motion(Mode.CLOCKWISE, 0, 5, 0);
        assertTrue(m.isFinite());
        assertEquals(0, m.length(), EPS);
    }

    @Test
    void orbits_followTheShapesOrientation() {
        MotionModifier modifier = new MotionModifier(Mode.COUNTERCLOCKWISE);
        modifier.prepare(null);
        ParticleRenderContext ctx = context();
        // rotating 90 degrees about X turns the shape's Y axis into +Z
        ctx.orientation = new Quaterniond().rotateX(Math.PI / 2);
        Vector3d m = motion(modifier, ctx, 1, 0, 0);
        assertVector(new Vector3d(0, 1, 0), m);
        assertEquals(0, m.dot(0, 0, 1), EPS, "the orbit should be around the rotated axis");
    }

    @Test
    void prepare_picksUpANewOrientation() {
        MotionModifier modifier = new MotionModifier(Mode.COUNTERCLOCKWISE);
        ParticleRenderContext ctx = context();
        modifier.prepare(null);
        assertVector(new Vector3d(0, 0, -1), motion(modifier, ctx, 1, 0, 0));

        ctx.orientation = new Quaterniond().rotateX(Math.PI / 2);
        modifier.prepare(null);
        assertVector(new Vector3d(0, 1, 0), motion(modifier, ctx, 1, 0, 0));
    }

    @Test
    void setsHasMotion_exceptForNone() {
        for (Mode mode : Mode.values()) {
            MotionModifier modifier = new MotionModifier(mode);
            modifier.prepare(null);
            ParticleRenderContext ctx = context();
            motion(modifier, ctx, 1, 2, 3);
            assertEquals(mode != Mode.NONE, ctx.hasMotion, mode.toString());
        }
        assertVector(new Vector3d(), motion(Mode.NONE, 1, 2, 3));
    }

    @Test
    void clone_andHash() {
        Set<Integer> hashes = new HashSet<>();
        for (Mode mode : Mode.values()) {
            MotionModifier modifier = new MotionModifier(mode);
            MotionModifier copy = modifier.clone();
            assertNotSame(modifier, copy);
            assertEquals(mode, copy.getMode());
            assertEquals(modifier.modifierHash(), copy.modifierHash());
            hashes.add(modifier.modifierHash());
        }
        assertEquals(Mode.values().length, hashes.size());
    }
}
