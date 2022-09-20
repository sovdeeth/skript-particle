package com.sovdee.skriptparticle.elements.shapes.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;

public class EffRotateShape extends Effect {

static {
        Skript.registerEffect(EffRotateShape.class,
                "rotate [shape[s]] %shapes% around (v:%-vector%|((:x|:y|:z)(-| )axis)) by %-number% [:degrees|:radians]",
                "rotate [shape[s]] %shapes% (by|with) [rotation] %rotation%");
    }

    private Expression<Shape> shapesExpr;
    private Expression<Vector> axisExpr;
    private Expression<Number> angleExpr;
    private String axis;
    private boolean convertToRadians = true;

    @Override
    protected void execute(Event e) {
        Number angle = angleExpr.getSingle(e);
        if (angle == null) return;
        if (convertToRadians) angle = Math.toRadians(angle.doubleValue());

        Quaternion rotation;
        if (axis != null) {
            switch (axis) {
                case "x":
                    rotation = new Quaternion(new Vector(1, 0, 0), angle.doubleValue());
                    break;
                case "y":
                    rotation = new Quaternion(new Vector(0, 1, 0), angle.doubleValue());
                    break;
                case "z":
                    rotation = new Quaternion(new Vector(0, 0, 1), angle.doubleValue());
                    break;
                default:
                    return;
            }
        } else {
            Vector axis = axisExpr.getSingle(e);
            if (axis == null) return;
            rotation = new Quaternion(axis, angle.doubleValue());
        }

        for (Shape shape : shapesExpr.getAll(e)) {
            shape.orientation().multiply(rotation.normalize());
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
            axis = parseResult.tags.get(0);
        }
        angleExpr = (Expression<Number>) exprs[2];
        convertToRadians = !parseResult.hasTag("radians");
        return true;
    }
}
