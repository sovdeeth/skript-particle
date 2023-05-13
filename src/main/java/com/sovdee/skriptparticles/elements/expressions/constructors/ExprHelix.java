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
import com.sovdee.skriptparticles.shapes.Helix;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Helix / Spiral")
@Description({
        "Creates a helix or spiral shape with the given radius and height. The radius and height must be greater than 0.",
        "The winding rate is the number of loops per meter or block. If omitted, the winding rate will be 1 loop per block.",
        "The height of the helix can be manipulated through both the length and height expressions."
})
@Examples({
        "set {_shape} to helix with radius 10 and height 5",
        "set {_shape} to a spiral with radius 3 and height 5 and winding rate of 2 loops per meter",
        "set {_shape} to a solid anti-clockwise helix with radius 3, height 5, winding rate 1"
})
@Since("1.0.0")
public class ExprHelix extends SimpleExpression<Helix> {

    static {
        Skript.registerExpression(ExprHelix.class, Helix.class, ExpressionType.COMBINED,
                "[a[n]] [:solid] [[1:(counter|anti)[-]]clockwise] (helix|spiral) (with|of) radius %number%[,] [and] height %number%[[,] [and] winding rate [of] %-number% [loops per (meter|block)]]");
    }

    private Expression<Number> radius;
    private Expression<Number> height;
    private Expression<Number> windingRate;
    private boolean isClockwise = true;
    private Style style;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        height = (Expression<Number>) exprs[1];
        if (exprs.length > 2)
            windingRate = (Expression<Number>) exprs[2];
        isClockwise = parseResult.mark == 0;
        style = parseResult.hasTag("solid") ? Style.SURFACE : Style.OUTLINE;

        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of a helix must be greater than 0. (radius: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The height of a helix must be greater than 0. (height: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        if (windingRate instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The winding rate of a helix must be greater than 0. (winding rate: " +
                    literal.getSingle().doubleValue() + ")");
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    protected Helix[] get(Event event) {
        Number radius = this.radius.getSingle(event);
        Number height = this.height.getSingle(event);
        Number windingRate = this.windingRate == null ? 1 : this.windingRate.getSingle(event);
        if (radius == null || height == null || windingRate == null)
            return null;
        radius = Math.max(radius.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), MathUtil.EPSILON);
        double slope = 1.0 / Math.max(windingRate.doubleValue(), MathUtil.EPSILON);
        int direction = isClockwise ? 1 : -1;
        Helix helix = new Helix(radius.doubleValue(), height.doubleValue(), slope/(2*Math.PI), direction);
        helix.setStyle(style);
        return new Helix[]{helix};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Helix> getReturnType() {
        return Helix.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a " + (isClockwise ? "clockwise" : "counter-clockwise") + " helix with radius " +
                radius.toString(event, debug) + ", height " + height.toString(event, debug) +
                (windingRate == null ? "" : ", and winding rate " + windingRate.toString(event, debug));
    }

}
