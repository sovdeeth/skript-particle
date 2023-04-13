package com.sovdee.skriptparticles.elements.expressions;


import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.Nullable;

@Name("Shape Rotation")
@Description("Describes a rotation around a vector by a given angle.")
@Examples({
        "set {_rotation} to rotation around vector(1, 0, 0) by an angle of 90 degrees",
        "set {_rotation} to a rotation around vector(1, 1, 1) by 1.57 radians",
        "set {_rotation} to a rotation around {_vector} with angle 174 degrees"
})
public class ExprRotation extends SimpleExpression<Quaternion> {

    static {
        Skript.registerExpression(ExprRotation.class, Quaternion.class, ExpressionType.COMBINED,
                "[the|a] rotation (from|around) [the] [vector] %vector% (with|by) [[the|an] angle [of]] %number% [:degrees|radians]");
    }

    private Expression<Number> angle;
    private Expression<Vector> axis;
    private boolean convertToRadians = true;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        axis = (Expression<Vector>) exprs[0];
        angle = (Expression<Number>) exprs[1];
        if (!parseResult.hasTag("degrees"))
            convertToRadians = true;
        return true;
    }

    @Override
    @Nullable
    protected Quaternion[] get(Event event) {
        if (axis == null || angle == null)
            return null;
        Vector axis = this.axis.getSingle(event);
        Number angle = this.angle.getSingle(event);
        if (axis == null || angle == null)
            return null;
        float angleFloat = angle.floatValue();
        if (convertToRadians)
            angleFloat = (float) Math.toRadians(angleFloat);
        return new Quaternion[]{new Quaternion().rotationAxis(angleFloat, axis)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Quaternion> getReturnType() {
        return Quaternion.class;
    }

    @Override
    public String toString(@Nullable Event e, boolean debug) {
        return "rotation around " + axis.toString(e, debug) + " by " + angle.toString(e, debug) + (convertToRadians ? " degrees" : " radians");
    }

}