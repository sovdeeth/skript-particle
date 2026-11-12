package com.sovdee.shapes.modifiers;

import com.sovdee.shapes.modifiers.EasingFunction.Mode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Every eased modifier and gradient runs its input through an easing, once per point.
 */
class EasingFunctionTest {

    private static final int STEPS = 1000;

    private static Map<String, EasingFunction> easings() {
        Map<String, EasingFunction> m = new LinkedHashMap<>();
        m.put("LINEAR", EasingFunction.LINEAR);
        m.put("QUAD_IN", EasingFunction.QUAD_IN);
        m.put("QUAD_OUT", EasingFunction.QUAD_OUT);
        m.put("QUAD_IN_OUT", EasingFunction.QUAD_IN_OUT);
        m.put("CUBIC_IN", EasingFunction.CUBIC_IN);
        m.put("CUBIC_OUT", EasingFunction.CUBIC_OUT);
        m.put("CUBIC_IN_OUT", EasingFunction.CUBIC_IN_OUT);
        m.put("SINE_IN", EasingFunction.SINE_IN);
        m.put("SINE_OUT", EasingFunction.SINE_OUT);
        m.put("SINE_IN_OUT", EasingFunction.SINE_IN_OUT);
        m.put("powerIn(5)", EasingFunction.powerIn(5));
        m.put("powerOut(0.5)", EasingFunction.powerOut(0.5));
        m.put("powerInOut(4)", EasingFunction.powerInOut(4));
        return m;
    }

    static Stream<Arguments> all() {
        return easings().entrySet().stream().map(e -> Arguments.of(e.getKey(), e.getValue()));
    }

    static Stream<Arguments> inOut() {
        return Stream.of(
                Arguments.of("QUAD_IN_OUT", EasingFunction.QUAD_IN_OUT),
                Arguments.of("CUBIC_IN_OUT", EasingFunction.CUBIC_IN_OUT),
                Arguments.of("SINE_IN_OUT", EasingFunction.SINE_IN_OUT),
                Arguments.of("powerInOut(4)", EasingFunction.powerInOut(4)),
                Arguments.of("powerInOut(1.5)", EasingFunction.powerInOut(1.5)));
    }

    // ----- properties every easing must have -----

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void startsAtZeroAndEndsAtOne(String name, EasingFunction f) {
        assertEquals(0.0, f.apply(0.0), 1e-12, name + "(0)");
        assertEquals(1.0, f.apply(1.0), 1e-12, name + "(1)");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void neverDecreases(String name, EasingFunction f) {
        double previous = f.apply(0);
        for (int i = 1; i <= STEPS; i++) {
            double t = i / (double) STEPS;
            double v = f.apply(t);
            assertTrue(v >= previous - 1e-12, name + " decreases at t=" + t + ": " + previous + " -> " + v);
            previous = v;
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("all")
    void staysWithinZeroToOne(String name, EasingFunction f) {
        for (int i = 0; i <= STEPS; i++) {
            double v = f.apply(i / (double) STEPS);
            assertTrue(v >= -1e-12 && v <= 1 + 1e-12, name + " left [0, 1]: " + v);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("inOut")
    void inOut_isSymmetricAroundTheMidpoint(String name, EasingFunction f) {
        assertEquals(0.5, f.apply(0.5), 1e-12, name + "(0.5)");
        for (int i = 0; i <= STEPS; i++) {
            double t = i / (double) STEPS;
            assertEquals(1.0, f.apply(t) + f.apply(1 - t), 1e-9, name + " is not symmetric at t=" + t);
        }
    }

    // ----- specific curves -----

    @Test
    void out_mirrorsIn() {
        EasingFunction[][] pairs = {
                {EasingFunction.QUAD_IN, EasingFunction.QUAD_OUT},
                {EasingFunction.CUBIC_IN, EasingFunction.CUBIC_OUT},
                {EasingFunction.SINE_IN, EasingFunction.SINE_OUT},
                {EasingFunction.powerIn(3.5), EasingFunction.powerOut(3.5)},
        };
        for (EasingFunction[] pair : pairs) {
            for (int i = 0; i <= STEPS; i++) {
                double t = i / (double) STEPS;
                assertEquals(1 - pair[0].apply(1 - t), pair[1].apply(t), 1e-9, pair[0] + " vs " + pair[1] + " at " + t);
            }
        }
    }

    @Test
    void linear_isTheIdentity() {
        for (int i = 0; i <= 100; i++) {
            double t = i / 100.0;
            assertEquals(t, EasingFunction.LINEAR.apply(t), 1e-12);
        }
    }

    @Test
    void exponentOne_isLinearInEveryMode() {
        for (Mode mode : Mode.values()) {
            EasingFunction f = new EasingFunction.PowerEasing(1, mode);
            for (int i = 0; i <= 100; i++)
                assertEquals(i / 100.0, f.apply(i / 100.0), 1e-12, mode.toString());
        }
    }

    @Test
    void knownValues() {
        assertEquals(0.25, EasingFunction.QUAD_IN.apply(0.5), 1e-12);
        assertEquals(0.75, EasingFunction.QUAD_OUT.apply(0.5), 1e-12);
        assertEquals(0.125, EasingFunction.CUBIC_IN.apply(0.5), 1e-12);
        assertEquals(0.875, EasingFunction.CUBIC_OUT.apply(0.5), 1e-12);
        assertEquals(0.125, EasingFunction.QUAD_IN_OUT.apply(0.25), 1e-12);
        assertEquals(1 - Math.cos(Math.PI / 4), EasingFunction.SINE_IN.apply(0.5), 1e-12);
        assertEquals(Math.sin(Math.PI / 4), EasingFunction.SINE_OUT.apply(0.5), 1e-12);
    }

    @Test
    void easeIn_isSlowerThanLinearAndEaseOutFaster() {
        for (int i = 1; i < 100; i++) {
            double t = i / 100.0;
            assertTrue(EasingFunction.QUAD_IN.apply(t) < t);
            assertTrue(EasingFunction.QUAD_OUT.apply(t) > t);
            assertTrue(EasingFunction.SINE_IN.apply(t) < t);
            assertTrue(EasingFunction.SINE_OUT.apply(t) > t);
        }
    }

    // ----- hashes (used in geometry cache keys) -----

    @Test
    void easingHash_isEqualForEqualConfigurations() {
        assertEquals(EasingFunction.QUAD_IN.easingHash(), EasingFunction.powerIn(2).easingHash());
        assertEquals(EasingFunction.CUBIC_IN_OUT.easingHash(), EasingFunction.powerInOut(3).easingHash());
        assertEquals(EasingFunction.SINE_OUT.easingHash(), new EasingFunction.SineEasing(Mode.OUT).easingHash());
    }

    @Test
    void easingHash_differsBetweenDifferentCurves() {
        Map<String, EasingFunction> all = easings();
        Set<Integer> hashes = new HashSet<>();
        for (EasingFunction f : all.values())
            hashes.add(f.easingHash());
        assertEquals(all.size(), hashes.size(), "two different easings share a hash: " + all.keySet());
    }
}
