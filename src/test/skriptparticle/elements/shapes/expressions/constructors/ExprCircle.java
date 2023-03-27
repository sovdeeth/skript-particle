package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Circle;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprCircle extends SimpleExpression<Circle> {

    static {
        Skript.registerExpression(ExprCircle.class, Circle.class, ExpressionType.COMBINED, "[a] (circle|:disc) [with|of] radius %number%");
    }

    private Expression<Number> radius;
    private boolean isDisc;

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "circle";
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
        isDisc = parseResult.hasTag("disc");
        return true;
    }


    @Override
    protected Circle[] get(Event event) {
        Number r = radius.getSingle(event);
        if (r == null || r.doubleValue() <= 0) {
            Skript.error("The radius of the circle must be greater than 0; defaulting to 1. (radius: " + r + ")");
            r = 1;
        }
        Circle circle = new Circle(r.doubleValue());
        if (isDisc) circle.style(Shape.Style.SURFACE);
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
