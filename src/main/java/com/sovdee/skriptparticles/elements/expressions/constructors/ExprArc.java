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
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Arc or Sector")
@Description({
        "Creates an arc or sector with the given radius and angle. The radius must be greater than 0 and the height, if given, must be positive.",
        "The angle must be between 0 and 360 degrees. If the angle is 360 degrees, the shape will be a circle or cylinder.",
        "An arc is a portion of the circle's circumference. A sector is a portion of the circle's area."
})
@Examples({
        "set {_shape} to an arc with radius 10 and angle 45 degrees",
        "set {_shape} to a circular sector of radius 3 and angle 90 degrees",
        "set {_shape} to a sector of radius 3 and height 5 and angle 90 degrees",
        "set {_shape} to a cylindrical sector of radius 1, height 0.5, and angle 45"
})
@Since("1.0.0")
public class ExprArc extends SimpleExpression<Circle> {

    static {
        Skript.registerExpression(ExprArc.class, Circle.class, ExpressionType.COMBINED,
                "[a[n]] [circular] (arc|:sector) (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]",
                "[a[n]] [cylindrical] (arc|:sector) (with|of) radius %number%[,| and] height %-number%[,] [and] [cutoff] angle %number% [degrees|:radians]");
    }

    private Expression<Number> radius;
    private Expression<Number> angle;
    private Expression<Number> height;
    private boolean isRadians = false;
    private boolean isSector = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (matchedPattern == 1) {
            height = (Expression<Number>) exprs[1];
            angle = (Expression<Number>) exprs[2];
        } else {
            angle = (Expression<Number>) exprs[1];
        }
        isRadians = parseResult.hasTag("radians");
        isSector = parseResult.hasTag("sector");

        if (radius instanceof Literal) {
            double r = ((Literal<Number>) radius).getSingle().doubleValue();
            if (r <= 0){
                Skript.error("The radius of the arc must be greater than 0. (radius: " + r + ")");
                return false;
            }
        }

        if (height instanceof Literal) {
            double h = ((Literal<Number>) height).getSingle().doubleValue();
            if (h < 0){
                Skript.error("The height of the arc must be greater than or equal to 0. (height: " + h + ")");
                return false;
            }
        }

        if (angle instanceof Literal) {
            double c = ((Literal<Number>) angle).getSingle().doubleValue();
            if (c <= 0 || c > 360){
                Skript.error("The cutoff angle of the arc must be between 0 and 360. (angle: " + c + ")");
                return false;
            }
        }

        return true;
    }

    @Override
    @Nullable
    protected Circle[] get(Event event) {
        if (radius.getSingle(event) == null || angle.getSingle(event) == null)
            return new Circle[0];

        double radius = this.radius.getSingle(event).doubleValue();
        double angle = this.angle.getSingle(event).doubleValue();
        double height = this.height.getOptionalSingle(event).orElse(0).doubleValue();
        if (!isRadians)
            angle = Math.toRadians(angle);

        if (radius <= 0)
            radius = 1;

        height = Math.max(height, 0);
        angle = MathUtil.clamp(angle, 0, 2 * Math.PI);

        Circle arc = new Circle(radius, height, angle);
        if (isSector)
            arc.setStyle(Shape.Style.SURFACE);

        return new Circle[]{arc};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Circle> getReturnType() {
        return Circle.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return  (isSector ? "sector" : "arc") +
                " with radius " + radius.toString(event, debug) +
                " and angle " + angle.toString(event, debug) +
                (isRadians ? " radians " : " degrees ");
    }

}