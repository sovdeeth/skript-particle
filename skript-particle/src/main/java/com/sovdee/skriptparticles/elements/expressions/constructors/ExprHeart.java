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
import com.sovdee.shapes.shapes.Heart;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Heart")
@Description({
        "Creates a heart shape with the given width and height, and optionally eccentricity. The width (x) and length (z) must be greater than 0.",
        "The eccentricity defaults to 3, but must be at least 1. This determines how round/pointy the heart is. Values between 1 and 5 are recommended.",
        "Note that the width and length are not exact, but they're roughly the width and length of the heart.",
        "Finally, this shape does not support the particle count expression and its particle density is not uniform. If anyone knows a good way to compute the complete elliptic integral of the second kind, please let me know."
})
@Examples({
        "set {_heart} to heart with width 5 and length 4",
        "set {_heart} to heart shape with width 5, length 7, and eccentricity 2",
        "draw the shape of a heart of width 2 and length 2 at player"
})
@Since("1.0.1")
public class ExprHeart extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprHeart.class, Shape.class, ExpressionType.COMBINED, "[a] [:solid] heart [shape] (with|of) width %number%[,] [and] length %number%[[,] [and] eccentricity %-number%]");
    }

    private Expression<Number> width;
    private Expression<Number> length;
    private Expression<Number> eccentricity;
    private boolean isSolid;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult) {
        width = (Expression<Number>) expressions[0];
        length = (Expression<Number>) expressions[1];
        if (expressions.length > 2) {
            eccentricity = (Expression<Number>) expressions[2];
        }

        if (width instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The width of a heart must be greater than 0.");
            return false;
        }

        if (length instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The length of a heart must be greater than 0.");
            return false;
        }

        if (eccentricity instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The eccentricity of a heart must be greater than 1.");
            return false;
        }

        isSolid = parseResult.hasTag("solid");

        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Number width = this.width.getSingle(event);
        Number length = this.length.getSingle(event);
        Number eccentricity = this.eccentricity == null ? 3 : this.eccentricity.getSingle(event);
        if (width == null || length == null || eccentricity == null) {
            return null;
        }
        width = Math.max(width.doubleValue(), MathUtil.EPSILON);
        length = Math.max(length.doubleValue(), MathUtil.EPSILON);
        eccentricity = Math.max(eccentricity.doubleValue(), 1);

        Heart shape = new Heart(width.doubleValue(), length.doubleValue(), eccentricity.doubleValue());
        if (isSolid) {
            shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        }
        shape.getPointSampler().setDrawContext(new DrawData());
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
        return "a heart shape with width " + width.toString(event, debug) + ", height " + length.toString(event, debug) + (eccentricity == null ? "" : ", and eccentricity " + eccentricity.toString(event, debug) + ".");
    }
}
