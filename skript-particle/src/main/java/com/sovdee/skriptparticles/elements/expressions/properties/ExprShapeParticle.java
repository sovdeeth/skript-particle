package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.skriptparticles.particles.Particle;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.ParticleUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Particle")
@Description({
        "The particle of a shape. This is the particle that the shape uses to draw itself.",
        "Changing this will change the particle of the shape. Resetting or deleting it will set it back to the default particle (flame).",
        "You can set the particle to any base or custom particle."
})
@Examples({
        "set {_shape}'s particle to flame particle",
        "set {_shape}'s particle to 1 of electric spark particle with extra 0",
        "reset {_shape}'s particle",
        "",
        "create a custom soul fire flame particle:",
            "\tvelocity: inwards",
            "\textra: 0.5",
            "\tforce: true",
        "set {_shape}'s particle to the last created particle"
})
@Since("1.0.0")
public class ExprShapeParticle extends SimplePropertyExpression<Shape, Particle> {

    static {
        PropertyExpression.register(ExprShapeParticle.class, Particle.class, "[custom] particle", "shapes");
    }

    @Override
    @Nullable
    public Particle convert(Shape shape) {
        return shape.getParticle();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{Particle.class};
            case ADD, REMOVE, REMOVE_ALL -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                for (Shape shape : getExpr().getArray(event))
                    shape.setParticle((Particle) delta[0]);
                break;
            case RESET:
            case DELETE:
                for (Shape shape : getExpr().getArray(event))
                    shape.setParticle(ParticleUtil.getDefaultParticle());
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Particle> getReturnType() {
        return Particle.class;
    }

    @Override
    protected String getPropertyName() {
        return "custom particle";
    }

}
