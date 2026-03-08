package com.sovdee.skriptparticles.skript.shapes.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.shapes.shapes.Shape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Name("Shape Modifiers")
@Description({
        "The modifiers of a shape. Modifiers transform geometry (taper, twist, wave) or render properties (gradients, motion).",
        "Geometry modifiers are cached; render modifiers run per-frame.",
        "SET replaces all modifiers. ADD appends a modifier. REMOVE removes a specific modifier. DELETE/RESET clears all modifiers."
})
@Examples({
        "add taper from 1 to 0 to modifiers of {_shape}",
        "add a twist of 90 degrees to modifiers of {_helix}",
        "add gradient from red to blue along the y axis to modifiers of {_shape}",
        "add motion modifier counterclockwise to modifiers of {_shape}",
        "set modifiers of {_shape} to gradient from red to blue along the y axis",
        "delete modifiers of {_shape}",
})
@Since("2.0.0")
@SuppressWarnings("rawtypes")
public class ExprShapeModifiers extends SimpleExpression<PointModifier> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprShapeModifiers.class, PointModifier.class)
                .supplier(ExprShapeModifiers::new)
                .addPatterns(
                        "[point] modifiers of %shapes%",
                        "%shapes%'[s] [point] modifiers"
                )
                .build());
    }

    private Expression<Shape> shapes;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.shapes = (Expression<Shape>) exprs[0];
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        Shape[] shapeArr = shapes.getArray(event);
        if (shapeArr.length == 0) return new PointModifier[0];
        List<PointModifier<?>> mods = new ArrayList<>();
        for (Shape shape : shapeArr) {
            mods.addAll(shape.getPointSampler().getModifiers());
        }
        return mods.toArray(new PointModifier[0]);
    }

    @Override
    public Class<?> @Nullable [] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, REMOVE, ADD -> new Class[]{PointModifier.class};
            case DELETE -> new Class[0];
            default -> null;
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        Shape[] shapeArr = shapes.getArray(event);
        switch (mode) {
            case SET -> {
                if (delta == null) return;
                for (Shape shape : shapeArr) {
                    shape.getPointSampler().clearModifiers();
                    for (Object o : delta) {
                        if (o instanceof PointModifier<?> mod)
                            shape.getPointSampler().addModifier(mod.clone());
                    }
                }
            }
            case ADD -> {
                if (delta == null) return;
                for (Shape shape : shapeArr) {
                    for (Object o : delta) {
                        if (o instanceof PointModifier<?> mod)
                            shape.getPointSampler().addModifier(mod);
                    }
                }
            }
            case REMOVE -> {
                if (delta == null) return;
                for (Shape shape : shapeArr) {
                    for (Object o : delta) {
                        if (o instanceof PointModifier<?> mod)
                            shape.getPointSampler().removeModifier(mod);
                    }
                }
            }
            case DELETE, RESET -> {
                for (Shape shape : shapeArr)
                    shape.getPointSampler().clearModifiers();
            }
        }
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "modifiers of " + shapes.toString(event, debug);
    }
}
