package com.sovdee.skriptparticles.skript.shaders.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.shaders.CustomSkriptModifier;
import com.sovdee.skriptparticles.rendering.shaders.CustomSkriptModifier.PointModifierEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Custom Point Modifier")
@Description({
        "Creates a point modifier whose behaviour is defined by the section below it.",
        "The section runs once for every point of the shape, every time the shape is rendered.",
        "Use the 'point' expressions (point's x/y/z, point's index, point's visibility, etc.) to read and change the current point.",
        "Local variables from the surrounding code are copied into the modifier when it is created; changes to them inside",
        "the section persist from point to point during a single render, but never affect the outside code.",
        "Warning: the section runs off the main thread and once per point per frame - keep it lightweight and never",
        "access worlds, entities, or other game state from it. Delays are not allowed."
})
@Examples({
        "set {_shader} to a custom point modifier:",
        "\tadd 0.3 * sin(point's index * 20) to point's y",
        "\tif mod(point's index, 4) = 0:",
        "\t\tset point's visibility to false",
        "add {_shader} to modifiers of {_shape}"
})
@Since("2.0.0")
public class ExprSecCustomModifier extends SectionExpression<PointModifier> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSecCustomModifier.class, PointModifier.class)
                .supplier(ExprSecCustomModifier::new)
                .addPatterns(
                        "[a] [new] custom [point] (modifier|shader)"
                )
                .build());
    }

    private Trigger trigger;

    @Override
    public boolean init(Expression<?>[] expressions, int pattern, Kleenean delayed, ParseResult result,
                        @Nullable SectionNode node, @Nullable List<TriggerItem> triggerItems) {
        assert node != null; // isSectionOnly guarantees a section
        AtomicBoolean delayDetected = new AtomicBoolean(false);
        Runnable afterLoading = () -> delayDetected.set(!getParser().getHasDelayBefore().isFalse());
        trigger = loadCode(node, "custom point modifier", null, afterLoading, PointModifierEvent.class);
        if (delayDetected.get()) {
            Skript.error("Delays can't be used within a custom point modifier section");
            return false;
        }
        return true;
    }

    @Override
    public boolean isSectionOnly() {
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        return new PointModifier[]{new CustomSkriptModifier(trigger, Variables.copyLocalVariables(event))};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a custom point modifier";
    }

}
