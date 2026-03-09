package com.sovdee.skriptparticles.skript.shaders.effects;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.AbstractGradientModifier;
import org.bukkit.Color;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Skript effect that adds a color stop at a specific normalized position to a gradient modifier.
 * See {@link #init}, {@link #execute}, and {@link #toString} for behaviour details.
 * Documented from the Skript side via {@code @Name}, {@code @Description}, {@code @Examples}, and {@code @Since}.
 */
@Name("Add Gradient Color Stop")
@Description({
        "Adds a color stop at a specific position to a gradient modifier.",
        "Position is a number between 0 and 1.",
        "This effect is primarily used inside a section body when building a gradient with custom stop positions.",
})
@Examples({
        "add color stop at 0 colored red to {_gradient}",
        "add stop at 0.5 colored yellow to {_gradient}",
        "add color stop at 1 colored blue to {_gradient}",
})
@Since("2.0.0")
public class EffAddColorStop extends Effect {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffAddColorStop.class)
                .supplier(EffAddColorStop::new)
                .addPatterns("add [color] stop at %number% color[ed] %color% to %pointmodifier%")
                .build());
    }

    private Expression<Number> position;
    private Expression<Color> color;
    private Expression<PointModifier> modifier;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.position = (Expression<Number>) exprs[0];
        this.color = (Expression<Color>) exprs[1];
        this.modifier = (Expression<PointModifier>) exprs[2];
        return true;
    }

    /**
     * Adds the color stop to the modifier only if it is an {@link AbstractGradientModifier};
     * silently does nothing otherwise.
     */
    @Override
    protected void execute(Event event) {
        Number pos = position.getSingle(event);
        Color col = color.getSingle(event);
        PointModifier mod = modifier.getSingle(event);

        if (pos == null || col == null || mod == null) return;

        if (mod instanceof AbstractGradientModifier gradientModifier) {
            gradientModifier.addStop(pos.doubleValue(), col);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add color stop at " + position.toString(event, debug)
                + " colored " + color.toString(event, debug)
                + " to " + modifier.toString(event, debug);
    }
}
