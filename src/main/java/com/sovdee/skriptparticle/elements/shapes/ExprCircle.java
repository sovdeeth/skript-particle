package com.sovdee.skriptparticle.elements.shapes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Circle;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprCircle extends SimpleExpression<Circle> {

    static {
        Skript.registerExpression(ExprCircle.class, Circle.class, ExpressionType.COMBINED, "[(the|a)] circle [(with|of)] radius %number% [(and|[and] with) [a] step size [of] %-number% [(degrees|rad:radians)]]",
                                                                                                    "[(the|a)] circle [(with|of)] radius %number% (and|[and] with) [a] particle count [of] %number%",
                                                                                                    "[(the|a)] circle [(with|of)] radius %number% (and|[and] with) %number% (points|particles)");
    }

    private Expression<Number> radius;
    private Expression<Number> stepSizeOrParticleCount;
    private boolean countFlag = false;
    private boolean convertToRadians = true;
    private Circle circle;

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return circle.toString();
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal) {
            if (((Literal<Number>) radius).getSingle().doubleValue() <= 0){
                Skript.error("The radius of the circle must be greater than 0. (radius: " + ((Literal<Number>) radius).getSingle().doubleValue() + ")");
                return false;
            }
        }
        stepSizeOrParticleCount = (Expression<Number>) exprs[1];
        if (stepSizeOrParticleCount instanceof Literal) {
            if (((Literal<Number>) stepSizeOrParticleCount).getSingle().doubleValue() <= 0){
                Skript.error("The " + (matchedPattern == 0 ? "step size" : "particle count") + " of the circle must be greater than 0. (" + (matchedPattern == 0 ? "step size" : "particle count") + ": " + ((Literal<Number>) stepSizeOrParticleCount).getSingle().doubleValue() + ")");
                return false;
            }
        }
        if (matchedPattern != 0)
            countFlag = true;
        else if (!parseResult.hasTag("rad")) {
            convertToRadians = false;
        }

        return true;
    }


    @Override
    protected Circle[] get(Event event) {
        Number r = radius.getSingle(event);
        if (r == null || r.doubleValue() <= 0) {
            Skript.error("The radius of the circle must be greater than 0; defaulting to 1. (radius: " + r + ")");
            r = 1;
        }

        if (stepSizeOrParticleCount == null) {
            circle = new Circle(r.doubleValue());
            return new Circle[]{circle};
        }

        Number s = stepSizeOrParticleCount.getSingle(event);
        if (s != null && s.doubleValue() <= 0) {
            Skript.error("The " + (!countFlag ? "step size" : "particle count") + " of the circle must be greater than 0; defaulting to " + (!countFlag ? "12 degrees" : "30 particles") + ". (" + (!countFlag ? "step size" : "particle count") + ": " + s + ")");
            s = (!countFlag ? 12 : 30);
        }

        if (countFlag) {
            circle = new Circle(r.doubleValue()).stepSize(Math.PI * 2 / s.doubleValue());
        } else {
            if (convertToRadians)
                s = Math.PI * (s.doubleValue() / 180);
            circle = new Circle(r.doubleValue()).stepSize(s.doubleValue());
        }

        return new Circle[]{circle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Circle> getReturnType() {
        return Circle.class;
    }
}
