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

@Name("Point Index")
@Description("The zero-based draw-order index of the current point. Read-only.")
@Example("""
    set {_shader} to a custom shader:
        set {_t} to point's index / point count
    """)
@Since("2.0.0")
public class ExprPointIndex extends PointPropertyExpression<Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointIndex.class, Number.class)
                .supplier(ExprPointIndex::new)
                .addPatterns(
                        "[the] point's index",
                        "[the] index of [the] point"
                )
                .build());
    }

    @Override
    protected Number getValue(ParticleRenderContext ctx) { return ctx.index; }

    @Override
    public Class<Number> getReturnType() { return Number.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) { return "point's index"; }
}
