package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.shapes.Sphere;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import org.jetbrains.annotations.Nullable;

@Name("Particle Sphere")
@Description({
        "Creates a sphere shape with the given radius. The radius must be greater than 0.",
        "Default style is a hollow sphere, but you can use the 'solid' tag to make it solid."
})
@Examples({
        "set {_shape} to sphere with radius 3",
        "set {_shape} to solid sphere with radius 10"
})
@Since("1.0.0")
public class ExprSphere extends SimpleExpression<Sphere>{

    static {
        Skript.registerExpression(ExprSphere.class, Sphere.class, ExpressionType.COMBINED, "[a] [:solid] sphere [with|of] radius %number%");
    }

    private Expression<Number> radius;
    private boolean isSolid;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NotNull Kleenean isDelayed, @NotNull ParseResult parseResult) {
        radius = (Expression<Number>) exprs[0];
        if (radius instanceof Literal) {
            if (((Literal<Number>) radius).getSingle().doubleValue() <= 0){
                Skript.error("The radius of the sphere must be greater than 0. (radius: " + ((Literal<Number>) radius).getSingle().doubleValue() + ")");
                return false;
            }
        }
        isSolid = parseResult.hasTag("solid");
        return true;
    }

    @Override
    protected Sphere @NotNull [] get(@NotNull Event event) {
        Number radius = this.radius.getSingle(event);
        if (radius == null || radius.doubleValue() <= 0) {
            Skript.warning("The radius of the sphere must be greater than 0; defaulting to 1. (radius: " + radius + ")");
            radius = 1;
        }
        Sphere sphere = new Sphere(radius.doubleValue());
        if (isSolid) sphere.setStyle(Shape.Style.FILL);
        return new Sphere[]{sphere};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @NotNull
    public Class<? extends Sphere> getReturnType() {
        return Sphere.class;
    }

    @Override
    @NotNull
    public String toString(@Nullable Event event, boolean debug) {
        return "sphere with radius " + radius.toString(event, debug);
    }
}
