package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.ComplexShape;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class ExprShapesOfComplexShape extends SimpleExpression<Shape> {

    static {
        SimplePropertyExpression.register(ExprShapesOfComplexShape.class, Shape.class, "shapes", "complexshapes");
    }

    private Expression<ComplexShape> shapeExpr;

    @Override
    protected @Nullable Shape[] get(Event e) {
        ArrayList<Shape> shapes = new ArrayList<>();
        for (ComplexShape shape : shapeExpr.getAll(e)){
            shapes.addAll(shape.getShapes());
        }
        return shapes.toArray(new Shape[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "shapes of complex shape";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<ComplexShape>) exprs[0];
        return true;
    }
}
