package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.particles.CustomParticle;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;

public class ExprShapeWithParticle extends PropertyExpression<Shape, Shape> {

    static {
        Skript.registerExpression(ExprShapeWithNormal.class, Shape.class, ExpressionType.PROPERTY,"%shapes% with particle %customparticle%");
    }
    private Expression<CustomParticle> particleExpr;

    @Override
    @NotNull
    protected Shape[] get(Event e, Shape[] source) {
        for (int i = 0; i < source.length; i++) {
            if (source[i] == null)
                continue;

            source[i] = source[i].clone();

            if (particleExpr != null) {
                CustomParticle particle = particleExpr.getSingle(e);
                if (particle == null)
                    continue;
                source[i].setParticle(particle);
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
        return Arrays.toString(getExpr().getAll(e)) + " with particle " + (particleExpr != null ? particleExpr.getSingle(e) : new Vector(0,1,0));
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<Shape>) exprs[0]);
        particleExpr = (Expression<CustomParticle>) exprs[1];
        return true;
    }
}
