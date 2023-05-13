package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.RegularPolyhedron;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Shape.Style;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Particle Regular Polyhedron")
@Description({
        "Creates a regular polyhedron shape with the given radius. The radius must be greater than 0.",
        "Valid polyhedra are tetrahedra (4 faces), octahedra (8), dodecahedra (12), and icosahedra (20).",
        "",
        "Polyhedra currently do not support the particle count expression, only particle density."
})
@Examples({
        "set {_shape} to a tetrahedron with radius 1",
        "set {_shape} to a solid icosahedron with radius 2",
        "draw a tetrahedron with radius 5 at player"
})
public class ExprRegularPolyhedron extends SimpleExpression<RegularPolyhedron> {

    static {
        Skript.registerExpression(ExprRegularPolyhedron.class, RegularPolyhedron.class, ExpressionType.COMBINED, "[a[n]] [outlined|:hollow|:solid] (:tetra|:octa|:icosa|:dodeca)hedron (with|of) radius %number%");
    }

    private Expression<Number> radius;
    private int faces;
    private Style style;

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radius = (Expression<Number>) expressions[0];
        faces = parseResult.hasTag("tetra") ? 4 : parseResult.hasTag("octa") ? 8 : parseResult.hasTag("dodeca") ? 12 : 20;
        style = parseResult.hasTag("hollow") ? Shape.Style.SURFACE : parseResult.hasTag("solid") ? Shape.Style.FILL : Shape.Style.OUTLINE;

        if (radius instanceof Literal<Number> literal && literal.getSingle().doubleValue() <= 0){
            Skript.error("The radius of the polyhedron must be greater than 0. (radius: " +
                    ((Literal<Number>) radius).getSingle().doubleValue() + ")");
            return false;
        }

        return true;
    }

    @Override
    protected @Nullable RegularPolyhedron[] get(Event event) {
        if (radius.getSingle(event) == null)
            return new RegularPolyhedron[0];
        RegularPolyhedron regularPolyhedron = new RegularPolyhedron(radius.getSingle(event).doubleValue(), faces);
        regularPolyhedron.setStyle(style);
        return new RegularPolyhedron[]{regularPolyhedron};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends RegularPolyhedron> getReturnType() {
        return RegularPolyhedron.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "regular polyhedron with " + faces + " faces with radius " + radius.toString(event, b);
    }
}
