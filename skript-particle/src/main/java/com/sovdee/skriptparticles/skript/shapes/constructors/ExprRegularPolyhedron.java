package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.shapes.shapes.RegularPolyhedron;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;

import java.util.List;
import org.skriptlang.skript.registration.SyntaxRegistry;

@Name("Particle Regular Polyhedron")
@Description("""
    Creates a regular polyhedron shape with the given radius. The radius must be greater than 0.
    Valid polyhedra are tetrahedra (4 faces), octahedra (8), dodecahedra (12), and icosahedra (20).

    Polyhedra currently do not support the particle count expression, only particle density.
    """)
@Example("set {_shape} to a tetrahedron with radius 1")
@Example("set {_shape} to a solid icosahedron with radius 2")
@Example("draw the shape of a tetrahedron with radius 5 at player")
@Since("1.0.0")
public class ExprRegularPolyhedron extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprRegularPolyhedron.class, Shape.class)
                .supplier(ExprRegularPolyhedron::new)
                .addPatterns("[a[n]] [outlined|:hollow|:solid] (:tetra|:octa|:icosa|:dodeca)hedron (with|of) radius %number%")
                .build());
    }

    private Expression<Number> radius;
    private int faces;
    private SamplingStyle style;

    @Override
    @SuppressWarnings("unchecked")
    public boolean initialize(Expression<?>[] expressions, int matchedPattern, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radius = (Expression<Number>) expressions[0];
        faces = parseResult.hasTag("tetra") ? 4 : parseResult.hasTag("octa") ? 8 : parseResult.hasTag("dodeca") ? 12 : 20;
        style = parseResult.hasTag("hollow") ? SamplingStyle.SURFACE : parseResult.hasTag("solid") ? SamplingStyle.FILL : SamplingStyle.OUTLINE;

        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0) {
            Skript.error("The radius of the polyhedron must be greater than 0. (radius: " +
                    ((Literal<Number>) radius).getSingle().doubleValue() + ")");
            return false;
        }

        return true;
    }

    @Override
    protected @Nullable List<Shape> getShapes(Event event) {
        Number r = radius.getSingle(event);
        if (r == null)
            return null;
        RegularPolyhedron shape = new RegularPolyhedron(r.doubleValue(), faces);
        shape.getPointSampler().setStyle(style);
        shape.getPointSampler().setDrawContext(new DrawData());
        return List.of(shape);
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "regular polyhedron with " + faces + " faces with radius " + radius.toString(event, debug);
    }
}
