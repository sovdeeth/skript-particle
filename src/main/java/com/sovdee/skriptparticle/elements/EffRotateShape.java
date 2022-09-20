package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class EffRotateShape extends Effect {

static {
        Skript.registerEffect(EffRotateShape.class,
                "rotate [shape[s]] %shapes% around (v:%-vector%|((:x|:y|:z)(-| )axis)) by %-number% [:degrees|:radians]");
    }

    private Expression<Shape> shapesExpr;
    private Expression<Vector> axisExpr;
    private Expression<Number> angleExpr;
    private boolean convertToRadians = true;

    @Override
    protected void execute(Event e) {
        Number angle = angleExpr.getSingle(e);
        if (angle == null) return;
        if (convertToRadians) angle = Math.toRadians(angle.doubleValue());

        Vector axis = axisExpr.getSingle(e);
        if (axis == null) return;

        for (Shape shape : shapesExpr.getArray(e)) {
            shape.getNormal().rotateAroundAxis(axis.clone().normalize(), angle.doubleValue());
            shape.setNeedsUpdate(true);
        }

    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return null;
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapesExpr = (Expression<Shape>) exprs[0];
        if (parseResult.hasTag("v")) {
            axisExpr = (Expression<Vector>) exprs[1];
        } else {
            axisExpr = new SimpleLiteral<>(new Vector(parseResult.hasTag("x") ? 1 : 0, parseResult.hasTag("y") ? 1 : 0, parseResult.hasTag("z") ? 1 : 0), false);
        }
        angleExpr = (Expression<Number>) exprs[2];
        convertToRadians = !parseResult.hasTag("radians");
        return true;
    }
}
