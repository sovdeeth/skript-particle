package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.Heart;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Heart")
@Description("""
    Creates a heart shape with the given width and height, and optionally eccentricity. The width (x) and length (z) must be greater than 0.
    The eccentricity defaults to 3, but must be at least 1. This determines how round/pointy the heart is. Values between 1 and 5 are recommended.
    Note that the width and length are not exact, but they're roughly the width and length of the heart.
    Finally, this shape does not support the particle count expression and its particle density is not uniform.
    If anyone knows a good way to compute the complete elliptic integral of the second kind, please let me know.
    """)
@Example("set {_heart} to heart with width 5 and length 4")
@Example("set {_heart} to heart shape with width 5, length 7, and eccentricity 2")
@Example("draw the shape of a heart of width 2 and length 2 at player")
@Since("1.0.1")
public class ExprHeart extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprHeart.class, Shape.class)
                .supplier(ExprHeart::new)
                .addPatterns("[a] [:solid] heart [shape] (with|of) width %number%[,] [and] length %number%[[,] [and] eccentricity %-number%]")
                .build());
    }

    private Expression<Number> width;
    private Expression<Number> length;
    private Expression<Number> eccentricity;
    private boolean isSolid;

    @Override
    @SuppressWarnings("unchecked")
    public boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, ParseResult parseResult) {
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
    protected @Nullable List<Shape> getShapes(Event event) {
        Number width = this.width.getSingle(event);
        Number length = this.length.getSingle(event);
        Number eccentricity = this.eccentricity == null ? 3 : this.eccentricity.getSingle(event);
        if (width == null || length == null || eccentricity == null)
            return null;
        width = Math.max(width.doubleValue(), MathUtil.EPSILON);
        length = Math.max(length.doubleValue(), MathUtil.EPSILON);
        eccentricity = Math.max(eccentricity.doubleValue(), 1);

        Heart shape = new Heart(width.doubleValue(), length.doubleValue(), eccentricity.doubleValue());
        if (isSolid) {
            shape.getPointSampler().setStyle(SamplingStyle.SURFACE);
        }
        shape.getPointSampler().setDrawContext(new DrawData());
        return List.of(shape);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "a heart shape with width " + width.toString(event, debug) + ", height " + length.toString(event, debug) + (eccentricity == null ? "" : ", and eccentricity " + eccentricity.toString(event, debug) + ".");
    }
}
