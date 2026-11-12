package com.sovdee.skriptparticles.skript.shaders.expressions.point;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import com.sovdee.skriptparticles.rendering.shaders.CustomSkriptModifier;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Array;

/**
 * Abstract base for expressions that read or write fields of the current
 * {@link ParticleRenderContext} inside a {@link CustomSkriptModifier}.
 *
 * <p>Subclasses only need to implement {@link #getValue}, {@link #getReturnType},
 * and {@link #toString}. Settable subclasses additionally override
 * {@link #acceptChange} and {@link #change}, using {@link #getContext} to
 * retrieve the context from the event.</p>
 */
public abstract class PointPropertyExpression<T> extends SimpleExpression<T> {

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed,
                        SkriptParser.ParseResult parseResult) {
        if (!getParser().isCurrentEvent(CustomSkriptModifier.PointModifierEvent.class)) {
            Skript.error("Point properties can only be used inside a custom point modifier");
            return false;
        }
        return true;
    }

    /**
     * Returns the current {@link ParticleRenderContext} from the event,
     * or {@code null} if the event is not a {@link CustomSkriptModifier.PointModifierEvent}.
     */
    protected @Nullable ParticleRenderContext getContext(Event event) {
        if (!(event instanceof CustomSkriptModifier.PointModifierEvent pme)) return null;
        return pme.getPoint();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected final T[] get(Event event) {
        ParticleRenderContext ctx = getContext(event);
        if (ctx == null) return null;
        T value = getValue(ctx);
        if (value == null) return null;
        T[] arr = (T[]) Array.newInstance(getReturnType(), 1);
        arr[0] = value;
        return arr;
    }

    /**
     * Returns the value to expose for the given context.
     *
     * @param ctx the current point context
     * @return the value, or {@code null} to yield no result
     */
    protected abstract @Nullable T getValue(ParticleRenderContext ctx);

    @Override
    public boolean isSingle() {
        return true;
    }
}
