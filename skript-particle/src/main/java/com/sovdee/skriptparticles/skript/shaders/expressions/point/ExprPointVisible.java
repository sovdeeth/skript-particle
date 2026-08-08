package com.sovdee.skriptparticles.skript.shaders.expressions.point;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Point Visibility")
@Description("Whether the current point should be rendered. Set to false to skip rendering this point entirely.")
@Examples({
        "set point's visibility to false",
        "if point's visibility is true:",
})
@Since("2.0.0")
public class ExprPointVisible extends PointPropertyExpression<Boolean> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprPointVisible.class, Boolean.class)
                .supplier(ExprPointVisible::new)
                .addPatterns(
                        "[the] point's visibility",
                        "[the] visibility of [the] point"
                )
                .build());
    }

    @Override
    protected Boolean getValue(ParticleRenderContext ctx) {
        return ctx.visible;
    }

    @Override
    public @Nullable Class<?>[] acceptChange(ChangeMode mode) {
        return mode == ChangeMode.SET ? new Class[]{Boolean.class} : null;
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        ParticleRenderContext ctx = getContext(event);
        if (ctx == null || delta == null || delta[0] == null) return;
        ctx.visible = (Boolean) delta[0];
    }

    @Override
    public Class<Boolean> getReturnType() { return Boolean.class; }

    @Override
    public String toString(@Nullable Event event, boolean debug) { return "point's visibility"; }
}
