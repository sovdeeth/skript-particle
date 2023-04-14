package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Line;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle Line")
@Description({
        "Creates a line shape between two points, or in a direction for a given length. The length must be greater than 0.",
        "When defining a line from two points, the points can either be vectors or locations. " +
        "When using locations, this is a shape that can be drawn without a specific location. It will be drawn between the two given locations.",
        "If using vectors, or a direction and length, the shape does require a location to be drawn at."
})
@Examples({
        "set {_shape} to line from vector(0, 0, 0) to vector(10, 10, 10)",
        "set {_shape} to a line in direction vector(1, 1, 1) and length 10",
        "draw a line from vector(0, 0, 0) to vector(10, 10, 10) at player",
        "",
        "# note that the following does not require a location to be drawn at",
        "draw a line from player to player's target"
})
@Since("1.0.0")
public class ExprLine extends SimpleExpression<Line> {

    static {
        Skript.registerExpression(ExprLine.class, Line.class, ExpressionType.COMBINED,
                "[a] line (from|between) %vector% (to|and) %vector%",
                "[a] line (from|between) %location% (to|and) %location%",
                "[a] line (in [the]|from) direction %vector% [(and|[and] with) length %number%]");
    }

    private Expression<Vector> startVector;
    private Expression<Vector> endVector;

    private Expression<Location> startLocation;
    private Expression<Location> endLocation;

    private Expression<Vector> direction;
    private Expression<Number> length;

    private int matchedPattern = 0;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        switch (matchedPattern) {
            case 0 -> {
                startVector = (Expression<Vector>) exprs[0];
                endVector = (Expression<Vector>) exprs[1];
            }
            case 1 -> {
                startLocation = (Expression<Location>) exprs[0];
                endLocation = (Expression<Location>) exprs[1];
            }
            case 2 -> {
                direction = (Expression<Vector>) exprs[0];
                length = (Expression<Number>) exprs[1];
            }
        }
        this.matchedPattern = matchedPattern;
        return true;
    }

    @Override
    @Nullable
    protected Line[] get(Event event) {
        Line line;
        switch (matchedPattern) {
            case 0 -> {
                if (startVector.getSingle(event) == null || endVector.getSingle(event) == null)
                    return new Line[0];
                line = new Line(startVector.getSingle(event), endVector.getSingle(event));
            }
            case 1 -> {
                if (startLocation.getSingle(event) == null || endLocation.getSingle(event) == null)
                    return new Line[0];
                line = new Line(startLocation.getSingle(event), endLocation.getSingle(event));
            }
            case 2 -> {
                if (direction.getSingle(event) == null)
                    return new Line[0];
                Vector v = direction.getSingle(event);
                if (length != null && length.getSingle(event) != null)
                    v.multiply(length.getSingle(event).doubleValue());
                line = new Line(v);
            }
            default -> {
                return new Line[0];
            }
        }
        return new Line[]{line};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Line> getReturnType() {
        return Line.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (matchedPattern) {
            case 0 -> "a line from " + startVector.toString(event, debug) + " to " + endVector.toString(event, debug);
            case 1 -> "a line from " + startLocation.toString(event, debug) + " to " + endLocation.toString(event, debug);
            case 2 -> "a line in direction " + direction.toString(event, debug) + " with length " + length.toString(event, debug);
            default -> "a line";
        };
    }
}
