package com.sovdee.skriptparticles.skript.drawing;

import ch.njol.skript.registrations.EventValues;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.skript.drawing.expressions.ExprDrawnShapes;
import com.sovdee.skriptparticles.skript.drawing.sections.DrawShapeEffectSection.DrawEvent;
import com.sovdee.skriptparticles.skript.drawing.sections.EffSecDrawShape;
import com.sovdee.skriptparticles.skript.drawing.sections.EffSecDrawShapeAnimation;
import org.skriptlang.skript.addon.AddonModule;
import org.skriptlang.skript.addon.HierarchicalAddonModule;
import org.skriptlang.skript.addon.SkriptAddon;

/**
 * Holds syntax related to drawing shapes with particles.
 */
public class DrawingModule extends HierarchicalAddonModule {

    public DrawingModule(AddonModule parentModule) {
        super(parentModule);
    }

    @Override
    protected void initSelf(SkriptAddon addon) {
        EventValues.registerEventValue(DrawEvent.class, Shape.class, DrawEvent::getShape, EventValues.TIME_NOW);
    }

    @Override
    protected void loadSelf(SkriptAddon addon) {
        register(addon,
                EffSecDrawShape::register,
                EffSecDrawShapeAnimation::register,
                ExprDrawnShapes::register
        );
    }

    @Override
    public String name() {
        return "drawing";
    }

}
