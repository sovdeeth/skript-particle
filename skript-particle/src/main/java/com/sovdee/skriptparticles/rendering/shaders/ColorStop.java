package com.sovdee.skriptparticles.rendering.shaders;

import org.bukkit.Color;

import java.util.List;

/**
 * A color at a specific position in a gradient (position in [0, 1]).
 */
public record ColorStop(double position, Color color) {

    /**
     * Interpolates between a sorted list of color stops at position {@code t}.
     * Clamps t to [0, 1]. Assumes stops are sorted by position ascending.
     */
    public static Color interpolate(List<ColorStop> stops, double t) {
        if (stops.isEmpty()) return Color.WHITE;
        if (stops.size() == 1) return stops.getFirst().color();

        t = Math.max(0.0, Math.min(1.0, t));

        // Find surrounding stops
        ColorStop prev = stops.getFirst();
        for (int i = 1; i < stops.size(); i++) {
            ColorStop next = stops.get(i);
            if (t <= next.position()) {
                double range = next.position() - prev.position();
                if (range < 1e-12) return prev.color();
                double localT = (t - prev.position()) / range;
                return lerp(prev.color(), next.color(), localT);
            }
            prev = next;
        }
        // t is past the last stop
        return stops.getLast().color();
    }

    /**
     * Linearly interpolates between two colors.
     */
    private static Color lerp(Color a, Color b, double t) {
        return Color.fromRGB(
                (int) (a.getRed()   + t * (b.getRed()   - a.getRed())),
                (int) (a.getGreen() + t * (b.getGreen() - a.getGreen())),
                (int) (a.getBlue()  + t * (b.getBlue()  - a.getBlue()))
        );
    }
}
