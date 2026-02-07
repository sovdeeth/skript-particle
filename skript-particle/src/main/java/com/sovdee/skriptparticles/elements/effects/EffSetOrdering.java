package com.sovdee.skriptparticles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Comparator;

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

    static {
        Skript.registerEffect(EffSetOrdering.class,
                "set the animation order of %shapes% to (default|1:lowest-to-highest|2:highest-to-lowest)");
    }

    private Expression<Shape> shapes;
    private int order;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        shapes = (Expression<Shape>) expressions[0];
        order = parseResult.mark;
        return true;
    }

    @Override
    protected void execute(Event event) {
        @Nullable Comparator<Vector> order = switch (this.order) {
            case 1 -> (o1, o2) -> {
                double value1 = o1.getX() + o1.getY() + o1.getZ();
                double value2 = o2.getX() + o2.getY() + o2.getZ();
                return Double.compare(value1, value2);
            };
            case 2 -> (o1, o2) -> {
                double value1 = o1.getX() + o1.getY() + o1.getZ();
                double value2 = o2.getX() + o2.getY() + o2.getZ();
                return -1 * Double.compare(value1, value2);
            };
            default -> null;
        };
        for (Shape shape : shapes.getArray(event)) {
            shape.setOrdering(order);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "set the animation order of " + shapes.toString(event, debug) + " to " + switch (order) {
            case 0 -> "default";
            case 1 -> "lowest-to-highest";
            case 2 -> "highest-to-lowest";
            default -> "error";
        };
    }
}
