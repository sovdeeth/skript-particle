package com.sovdee.skriptparticles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

import org.jetbrains.annotations.Nullable;

@Name("Rotate Shape")
@Description({
        "Rotates shapes around a given axis by a given angle. The axis can be specified as a vector or as a single axis (x, y, or z). " +
        "By default, the rotation is done in degrees.",
        "The axis can also be specified as a local or relative axis, which rotate relative to the shape's current rotation.",
        "For example, you can rotate a shape around the global y axis, or around the shape's local y axis."
})
@Examples({
        "rotate shape {_shape} around x axis by 90 degrees",
        "rotate shape {_shape} around vector(0, 1, 0) by 1.4 radians",
        "rotate shape {_shape} around local y axis by 90"
})
@Since("1.0.0")
public class EffRotateShape extends Effect {

    static {
        Skript.registerEffect(EffRotateShape.class,
                "rotate shape[s] %shapes% around [relative:(relative|local)] (v:%-vector%|((:x|:y|:z)(-| )axis)) by %-number% [degrees|:radians]",
                "rotate shape[s] %shapes% (by|with) [rotation] %quaternion%"
                );
    }

    private Expression<Shape> shapes;
    private Expression<Vector> vectorAxis;
    private Expression<Number> angle;
    private Expression<Quaternionf> rotation;
    private String axis;
    private boolean relative = false;
    private boolean isRadians = false;
    private boolean isAxisAngle = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        shapes = (Expression<Shape>) exprs[0];
        if (matchedPattern == 1) {
            rotation = (Expression<Quaternionf>) exprs[1];
            return true;
        }
        isAxisAngle = true;
        if (parseResult.hasTag("v")) {
            vectorAxis = (Expression<Vector>) exprs[1];
        } else {
            relative = parseResult.hasTag("relative");
            axis = parseResult.hasTag("x") ? "x" : parseResult.hasTag("y") ? "y" : "z";
        }
        angle = (Expression<Number>) exprs[2];
        isRadians = parseResult.hasTag("radians");
        return true;
    }

    @Override
    protected void execute(Event event) {
        Quaternionf rotation;
        if (isAxisAngle) {
            Number angle = this.angle.getSingle(event);
            if (angle == null) return;
            if (!isRadians) angle = Math.toRadians(angle.doubleValue());
            Vector axis;
            if (this.axis != null) {
                switch (this.axis) {
                    case "x":
                        axis = new Vector(1, 0, 0);
                        break;
                    case "y":
                        axis = new Vector(0, 1, 0);
                        break;
                    case "z":
                        axis = new Vector(0, 0, 1);
                        break;
                    default:
                        return;
                }
            } else {
                axis = vectorAxis.getSingle(event);
                if (axis == null) return;
            }
            rotation = new Quaternionf().rotationAxis((float) angle.doubleValue(), (float) axis.getX(), (float) axis.getY(), (float) axis.getZ());
        } else {
            rotation = this.rotation.getSingle(event);
            if (rotation == null) return;
        }

        for (Shape shape : shapes.getAll(event)) {
            Quaternionf orientation = shape.getOrientation();
            if (relative) {
                shape.setOrientation(orientation.mul(rotation));
            } else {
                shape.setOrientation(orientation.premul(rotation));
            }
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "rotate shape " + shapes.toString(event, debug) + " around " + (relative ? "relative " : "") +
                (vectorAxis != null ? "vector " + vectorAxis.toString(event, debug) : axis + " axis") + " by " +
                angle.toString(event, debug) + (isRadians ? " radians" : " degrees");
    }

}
