package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.shapes.Star;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Star")
@Description("""
    Creates a star shape with the given number of points, inner radius, and outer radius.
    The number of points must be at least 2, and the inner and outer radii must be greater than 0.
    Note that "points" in this context is referring to the tips of the star, not the number of particles.
    """)
@Example("set {_shape} to star with 5 points, inner radius 1, and outer radius 2")
@Example("draw the shape of a star with 4 points, inner radius 2, and outer radius 4 at player")
@Since("1.0.1")
public class ExprStar extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprStar.class, Shape.class)
                .supplier(ExprStar::new)
                .addPatterns("[a] [:solid] star with %number% points(,| and) inner radius %number%[,] and outer radius %number%")
                .build());
    }

    private Expression<Number> points;
    private Expression<Number> innerRadius;
    private Expression<Number> outerRadius;
    private boolean isSolid;

    @Override
    @SuppressWarnings("unchecked")
    public boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
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
    protected @Nullable List<Shape> getShapes(Event event) {
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
            shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        shape.getPointSampler().setDrawContext(new DrawData());
        return List.of(shape);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a " + (isSolid ? "solid " : "") + "star shape with " + points.toString(event, debug) + " points, inner radius " + innerRadius.toString(event, debug) + ", and outer radius " + outerRadius.toString(event, debug);
    }
}
