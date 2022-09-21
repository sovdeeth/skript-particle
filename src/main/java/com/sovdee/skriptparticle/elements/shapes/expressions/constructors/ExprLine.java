package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Line;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprLine extends SimpleExpression<Line> {

    static {
        Skript.registerExpression(ExprLine.class, Line.class, ExpressionType.COMBINED, "[a] line (from|between) %vector% (to|and) %vector%",
                                                                                                "[a] line (from|between) %location% (to|and) %location%",
                                                                                                "[a] line (in [the]|from) direction %vector% (and|[and] with) length %number%");
    }

    private Expression<Vector> startExpr;
    private Expression<Vector> endExpr;

    private Expression<Location> startLocExpr;
    private Expression<Location> endLocExpr;

    private Expression<Vector> directionExpr;
    private Expression<Number> lengthExpr;

    private int matchedPattern = 0;

    @Override
    protected Line[] get(Event event) {
        Line line = null;
        switch (matchedPattern) {
            case 0:
                if (startExpr.getSingle(event) == null || endExpr.getSingle(event) == null)
                    return new Line[0];

                line = new Line(startExpr.getSingle(event), endExpr.getSingle(event));
                break;
            case 1:
                if (startLocExpr.getSingle(event) == null || endLocExpr.getSingle(event) == null)
                    return new Line[0];

                line = new Line(startLocExpr.getSingle(event), endLocExpr.getSingle(event));
                break;
            case 2:
                if (directionExpr.getSingle(event) == null || lengthExpr.getSingle(event) == null)
                    return new Line[0];

                line = new Line().end(directionExpr.getSingle(event).normalize().multiply(lengthExpr.getSingle(event).doubleValue()));
                break;
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
        String str = "";
        switch (matchedPattern) {
            case 0:
                str = "the line from " + startExpr.getSingle(event) + " to " + endExpr.getSingle(event);
                break;
            case 1:
                str = "the line from " + startLocExpr.getSingle(event) + " to " + endLocExpr.getSingle(event);
                break;
            case 2:
                str = "the line in direction " + directionExpr.getSingle(event) + " with length " + lengthExpr.getSingle(event);
                break;
        }
        return str;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.matchedPattern = matchedPattern;
        switch (matchedPattern) {
            case 0:
                startExpr = (Expression<Vector>) exprs[0];
                endExpr = (Expression<Vector>) exprs[1];
                break;
            case 1:
                startLocExpr = (Expression<Location>) exprs[0];
                endLocExpr = (Expression<Location>) exprs[1];
                break;
            case 2:
                directionExpr = (Expression<Vector>) exprs[0];
                lengthExpr = (Expression<Number>) exprs[1];
                break;
        }
        return true;
    }
}
