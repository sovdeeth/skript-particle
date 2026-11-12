package com.sovdee.skriptparticles.skript.shaders;

import ch.njol.skript.lang.SkriptParser.ParseResult;
import com.sovdee.shapes.modifiers.StandardInput;
import org.jetbrains.annotations.Nullable;

/**
 * The shared pattern fragment for choosing a {@link StandardInput}, so every syntax that takes an input accepts
 * the same words. Inputs are an inline choice rather than a type: a type whose values are bare words like
 * {@code y} would compete with other syntax such as {@code y of %vector%}.
 */
public final class ModifierInputs {

    private ModifierInputs() {}

    private static final String SPATIAL = "input_x:x[-| ]axis|input_y:y[-| ]axis|input_z:z[-| ]axis"
            + "|input_radius:radius|input_spherical:spherical distance|input_angle:angle";

    /** Every input that depends on where a point is; excludes the draw order. */
    public static final String SPATIAL_INPUTS = "(" + SPATIAL + ")";

    /** Every input, including the draw order. */
    public static final String ALL_INPUTS = "(" + SPATIAL + "|input_t:draw order)";

    /**
     * @return the input chosen by a pattern containing {@link #SPATIAL_INPUTS} or {@link #ALL_INPUTS},
     * or {@code null} if none was chosen
     */
    public static @Nullable StandardInput fromTags(ParseResult result) {
        for (String tag : result.tags) {
            switch (tag) {
                case "input_x": return StandardInput.X;
                case "input_y": return StandardInput.Y;
                case "input_z": return StandardInput.Z;
                case "input_radius": return StandardInput.RADIUS;
                case "input_spherical": return StandardInput.SPHERICAL;
                case "input_angle": return StandardInput.ANGLE;
                case "input_t": return StandardInput.T;
                default: break;
            }
        }
        return null;
    }

    /**
     * @return how the input is written in a pattern, for {@code toString}
     */
    public static String toString(StandardInput input) {
        return switch (input) {
            case X -> "x axis";
            case Y -> "y axis";
            case Z -> "z axis";
            case RADIUS -> "radius";
            case SPHERICAL -> "spherical distance";
            case ANGLE -> "angle";
            case T -> "draw order";
        };
    }
}
