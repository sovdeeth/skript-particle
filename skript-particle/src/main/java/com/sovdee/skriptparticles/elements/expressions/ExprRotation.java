package com.sovdee.skriptparticles.elements.expressions;


import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Rotation")
@Description("Describes a rotation around a vector by a given angle, or from one vector to another. An alternative to the `axisAngle` and `quaternion` functions. Returns a quaternion.")
@Examples({
        "set {_rotation} to rotation around vector(1, 0, 0) by an angle of 90 degrees",
        "set {_rotation} to a rotation around vector(1, 1, 1) by 1.57 radians",
        "set {_rotation} to a rotation from vector(1, 0, 0) to vector(0, 1, 0)",
        "set {_rotation} to a rotation between vector(1, 0, 1) and vector(0, 1, 2)"
})
@Since("1.0.0")
public class ExprRotation extends SimpleExpression<Quaternion> {

    static {
        Skript.registerExpression(ExprRotation.class, Quaternion.class, ExpressionType.COMBINED,
                "[the|a] rotation (from|around) [the] [vector] %vector% (with|by) [[the|an] angle [of]] %number% [degrees|:radians]",
                "[the|a] rotation (from|between) %vector% (to|and) %vector%");
    }

    @Nullable
    private Expression<Number> angle;
    @Nullable
    private Expression<Vector> axis;
    @Nullable
    private Expression<Vector> from;
    @Nullable
    private Expression<Vector> to;
    private boolean isRadians = false;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (matchedPattern == 0) {
            axis = (Expression<Vector>) exprs[0];
            angle = (Expression<Number>) exprs[1];
            isRadians = parseResult.hasTag("radians");
        } else {
            from = (Expression<Vector>) exprs[0];
            to = (Expression<Vector>) exprs[1];
        }
        return true;
    }

    @Override
    @Nullable
    protected Quaternion[] get(Event event) {
        if (axis != null && angle != null) {
            @Nullable Vector axis = this.axis.getSingle(event);
            @Nullable Number angle = this.angle.getSingle(event);
            if (axis == null || angle == null)
                return new Quaternion[0];
            float angleFloat = angle.floatValue();
            if (!isRadians)
                angleFloat = (float) Math.toRadians(angleFloat);
            return new Quaternion[]{new Quaternion().rotationAxis(angleFloat, axis)};
        } else {
            if (from == null || to == null)
                return new Quaternion[0];
            @Nullable Vector from = this.from.getSingle(event);
            @Nullable Vector to = this.to.getSingle(event);
            if (from == null || to == null)
                return new Quaternion[0];
            return new Quaternion[]{new Quaternion().rotationTo(from, to)};
        }
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
    public String toString(@Nullable Event event, boolean debug) {
        if (axis == null)
            return "rotation from " + (from != null ? from.toString(event, debug) : "null") + " to " + (to != null ? to.toString(event, debug) : "null");
        return "rotation around " + axis.toString(event, debug) + " by " + (angle != null ? angle.toString(event, debug) : "null") + (isRadians ? " radians" : " degrees");
    }

}
