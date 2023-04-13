package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;

import org.jetbrains.annotations.Nullable;

@Name("Shape Particle Density / Particle Count")
@Description({
        "The density at which particles are spawned in a shape. This is in \"particles per meter\", and defaults to 4." +
        "Be careful with this property, as it can cause lag if set to a high number. It's recommended to not go above 20 or so for this reason. 1000 ppm is the maximum value.",
        "Keep in mind that this value scales with dimensions. A 1 meter line with a density of 20 will spawn 20 particles, but a 1 meter cube with a density of 20 will spawn 8,000 particles (2,400 if hollow).",
        "",
        "This syntax also supports setting the particle count directly, which is the amount of particles spawned in a shape.",
        "Note that this is NOT exact. The actual amount of particles spawned will be the closest multiple of the particle count to the amount of particles needed to draw the shape.",
        "For example, if the particle count of a solid cube is set to 100, the actual amount of particles spawned will be 125, which is the nearest cubic number.",
        "The returned value of this expression will always be the accurate amount of particles spawned in a shape.",
        "",
        "Changing this will change the particle density or count of the shape. Resetting or deleting it will set it back to the default density (4).",
})
@Examples({
        "set {_shape}'s particle density to 10",
        "set {_shape}'s particle count to 100",
        "reset {_shape}'s particle density",
        "reset {_shape}'s particle count",
})
@Since("1.0.0")
public class ExprShapeParticleDensity extends SimplePropertyExpression<Shape, Number> {

    static {
        // most shapes kinda disregard the particle count, but it's still useful for some
        // every shape responds to the particle density property pretty well though
        PropertyExpression.register(ExprShapeParticleDensity.class, Number.class, "particle (:density|:count)", "shapes");
    }

    private boolean flag;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        flag = parseResult.hasTag("density");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        if (flag)
            return 1 / shape.getParticleDensity();
        else
            return shape.getParticleCount();
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET, DELETE -> new Class[]{Number.class};
            case REMOVE_ALL -> null;
        };
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, ChangeMode mode) {
        if (delta == null || delta.length == 0)
            return;
        Shape[] shapes = getExpr().getArray(event);
        if (shapes.length == 0)
            return;
        double change = ((Number) delta[0]).doubleValue();
        switch (mode) {
            case REMOVE:
                change = -change;
            case ADD:
                for (Shape shape : shapes) {
                    if (flag) {
                        // clamp to 0.001 and 1000, enough to kill the client but not enough to cause an actual error
                        shape.setParticleDensity(MathUtil.clamp(1/(1/shape.getParticleDensity() + change), 0.001, 1000));
                    } else
                        // clamp to 1, the minimum amount of particles
                        shape.setParticleCount(Math.max(1, shape.getParticleCount() + (int) change));
                }
                break;
            case SET:
                for (Shape shape : shapes) {
                    if (flag)
                        shape.setParticleDensity(MathUtil.clamp(1/change, 0.001, 1000));
                    else
                        shape.setParticleCount(Math.max(1, (int) change));
                }
                break;
            case DELETE:
            case RESET:
                for (Shape shape : shapes) {
                    shape.setParticleDensity(0.25);
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return "particle " + (flag ? "density" : "count");
    }

}
