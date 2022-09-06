package com.sovdee.skriptparticle.elements.shapes;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.ComplexShape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprComplexShape extends SimpleExpression<ComplexShape> {

    static {
        Skript.registerExpression(ExprComplexShape.class, ComplexShape.class, ExpressionType.COMBINED, "[the] complex shape [with name|named|with id] %string%");
    }

    private Expression<String> nameExpr;

    @Override
    protected @Nullable ComplexShape[] get(Event event) {
        return new ComplexShape[]{StructComplexShape.getCustomShapes().getOrDefault(nameExpr.getSingle(event), null)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends ComplexShape> getReturnType() {
        return ComplexShape.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "Complex shape with name " + nameExpr.toString(event, debug);
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        nameExpr = (Expression<String>) exprs[0];
        return true;
    }
}
