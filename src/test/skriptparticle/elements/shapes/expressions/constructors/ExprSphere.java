package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Sphere;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprSphere extends SimpleExpression<Sphere>{

    static {
        Skript.registerExpression(ExprSphere.class, Sphere.class, ExpressionType.COMBINED, "[a] [:solid] sphere [with|of] radius %number%");
    }

    private Expression<Number> radius;
    private boolean isSolid;

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "sphere";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal) {
            if (((Literal<Number>) radius).getSingle().doubleValue() <= 0){
                Skript.error("The radius of the sphere must be greater than 0. (radius: " + ((Literal<Number>) radius).getSingle().doubleValue() + ")");
                return false;
            }
        }
        isSolid = parseResult.hasTag("solid");
        return true;
    }


    @Override
    protected Sphere[] get(Event event) {
        Number r = radius.getSingle(event);
        if (r == null || r.doubleValue() <= 0) {
            Skript.error("The radius of the sphere must be greater than 0; defaulting to 1. (radius: " + r + ")");
            r = 1;
        }
        Sphere sphere = new Sphere(r.doubleValue());
        if (isSolid) sphere.style(Shape.Style.FILL);
        return new Sphere[]{sphere};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Sphere> getReturnType() {
        return Sphere.class;
    }
}
