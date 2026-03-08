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
import com.sovdee.shapes.shapes.Ellipse;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Ellipse or Elliptical Cylinder")
@Description("""
    Creates an ellipse, elliptical disc, or elliptical cylinder shape with the given radii. The radii must be greater than 0.
    The first radius is the x radius, and the second radius is the z radius. These are relative to the shape's rotation,
    so they only correspond exactly to the x and z axes if the shape is not rotated.
    NOTE: Very eccentric elliptical discs/sectors (those with a large difference between the x and z radii) may have many more particles than expected. Be careful.
    """)
@Example("set {_shape} to oval with radii 10 and 3")
@Example("set {_shape} to a solid ellipse of radius 3 and 5")
@Example("set {_shape} to a hollow elliptical cylinder with radii 3 and 6 and height 5")
@Since("1.0.0")
public class ExprEllipse extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprEllipse.class, Shape.class)
                .supplier(ExprEllipse::new)
                .addPatterns(
                        "[a[n]] [surface:(solid|filled)] (ellipse|oval) (with|of) radi(i|us) %number% and %number%",
                        "[a[n]] [hollow|2:solid] elliptical (cylinder|1:tube) (with|of) radi(i|us) %number%(,| and) %number%[,] and height %number%"
                )
                .build());
    }

    private Expression<Number> xRadius;
    private Expression<Number> zRadius;
    private Expression<Number> height;
    private SamplingStyle style;

    @Override
    @SuppressWarnings("unchecked")
    public boolean initialize(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        xRadius = (Expression<Number>) exprs[0];
        zRadius = (Expression<Number>) exprs[1];
        style = SamplingStyle.OUTLINE;
        if (parseResult.hasTag("surface")) {
            style = SamplingStyle.SURFACE;
        }
        if (matchedPattern == 1) {
            height = (Expression<Number>) exprs[2];
            style = switch (parseResult.mark) {
                case 0 -> SamplingStyle.SURFACE;
                case 1 -> SamplingStyle.OUTLINE;
                default -> SamplingStyle.FILL;
            };
        }

        if (xRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (length radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (zRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (width radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() < 0) {
            Skript.error("The height of the elliptical cylinder must be greater than or equal to 0. (height: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        return true;
    }

    @Override
    protected @Nullable List<Shape> getShapes(Event event) {
        Number xRadius = this.xRadius.getSingle(event);
        Number zRadius = this.zRadius.getSingle(event);
        Number height = this.height == null ? 0 : this.height.getSingle(event);
        if (xRadius == null || zRadius == null || height == null)
            return null;

        xRadius = Math.max(xRadius.doubleValue(), MathUtil.EPSILON);
        zRadius = Math.max(zRadius.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), 0);

        Ellipse shape = new Ellipse(xRadius.doubleValue(), zRadius.doubleValue(), height.doubleValue());
        shape.getPointSampler().setStyle(style);
        shape.getPointSampler().setDrawContext(new DrawData());
        return List.of(shape);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (height == null ? "ellipse of x radius " + xRadius.toString(event, debug) + " and z radius " + zRadius.toString(event, debug) :
                "elliptical cylinder of x radius " + xRadius.toString(event, debug) + " and z radius " + zRadius.toString(event, debug) +
                        " and height " + (height != null ? height.toString(event, debug) : "0"));
    }
}
