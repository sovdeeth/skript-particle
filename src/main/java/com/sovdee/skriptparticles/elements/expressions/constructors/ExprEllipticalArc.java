package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.EllipticalArc;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEllipticalArc extends SimpleExpression<EllipticalArc> {

    static {
        Skript.registerExpression(ExprEllipticalArc.class, EllipticalArc.class, ExpressionType.COMBINED,
                "[an] elliptical (arc|:sector) (with|of) radi(i|us) %number%(,| and) %number%[,] and [cutoff] angle [of] %number% [degrees|:radians]",
                "[an] elliptical [cylindrical] (arc|:sector) (with|of) radi(i|us) %number%(,| and) %number%(,| and) height %-number%[,] and [cutoff] angle [of] %number% [degrees|:radians]");
    }

    private Expression<Number> lengthRadius;
    private Expression<Number> widthRadius;
    private Expression<Number> height;
    private Expression<Number> angle;
    private Style style;
    private boolean isRadians;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        lengthRadius = (Expression<Number>) exprs[0];
        widthRadius = (Expression<Number>) exprs[1];
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

        if (lengthRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (widthRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (angle instanceof Literal<Number> literal) {
            double c = literal.getSingle().doubleValue();
            if (c <= 0 || c > 360){
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
    protected EllipticalArc[] get(Event event) {
        Number lengthRadius = this.lengthRadius.getSingle(event);
        Number widthRadius = this.widthRadius.getSingle(event);
        Number height = this.height == null ? 0 : this.height.getSingle(event);
        Number angle = this.angle.getSingle(event);
        if (lengthRadius == null || widthRadius == null || angle == null || height == null)
            return null;

        lengthRadius = Math.max(lengthRadius.doubleValue(), MathUtil.EPSILON);
        widthRadius = Math.max(widthRadius.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), 0);
        if (!isRadians)
            angle = Math.toRadians(angle.doubleValue());

        angle = MathUtil.clamp(angle.doubleValue(), 0, Math.PI * 2);
        EllipticalArc arc = new EllipticalArc(lengthRadius.doubleValue(), widthRadius.doubleValue(), height.doubleValue(), angle.doubleValue());
        arc.setStyle(style);
        return new EllipticalArc[]{arc};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends EllipticalArc> getReturnType() {
        return EllipticalArc.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return  (this.style == Style.FILL ? "sector" : "arc") +
                " with radius " + lengthRadius.toString(event, debug) + " and " + widthRadius.toString(event, debug) +
                (height == null ? "" : " and height " + height.toString(event, debug)) +
                " and angle " + angle.toString(event, debug) +
                (isRadians ? " radians " : " degrees ");
    }
}
