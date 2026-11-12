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

@Name("Point Scale")
@Description("The scale of the shape being drawn. Read-only.")
@Example("""
    set {_shader} to a custom shader:
        set {_s} to point's scale
    """)
@Since("2.0.0")
public class ExprPointScale extends PointPropertyExpression<Number> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointScale.class, Number.class)
                .supplier(ExprPointScale::new)
                .addPatterns(
                        "[the] point's scale",
                        "[the] scale of [the] point"
                )
                .build());
    }

    @Override
    protected Number getValue(ParticleRenderContext ctx) { return ctx.scale; }

    @Override
    public Class<Number> getReturnType() { return Number.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) { return "point's scale"; }
}
