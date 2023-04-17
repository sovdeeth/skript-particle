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
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle Line")
@Description({
        "Creates a line shape between two points, or in a direction for a given length. The length must be greater than 0.",
        "When defining a line from two points, the points can either be vectors or locations/entities. " +
        "You cannot use both vectors and locations/entities, but you can mix and match locations and entities." +
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
                "[a] line (from|between) %location/entity/vector% (to|and) %location/entity/vector%",
                "[a] line (in [the]|from) direction %vector% [(and|[and] with) length %number%]");
    }

    private Expression<?> start;
    private Expression<?> end;

    private Expression<Vector> direction;
    private Expression<Number> length;

    private int matchedPattern = 0;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        switch (matchedPattern) {
            case 0 -> {
                start = exprs[0];
                end = exprs[1];
            }
            case 1 -> {
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
                Object start = this.start.getSingle(event);
                Object end = this.end.getSingle(event);
                // if both are vectors, create a line from them
                if (start instanceof Vector && end instanceof Vector) {
                    line = new Line((Vector) start, (Vector) end);
                    break;
                } else if (start instanceof Vector || end instanceof Vector) {
                    return new Line[0];
                }
                // if neither are vectors, create a dynamic line
                start = DynamicLocation.fromLocationEntity(start);
                end = DynamicLocation.fromLocationEntity(end);
                if (end == null || start == null)
                    return new Line[0];
                line = new Line((DynamicLocation) start,(DynamicLocation) end);
            }
            case 1 -> {
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
            case 0 -> "a line from " + start.toString(event, debug) + " to " + end.toString(event, debug);
            case 1 -> "a line in direction " + direction.toString(event, debug) + " with length " + length.toString(event, debug);
            default -> "a line";
        };
    }
}
