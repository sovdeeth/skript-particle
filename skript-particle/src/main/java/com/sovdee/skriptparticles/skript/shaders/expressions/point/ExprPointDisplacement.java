package com.sovdee.skriptparticles.skript.shaders.expressions.point;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
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

@Name("Point Displacement")
@Description("The X, Y, or Z world-space displacement of the current point. Added to the world position at render time.")
@Examples({
        "add 0.5 to the point's y displacement",
        "set the x displacement of point to 1",
})
@Since("2.0.0")
public class ExprPointDisplacement extends PointPropertyExpression<Number> {

    // mark: 0=x, 1=y, 2=z
    private int axis;

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointDisplacement.class, Number.class)
                .supplier(ExprPointDisplacement::new)
                .addPatterns(
                        "[the] point's (0¦x|1¦y|2¦z) displacement",
                        "[the] (0¦x|1¦y|2¦z) displacement of [the] point"
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
            case 1 -> ctx.displacementY;
            case 2 -> ctx.displacementZ;
            default -> ctx.displacementX;
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
        double value = ((Number) delta[0]).doubleValue();
        switch (mode) {
            case SET -> setAxis(ctx, value);
            case ADD -> setAxis(ctx, getAxis(ctx) + value);
            case REMOVE -> setAxis(ctx, getAxis(ctx) - value);
        }
    }

    private double getAxis(ParticleRenderContext ctx) {
        return switch (axis) {
            case 1 -> ctx.displacementY;
            case 2 -> ctx.displacementZ;
            default -> ctx.displacementX;
        };
    }

    private void setAxis(ParticleRenderContext ctx, double value) {
        switch (axis) {
            case 1 -> ctx.displacementY = value;
            case 2 -> ctx.displacementZ = value;
            default -> ctx.displacementX = value;
        }
    }

    @Override
    public Class<Number> getReturnType() { return Number.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "point's " + switch (axis) { case 1 -> "y"; case 2 -> "z"; default -> "x"; } + " displacement";
    }
}
