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
import com.sovdee.skriptparticles.shapes.Arc;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Arc")
@Description({
        "Creates an arc or sector with the given radius and angle. The radius must be greater than 0.",
        "The angle must be between 0 and 360 degrees. If the angle is 360 degrees, the shape will be a circle.",
        "An arc is a portion of the circle's circumference. A sector is a portion of the circle's area."
})
@Examples({
        "set {_shape} to an arc with radius 10 and angle 45 degrees",
        "set {_shape} to a sector of radius 3 and angle 90 degrees"
})
@Since("1.0.0")
public class ExprArc extends SimpleExpression<Arc> {

    static {
        Skript.registerExpression(ExprArc.class, Arc.class, ExpressionType.COMBINED,
                "[a[n]] [circular] (arc|:sector) (with|of) radius %number%[,| and] [cutoff] angle %number% [degrees|:radians]");
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

        if (radius instanceof Literal) {
            double r = ((Literal<Number>) radius).getSingle().doubleValue();
            if (r <= 0){
                Skript.error("The radius of the arc must be greater than 0. (radius: " + r + ")");
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
    protected Arc[] get(Event event) {
        if (radius.getSingle(event) == null || angle.getSingle(event) == null)
            return new Arc[0];

        double radius = this.radius.getSingle(event).doubleValue();
        double angle = this.angle.getSingle(event).doubleValue();
        if (!isRadians)
            angle = Math.toRadians(angle);

        angle = MathUtil.clamp(angle, 0, 2*Math.PI);
        radius = Math.max(radius, 0);

        Arc arc = new Arc(radius, angle);
        if (isSector)
            arc.setStyle(Shape.Style.FILL);

        return new Arc[]{arc};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Arc> getReturnType() {
        return Arc.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (isSector ? "sector" : "arc") +  " with radius " + radius.toString(event, debug) + " and angle " + angle.toString(event, debug) + (isRadians ? " radians" : " degrees");
    }

}