package com.sovdee.skriptparticle.elements.shapes.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.RegularPolyhedron;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class ExprRegularPolyhedron extends SimpleExpression<RegularPolyhedron> {

    static {
        Skript.registerExpression(ExprRegularPolyhedron.class, RegularPolyhedron.class, ExpressionType.COMBINED, "[a[n]] [outlined|:hollow|:solid] (:tetra|:octa|:icosa|:dodeca)hedron (with|of) radius %number%");
    }

    private Expression<Number> radiusExpr;
    private int faces;
    private Shape.Style style;

    @Override
    protected @Nullable RegularPolyhedron[] get(Event event) {
        if (radiusExpr.getSingle(event) == null)
            return new RegularPolyhedron[0];
        RegularPolyhedron regularPolyhedron = new RegularPolyhedron(radiusExpr.getSingle(event).doubleValue(), faces);
        regularPolyhedron.style(style);
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
        return "regular polyhedron of " + faces + " faces with radius " + radiusExpr.toString(event, b);
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        radiusExpr = (Expression<Number>) expressions[0];
        faces = parseResult.hasTag("tetra") ? 4 : parseResult.hasTag("octa") ? 8 : parseResult.hasTag("dodeca") ? 12 : 20;
        style = parseResult.hasTag("hollow") ? Shape.Style.SURFACE : parseResult.hasTag("solid") ? Shape.Style.FILL : Shape.Style.OUTLINE;
        return true;
    }
}
