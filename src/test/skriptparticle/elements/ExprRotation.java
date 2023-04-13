package com.sovdee.skriptparticle.elements;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.util.Quaternion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;

import org.jetbrains.annotations.Nullable;

public class ExprRotation extends SimpleExpression<Quaternion> {

    static {
        Skript.registerExpression(ExprRotation.class, Quaternion.class, ExpressionType.COMBINED, "[the] rotation (from|around) [the] [vector] %vector% (and|with|by) [the|an] angle [of] %number% [:degrees|radians]");
    }

    private Expression<Number> rotationExpr;
    private Expression<Vector> normalExpr;


    private boolean convertToRadians = true;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        normalExpr = (Expression<Vector>) exprs[1];
        rotationExpr = (Expression<Number>) exprs[2];
        if (!parseResult.hasTag("degrees"))
            convertToRadians = true;
        return true;
    }

    @Override
    protected @Nullable Quaternion[] get(Event e) {
        if (normalExpr == null || rotationExpr == null)
            return null;
        if (normalExpr.getSingle(e) == null || rotationExpr.getSingle(e) == null)
            return null;
        return new Quaternion[]{new Quaternion(normalExpr.getSingle(e), rotationExpr.getSingle(e).doubleValue())};
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
        return "rotation of " + normalExpr.toString(e, debug) + " and angle " + rotationExpr.toString(e, debug);
    }
}
