package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ExprShapeWithOrientation extends PropertyExpression<Shape, Shape> {

    static {
        Skript.registerExpression(ExprShapeWithOrientation.class, Shape.class, ExpressionType.PROPERTY, "%shapes% (with rotation|rotated) %number% [(degrees|rad:radians)]");
    }

    private Expression<Number> rotationExpr;

    private boolean convertToRadians = true;


    @Override
    @NotNull
    protected Shape[] get(Event e, Shape[] source) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null)
                continue;

            source[i] = source[i].clone();
            if (rotationExpr != null) {
                Number rotation = rotationExpr.getSingle(e);

                if (rotation == null)
                    continue;

                double rot = rotation.doubleValue();
                if (convertToRadians)
                    rot = Math.toRadians(rot);

                source[i].setRotation(source[i].getRotation() + rot);
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
    public String toString(@Nullable Event e, boolean debug) {
        return Arrays.toString(getExpr().getAll(e)) + " with rotation " + (rotationExpr != null ? rotationExpr.getSingle(e) : 0) + " degrees";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        rotationExpr = (Expression<Number>) exprs[1];
        if (!parseResult.hasTag("rad"))
            convertToRadians = true;
        return true;
    }
}