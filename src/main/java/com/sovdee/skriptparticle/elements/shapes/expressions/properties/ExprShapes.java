package com.sovdee.skriptparticle.elements.shapes.expressions.properties;

import ch.njol.skript.Skript;
import ch.njol.skript.classes.Changer;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.particles.ParticleGradient;
import com.sovdee.skriptparticle.elements.shapes.structures.StructComplexShape;
import com.sovdee.skriptparticle.elements.shapes.types.ComplexShape;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import com.sovdee.skriptparticle.util.ParticleUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class ExprShapes extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprShapes.class, Shape.class, ExpressionType.SIMPLE, "[the] [list of] shapes [list]");
    }

    private ComplexShape shape;

    @Override
    public @Nullable Class<?>[] acceptChange(Changer.ChangeMode mode) {
        return new Class[]{Shape[].class};
    }

    @Override
    public void change(Event event, @Nullable Object[] delta, Changer.ChangeMode mode) {
        if ((delta == null || delta.length == 0) && (mode != Changer.ChangeMode.RESET && mode != Changer.ChangeMode.DELETE && mode != Changer.ChangeMode.REMOVE_ALL))
            return;
        if (shape == null)
            return;
        // override default particles
        if (delta != null && delta.length != 0) {
            for (Shape shape : (Shape[]) delta){
                if (shape.particle().equals(ParticleUtil.DEFAULT_PB) && !(this.shape.particle() instanceof ParticleGradient)){
                    shape.particle(this.shape.particle());
                }
            }
        }
        switch (mode) {
            case ADD:
                shape.addShapes((Shape[]) delta);
                break;
            case REMOVE:
                shape.removeShapes((Shape[]) delta);
                break;
            case SET:
                shape.setShapes((Shape[]) delta);
                break;
            case RESET:
            case DELETE:
            case REMOVE_ALL:
                shape.setShapes();
                break;
            default:
                assert false;
        }
    }

    @Override
    protected @Nullable Shape[] get(Event e) {
        return shape.getShapes().toArray(new Shape[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public @NotNull Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public @NotNull String toString(@Nullable Event event, boolean debug) {
        return "shapes";
    }

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        if (!getParser().isCurrentStructure(StructComplexShape.class)){
            Skript.error("You can only use the list of shapes while creating a custom shape.");
            return false;
        }
        this.shape = ((StructComplexShape) getParser().getCurrentStructure()).getComplexShape();
        return true;
    }
}
