package com.sovdee.skriptparticle.elements.particles;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ExprParticleGradientPointList extends PropertyExpression<ParticleGradient, ParticleGradient.ParticleGradientPoint> {

    static {
        register(ExprParticleGradientPointList.class, ParticleGradient.ParticleGradientPoint.class, "[gradient] point[s] [list]", "particlegradient");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{ParticleGradient.ParticleGradientPoint.class};
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0 || getExpr().getSingle(e) == null)
            return;
        switch (mode) {
            case ADD:
                getExpr().getSingle(e).addPoints((ParticleGradient.ParticleGradientPoint[]) delta);
                break;
            case REMOVE:
                getExpr().getSingle(e).removePoints((ParticleGradient.ParticleGradientPoint[]) delta);
                break;
            case SET:
                getExpr().getSingle(e).setPoints((ParticleGradient.ParticleGradientPoint[]) delta);
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                getExpr().getSingle(e).setPoints();
                break;
            default:
                assert false;
        }
    }



    @Override
    public Class<? extends ParticleGradient.ParticleGradientPoint> getReturnType() {
        return ParticleGradient.ParticleGradientPoint.class;
    }

    @Override
    protected ParticleGradient.ParticleGradientPoint[] get(Event event, ParticleGradient[] particleGradients) {
        if (particleGradients == null || particleGradients.length == 0)
            return new ParticleGradient.ParticleGradientPoint[0];
        ArrayList<ParticleGradient.ParticleGradientPoint> points = new ArrayList<>();
        for (ParticleGradient particleGradient : particleGradients)
            points.addAll(particleGradient.points());
        return points.toArray(new ParticleGradient.ParticleGradientPoint[0]);
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "gradient point list";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        setExpr((Expression<ParticleGradient>) expressions[0]);
        return true;
    }
}
