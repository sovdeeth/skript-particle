package com.sovdee.skriptparticles.elements.types;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.PointModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Registers Skript type definitions for the modifier and easing systems.
 * Registers the {@code pointmodifier} type (backed by {@link com.sovdee.shapes.modifiers.PointModifier})
 * and the {@code easing} type (backed by {@link com.sovdee.shapes.modifiers.EasingFunction}),
 * each with a non-parseable parser that provides human-readable {@code toString} and variable-name representations.
 * All registration happens in the static initializer.
 */
public class ShaderTypes {
    static {

        Classes.registerClass(new ClassInfo<>(PointModifier.class, "pointmodifier")
                .user("point ?modifiers?")
                .name("Point Modifier")
                .description("A modifier applied to shape points. Geometry modifiers (taper, twist, wave) transform positions and are cached. Render modifiers (gradients, motion) run per-frame and affect color, motion, or visibility.")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable PointModifier<?> parse(String s, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public @NotNull String toString(PointModifier o, int flags) { return o.toString(); }
                    @Override
                    public @NotNull String toVariableNameString(PointModifier o) { return "modifier:" + o.getClass().getSimpleName(); }
                })
        );

        Classes.registerClass(new ClassInfo<>(EasingFunction.class, "easing")
                .user("easings?")
                .name("Easing Function")
                .description("A curve that reshapes the normalised input value of modifiers and gradients.",
                        "Supported curves: linear, quadratic ease in/out/in-out, cubic ease in/out/in-out,",
                        "sine ease in/out/in-out, power ease with a custom exponent, and custom Skript functions.")
                .parser(new Parser<>() {
                    @Override
                    public @Nullable EasingFunction parse(String s, ParseContext context) { return null; }
                    @Override
                    public boolean canParse(ParseContext context) { return false; }
                    @Override
                    public @NotNull String toString(EasingFunction o, int flags) {
                        return switch (o) {
                            case EasingFunction.PowerEasing p -> switch (p.mode()) {
                                case IN -> "ease in (power " + p.exponent() + ")";
                                case OUT -> "ease out (power " + p.exponent() + ")";
                                case IN_OUT -> "ease in-out (power " + p.exponent() + ")";
                            };
                            case EasingFunction.SineEasing s -> switch (s.mode()) {
                                case IN -> "sine ease in";
                                case OUT -> "sine ease out";
                                case IN_OUT -> "sine ease in-out";
                            };
                            default -> "easing";
                        };
                    }
                    @Override
                    public @NotNull String toVariableNameString(EasingFunction o) { return "easing:" + o.easingHash(); }
                })
        );
    }
}
