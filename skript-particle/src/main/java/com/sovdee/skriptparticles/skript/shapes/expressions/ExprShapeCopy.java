package com.sovdee.skriptparticles.skript.shapes.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Shape Copy")
@Description("Returns a copy of the given shape. This is useful if you want a modified version of a shape without changing the original.")
@Examples({
        "set {_shape-2} to a copy of {_shape}",
        "set {_shape} to a copy of a sphere with radius 1"
})
@Since("1.0.0")
public class ExprShapeCopy extends SimpleExpression<Shape> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprShapeCopy.class, Shape.class)
                .supplier(ExprShapeCopy::new)
                .addPatterns("[a] shape cop(y|ies) of %shapes%")
                .priority(SyntaxInfo.SIMPLE)
                .build());
    }

    private Expression<Shape> shapeExpr;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) expressions[0];
        return true;
    }

    @Override
    protected Shape[] get(@NotNull Event event) {
        Shape[] shape = shapeExpr.getArray(event);

        if (shape.length == 0)
            return new Shape[0];

        List<Shape> copy = new ArrayList<>();
        for (Shape value : shape) {
            copy.add(value.clone());
        }

        return copy.toArray(new Shape[0]);
    }

    @Override
    public boolean isSingle() {
        return shapeExpr.isSingle();
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "shape copy";
    }
}
