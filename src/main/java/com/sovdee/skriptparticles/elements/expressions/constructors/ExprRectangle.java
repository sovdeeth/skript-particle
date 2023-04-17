package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Rectangle;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.DynamicLocation;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

public class ExprRectangle extends SimpleExpression<Rectangle> {

    // TODO: zy and xy planes

    static {
        Skript.registerExpression(ExprRectangle.class, Rectangle.class, ExpressionType.COMBINED,
                "[a] [solid:(solid|filled)] rectangle [with|of] length %number%[,] [and] width %number%",
                "[a] [solid:(solid|filled)] rectangle [with|of] width %number%[,] [and] length %number%",
                "[a] [solid:(solid|filled)] rectangle (from|with corners [at]) %location/entity/vector% (to|and) %location/entity/vector%"
        );
    }

    private Expression<Number> lengthExpr;
    private Expression<Number> widthExpr;
    private boolean isSolid;
    private Expression<?> corner1Expr;
    private Expression<?> corner2Expr;
    private int matchedPattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        isSolid = parseResult.hasTag("solid");
        this.matchedPattern = matchedPattern;
        if (matchedPattern == 0) {
            lengthExpr = (Expression<Number>) exprs[0];
            widthExpr = (Expression<Number>) exprs[1];
        } else if (matchedPattern == 1) {
            lengthExpr = (Expression<Number>) exprs[1];
            widthExpr = (Expression<Number>) exprs[0];
        } else {
            corner1Expr = exprs[0];
            corner2Expr = exprs[1];
        }
        return true;
    }

    @Override
    protected @Nullable Rectangle[] get(Event event) {
        Rectangle rectangle;
        if (matchedPattern <= 1) {
            if (lengthExpr == null || widthExpr == null) return new Rectangle[0];
            Number length = lengthExpr.getSingle(event);
            Number width = widthExpr.getSingle(event);
            if (length == null || width == null) return new Rectangle[0];
            rectangle = new Rectangle(length.doubleValue(), width.doubleValue());
        } else {
            if (corner1Expr == null || corner2Expr == null) return new Rectangle[0];
            Object corner1 = corner1Expr.getSingle(event);
            Object corner2 = corner2Expr.getSingle(event);
            if (corner1 == null || corner2 == null) return new Rectangle[0];

            // if both are vectors, create a static rectangle
            if (corner1 instanceof Vector && corner2 instanceof Vector) {
                rectangle = new Rectangle((Vector) corner1, (Vector) corner2);
                return new Rectangle[]{rectangle};
            } else if (corner1 instanceof Vector || corner2 instanceof Vector) {
                return new Rectangle[0]; // if only one is a vector, return empty array
            }

            // if neither are vectors, create a dynamic rectangle
            corner1 = DynamicLocation.fromLocationEntity(corner1);
            corner2 = DynamicLocation.fromLocationEntity(corner2);
            if (corner1 == null || corner2 == null)
                return new Rectangle[0];
            rectangle = new Rectangle((DynamicLocation) corner1, (DynamicLocation) corner2);
        }
        if (isSolid) rectangle.setStyle(Shape.Style.SURFACE);
        return new Rectangle[]{rectangle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Rectangle> getReturnType() {
        return Rectangle.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isSolid ? "solid " : "") + switch (matchedPattern) {
            case 0 -> "rectangle with length " + lengthExpr.toString(event, debug) + " and width " + widthExpr.toString(event, debug);
            case 1 -> "rectangle from " + corner1Expr.toString(event, debug) + " to " + corner2Expr.toString(event, debug);
            default -> "";
        };
    }
}
