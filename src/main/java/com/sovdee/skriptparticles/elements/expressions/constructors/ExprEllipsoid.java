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
import com.sovdee.skriptparticles.shapes.Ellipsoid;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Ellipsoid")
@Description({
        "Creates a ellipsoid shape with the given radii. The radii must be greater than 0.",
        "The first radius is the x radius, and the second is the y radius, and the last is the z radius. " +
        "These are relative to the shape's rotation, so they only correspond exactly to the world axes if the shape is not rotated.",
        "Note that this shape is modified using the Length/Width/Height modifiers, not the Radius modifier. This means the length/width/height " +
        "of the shape will be twice the radius in each direction. Length is the x radius, width is the z radius, and height is the y radius.",
        "NOTE: Very eccentric solid ellipsoids (those with a large difference between the radii) may have many more particles than expected. Be careful."
})
@Examples({
        "set {_shape} to ellipsoid with radii 10, 3, and 8",
        "set {_shape} to a solid ellipsoid of radius 3 and 5 and 6",
        "set {_shape} to a hollow ellipsoid with radii 3, 6 and 5"
})
@Since("1.0.0")
public class ExprEllipsoid extends SimpleExpression<Ellipsoid> {

    static {
        Skript.registerExpression(ExprEllipsoid.class, Ellipsoid.class, ExpressionType.COMBINED,
                "[a[n]] [(outlined|wireframe|:hollow|fill:(solid|filled))] ellipsoid (with|of) radi(i|us) %number%(,| and) %number%[,] and %number%");
    }

    private Expression<Number> xRadius;
    private Expression<Number> yRadius;
    private Expression<Number> zRadius;
    private Style style;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        xRadius = (Expression<Number>) exprs[0];
        yRadius = (Expression<Number>) exprs[1];
        zRadius = (Expression<Number>) exprs[2];

        if (xRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The x radius of the ellipsoid must be greater than 0. (x radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (yRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The y radius of the ellipsoid must be greater than  0. (y radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (zRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The z radius of the ellipsoid must be greater than 0. (z radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        style = (parseResult.hasTag("hollow") ? Style.SURFACE : (parseResult.hasTag("fill") ? Style.FILL : Style.OUTLINE));
        return true;
    }

    @Override
    @Nullable
    protected Ellipsoid[] get(Event event) {
        Number xRadius = this.xRadius.getSingle(event);
        Number yRadius = this.yRadius.getSingle(event);
        Number zRadius = this.zRadius.getSingle(event);
        if (xRadius == null || yRadius == null || zRadius == null)
            return null;

        xRadius = Math.max(xRadius.doubleValue(), MathUtil.EPSILON);
        yRadius = Math.max(yRadius.doubleValue(), MathUtil.EPSILON);
        zRadius = Math.max(zRadius.doubleValue(), MathUtil.EPSILON);

        Ellipsoid ellipsoid = new Ellipsoid(xRadius.doubleValue(), yRadius.doubleValue(), zRadius.doubleValue());
        ellipsoid.setStyle(style);
        return new Ellipsoid[] {ellipsoid};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Ellipsoid> getReturnType() {
        return Ellipsoid.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "ellipsoid with radii " + xRadius.toString(event, debug) + ", " + yRadius.toString(event, debug) + ", and " + zRadius.toString(event, debug);
    }

}
