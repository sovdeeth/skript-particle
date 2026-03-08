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
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.shapes.Sphere;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.MathUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Sphere")
@Description("""
    Creates a sphere shape with the given radius. The radius must be greater than 0.
    Default style is a hollow sphere, but you can use the 'solid' tag to make it solid.
    """)
@Example("set {_shape} to sphere with radius 3")
@Example("set {_shape} to solid sphere with radius 10")
@Since("1.0.0")
public class ExprSphere extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprSphere.class, Shape.class)
                .supplier(ExprSphere::new)
                .addPatterns("[a] [:solid] sphere (with|of) radius %number%")
                .build());
    }

    private Expression<Number> radius;
    private boolean isSolid;

    @Override
    @SuppressWarnings("unchecked")
    public boolean initialize(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the sphere must be greater than 0. (radius: " + literal.getSingle().doubleValue() + ")");
            return false;
        }
        isSolid = parseResult.hasTag("solid");
        return true;
    }

    @Override
    protected @Nullable List<Shape> getShapes(Event event) {
        Number radius = this.radius.getSingle(event);
        if (radius == null)
            return null;

        radius = Math.max(radius.doubleValue(), MathUtil.EPSILON);

        Sphere shape = new Sphere(radius.doubleValue());
        if (isSolid) shape.getPointSampler().setStyle(SamplingStyle.FILL);
        shape.getPointSampler().setDrawContext(new DrawData());
        return List.of(shape);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "sphere with radius " + radius.toString(event, debug);
    }
}
