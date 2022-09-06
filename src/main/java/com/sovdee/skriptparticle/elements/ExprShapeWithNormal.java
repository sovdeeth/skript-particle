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

public class ExprShapeWithNormal extends PropertyExpression<Shape, Shape> {

    static {
        Skript.registerExpression(ExprShapeWithNormal.class, Shape.class, ExpressionType.PROPERTY,"%shapes% with normal [vector] %vector%");
    }
    private Expression<Vector> normalExpr;


    @Override
    @NotNull
    protected Shape[] get(Event event, Shape[] source) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null)
                continue;

            source[i] = source[i].clone();

            if (normalExpr != null) {
                Vector normal = normalExpr.getSingle(event);
                if (normal == null)
                    continue;
                source[i].setNormal(normal);
            }
        }
        return source;
    }

    @Override
    @NotNull
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return Arrays.toString(getExpr().getAll(event)) + " with normal vector " + (normalExpr != null ? normalExpr.getSingle(event) : new Vector(0,1,0));
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        normalExpr = (Expression<Vector>) exprs[1];
        return true;
    }
}
