package com.sovdee.skriptparticle.elements.shapes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Line;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprLine extends SimpleExpression<Line> {

    static {
        Skript.registerExpression(ExprLine.class, Line.class, ExpressionType.COMBINED, "[(the|a)] line (from|between) %vector% (to|and) %vector% [with [a] step size [of] %-number% [meters]]",
                                                                                                "[(the|a)] line (from|between) %location% (to|and) %location% [with [a] step size [of] %-number% [meters]]",
                                                                                                "[(the|a)] line (in [the]|from) direction %vector% (and|[and] with) length %number% [with [a] step size [of] %-number% [meters]]");
    }

    private Expression<Vector> startExpr;
    private Expression<Vector> endExpr;
    private Expression<Number> stepSizeExpr = null;

    private Expression<Location> startLocExpr;
    private Expression<Location> endLocExpr;

    private Expression<Vector> directionExpr;
    private Expression<Number> lengthExpr;

    private int matchedPattern = 0;

    @Override
    protected Line[] get(Event e) {
        Line line = null;
        switch (matchedPattern) {
            case 0:
                if (startExpr.getSingle(e) == null || endExpr.getSingle(e) == null)
                    return new Line[0];

                line = new Line(startExpr.getSingle(e), endExpr.getSingle(e));
                break;
            case 1:
                if (startLocExpr.getSingle(e) == null || endLocExpr.getSingle(e) == null)
                    return new Line[0];

                line = new Line(startLocExpr.getSingle(e), endLocExpr.getSingle(e));
                break;
            case 2:
                if (directionExpr.getSingle(e) == null || lengthExpr.getSingle(e) == null)
                    return new Line[0];

                line = new Line(directionExpr.getSingle(e), lengthExpr.getSingle(e).doubleValue());
                break;
        }
        if (stepSizeExpr != null && stepSizeExpr.getSingle(e) != null) {
            if (stepSizeExpr.getSingle(e).doubleValue() <= 0) {
                Skript.error("Step size must be greater than 0. (step size: " + stepSizeExpr.getSingle(e) + ")");
                return new Line[0];
            }
            line.setStepSize(stepSizeExpr.getSingle(e).doubleValue());
            line.generatePoints();
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
    public String toString(@Nullable Event e, boolean debug) {
        String str = "";
        switch (matchedPattern) {
            case 0:
                str = "the line from " + startExpr.getSingle(e) + " to " + endExpr.getSingle(e);
                break;
            case 1:
                str = "the line from " + startLocExpr.getSingle(e) + " to " + endLocExpr.getSingle(e);
                break;
            case 2:
                str = "the line in direction " + directionExpr.getSingle(e) + " with length " + lengthExpr.getSingle(e);
                break;
        }
        return str + (stepSizeExpr != null ? " with step size " + stepSizeExpr.getSingle(e) : "");
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
        if (exprs.length > 2) {
            stepSizeExpr = (Expression<Number>) exprs[2];
            if (stepSizeExpr instanceof Literal) {
                if (((Literal<Number>) stepSizeExpr).getSingle().doubleValue() <= 0) {
                    Skript.error("Step size must be greater than 0. (step size: " + ((Literal<Number>) stepSizeExpr).getSingle().doubleValue() + ")");
                    return false;
                }
            }
        }
        return true;
    }
}
