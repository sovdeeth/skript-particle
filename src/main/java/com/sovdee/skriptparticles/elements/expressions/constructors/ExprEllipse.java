package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Ellipse;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

public class ExprEllipse extends SimpleExpression<Ellipse> {

    static {
        Skript.registerExpression(ExprEllipse.class, Ellipse.class, ExpressionType.COMBINED,
                "[a[n]] [surface:(solid|filled)] (ellipse|oval) (with|of) radi(i|us) %number% and %number%",
                "[a[n]] [hollow|2:solid] elliptical (cylinder|1:tube) (with|of) radi(i|us) %number%(,| and) %number%[,] and height %number%");
    }

    private Expression<Number> lengthRadius;
    private Expression<Number> widthRadius;
    private Expression<Number> height;
    private Style style;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        lengthRadius = (Expression<Number>) exprs[0];
        widthRadius = (Expression<Number>) exprs[1];
        style = Style.OUTLINE;
        if (parseResult.hasTag("surface")) {
            style = Style.SURFACE;
        }
        if (matchedPattern == 1) {
            height = (Expression<Number>) exprs[2];
            style = switch (parseResult.mark) {
                case 0 -> Shape.Style.SURFACE;
                case 1 -> Shape.Style.OUTLINE;
                default -> Shape.Style.FILL;
            };
        }

        if (lengthRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the ellipse must be greater than 0. (length radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (widthRadius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
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
    @Nullable
    protected Ellipse[] get(Event event) {
        Number lengthRadius = this.lengthRadius.getSingle(event);
        Number widthRadius = this.widthRadius.getSingle(event);
        Number height = this.height == null ? 0 : this.height.getSingle(event);
        if (lengthRadius == null || widthRadius == null || height == null) {
            return null;
        }
        Ellipse ellipse = new Ellipse(lengthRadius.doubleValue(), widthRadius.doubleValue(), height.doubleValue());
        ellipse.setStyle(style);
        return new Ellipse[]{ellipse};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Ellipse> getReturnType() {
        return Ellipse.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return (height == null ? "ellipse of x radius " + lengthRadius.toString(event, debug) + " and z radius " + widthRadius.toString(event, debug)  :
                "elliptical cylinder of x radius " + lengthRadius.toString(event, debug) + " and z radius " + widthRadius.toString(event, debug) +
                        " and height " + (height != null ? height.toString(event, debug) : "0"));
    }
}
