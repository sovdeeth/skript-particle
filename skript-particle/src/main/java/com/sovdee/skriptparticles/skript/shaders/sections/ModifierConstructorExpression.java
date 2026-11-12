package com.sovdee.skriptparticles.skript.shaders.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for the modifier constructors. Each can take an optional section that runs once, right after the
 * modifier is created, in which {@code the new modifier} refers to it (and modifier properties default to it),
 * the same way Skript's world border section works.
 */
public abstract class ModifierConstructorExpression extends SectionExpression<PointModifier> {

    // static so the default expression for point modifiers can stay stateless; saved and restored around each
    // section, so nested constructors see their own modifier
    private static final ThreadLocal<@Nullable PointModifier<?>> CURRENT = new ThreadLocal<>();

    private boolean hasSection = false;

    @Override
    public boolean init(
            Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult,
            @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> triggerItems
    ) {
        if (!initialize(expressions, matchedPattern, hasDelayBefore, parseResult))
            return false;
        if (sectionNode != null) {
            getParser().setHasDelayBefore(Kleenean.FALSE);
            loadCode(sectionNode);
            if (!getParser().getHasDelayBefore().isFalse()) {
                Skript.error("Delays can't be used within a modifier creation section");
                return false;
            }
            getParser().setHasDelayBefore(hasDelayBefore);
            hasSection = true;
        }
        return true;
    }

    protected abstract boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult);

    /**
     * @return the newly created modifier, or {@code null} if one of the inputs was not set
     */
    protected abstract @Nullable PointModifier<?> create(Event event);

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        PointModifier<?> modifier = create(event);
        if (modifier == null)
            return new PointModifier[0];
        if (hasSection) {
            PointModifier<?> outer = CURRENT.get();
            CURRENT.set(modifier);
            try {
                runSection(event);
            } finally {
                CURRENT.set(outer);
            }
        }
        return new PointModifier[]{modifier};
    }

    /**
     * @return the modifier whose section is running on this thread, if any
     */
    public static @Nullable PointModifier<?> current() {
        return CURRENT.get();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }
}
