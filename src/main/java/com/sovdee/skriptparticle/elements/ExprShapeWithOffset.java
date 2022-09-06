package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ExprShapeWithOffset extends PropertyExpression<Shape, Shape> {

    static {
        Skript.registerExpression(ExprShapeWithOffset.class, Shape.class, ExpressionType.PROPERTY,"%shapes% with offset [vector] %vector%");
    }
    private Expression<Vector> offsetExpr;


    @Override
    @NotNull
    protected Shape[] get(Event event, Shape[] source) {
        Shape[] newShapes = new Shape[source.length];
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null)
                continue;

            newShapes[i] = source[i].clone();

            if (offsetExpr != null) {
                Vector offset = offsetExpr.getSingle(event);
                if (offset == null)
                    continue;
                newShapes[i].setOffset(offset);
            }
        }
        return newShapes;
    }

    @Override
    @NotNull
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return Arrays.toString(getExpr().getAll(event)) + " with offset vector " + (offsetExpr != null ? offsetExpr.getSingle(event) : new Vector(0,0,0));
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        offsetExpr = (Expression<Vector>) exprs[1];
        return true;
    }
}
