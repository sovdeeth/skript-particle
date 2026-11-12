package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.PropertyExpression;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.sampling.PointLimitExceededException;
import com.sovdee.shapes.sampling.PointSampler;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Particle Density / Point Count")
@Description({
        "The density at which particles are spawned in a shape. This is in \"particles per meter\" (ppm), and defaults to 4. " +
                "Be careful with this property, as it can cause lag if set to a high number. It's recommended to not go above 20 or so for this reason. 1000 ppm is the maximum value.",
        "Keep in mind that this value scales with dimensions. A 1 meter line with a density of 20 will spawn 20 particles, but a 1 meter cube with a density of 20 will spawn 8,000 particles (2,400 if hollow).",
        "Shapes are never drawn with more points than their point limit (see Shape Point Limit). If a density would produce more, the shape is drawn at the finest density that stays within the limit, and this expression returns that density instead.",
        "Also, be aware that this may not be exact. Some shapes, like cuboids, will tweak the density a bit to ensure particles land exactly on the borders of the shape.",
        "",
        "This syntax also supports setting the point count directly, which is the amount of particles spawned in a shape.",
        "Note that this is NOT exact. The actual amount of particles spawned will be the closest amount to the point count that the shape can be drawn with.",
        "For example, if the point count of a solid cube is set to 100, the actual amount of particles spawned will be 125, which is the nearest cubic number.",
        "The returned value of this expression will always be the accurate amount of particles spawned in a shape.",
        "",
        "Changing this will change the particle density or point count of the shape. Resetting or deleting it will set it back to the default density (4).",
})
@Example("set {_shape}'s particle density to 10")
@Example("set {_shape}'s point count to 100")
@Example("reset {_shape}'s particle density")
@Example("reset {_shape}'s point count")
@Since("1.0.0")
public class ExprShapeParticleDensity extends SimplePropertyExpression<Shape, Number> {

    public static void register(SyntaxRegistry registry) {
        // "point count" rather than "particle count": Skript has its own "particle count of %particles%" expression,
        // which would be chosen over this one whenever the type of the shape is not known at parse time.
        // most shapes kinda disregard the point count, but it's still useful for some
        // every shape responds to the particle density property pretty well though
        registry.register(SyntaxRegistry.EXPRESSION,
                PropertyExpression.infoBuilder(ExprShapeParticleDensity.class, Number.class, "(density:(particle|point) density|point count)", "shapes", false)
                        .supplier(ExprShapeParticleDensity::new)
                        .build());
    }

    private boolean isDensity;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        isDensity = parseResult.hasTag("density");
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    public @Nullable Number convert(Shape shape) {
        PointSampler sampler = shape.getPointSampler();
        try {
            if (isDensity) {
                // the density actually in use, which may be coarser than the one set if the point cap applies
                return 1 / sampler.getEffectiveDensity(shape);
            }
            return sampler.getPointCount(shape);
        } catch (PointLimitExceededException e) {
            return null;
        }
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case ADD, REMOVE, SET, RESET, DELETE -> new Class[]{Number.class};
            case REMOVE_ALL -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        Shape[] shapes = getExpr().getArray(event);
        if (shapes.length == 0)
            return;
        if (mode == ChangeMode.DELETE || mode == ChangeMode.RESET) {
            // back to the default density, limited by the shape's point cap
            for (Shape shape : shapes)
                shape.getPointSampler().resetDensity();
            return;
        }
        if (delta == null || delta.length == 0)
            return;
        double change = ((Number) delta[0]).doubleValue();
        switch (mode) {
            case REMOVE:
                change = -change;
            case ADD:
                for (Shape shape : shapes) {
                    PointSampler sampler = shape.getPointSampler();
                    try {
                        if (isDensity) {
                            // clamp to 0.001 and 1000, enough to kill the client but not enough to cause an actual error
                            double particlesPerMeter = 1 / sampler.getEffectiveDensity(shape) + change;
                            sampler.setDensity(particlesPerMeter <= 0 ? 1000 : MathUtil.clamp(1 / particlesPerMeter, 0.001, 1000));
                        } else {
                            // clamp to 1, the minimum amount of particles
                            sampler.setParticleCount(shape, Math.max(1, sampler.getPointCount(shape) + (int) change));
                        }
                    } catch (PointLimitExceededException ignored) {
                        // the shape cannot be sampled as it is, so there is nothing to add to
                    }
                }
                break;
            case SET:
                for (Shape shape : shapes) {
                    PointSampler sampler = shape.getPointSampler();
                    if (isDensity) {
                        sampler.setDensity(change <= 0 ? 1000 : MathUtil.clamp(1 / change, 0.001, 1000));
                    } else {
                        sampler.setParticleCount(shape, Math.max(1, (int) change));
                    }
                }
                break;
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
        return isDensity ? "particle density" : "point count";
    }

}
