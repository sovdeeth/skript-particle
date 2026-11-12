package com.sovdee.skriptparticles.skript.shaders.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.MotionModifier;
import com.sovdee.skriptparticles.skript.shaders.sections.ModifierConstructorExpression;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Motion Modifier Constructor")
@Description({
        "Creates a modifier that sends particles moving, relative to the shape: " +
                "clockwise or counterclockwise around its y axis, or inwards or outwards from its center.",
        "Only particles that can move, like flames, are affected.",
        "Like the other modifier constructors, this can take a section in which 'the new modifier' is the modifier being created."
})
@Example("add a motion modifier counterclockwise to modifiers of {_shape}")
@Example("add a motion modifier inwards to modifiers of {_shape}")
@Example("set modifiers of {_shape} to a motion modifier clockwise")
@Since("2.0.0")
public class ExprMotionModifier extends ModifierConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprMotionModifier.class, PointModifier.class)
                .supplier(ExprMotionModifier::new)
                .addPatterns("[a] motion modifier (:clockwise|:counterclockwise|:inwards|:outwards)")
                .priority(SyntaxInfo.SIMPLE)
                .build());
    }

    private MotionModifier.Mode mode;

    @Override
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        mode = MotionModifier.Mode.valueOf(result.tags.getFirst().toUpperCase());
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        return new MotionModifier(mode);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a motion modifier " + mode.name().toLowerCase();
    }
}
