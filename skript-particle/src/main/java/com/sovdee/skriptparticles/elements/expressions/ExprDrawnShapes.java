package com.sovdee.skriptparticles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

/**
 * Skript expression that returns the shape currently being drawn inside a draw-shape section.
 * Backed by {@link ch.njol.skript.expressions.base.EventValueExpression} for the {@link com.sovdee.skriptparticles.elements.sections.DrawShapeEffectSection.DrawEvent DrawEvent}.
 * Modifications to the returned shape affect the draw result for that iteration without altering the original shape.
 * Documented from the Skript side via {@code @Name}, {@code @Description}, {@code @Examples}, and {@code @Since}.
 */
@Name("Drawn Shape")
@Description("Returns the shape that is being drawn by the draw section.")
@Examples({
        "draw the shapes {_shapes::*} at player's head with radius 1:",
            "\t# only affects the drawn version, the original shape is not changed",
            "\tset radius of drawn shape to 2"
})
@Since("1.0.0")
public class ExprDrawnShapes extends EventValueExpression<Shape> {

    static {
        Skript.registerExpression(ExprDrawnShapes.class, Shape.class, ExpressionType.SIMPLE, "[the] drawn shape");
    }

    /**
     * Initializes the superclass with the {@link com.sovdee.shapes.shapes.Shape} event-value type.
     */
    public ExprDrawnShapes() {
        super(Shape.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the drawn shape";
    }

}
