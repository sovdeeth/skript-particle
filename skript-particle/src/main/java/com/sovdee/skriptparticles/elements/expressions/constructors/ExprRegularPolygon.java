package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.RegularPolygon;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Regular Polygon")
@Description({
        "Creates a regular polygon with the given number of sides and radius. The number of sides must be at least 3. " +
                "The radius must be greater than 0."
})
@Examples({
        "set {_shape} to a regular polygon with 5 sides and radius 10",
        "set {_shape} to a solid regular polygon with 6 sides and side length 3",
        "draw the shape of a triangle with side length 5 at player"
})
public class ExprRegularPolygon extends SimpleExpression<Shape> {

    // TODO: add ExprRegularPrism

    static {
        Skript.registerExpression(ExprRegularPolygon.class, Shape.class, ExpressionType.COMBINED,
                "[a] [solid:(solid|filled)] regular polygon with %number% sides and radius %number%",
                "[a] [solid:(solid|filled)] regular polygon with %number% sides and side length %number%",
                "[a[n]] [solid:(solid|filled)] (3:[equilateral ]triangle|4:square|5:pentagon|6:hexagon|7:heptagon|8:octagon) with radius %number%",
                "[a[n]] [solid:(solid|filled)] (3:[equilateral ]triangle|4:square|5:pentagon|6:hexagon|7:heptagon|8:octagon) with side length %number%"
        );
    }

    private Expression<Number> radius;
    private Expression<Number> sideLength;
    private Expression<Number> sides;
    private SamplingStyle style;
    private int matchedPattern;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        style = parseResult.hasTag("solid") ? SamplingStyle.SURFACE : SamplingStyle.OUTLINE;
        this.matchedPattern = matchedPattern;
        switch (matchedPattern) {
            case 0 -> {
                sides = (Expression<Number>) exprs[0];
                radius = (Expression<Number>) exprs[1];
            }
            case 1 -> {
                sides = (Expression<Number>) exprs[0];
                sideLength = (Expression<Number>) exprs[1];
            }
            case 2 -> {
                sides = new SimpleLiteral<>(parseResult.mark, false);
                radius = (Expression<Number>) exprs[0];
            }
            case 3 -> {
                sides = new SimpleLiteral<>(parseResult.mark, false);
                sideLength = (Expression<Number>) exprs[0];
            }
        }

        if (sides instanceof Literal<Number> literal && literal.getSingle().intValue() < 3) {
            Skript.error("The number of sides must be at least 3. (sides: " + literal.getSingle() + ")");
            return false;
        }

        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius must be greater than 0. (radius: " + literal.getSingle() + ")");
            return false;
        }

        if (sideLength instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The side length must be greater than 0. (side length: " + literal.getSingle() + ")");
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Number sides = this.sides.getSingle(event);
        if (sides == null)
            return null;

        // get radius, if radius is not specified, calculate it from side length
        Number radius;
        if (matchedPattern % 2 == 0) {
            radius = this.radius.getSingle(event);
            if (radius == null)
                return null;
        } else {
            radius = sideLength.getSingle(event);
            if (radius == null)
                return null;
            radius = radius.doubleValue() / (2 * Math.sin(Math.PI / sides.doubleValue()));
        }

        sides = Math.max(sides.intValue(), 3);
        radius = Math.max(radius.doubleValue(), MathUtil.EPSILON);
        RegularPolygon shape = new RegularPolygon(sides.intValue(), radius.doubleValue());
        shape.getPointSampler().setStyle(style);
        shape.getPointSampler().setDrawContext(new DrawData());
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
        return (style == SamplingStyle.SURFACE ? "filled" : "outlined") +
                switch (matchedPattern) {
                    case 0, 2 ->
                            " regular polygon with " + sides.toString(event, debug) + " sides and radius " + radius.toString(event, debug);
                    case 1, 3 ->
                            " regular polygon with " + sides.toString(event, debug) + " sides and side length " + sideLength.toString(event, debug);
                    default -> "regular polygon";
                };
    }

}
