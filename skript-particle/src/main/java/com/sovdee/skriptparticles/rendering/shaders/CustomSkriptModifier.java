package com.sovdee.skriptparticles.rendering.shaders;

import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.variables.Variables;
import com.sovdee.shapes.modifiers.PointContext;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.modifiers.ShapeBounds;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A render modifier whose behaviour is defined by a user-written Skript section.
 * The section body runs once per point, on the render thread (async by default).
 * <p>
 * Local variables from the creating event are snapshotted when the modifier is built and
 * installed onto the point event once per render pass ({@link #prepare}). The body sees and may
 * mutate those locals, and mutations persist from point to point within a pass; each new pass
 * starts fresh from the snapshot. Writes never propagate back to the creating event's locals.
 */
public class CustomSkriptModifier implements PointModifier<ParticleRenderContext> {

    private final Trigger trigger;
    /**
     * Owns the creation-time locals snapshot (locals are keyed by event instance in Skript).
     * Read-only after construction and shared by clones.
     */
    private final @Nullable PointModifierEvent localsHolder;
    private final ThreadLocal<PointModifierEvent> threadEvent = ThreadLocal.withInitial(PointModifierEvent::new);
    /**
     * Incremented each pass; batches of one pass may hop scheduler threads, so each thread's
     * event installs the locals snapshot lazily on its first point of the pass.
     */
    private volatile int passId = 0;

    public CustomSkriptModifier(Trigger trigger, @Nullable Object localsSnapshot) {
        this.trigger = trigger;
        if (localsSnapshot != null) {
            this.localsHolder = new PointModifierEvent();
            Variables.setLocalVariables(localsHolder, localsSnapshot);
        } else {
            this.localsHolder = null;
        }
    }

    private CustomSkriptModifier(Trigger trigger, @Nullable PointModifierEvent localsHolder) {
        this.trigger = trigger;
        this.localsHolder = localsHolder;
    }

    @Override
    public void prepare(ShapeBounds bounds) {
        passId++;
    }

    @Override
    public void modify(ParticleRenderContext point) {
        var event = threadEvent.get();
        if (event.passId != passId) {
            // first point of this pass on this thread: replace leftovers with a fresh snapshot copy
            event.passId = passId;
            Variables.removeLocals(event);
            if (localsHolder != null) {
                Object copy = Variables.copyLocalVariables(localsHolder);
                if (copy != null)
                    Variables.setLocalVariables(event, copy);
            }
        }
        event.setPoint(point); // reuse event to avoid extra allocation
        // walk instead of Trigger#execute: execute would remove the pass-shared locals after every point
        TriggerItem.walk(trigger, event);
    }

    @Override
    public Class<? extends PointContext> contextType() {
        return ParticleRenderContext.class;
    }

    @Override
    public int modifierHash() {
        return System.identityHashCode(trigger);
    }

    @Override
    public CustomSkriptModifier clone() {
        return new CustomSkriptModifier(trigger, localsHolder);
    }

    @Override
    public String toString() {
        return "custom point modifier (" + trigger.getName() + ")";
    }

    public static class PointModifierEvent extends Event {

        private ParticleRenderContext point;
        private int passId = -1;

        public PointModifierEvent() {
            this.point = null;
        }

        public ParticleRenderContext getPoint() {
            return point;
        }

        public void setPoint(ParticleRenderContext point) {
            this.point = point;
        }

        @Override
        public @NotNull HandlerList getHandlers() {
            throw new IllegalStateException("Should not be listened to!");
        }
    }
}
