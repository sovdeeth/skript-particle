package com.sovdee.skriptparticle.elements;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticle.particles.CustomParticle;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeParticle extends SimplePropertyExpression<Shape, CustomParticle> {

    static {
        register(ExprShapeParticle.class, CustomParticle.class, "particle", "shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.REMOVE_ALL)
            return new Class[]{CustomParticle.class};
        return new Class[0];
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0) return;
        switch (mode) {
            case SET:
                for (Shape shape : getExpr().getArray(event))
                    shape.setParticle((CustomParticle) delta[0]);
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getArray(event))
                    shape.setParticle(null);
                break;
            default:
                assert false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "particle";
    }

    @Override
    public @Nullable CustomParticle convert(Shape shape) {
        return shape.getParticle();
    }

    @Override
    public Class<? extends CustomParticle> getReturnType() {
        return CustomParticle.class;
    }
}
