package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeParticleDensity extends SimplePropertyExpression<Shape, Number> {

    static {
        // most shapes kinda disregard the particle count, but it's still useful for some
        // every shape responds to the particle density property pretty well though
        register(ExprShapeParticleDensity.class, Number.class, "particle (:density|:count)", "shapes");
    }

    private boolean flag;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        flag = parseResult.hasTag("density");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected String getPropertyName() {
        return "particle density/count";
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        if (flag)
            return shape.particleDensity();
        else
            return shape.particleCount();
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Number.class};
    }

    @Override
    public void change(Event e, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        Shape[] shapes = getExpr().getAll(e);
        if (shapes.length == 0)
            return;
        Number number = (Number) delta[0];
        switch (mode) {
            case SET:
                for (Shape shape : shapes) {
                    if (flag)
                        shape.particleDensity(number.doubleValue());
                    else
                        shape.particleCount(number.intValue());
                }
                break;
            case ADD:
                for (Shape shape : shapes) {
                    if (flag)
                        shape.particleDensity(shape.particleDensity() + number.doubleValue());
                    else
                        shape.particleCount(shape.particleCount() + number.intValue());
                }
                break;
            case REMOVE:
                for (Shape shape : shapes) {
                    if (flag)
                        shape.particleDensity(shape.particleDensity() - number.doubleValue());
                    else
                        shape.particleCount(shape.particleCount() - number.intValue());
                }
                break;
            case DELETE:
            case RESET:
            case REMOVE_ALL:
                for (Shape shape : shapes) {
                    shape.particleDensity(0.1);
                }
                break;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }
}
