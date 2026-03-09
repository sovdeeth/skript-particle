package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.util.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for all the shape constructors
 */
public abstract class ShapeConstructorExpression extends SectionExpression<Shape> {

    private boolean hasSection = false;
    private final ThreadLocal<Shape> threadShape = new ThreadLocal<>();

    @Override
    public boolean init(
            Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult,
            @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list
    ) {
        if (!initialize(expressions, matchedPattern, hasDelayBefore, parseResult))
            return false;

        if (SkriptUtil.getCurrentSectionExpression(ShapeConstructorExpression.class, getParser()) != null) {
            Skript.error("Shapes cannot be constructed within other shape's constructor sections!");
            return false;
        }

        if (sectionNode != null) {
            getParser().setHasDelayBefore(Kleenean.FALSE);
            loadCode(sectionNode);
            if (!getParser().getHasDelayBefore().isFalse()) {
                Skript.error("Delays can't be used within a shape creation section");
                return false;
            }
            getParser().setHasDelayBefore(hasDelayBefore);
            hasSection = true;
        }
        return false;
    }

    protected abstract boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult);

    @Override
    protected Shape @Nullable [] get(Event event) {
        List<Shape> shapes = getShapes(event);
        if (shapes == null)
            return null;
        // run section code to modify shapes
        if (hasSection) {
            for (Shape shape : shapes) {
                threadShape.set(shape);
                runSection(event);
            }
            threadShape.remove();
        }
        return shapes.toArray(new Shape[0]);
    }

    /**
     * Returns the initially constructed shapes for modification by the section, if it exists.
     * @param event The event context.
     * @return The created shape.
     */
    protected abstract @Nullable List<Shape> getShapes(Event event);

    /**
     * @return The shape currently being constructed by this syntax on this thread.
     */
    public Shape getCurrentShape() {
        return threadShape.get();
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

}
