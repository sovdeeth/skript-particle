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
import ch.njol.util.Kleenean;
import com.sovdee.shapes.Shape;
import com.sovdee.shapes.Shape.Style;
import com.sovdee.shapes.Star;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Star")
@Description({
        "Creates a star shape with the given number of points, inner radius, and outer radius. The number of points must be at least 2, and the inner and outer radii must be greater than 0.",
        "Note that \"points\" in this context is referring to the tips of the star, not the number of particles."
})
@Examples({
        "set {_shape} to star with 5 points, inner radius 1, and outer radius 2",
        "draw the shape of a star with 4 points, inner radius 2, and outer radius 4 at player"
})
public class ExprStar extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprStar.class, Shape.class, ExpressionType.COMBINED, "[a] [:solid] star with %number% points(,| and) inner radius %number%[,] and outer radius %number%");
    }

    private Expression<Number> points;
    private Expression<Number> innerRadius;
    private Expression<Number> outerRadius;
    private boolean isSolid;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult) {
        points = (Expression<Number>) expressions[0];
        innerRadius = (Expression<Number>) expressions[1];
        outerRadius = (Expression<Number>) expressions[2];
        isSolid = parseResult.hasTag("solid");

        if (points instanceof Literal<Number> literal && literal.getSingle().doubleValue() < 2) {
            Skript.error("A star must have at least 2 points. (points: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (innerRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The inner radius of a star must be greater than 0.");
            return false;
        }

        if (outerRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The outer radius of a star must be greater than 0.");
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Number points = this.points.getSingle(event);
        Number innerRadius = this.innerRadius.getSingle(event);
        Number outerRadius = this.outerRadius.getSingle(event);
        if (points == null || innerRadius == null || outerRadius == null)
            return null;

        double angle = Math.PI * 2 / Math.max(points.intValue(), 2);
        innerRadius = Math.max(innerRadius.doubleValue(), MathUtil.EPSILON);
        outerRadius = Math.max(outerRadius.doubleValue(), MathUtil.EPSILON);

        Star shape = new Star(innerRadius.doubleValue(), outerRadius.doubleValue(), angle);
        if (isSolid)
            shape.setStyle(Style.SURFACE);
        shape.setDrawContext(new DrawData());
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
    public String toString(@Nullable Event event, boolean b) {
        return "a " + (isSolid ? "solid " : "") + "star shape with " + points.toString(event, b) + " points, inner radius " + innerRadius.toString(event, b) + ", and outer radius " + outerRadius.toString(event, b);
    }
}
