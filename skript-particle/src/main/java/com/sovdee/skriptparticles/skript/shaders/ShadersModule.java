package com.sovdee.skriptparticles.skript.shaders;

import ch.njol.skript.classes.ClassInfo;
import ch.njol.skript.classes.Parser;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.registrations.Classes;
import com.sovdee.shapes.modifiers.EasingFunction;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.skript.shaders.effects.EffAddColorStop;
import com.sovdee.skriptparticles.skript.shaders.expressions.ExprEasing;
import com.sovdee.skriptparticles.skript.shaders.expressions.ExprGradientModifier;
import com.sovdee.skriptparticles.skript.shaders.expressions.ExprModifier;
import com.sovdee.skriptparticles.skript.shaders.expressions.ExprMotionModifier;
import com.sovdee.skriptparticles.skript.shaders.expressions.ExprNewModifier;
import com.sovdee.skriptparticles.skript.shaders.expressions.properties.ExprGradientColors;
import com.sovdee.skriptparticles.skript.shaders.expressions.properties.ExprModifierEasing;
import com.sovdee.skriptparticles.skript.shaders.expressions.properties.ExprScaleModifierRange;
import com.sovdee.skriptparticles.skript.shaders.expressions.properties.ExprTwistAngle;
import com.sovdee.skriptparticles.skript.shaders.expressions.properties.ExprWaveProperty;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointCoordinate;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointCount;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointDisplacement;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointIndex;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointMotion;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointScale;
import com.sovdee.skriptparticles.skript.shaders.expressions.point.ExprPointVisible;
import com.sovdee.skriptparticles.skript.shaders.sections.ExprSecCustomModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

public class ShadersModule extends HierarchicalAddonModule {

    public ShadersModule(AddonModule parentModule) {
        super(parentModule);
    }

    @Override
    protected void initSelf(SkriptAddon addon) {
        Classes.registerClass(new ClassInfo<>(PointModifier.class, "pointmodifier")
                .user("point ?modifiers?")
                .name("Point Modifier")
                .description("A modifier applied to shape points. Geometry modifiers (scale, twist, wave) move points and are cached with the shape. Render modifiers (gradients, motion, custom shaders) run each time the shape is drawn and change the particles' colour, motion or visibility.")
                // inside a modifier constructor section, modifier properties default to the new modifier
                .defaultExpression(new ExprNewModifier())
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

    @Override
    protected void loadSelf(SkriptAddon addon) {
        register(addon,
                ExprGradientModifier::register,
                ExprMotionModifier::register,
                ExprModifier::register,
                ExprNewModifier::register,
                ExprWaveProperty::register,
                ExprTwistAngle::register,
                ExprScaleModifierRange::register,
                ExprModifierEasing::register,
                ExprGradientColors::register,
                ExprEasing::register,
                EffAddColorStop::register,
                ExprPointCoordinate::register,
                ExprPointDisplacement::register,
                ExprPointMotion::register,
                ExprPointVisible::register,
                ExprPointScale::register,
                ExprPointIndex::register,
                ExprPointCount::register,
                ExprSecCustomModifier::register
        );
    }

    @Override
    public String name() {
        return "shaders";
    }

}
