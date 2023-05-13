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
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

@Name("Particle Circle or Cylinder")
@Description({
        "Creates a circle, disc, or cylinder shape with the given radius. The radius must be greater than 0."
})
@Examples({
        "set {_shape} to circle with radius 10",
        "set {_shape} to a disc of radius 3",
        "set {_shape} to a solid cylinder with radius 3 and height 5"
})
@Since("1.0.0")
public class ExprCircle extends SimpleExpression<Circle>{

    static {
        Skript.registerExpression(ExprCircle.class, Circle.class, ExpressionType.COMBINED,
                "[a] (circle|:disc) (with|of) radius %number%",
                "[a] [hollow|2:solid] (cylinder|1:tube) (with|of) radius %number% and height %number%");
    }

    private Expression<Number> radius;
    private Expression<Number> height;
    private Shape.Style style;
    private boolean isCylinder;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        isCylinder = matchedPattern == 1;

        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0){
            Skript.error("The radius of the "+ (isCylinder ? "cylinder" : "circle") + " must be greater than 0. (radius: " +
                    ((Literal<Number>) radius).getSingle().doubleValue() + ")");
            return false;
        }

        if (isCylinder) {
            height = (Expression<Number>) exprs[1];
            if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() < 0){
                Skript.error("The height of the cylinder must be greater than or equal to 0. (height: " +
                        ((Literal<Number>) height).getSingle().doubleValue() + ")");
                return false;
            }

            style = switch (parseResult.mark) {
                case 0 -> Shape.Style.SURFACE;
                case 1 -> Shape.Style.OUTLINE;
                default -> Shape.Style.FILL;
            };
        } else {
            style = parseResult.hasTag("disc") ? Shape.Style.SURFACE : Shape.Style.OUTLINE;
        }
        return true;
    }

    @Override
    @Nullable
    protected Circle[] get(@NotNull Event event) {
        Number radius = this.radius.getSingle(event);
        Number height = this.height != null ? this.height.getSingle(event) : 0;
        if (radius == null || height == null)
            return null;

        radius = Math.max(radius.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), 0);

        Circle circle = new Circle(radius.doubleValue(), height.doubleValue());
        circle.setStyle(style);
        return new Circle[]{circle};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @NotNull
    public Class<? extends Circle> getReturnType() {
        return Circle.class;
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return (isCylinder ? "circle of radius " + radius.toString(event, debug) :
                "cylinder of radius " + radius.toString(event, debug) + " and height " + (height != null ? height.toString(event, debug) : "0"));
    }
}
