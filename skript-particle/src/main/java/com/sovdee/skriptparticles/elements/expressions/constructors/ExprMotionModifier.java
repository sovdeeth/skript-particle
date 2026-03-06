package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.MotionModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Motion Modifier Constructor")
@Description({
        "Creates a motion (velocity) modifier for particles.",
        "Modes: clockwise, counterclockwise, inwards, outwards, none.",
        "The velocity is normalized and based on each point's position relative to the shape origin.",
})
@Examples({
        "add motion modifier counterclockwise to modifiers of {_shape}",
        "add motion modifier inwards to modifiers of {_shape}",
        "set modifiers of {_shape} to motion modifier clockwise",
})
@Since("2.0.0")
public class ExprMotionModifier extends SimpleExpression<PointModifier> {

    static {
        Skript.registerExpression(ExprMotionModifier.class, PointModifier.class, ExpressionType.SIMPLE,
                "[a] motion modifier (0¦clockwise|1¦counterclockwise|2¦inwards|3¦outwards|4¦none)"
        );
    }

    private MotionModifier.Mode mode;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.mode = switch (parseResult.mark) {
            case 0 -> MotionModifier.Mode.CLOCKWISE;
            case 1 -> MotionModifier.Mode.COUNTERCLOCKWISE;
            case 2 -> MotionModifier.Mode.INWARDS;
            case 3 -> MotionModifier.Mode.OUTWARDS;
            default -> MotionModifier.Mode.NONE;
        };
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        return new PointModifier[]{new MotionModifier(mode)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "motion modifier " + mode.name().toLowerCase();
    }
}
