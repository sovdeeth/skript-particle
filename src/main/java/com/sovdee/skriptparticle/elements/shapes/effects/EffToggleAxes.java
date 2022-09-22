package com.sovdee.skriptparticle.elements.shapes.effects;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticle.elements.shapes.types.Shape;
import org.bukkit.event.Event;

import javax.annotation.Nullable;

public class EffToggleAxes extends Effect {

    static {
        Skript.registerEffect(EffToggleAxes.class, "(:show|:hide) [:local] [and] [:global] axes of [shape[s]] %shapes%");
    }

    private Expression<Shape> shapeExpr;
    private boolean localFlag = false;
    private boolean globalFlag = false;
    private boolean showFlag = false;

    @Override
    protected void execute(Event event) {
        Shape[] shapes = shapeExpr.getAll(event);
        for (Shape shape : shapes) {
            if (localFlag)
                shape.showLocalAxes = showFlag;
            if (globalFlag)
                shape.showGlobalAxes = showFlag;
        }
    }

    @Override
    public String toString(@Nullable Event event, boolean b) {
        return "Toggle global/local axes of shapes";
    }

    @Override
    public boolean init(Expression<?>[] expressions, int i, Kleenean kleenean, SkriptParser.ParseResult parseResult) {
        shapeExpr = (Expression<Shape>) expressions[0];
        localFlag = parseResult.hasTag("local");
        globalFlag = parseResult.hasTag("global");
        showFlag = parseResult.hasTag("show");
        return true;
    }
}
