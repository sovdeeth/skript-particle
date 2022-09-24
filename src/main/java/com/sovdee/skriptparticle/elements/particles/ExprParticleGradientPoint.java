package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.util.ColorRGB;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.particles.ParticleGradient.ParticleGradientPoint;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class ExprParticleGradientPoint extends SimpleExpression<ParticleGradientPoint> {

    static {
        Skript.registerExpression(ExprParticleGradientPoint.class, ParticleGradientPoint.class, ExpressionType.SIMPLE, "[a] [particle] gradient point [from|at] [vector] %vector%[,] [and] colo[u]r %color%");
    }

    private Expression<Vector> point;
    private Expression<ColorRGB> colour;

    @Override
    protected @Nullable ParticleGradientPoint[] get(Event event) {
        if (point.getSingle(event) == null || colour.getSingle(event) == null)
            return null;
        return new ParticleGradientPoint[]{new ParticleGradientPoint(point.getSingle(event), colour.getSingle(event))};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ParticleGradientPoint> getReturnType() {
        return ParticleGradientPoint.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "gradient point";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        point = (Expression<Vector>) expressions[0];
        colour = (Expression<ColorRGB>) expressions[1];
        return true;
    }
}
