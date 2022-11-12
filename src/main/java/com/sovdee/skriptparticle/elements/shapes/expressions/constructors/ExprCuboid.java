package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Cuboid;
import com.sovdee.skriptparticle.util.Style;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprCuboid extends SimpleExpression<Cuboid> {

    static {
            Skript.registerExpression(ExprCuboid.class, Cuboid.class, ExpressionType.COMBINED, "[a[n]] [outlined|:hollow|:solid] cuboid (with|from) [dimensions] [x] %number%, [y] %number%(,| and) [z] %number%",
                "[a[n]] [outlined|:hollow|:solid] cuboid (with|from) [corners] %location% [to|and] %location%",
                "[a[n]] [outlined|:hollow|:solid] cuboid (with|from) [corners] %vector% [to|and] %vector%");
    }

    private Expression<Number> widthExpr;
    private Expression<Number> heightExpr;
    private Expression<Number> depthExpr;
    private Expression<Location> startLocExpr;
    private Expression<Location> endLocExpr;
    private Expression<Vector> startVecExpr;
    private Expression<Vector> endVecExpr;
    private int matchedPattern;
    private Style style;

    @Override
    protected @Nullable Cuboid[] get(Event event) {
        Cuboid cuboid;
        switch (matchedPattern) {
            case 0:
                if (widthExpr.getSingle(event) == null || heightExpr.getSingle(event) == null || depthExpr.getSingle(event) == null)
                    return new Cuboid[0];
                cuboid = new Cuboid(widthExpr.getSingle(event).doubleValue() / 2, heightExpr.getSingle(event).doubleValue() / 2, depthExpr.getSingle(event).doubleValue() / 2);
                break;
            case 1:
                if (startLocExpr.getSingle(event) == null || endLocExpr.getSingle(event) == null)
                    return new Cuboid[0];
                cuboid = new Cuboid(startLocExpr.getSingle(event), endLocExpr.getSingle(event));
                break;
            case 2:
                if (startVecExpr.getSingle(event) == null || endVecExpr.getSingle(event) == null)
                    return new Cuboid[0];
                cuboid = new Cuboid(startVecExpr.getSingle(event), endVecExpr.getSingle(event));
                break;
            default:
                return new Cuboid[0];
        };
        cuboid.style(style);
        return new Cuboid[]{cuboid};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Cuboid> getReturnType() {
        return Cuboid.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return switch (matchedPattern) {
            case 0 -> "cuboid with width " + widthExpr.toString(event, b) + ", height " + heightExpr.toString(event, b) + ", and depth " + depthExpr.toString(event, b);
            case 1 -> "cuboid with location corners " + startLocExpr.toString(event, b) + " and " + endLocExpr.toString(event, b);
            case 2 -> "cuboid with vector corners " + startVecExpr.toString(event, b) + " and " + endVecExpr.toString(event, b);
            default -> "";
        };
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        switch (matchedPattern) {
            case 0:
                widthExpr = (Expression<Number>) expressions[0];
                heightExpr = (Expression<Number>) expressions[1];
                depthExpr = (Expression<Number>) expressions[2];
                break;
            case 1:
                startLocExpr = (Expression<Location>) expressions[0];
                endLocExpr = (Expression<Location>) expressions[1];
                break;
            case 2:
                startVecExpr = (Expression<Vector>) expressions[0];
                endVecExpr = (Expression<Vector>) expressions[1];
                break;
        }
        this.matchedPattern = matchedPattern;
        this.style = parseResult.hasTag("hollow") ? Style.SURFACE : parseResult.hasTag("solid") ? Style.FILL : Style.OUTLINE;
        return true;
    }
}
