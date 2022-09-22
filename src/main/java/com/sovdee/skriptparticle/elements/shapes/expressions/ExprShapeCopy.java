package com.sovdee.skriptparticle.elements.shapes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeCopy extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprShapeCopy.class, Shape.class, ExpressionType.SIMPLE, "[a] copy of %shape%");
    }

    Expression<Shape> shapeExpr;

    @Override
    protected @Nullable Shape[] get(Event event) {
        Shape[] shape = shapeExpr.getAll(event);
        if (shape == null || shape.length == 0)
            return new Shape[0];
        Shape[] copy = new Shape[shape.length];
        for (int i = 0; i < shape.length; i++) {
            if (shape[i] == null)
                continue;
            copy[i] = shape[i].clone();
        }
        return copy;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "shape copy";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) expressions[0];
        return true;
    }
}
