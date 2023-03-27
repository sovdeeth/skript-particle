package com.sovdee.skriptparticles.elements.sections;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.EffectSection;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.Trigger;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Direction;
import ch.njol.skript.util.Getter;
import ch.njol.skript.variables.Variables;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Name("Draw Shape")
@Description({
        "Draws the given shapes at the given locations. The shapes will be drawn in the order they are given.",
        "The code inside the draw shape section will be executed before drawing begins, and will not affect the original shapes.",
        "This means you can set particle data, or change the shape's location, rotation, or scale, without affecting the shape the next time it's drawn.",
})
@Examples({
        "draw a sphere with radius 1 at player's location",
        "draw (a sphere with radius 1 and a cube with radius 1) at player's location",
        "",
        "draw {_shape} at player's location:",
        "\tset {_shape}'s particle to dust using dustOption(red, 1)"
})
@Since("1.0.0")
public class EffSecDrawShape extends EffectSection {

    public static class DrawEvent extends Event {
        private final Shape shape;
        public DrawEvent(Shape shape) {
            this.shape = shape;
        }

        public Shape getShape() {
            return shape;
        }

        @Override
        @NotNull
        public HandlerList getHandlers() {
            throw new IllegalStateException();
        }
    }


    static {
        Skript.registerSection(EffSecDrawShape.class,
                "draw [shape[s]] %shapes% [%directions% %locations%] [for %-players%]"
        );
        EventValues.registerEventValue(EffSecDrawShape.DrawEvent.class, Shape.class, new Getter<>() {
            @Override
            public Shape get(EffSecDrawShape.DrawEvent event) {
                return event.getShape();
            }
        }, EventValues.TIME_NOW);
    }

    private Expression<Location> locations;
    private Expression<Shape> shapes;
    private Expression<Player> players;

    @Nullable
    private Trigger trigger;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult, @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list) {
        shapes = (Expression<Shape>) expressions[0];
        locations = Direction.combine((Expression<? extends Direction>) expressions[1], (Expression<? extends Location>) expressions[2]);
        players = (Expression<Player>) expressions[3];

        if (sectionNode != null) {
            AtomicBoolean delayed = new AtomicBoolean(false);
            Runnable afterLoading = () -> delayed.set(!getParser().getHasDelayBefore().isFalse());
            trigger = loadCode(sectionNode, "draw", afterLoading, EffSecDrawShape.DrawEvent.class);
            if (delayed.get()) {
                Skript.error("Delays can't be used within a Draw Shape Effect Section");
                return false;
            }
        }

        return true;
    }

    @Override
    @Nullable
    protected TriggerItem walk(Event event) {
        Object localVars = Variables.copyLocalVariables(event);

        Collection<Player> recipients = new ArrayList<>();
        if (players != null) {
            recipients.addAll(List.of(players.getArray(event)));
        } else {
            recipients.addAll(Bukkit.getOnlinePlayers());
        }

        Consumer<Shape> consumer;
        if (trigger != null) {
            consumer = shape -> {
                DrawEvent drawEvent = new EffSecDrawShape.DrawEvent(shape);
                Variables.setLocalVariables(drawEvent, localVars);
                TriggerItem.walk(trigger, drawEvent);
                Variables.setLocalVariables(event, Variables.copyLocalVariables(drawEvent));
                Variables.removeLocals(drawEvent);
            };
        } else {
            consumer = null;
        }

        Shape shapeCopy;
        for (Location location : locations.getArray(event)) {
            for (Shape shape : shapes.getArray(event)) {
                if (consumer != null) {
                    // copy the shape so that it can be modified by the consumer without affecting the original
                    shapeCopy = shape.clone();
                    shapeCopy.draw(location, consumer, recipients);
                } else {
                    shape.draw(location, null, null, recipients);
                }
            }
        }

        return super.walk(event, false);
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean b) {
        return "draw shape " + shapes.toString(event, b) + " at " + locations.toString(event, b) + " for " + (players == null ? "all players" : players.toString(event, b));
    }
}
