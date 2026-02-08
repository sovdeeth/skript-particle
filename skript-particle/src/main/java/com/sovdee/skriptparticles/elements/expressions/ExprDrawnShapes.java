package com.sovdee.skriptparticles.elements.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import ch.njol.skript.lang.ExpressionType;
import com.sovdee.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

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

    public ExprDrawnShapes() {
        super(Shape.class);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the drawn shape";
    }

}
