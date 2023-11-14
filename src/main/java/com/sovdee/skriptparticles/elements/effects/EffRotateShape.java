package com.sovdee.skriptparticles.elements.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.elements.sections.DrawShapeEffectSection.DrawEvent;
import com.sovdee.skriptparticles.shapes.Shape;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

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
                "rotate shape[s] %shapes% around [relative:(relative|local)] (v:%-vector%|((:x|:y|:z)(-| )axis)) by %number% [degrees|:radians]",
                "rotate shape[s] %shapes% (by|with) [rotation] %quaternion%",
                "rotate [drawn] shape[s] around [relative:(relative|local)] (v:%-vector%|((:x|:y|:z)(-| )axis)) by %number% [degrees|:radians]",
                "rotate [drawn] shape[s] (by|with) [rotation] %quaternion%"
        );
    }

    private Expression<Shape> shapes;
    private boolean useDrawnShapes = false;
    private Expression<Vector> vectorAxis;
    private Expression<Number> angle;
    private Expression<Quaternionf> rotation;
    private String axis;
    private boolean relative = false;
    private boolean isRadians = false;
    private boolean isAxisAngle = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        int offset = 0;
        if (matchedPattern >= 2) {
            if (!getParser().isCurrentEvent(DrawEvent.class)) {
                Skript.error("You must supply a shape to rotate when using this effect outside of the draw shape section.");
                return false;
            }
            useDrawnShapes = true;
            offset = 1;
        } else {
            shapes = (Expression<Shape>) exprs[0];
        }
        if (matchedPattern % 2 == 1) {
            rotation = (Expression<Quaternionf>) exprs[1 - offset];
            return true;
        }
        isAxisAngle = true;
        if (parseResult.hasTag("v")) {
            vectorAxis = (Expression<Vector>) exprs[1 - offset];
        } else {
            relative = parseResult.hasTag("relative");
            axis = parseResult.hasTag("x") ? "x" : parseResult.hasTag("y") ? "y" : "z";
        }
        angle = (Expression<Number>) exprs[2 - offset];
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

        Shape[] shapes;
        if (useDrawnShapes && event instanceof DrawEvent) {
            shapes = new Shape[]{((DrawEvent) event).getShape()};
        } else {
            shapes = this.shapes.getAll(event);
        }

        for (Shape shape : shapes) {
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
        if (rotation != null)
            return "rotate shape " + shapes.toString(event, debug) + " by " + rotation.toString(event, debug);
        return "rotate shape " + shapes.toString(event, debug) + " around " + (relative ? "relative " : "") +
                (vectorAxis != null ? "vector " + vectorAxis.toString(event, debug) : axis + " axis") + " by " +
                angle.toString(event, debug) + (isRadians ? " radians" : " degrees");
    }

}
