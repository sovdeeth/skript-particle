package com.sovdee.skriptparticles.skript.drawing.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.EventValueExpression;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Drawn/Current Shape")
@Description("Returns the shape that is being drawn by the draw section.")
@Examples({
        "draw the shapes {_shapes::*} at player's head with radius 1:",
            "\t# only affects the drawn version, the original shape is not changed",
            "\tset radius of drawn shape to 2"
})
@Since("1.0.0")
public class ExprDrawnShapes extends EventValueExpression<Shape> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
            SyntaxRegistry.EXPRESSION,
            infoBuilder(ExprDrawnShapes.class, Shape.class, "[drawn] shape")
                .supplier(ExprDrawnShapes::new)
                .build());
    }

    public ExprDrawnShapes() {
        super(Shape.class);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the drawn shape";
    }

}
