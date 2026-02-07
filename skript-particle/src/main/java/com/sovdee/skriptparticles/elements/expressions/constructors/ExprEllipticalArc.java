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
import com.sovdee.skriptparticles.shapes.DrawableShape;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Elliptical Arc or Sector")
@Description({
        "Creates an elliptical arc or sector with the given radii and cutoff angle. The radii must be greater than 0 and the height, if given, must be positive.",
        "The angle must be between 0 and 360 degrees. If the angle is 360 degrees, the shape will be a ellipse or elliptical cylinder.",
        "An arc is a portion of the ellipse's circumference. A sector is a portion of the ellipse's area.",
        "NOTE: Very eccentric elliptical sectors (those with a large difference between the x and z radii) may have many more particles than expected. Be careful."
})
@Examples({
        "set {_shape} to an elliptical arc with radii 10 and 3 and cutoff angle of 90 degrees",
        "set {_shape} to a elliptical sector of radius 3 and 5 and cutoff angle of 45 degrees",
        "set {_shape} to a elliptical cylinder with radii 3 and 5, height 10, and cutoff angle of 3.1415 radians"
})
@Since("1.0.0")
public class ExprEllipticalArc extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprEllipticalArc.class, Shape.class, ExpressionType.COMBINED,
                "[an] elliptical (arc|:sector) (with|of) radi(i|us) %number%(,| and) %number%[,] and [cutoff] angle [of] %number% [degrees|:radians]",
                "[an] elliptical [cylindrical] (arc|:sector) (with|of) radi(i|us) %number%(,| and) %number%(,| and) height %-number%[,] and [cutoff] angle [of] %number% [degrees|:radians]");
    }

    private Expression<Number> xRadius;
    private Expression<Number> zRadius;
    private Expression<Number> height;
    private Expression<Number> angle;
    private Style style;
    private boolean isRadians;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
//        Skript.error("Elliptical arcs are currently disabled. If you know how to efficiently compute the inverse of the " +
//                "elliptic integral of the second kind, please send me a message on Discord or Github.");
//        return false;
        xRadius = (Expression<Number>) exprs[0];
        zRadius = (Expression<Number>) exprs[1];
        if (matchedPattern == 1) {
            height = (Expression<Number>) exprs[2];
            angle = (Expression<Number>) exprs[3];

            if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() < 0) {
                Skript.error("The height of the arc cannot be negative. (height: " + literal.getSingle().doubleValue() + ")");
                return false;
            }
        } else {
            angle = (Expression<Number>) exprs[2];
        }

        if (xRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (zRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (angle instanceof Literal<Number> literal) {
            double c = literal.getSingle().doubleValue();
            if (c <= 0 || c > 360) {
                Skript.error("The cutoff angle of the arc must be between 0 and 360. (angle: " + c + ")");
                return false;
            }
        }

        style = parseResult.hasTag("sector") ? Style.FILL : Style.OUTLINE;
        isRadians = parseResult.hasTag("radians");
        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Number xRadius = this.xRadius.getSingle(event);
        Number zRadius = this.zRadius.getSingle(event);
        Number height = this.height == null ? 0 : this.height.getSingle(event);
        Number angle = this.angle.getSingle(event);
        if (xRadius == null || zRadius == null || angle == null || height == null)
            return null;

        xRadius = Math.max(xRadius.doubleValue(), MathUtil.EPSILON);
        zRadius = Math.max(zRadius.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), 0);
        if (!isRadians)
            angle = Math.toRadians(angle.doubleValue());

        angle = MathUtil.clamp(angle.doubleValue(), 0, Math.PI * 2);
        DrawableShape shape = new DrawableShape(new com.sovdee.shapes.EllipticalArc(xRadius.doubleValue(), zRadius.doubleValue(), height.doubleValue(), angle.doubleValue()));
        shape.setStyle(style);
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
        return (this.style == Style.FILL ? "sector" : "arc") +
                " with radius " + xRadius.toString(event, debug) + " and " + zRadius.toString(event, debug) +
                (height == null ? "" : " and height " + height.toString(event, debug)) +
                " and angle " + angle.toString(event, debug) +
                (isRadians ? " radians " : " degrees ");
    }
}
