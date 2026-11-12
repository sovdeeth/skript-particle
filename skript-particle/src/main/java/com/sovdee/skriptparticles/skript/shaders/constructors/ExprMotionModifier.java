package com.sovdee.skriptparticles.skript.shaders.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.MotionModifier;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Motion Modifier Constructor")
@Description({
        "Creates a modifier that sends particles moving. Only particles that can move, like flames or end rods, are affected, " +
                "and they move faster the higher the particle's speed (its extra value).",
        "- clockwise / counterclockwise: around the shape's y axis.",
        "- inwards / outwards: towards or away from the shape's center.",
        "- a global velocity: every particle moves the same way, in world space.",
        "- a relative velocity: each particle moves relative to where it is around the shape. " +
                "The x of the vector is outwards from the shape's y axis (negative pulls inwards), " +
                "the y is along the shape's y axis, and the z is around it (positive is counterclockwise). " +
                "This follows the shape as it rotates, so a relative velocity of vector(0, 1, 1) spirals upwards.",
        "With a velocity, its length is the speed: vector(0, 2, 0) moves twice as fast as vector(0, 1, 0).",
        "Like the other modifier constructors, this can take a section in which 'the new modifier' is the modifier being created."
})
@Example("add a motion modifier counterclockwise to modifiers of {_shape}")
@Example("add a motion modifier inwards to modifiers of {_shape}")
@Example("add a motion modifier with velocity vector(0, 1, 0) to modifiers of {_shape}")
@Example("""
        # souls that spiral inwards and up, like water going down a drain in reverse
        set particle of {_ring} to sculk soul particle with extra value 0.1
        add a motion modifier with relative velocity vector(-1, 0.5, 2) to modifiers of {_ring}
        """)
@Since("2.0.0")
public class ExprMotionModifier extends ModifierConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprMotionModifier.class, PointModifier.class)
                .supplier(ExprMotionModifier::new)
                .addPatterns(
                        "[a] motion modifier (:clockwise|:counterclockwise|:inwards|:outwards)",
                        "[a] motion modifier (with|of) [a] [global|:relative] velocity [of] %vector%"
                )
                .priority(SyntaxInfo.SIMPLE)
                .build());
    }

    private MotionModifier.Mode mode;
    private @Nullable Expression<Vector> velocity;

    @Override
    @SuppressWarnings("unchecked")
    protected boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult result) {
        if (matchedPattern == 0) {
            mode = MotionModifier.Mode.valueOf(result.tags.getFirst().toUpperCase());
        } else {
            mode = result.hasTag("relative") ? MotionModifier.Mode.RELATIVE : MotionModifier.Mode.GLOBAL;
            velocity = (Expression<Vector>) expressions[0];
        }
        return true;
    }

    @Override
    protected @Nullable PointModifier<?> create(Event event) {
        if (velocity == null)
            return new MotionModifier(mode);
        Vector vector = velocity.getSingle(event);
        if (vector == null)
            return null;
        return new MotionModifier(mode, VectorConversion.toJOML(vector));
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        if (velocity == null)
            return "a motion modifier " + mode.name().toLowerCase();
        return "a motion modifier with " + (mode == MotionModifier.Mode.RELATIVE ? "relative" : "global")
                + " velocity " + velocity.toString(event, debug);
    }
}
