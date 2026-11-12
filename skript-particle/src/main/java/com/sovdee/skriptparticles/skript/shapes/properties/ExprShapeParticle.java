package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import com.sovdee.shapes.shapes.Shape;
import org.skriptlang.skript.registration.SyntaxRegistry;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.ParticleUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.bukkit.particles.particleeffects.ParticleEffect;

@Name("Shape Particle")
@Description({
        "The particle of a shape. This is the particle that the shape uses to draw itself.",
        "Changing this will change the particle of the shape. Resetting or deleting it will set it back to the default particle (flame).",
        "You can set it to any particle Skript can describe, including its data, offset, and extra value.",
        "Each shape keeps its own copy, so changing the particle of one shape never changes another."
})
@Example("set {_shape}'s particle to flame particle")
@Example("set {_shape}'s particle to red dust particle")
@Example("set particle of {_shape} to 5 electric spark particles")
@Example("reset {_shape}'s particle")
@Since("1.0.0")
public class ExprShapeParticle extends SimplePropertyExpression<Shape, ParticleEffect> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapeParticle.class, ParticleEffect.class, "[shape] particle", "shapes", false)
                        .supplier(ExprShapeParticle::new)
                        .build());
    }

    @Override
    @Nullable
    public ParticleEffect convert(Shape shape) {
        return DrawData.of(shape).getParticle();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, RESET, DELETE -> new Class[]{ParticleEffect.class};
            case ADD, REMOVE, REMOVE_ALL -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        switch (mode) {
            case SET:
                if (delta == null || delta.length == 0) return;
                // each shape gets its own copy, so changing one shape's particle later does not change the others
                for (Shape shape : getExpr().getArray(event))
                    DrawData.of(shape).setParticle(((ParticleEffect) delta[0]).copy());
                break;
            case RESET:
            case DELETE:
                for (Shape shape : getExpr().getArray(event))
                    DrawData.of(shape).setParticle(ParticleUtil.getDefaultParticle());
                break;
            case ADD:
            case REMOVE:
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends ParticleEffect> getReturnType() {
        return ParticleEffect.class;
    }

    @Override
    protected String getPropertyName() {
        return "shape particle";
    }

}
