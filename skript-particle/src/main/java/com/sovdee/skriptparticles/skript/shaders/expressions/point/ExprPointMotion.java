package com.sovdee.skriptparticles.skript.shaders.expressions.point;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Point Motion")
@Description({
        "The X, Y, or Z motion (velocity) of the current point.",
        "Setting any motion component automatically enables motion for this point.",
})
@Example("""
    set {_shader} to a custom shader:
        set the point's y motion to 0.5
        set the x motion of point to point's x / 2
    """)
@Since("2.0.0")
public class ExprPointMotion extends PointPropertyExpression<Number> {

    // mark: 0=x, 1=y, 2=z
    private int axis;

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointMotion.class, Number.class)
                .supplier(ExprPointMotion::new)
                .addPatterns(
                        "[the] point's (0¦x|1¦y|2¦z) motion",
                        "[the] (0¦x|1¦y|2¦z) motion of [the] point"
                )
                .build());
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
        this.axis = parseResult.mark;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    protected Number getValue(ParticleRenderContext ctx) {
        return switch (axis) {
            case 1 -> ctx.motionY;
            case 2 -> ctx.motionZ;
            default -> ctx.motionX;
        };
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, ADD, REMOVE -> new Class[]{Number.class};
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        ParticleRenderContext ctx = getContext(event);
        if (ctx == null || delta == null || delta[0] == null) return;
        float value = ((Number) delta[0]).floatValue();
        switch (mode) {
            case SET -> setAxis(ctx, value);
            case ADD -> setAxis(ctx, getAxis(ctx) + value);
            case REMOVE -> setAxis(ctx, getAxis(ctx) - value);
        }
        ctx.hasMotion = true;
    }

    private float getAxis(ParticleRenderContext ctx) {
        return switch (axis) {
            case 1 -> ctx.motionY;
            case 2 -> ctx.motionZ;
            default -> ctx.motionX;
        };
    }

    private void setAxis(ParticleRenderContext ctx, float value) {
        switch (axis) {
            case 1 -> ctx.motionY = value;
            case 2 -> ctx.motionZ = value;
            default -> ctx.motionX = value;
        }
    }

    @Override
    public Class<Number> getReturnType() { return Number.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "point's " + switch (axis) { case 1 -> "y"; case 2 -> "z"; default -> "x"; } + " motion";
    }
}
