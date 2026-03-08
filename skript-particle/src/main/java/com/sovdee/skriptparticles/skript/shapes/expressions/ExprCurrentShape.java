package com.sovdee.skriptparticles.skript.shapes.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.skript.drawing.expressions.ExprDrawnShapes;
import com.sovdee.skriptparticles.skript.drawing.sections.DrawShapeEffectSection;
import com.sovdee.skriptparticles.skript.shapes.constructors.ShapeConstructorExpression;
import com.sovdee.skriptparticles.util.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

public class ExprCurrentShape extends SimpleExpression<Shape> {

    public static void register(SyntaxRegistry registry) {
        registry.register(
            SyntaxRegistry.EXPRESSION,
            SyntaxInfo.Expression.builder(ExprCurrentShape.class, Shape.class)
                .addPattern("[the] [current] shape")
                .supplier(ExprCurrentShape::new)
                .build());
    }

    private ShapeConstructorExpression section;
    private ExprDrawnShapes eventExpr;

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        section = SkriptUtil.getCurrentSectionExpression(ShapeConstructorExpression.class, getParser());
        if (section == null) {
            if (getParser().isCurrentEvent(DrawShapeEffectSection.DrawEvent.class)) {
                eventExpr = new ExprDrawnShapes();
            } else {
                Skript.error("The 'current shape' expression can only be used inside of a shape construction section. Use 'drawn shape' for draw shape sections.");
                return false;
            }
        }
        return true;
    }

    @Override
    protected Shape @Nullable [] get(Event event) {
        Shape shape;
        if (section != null) {
            shape = section.getCurrentShape();
        } else {
            shape = eventExpr.getSingle(event);
        }
        return new Shape[]{shape};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the current shape";
    }

}
