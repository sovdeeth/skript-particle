package com.sovdee.skriptparticles.skript.shapes.effects;

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
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Skript effect that shows or hides the local and/or global debug axes of one or more shapes.
 * Intended for debugging purposes; toggles the corresponding flag on each shape's {@link DrawData DrawData}.
 * Documented from the Skript side via {@code @Name}, {@code @Description}, {@code @Examples}, and {@code @Since}.
 */
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

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EFFECT, SyntaxInfo.builder(EffToggleAxes.class)
                .supplier(EffToggleAxes::new)
                .addPatterns("(:show|:hide) [:local] [and] [:global] axes of [shape[s]] %shapes%")
                .build());
    }

    private Expression<Shape> shape;
    private boolean localFlag = false;
    private boolean globalFlag = false;
    private boolean showFlag = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        shape = (Expression<Shape>) expressions[0];
        localFlag = parseResult.hasTag("local");
        globalFlag = parseResult.hasTag("global");
        showFlag = parseResult.hasTag("show");
        return true;
    }

    /**
     * Applies the show/hide flags to the local and/or global axes of each shape's DrawData.
     */
    @Override
    protected void execute(Event event) {
        Shape[] shapes = shape.getArray(event);
        for (Shape shape : shapes) {
            DrawData dd = DrawData.of(shape);
            if (globalFlag)
                dd.showGlobalAxes(showFlag);
            if (localFlag)
                dd.showLocalAxes(showFlag);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (showFlag ? "show" : "hide") + (globalFlag ? "global" : "") + (globalFlag && localFlag ? " and " : "") +
                (localFlag ? "local" : "") + " axes of shapes " + shape.toString(event, debug);
    }
}
