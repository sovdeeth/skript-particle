package com.sovdee.skriptparticles.rendering.shaders;

import ch.njol.skript.lang.Trigger;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.rendering.ParticleRenderContext;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CustomSkriptModifier implements PointModifier<ParticleRenderContext> {

    private Trigger trigger;
    private final ThreadLocal<PointModifierEvent> threadEvent = ThreadLocal.withInitial(PointModifierEvent::new);

    @Override
    public void modify(ParticleRenderContext point) {
        // run trigger with point
        var event = threadEvent.get();
        event.setPoint(point); // reuse event to avoid extra allocation
        trigger.execute(event);
    }

    @Override
    public int modifierHash() {
        return System.identityHashCode(trigger);
    }

    @Override
    public CustomSkriptModifier clone() {
        CustomSkriptModifier copy = new CustomSkriptModifier();
        copy.trigger = trigger; // trigger is a stateless executor, safe to share
        return copy;           // threadEvent is fresh via ThreadLocal.withInitial
    }

    public static class PointModifierEvent extends Event {

        private ParticleRenderContext point;

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
