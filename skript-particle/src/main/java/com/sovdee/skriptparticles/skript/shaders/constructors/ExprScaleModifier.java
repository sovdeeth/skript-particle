package com.sovdee.skriptparticles.skript.shaders.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ScaleAxes;
import com.sovdee.shapes.modifiers.ScalingModifier;
import com.sovdee.shapes.modifiers.StandardInput;
import com.sovdee.skriptparticles.skript.shaders.ModifierInputs;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Scale Modifier Constructor")
@Description({
        "Creates a modifier that scales a shape's points by an amount that changes along an input, " +
                "from the start scale where the input is 0 to the end scale where it is 1.",
        "The input can be the x, y (default) or z axis, the draw order, the radius (distance from the shape's y axis), " +
                "the spherical distance (distance from its center), or the angle around its y axis. " +
                "Inputs go from 0 where they are lowest in the shape to 1 where they are highest.",
        "By default, it scales the x and z axes, so a scale from 1 to 0 along the y axis makes a taper.",
        "This can take a section to set the rest of the modifier's properties, like its easing. " +
                "Inside it, 'the new modifier' is the modifier being created, and modifier properties use it by default."
})
@Example("add a scale from 1 to 0 along the y axis to modifiers of {_shape}")
@Example("add a scale from 1 to 2 along the radius scaling the xyz axes to modifiers of {_shape}")
@Example("""
        set {_taper} to a scale from 1 to 0 along the y axis:
            set modifier easing to ease in quadratic
        add {_taper} to modifiers of {_shape}
        """)
@Since("2.0.0")
public class ExprScaleModifier extends ModifierConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprScaleModifier.class, PointModifier.class)
                .supplier(ExprScaleModifier::new)
                .addPattern("[a] scale from %number% to %number% [along [the] " + ModifierInputs.ALL_INPUTS + "]"
                        + " [scaling [the] (axes_x:x|axes_y:y|axes_z:z|axes_xy:xy|axes_xz:xz|axes_yz:yz|axes_xyz:xyz) [axis|axes]]")
                .build());
    }

    private Expression<Number> start, end;
    private StandardInput input;
    private ScaleAxes axes = ScaleAxes.XZ;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        start = (Expression<Number>) expressions[0];
        end = (Expression<Number>) expressions[1];
        StandardInput chosen = ModifierInputs.fromTags(result);
        input = chosen == null ? StandardInput.Y : chosen;
        for (String tag : result.tags) {
            if (tag.startsWith("axes_"))
                axes = ScaleAxes.valueOf(tag.substring(5).toUpperCase());
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        Number from = start.getSingle(event), to = end.getSingle(event);
        if (from == null || to == null)
            return null;
        return new ScalingModifier(from.doubleValue(), to.doubleValue(), input, axes);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a scale from " + start.toString(event, debug) + " to " + end.toString(event, debug)
                + " along the " + ModifierInputs.toString(input) + " scaling the " + axes.name().toLowerCase() + " axes";
    }
}
