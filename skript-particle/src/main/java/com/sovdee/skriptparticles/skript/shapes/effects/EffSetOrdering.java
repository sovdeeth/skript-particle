package com.sovdee.skriptparticles.skript.shapes.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.Comparator;

/**
 * Skript effect that sets the order in which a shape's points are produced, and therefore the order in which
 * an animated draw reveals them. Ported unchanged from 1.x.
 */
@Name("Shape Animation Ordering")
@Description({
        "Controls the order in which the draw animation effect will draw points. Currently WIP, only supports 2 special orderings.",
        "lowest-to-highest, which draws from -x -y -z to x y z, and the reverse."
})
@Examples(
        "set the animation order of {_circle} to lowest-to-highest"
)
@Since("1.3.0")
public class EffSetOrdering extends Effect {

    private static final Comparator<Vector3d> LOWEST_TO_HIGHEST = Comparator.comparingDouble(v -> v.x + v.y + v.z);
    private static final Comparator<Vector3d> HIGHEST_TO_LOWEST = LOWEST_TO_HIGHEST.reversed();

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffSetOrdering.class)
                .supplier(EffSetOrdering::new)
                .addPatterns("set the animation order of %shapes% to (default|1:lowest-to-highest|2:highest-to-lowest)")
                .build());
    }

    private Expression<Shape> shapes;
    private int order;

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        shapes = (Expression<Shape>) expressions[0];
        order = parseResult.mark;
        return true;
    }

    @Override
    protected void execute(Event event) {
        @Nullable Comparator<Vector3d> ordering = switch (order) {
            case 1 -> LOWEST_TO_HIGHEST;
            case 2 -> HIGHEST_TO_LOWEST;
            default -> null;
        };
        for (Shape shape : shapes.getArray(event)) {
            shape.getPointSampler().setOrdering(ordering);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set the animation order of " + shapes.toString(event, debug) + " to " + switch (order) {
            case 1 -> "lowest-to-highest";
            case 2 -> "highest-to-lowest";
            default -> "default";
        };
    }
}
