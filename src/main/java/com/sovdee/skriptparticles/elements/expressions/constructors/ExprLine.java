package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Line;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Particle Line")
@Description({
        "Creates a line shape between points, or in a direction for a given length. The length must be greater than 0.",
        "When defining a line from points, the points can either be vectors or locations/entities. Each point in the first set will connect to each point in the second set. " +
        "You can use the third pattern to connect points in series, like a path along the points.",
        "",
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
        "draw a line from player to player's target",
        "draw a line from player to (all players in radius 10 of player)",
        "draw a line connecting {_locations::*}"
})
@Since("1.0.0")
public class ExprLine extends SimpleExpression<Line> {

    static {
        Skript.registerExpression(ExprLine.class, Line.class, ExpressionType.COMBINED,
                "[a] line[s] (from|between) %locations/entities/vectors% (to|and) %locations/entities/vectors%",
                "[a] line (in [the]|from) direction %vector% [(and|[and] with) length %number%]",
                "[a] line (between|connecting) %locations/entities/vectors%");
    }

    private Expression<?> start;
    private Expression<?> end;

    private Expression<?> points;

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

                if (length instanceof Literal<Number> literal) {
                    if (literal.getSingle().doubleValue() <= 0) {
                        Skript.error("The length of a line must be greater than 0.");
                        return false;
                    }
                }
            }
            case 2 -> points = exprs[0];
        }
        this.matchedPattern = matchedPattern;
        return true;
    }

    @Override
    @Nullable
    protected Line[] get(Event event) {
        List<Line> lines = new ArrayList<>();
        switch (matchedPattern) {
            case 0 -> {
                Object[] start = this.start.getArray(event);
                Object[] end = this.end.getArray(event);
                // if both are vectors, create a line from them
                for (Object startObject : start) {
                    for (Object endObject : end) {
                        if (startObject instanceof Vector startVector && endObject instanceof Vector endVector) {
                            lines.add(new Line(startVector, endVector));
                            continue;
                        } else if (startObject instanceof Vector || endObject instanceof Vector) {
                            continue;
                        }
                        // if neither are vectors, create a dynamic line
                        DynamicLocation startPoint = DynamicLocation.fromLocationEntity(startObject);
                        DynamicLocation endPoint  = DynamicLocation.fromLocationEntity(endObject);
                        if (endPoint == null || startPoint == null) {
                            continue;
                        }
                        lines.add(new Line(startPoint, endPoint));
                    }
                }
            }
            case 1 -> {
                Vector v = direction.getSingle(event);
                Number length = this.length.getSingle(event);
                if (v == null)
                    return null;

                if (length != null)
                    v.multiply(Math.max(length.doubleValue(), MathUtil.EPSILON));

                lines.add(new Line(v));
            }
            case 2 -> {
                Object[] points = this.points.getArray(event);
                if (points instanceof Vector[] vectors) {
                    for (int i = 0; i < vectors.length - 1; i++) {
                        lines.add(new Line(vectors[i], vectors[i + 1]));
                    }
                } else {
                    List<DynamicLocation> locations = new ArrayList<>();
                    for (Object point : points) {
                        if (point instanceof Vector)
                            continue;
                        DynamicLocation location = DynamicLocation.fromLocationEntity(point);
                        if (location == null)
                            continue;
                        locations.add(location);
                    }
                    for (int i = 0; i < locations.size() - 1; i++) {
                        lines.add(new Line(locations.get(i), locations.get(i + 1)));
                    }
                }
            }
            default -> {
                return null;
            }
        }
        return lines.toArray(new Line[0]);
    }

    @Override
    public boolean isSingle() {
        return switch (matchedPattern) {
            case 0 -> end.isSingle() && start.isSingle();
            case 1 -> true;
            default -> false;
        };
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
            case 2 -> "a line connecting " + points.toString(event, debug);
            default -> "a line";
        };
    }
}
