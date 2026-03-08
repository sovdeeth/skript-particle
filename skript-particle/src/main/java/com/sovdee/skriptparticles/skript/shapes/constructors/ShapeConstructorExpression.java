package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.config.SectionNode;
import ch.njol.skript.expressions.base.SectionExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.TriggerItem;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Base class for all the shape constructors
 */
public abstract class ShapeConstructorExpression extends SectionExpression<Shape> {

    // need to load code, then get shape and then run code

    @Override
    public boolean init(
            Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult,
            @Nullable SectionNode sectionNode, @Nullable List<TriggerItem> list
    ) {
        if (!initialize(expressions, matchedPattern, hasDelayBefore, parseResult))
            return false;
        // load code
        return false;
    }

    protected abstract boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean hasDelayBefore, ParseResult parseResult);

    @Override
    protected Shape @Nullable [] get(Event event) {
        List<Shape> shapes = getShapes(event);
        if (shapes == null)
            return null;
        // run section code to modify shapes
        return shapes.toArray(new Shape[0]);
    }

    protected abstract @Nullable List<Shape> getShapes(Event event);

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

}
