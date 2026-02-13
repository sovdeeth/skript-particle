package com.sovdee.skriptparticles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.AbstractGradientModifier;
import org.bukkit.Color;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.Nullable;

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

    static {
        Skript.registerEffect(EffAddColorStop.class,
                "add [color] stop at %number% color[ed] %color% to %pointmodifier%"
        );
    }

    private Expression<Number> position;
    private Expression<Color> color;
    private Expression<PointModifier> modifier;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.position = (Expression<Number>) exprs[0];
        this.color = (Expression<Color>) exprs[1];
        this.modifier = (Expression<PointModifier>) exprs[2];
        return true;
    }

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

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "add color stop at " + position.toString(event, debug)
                + " colored " + color.toString(event, debug)
                + " to " + modifier.toString(event, debug);
    }
}
