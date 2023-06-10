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
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.SphericalCap;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Spherical Cap")
@Description({
        "Creates a spherical cap or spherical sector shape with the given radius and cutoff angle. The radius must be greater than 0.",
        "The angle must be between 0 and 180 degrees. If the angle is 180 degrees, the shape will be a sphere.",
        "A spherical cap is a portion of the surface of a sphere. A spherical sector, or spherical cone, is essentially a cone with a rounded base."
})
@Examples({
        "set {_shape} to spherical cap with radius 10 and angle 45 degrees",
        "set {_shape} to a spherical sector of radius 3 and angle 90 degrees"
})
@Since("1.0.0")
public class ExprSphericalCap extends SimpleExpression<SphericalCap> {

    static {
        Skript.registerExpression(ExprSphericalCap.class, SphericalCap.class, ExpressionType.COMBINED,
                "[a] spherical (cap|:sector) (with|of) radius %number% and [cutoff] angle [of] %number% [degrees|:radians]");
    }

    private Expression<Number> radius;
    private Expression<Number> angle;
    private boolean isRadians = false;
    private boolean isSector = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        angle = (Expression<Number>) exprs[1];
        isRadians = parseResult.hasTag("radians");
        isSector = parseResult.hasTag("sector");

        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the spherical cap must be greater than 0. (radius: " + literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (angle instanceof Literal<Number> literal) {
            double c = literal.getSingle().doubleValue();
            if (c <= 0 || c > 180) {
                Skript.error("The cutoff angle of the spherical cap must be between 0 and 180. (angle: " + c + ")");
                return false;
            }
        }

        return true;
    }

    @Override
    @Nullable
    protected SphericalCap[] get(Event event) {
        Number radius = this.radius.getSingle(event);
        Number angle = this.angle.getSingle(event);
        if (radius == null || angle == null)
            return null;

        radius = Math.max(radius.doubleValue(), MathUtil.EPSILON);
        if (!isRadians)
            angle = Math.toRadians(angle.doubleValue());

        SphericalCap cap = new SphericalCap(radius.doubleValue(), angle.doubleValue());
        if (isSector)
            cap.setStyle(Shape.Style.FILL);

        return new SphericalCap[]{cap};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends SphericalCap> getReturnType() {
        return SphericalCap.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "spherical " + (isSector ? "sector" : "cap") + " with radius " + radius.toString(event, debug) + " and angle " + angle.toString(event, debug) + (isRadians ? " radians" : " degrees");
    }

}
