package com.sovdee.skriptparticles.rendering.shaders;

import org.bukkit.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ColorStopTest {

    private static final Color BLACK = Color.fromRGB(0, 0, 0);
    private static final Color WHITE = Color.fromRGB(255, 255, 255);
    private static final List<ColorStop> BLACK_TO_WHITE = List.of(new ColorStop(0, BLACK), new ColorStop(1, WHITE));

    @Test
    void noStops_isWhite() {
        assertEquals(Color.WHITE, ColorStop.interpolate(List.of(), 0.5));
    }

    @Test
    void oneStop_isThatColorEverywhere() {
        List<ColorStop> stops = List.of(new ColorStop(0.3, Color.RED));
        for (double t : new double[]{-1, 0, 0.3, 0.9, 2})
            assertEquals(Color.RED, ColorStop.interpolate(stops, t));
    }

    @Test
    void endpoints_matchTheirStops() {
        assertEquals(BLACK, ColorStop.interpolate(BLACK_TO_WHITE, 0));
        assertEquals(WHITE, ColorStop.interpolate(BLACK_TO_WHITE, 1));
    }

    @Test
    void blendsLinearlyBetweenStops() {
        assertEquals(Color.fromRGB(127, 127, 127), ColorStop.interpolate(BLACK_TO_WHITE, 0.5));
        assertEquals(Color.fromRGB(63, 63, 63), ColorStop.interpolate(BLACK_TO_WHITE, 0.25));
        // channels are blended independently
        Color mixed = ColorStop.interpolate(List.of(
                new ColorStop(0, Color.fromRGB(255, 0, 100)),
                new ColorStop(1, Color.fromRGB(0, 255, 100))), 0.5);
        assertEquals(Color.fromRGB(127, 127, 100), mixed);
    }

    @Test
    void clampsOutOfRangeInput() {
        assertEquals(BLACK, ColorStop.interpolate(BLACK_TO_WHITE, -3));
        assertEquals(WHITE, ColorStop.interpolate(BLACK_TO_WHITE, 7));
        assertEquals(BLACK, ColorStop.interpolate(BLACK_TO_WHITE, Double.NEGATIVE_INFINITY));
    }

    @Test
    void beforeTheFirstAndAfterTheLastStop_holdTheirColor() {
        List<ColorStop> stops = List.of(new ColorStop(0.25, Color.RED), new ColorStop(0.75, Color.BLUE));
        assertEquals(Color.RED, ColorStop.interpolate(stops, 0));
        assertEquals(Color.RED, ColorStop.interpolate(stops, 0.25));
        assertEquals(Color.BLUE, ColorStop.interpolate(stops, 0.75));
        assertEquals(Color.BLUE, ColorStop.interpolate(stops, 1));
        assertEquals(Color.fromRGB(127, 0, 127), ColorStop.interpolate(stops, 0.5));
    }

    @Test
    void picksTheRightSegment() {
        List<ColorStop> stops = List.of(
                new ColorStop(0, Color.RED),
                new ColorStop(0.5, Color.LIME),
                new ColorStop(1, Color.BLUE));
        assertEquals(Color.fromRGB(127, 127, 0), ColorStop.interpolate(stops, 0.25));
        assertEquals(Color.LIME, ColorStop.interpolate(stops, 0.5));
        assertEquals(Color.fromRGB(0, 127, 127), ColorStop.interpolate(stops, 0.75));
    }

    @Test
    void coincidentStops_makeAHardEdgeInsteadOfFailing() {
        List<ColorStop> stops = List.of(
                new ColorStop(0, Color.RED),
                new ColorStop(0.5, Color.RED),
                new ColorStop(0.5, Color.BLUE),
                new ColorStop(1, Color.BLUE));
        assertEquals(Color.RED, ColorStop.interpolate(stops, 0.49));
        assertEquals(Color.RED, ColorStop.interpolate(stops, 0.5));
        assertEquals(Color.BLUE, ColorStop.interpolate(stops, 0.51));
    }
}
