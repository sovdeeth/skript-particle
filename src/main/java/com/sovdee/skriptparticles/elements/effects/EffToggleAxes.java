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
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

@Name("Toggle Axes")
@Description({
        "Toggles the visibility of the local and/or global axes of a shape.",
        "When on, the shape will also draw its local and/or global axes when drawn.",
        "This is intended for debugging purposes."
})
@Examples({
        "show local axes of {_shape}",
        "hide global axes of {_shape}",
        "hide local and global axes of {_shape}",
        "show local axes of {_shape} and {_shape2}"
})
@Since("1.0.0")
public class EffToggleAxes extends Effect {

    static {
        Skript.registerEffect(EffToggleAxes.class, "(:show|:hide) [:local] [and] [:global] axes of [shape[s]] %shapes%");
    }

    private Expression<Shape> shape;
    private boolean localFlag = false;
    private boolean globalFlag = false;
    private boolean showFlag = false;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        shape = (Expression<Shape>) expressions[0];
        localFlag = parseResult.hasTag("local");
        globalFlag = parseResult.hasTag("global");
        showFlag = parseResult.hasTag("show");
        return true;
    }

    @Override
    protected void execute(Event event) {
        Shape[] shapes = shape.getArray(event);
        for (Shape shape : shapes) {
            if (globalFlag)
                shape.showGlobalAxes(showFlag);
            if (localFlag)
                shape.showLocalAxes(showFlag);
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (showFlag ? "show" : "hide") + (globalFlag ? "global" : "") + (globalFlag && localFlag ? " and " : "") +
                (localFlag ? "local" : "") + " axes of shapes " + shape.toString(event, debug);
    }
}
