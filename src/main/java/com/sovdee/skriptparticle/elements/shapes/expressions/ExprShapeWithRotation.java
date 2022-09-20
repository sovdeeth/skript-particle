package com.sovdee.skriptparticle.elements.shapes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ExprShapeWithRotation extends PropertyExpression<Shape, Shape> {

    static {
        Skript.registerExpression(ExprShapeWithRotation.class, Shape.class, ExpressionType.PROPERTY, "%shapes% (with [rotation]|rotated [with]) %rotation%",
                                                                                                                "%shapes% with rotation %vector% [and angle %number% [:degrees|radians]]");
    }

    private Expression<Quaternion> orientationExpr;
    private Expression<Number> rotationExpr;
    private Expression<Vector> normalExpr;


    private boolean convertToRadians = true;


    @Override
    @NotNull
    protected Shape[] get(Event event, Shape[] source) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null)
                continue;

            source[i] = source[i].clone();
            if (orientationExpr != null) {
                Quaternion orientation = orientationExpr.getSingle(event);
                if (orientation == null)
                    continue;
                source[i].orientation(orientation);

            } else if (normalExpr != null) {
                Vector normal = normalExpr.getSingle(event);
                if (normal == null)
                    continue;

                double rotation = 0;
                if (rotationExpr != null) {
                    Number rot = rotationExpr.getSingle(event);
                    if (rot == null)
                        continue;
                    rotation = rot.doubleValue();
                }

                if (convertToRadians)
                    rotation = Math.toRadians(rotation);

                source[i].orientation().set(normal, rotation);
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
        return Arrays.toString(getExpr().getAll(event)) + " with rotation " + (rotationExpr != null ? rotationExpr.getSingle(event) : 0) + " degrees";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        if (matchedPattern == 0){
            orientationExpr = (Expression<Quaternion>) exprs[1];
        } else {
            normalExpr = (Expression<Vector>) exprs[1];
            rotationExpr = (Expression<Number>) exprs[2];
            if (!parseResult.hasTag("degrees"))
                convertToRadians = true;
        }
        return true;
    }
}