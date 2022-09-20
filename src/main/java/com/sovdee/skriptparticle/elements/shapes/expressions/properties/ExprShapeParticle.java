package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.classes.Changer;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.destroystokyo.paper.ParticleBuilder;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprShapeParticle extends SimplePropertyExpression<Shape, ParticleBuilder> {

    static {
        register(ExprShapeParticle.class, ParticleBuilder.class, "particle", "shapes");
    }

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        if (mode == Changer.ChangeMode.SET || mode == Changer.ChangeMode.DELETE || mode == Changer.ChangeMode.RESET || mode == Changer.ChangeMode.REMOVE_ALL)
            return new Class[]{ParticleBuilder.class};
        return new Class[0];
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if (delta == null || delta.length == 0) return;
        switch (mode) {
            case SET:
                for (Shape shape : getExpr().getArray(event))
                    shape.particle((ParticleBuilder) delta[0]);
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                for (Shape shape : getExpr().getArray(event))
                    shape.particle(null);
                break;
            default:
                assert false;
        }
    }

    @Override
    protected String getPropertyName() {
        return "particlebuilder";
    }

    @Override
    public @Nullable ParticleBuilder convert(Shape shape) {
        return shape.particle();
    }

    @Override
    public Class<? extends ParticleBuilder> getReturnType() {
        return ParticleBuilder.class;
    }
}
