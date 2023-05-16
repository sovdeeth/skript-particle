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
import com.sovdee.skriptparticles.shapes.Heart;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Heart")
@Description({
        "Creates a heart shape with the given width and height, and optionally eccentricity. The width and height must be greater than 0.",
        "The eccentricity defaults to 3, but must be at least 1. This determines how round/pointy the heart is. Values between 1 and 5 are recommended.",
        "Note that the width and heights are not exact, but they're roughly the width and height of the heart.",
        "Finally, this shape does not support the particle count expression and its particle density is not uniform. If anyone knows a good way to compute the complete elliptic integral of the second kind, please let me know."
})
@Examples({
        "set {_heart} to heart with width 5 and height 4",
        "set {_heart} to heart shape with width 5, height 7, and eccentricity 2",
        "draw a heart of width 2 and height 2 at player"
})
@Since("1.0.1")
public class ExprHeart extends SimpleExpression<Heart> {

    static {
        Skript.registerExpression(ExprHeart.class, Heart.class, ExpressionType.COMBINED, "[a] [:solid] heart [shape] (with|of) width %number%[,] [and] height %number%[[,] [and] eccentricity %-number%]");
    }

    private Expression<Number> width;
    private Expression<Number> height;
    private Expression<Number> eccentricity;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, ParseResult parseResult) {
        width = (Expression<Number>) expressions[0];
        height = (Expression<Number>) expressions[1];
        if (expressions.length > 2) {
            eccentricity = (Expression<Number>) expressions[2];
        }

        if (width instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The width of a heart must be greater than 0.");
            return false;
        }

        if (height instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The height of a heart must be greater than 0.");
            return false;
        }

        if (eccentricity instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The eccentricity of a heart must be greater than 1.");
            return false;
        }

        return true;
    }

    @Override
    @Nullable
    protected Heart[] get(Event event) {
        Number width = this.width.getSingle(event);
        Number height = this.height.getSingle(event);
        Number eccentricity = this.eccentricity == null ? 3 : this.eccentricity.getSingle(event);
        if (width == null || height == null || eccentricity == null) {
            return null;
        }
        width = Math.max(width.doubleValue(), MathUtil.EPSILON);
        height = Math.max(height.doubleValue(), MathUtil.EPSILON);
        eccentricity = Math.max(eccentricity.doubleValue(), 1);

        return new Heart[] {new Heart(width.doubleValue(), height.doubleValue(), eccentricity.doubleValue())};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Heart> getReturnType() {
        return Heart.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "a heart shape with width " + width.toString(event, b) + ", height " + height.toString(event, b) + ", and eccentricity " + eccentricity.toString(event, b) + ".";
    }
}
