package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Circle;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

@Name("Particle Circle")
@Description({
        "Creates a circle or disc shape with the given radius. The radius must be greater than 0.",
})
@Examples({
        "set {_shape} to circle with radius 10",
        "set {_shape} to a disc of radius 3"
})
@Since("1.0.0")
public class ExprCircle extends SimpleExpression<Circle>{

    static {
        Skript.registerExpression(ExprCircle.class, Circle.class, ExpressionType.COMBINED, "[a] (circle|:disc) [with|of] radius %number%");
    }

    private Expression<Number> radius;
    private boolean isSolid;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal) {
            if (((Literal<Number>) radius).getSingle().doubleValue() <= 0){
                Skript.error("The radius of the circle must be greater than 0. (radius: " + ((Literal<Number>) radius).getSingle().doubleValue() + ")");
                return false;
            }
        }
        isSolid = parseResult.hasTag("disc");
        return true;
    }

    @Override
    protected Circle @NotNull [] get(@NotNull Event event) {
        Number radius = this.radius.getSingle(event);
        if (radius == null || radius.doubleValue() <= 0) {
            Skript.error("The radius of the circle must be greater than 0; defaulting to 1. (radius: " + radius + ")");
            radius = 1;
        }
        Circle circle = new Circle(radius.doubleValue());
        if (isSolid) circle.setStyle(Shape.Style.SURFACE);
        return new Circle[]{circle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @NotNull
    public Class<? extends Circle> getReturnType() {
        return Circle.class;
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return "circle of radius " + radius.toString(event, debug);
    }
}
