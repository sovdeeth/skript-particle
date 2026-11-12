package com.sovdee.skriptparticles.skript.shaders.expressions.point;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Point Count")
@Description("The total number of points being drawn this pass. Read-only.")
@Example("""
    set {_shader} to a custom shader:
        set {_t} to point's index / point count
    """)
@Since("2.0.0")
public class ExprPointCount extends PointPropertyExpression<Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointCount.class, Number.class)
                .supplier(ExprPointCount::new)
                .addPatterns(
                        "[the] point count",
                        "[the] point count of [the] point",
                        "[the] point's [total] count"
                )
                .build());
    }

    @Override
    protected Number getValue(ParticleRenderContext ctx) { return ctx.totalPoints; }

    @Override
    public Class<Number> getReturnType() { return Number.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) { return "point count"; }
}
