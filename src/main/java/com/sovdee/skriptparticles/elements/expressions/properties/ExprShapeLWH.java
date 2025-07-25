package com.sovdee.skriptparticles.elements.expressions.properties;

import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.expressions.base.SimplePropertyExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.LWHShape;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Shape Length/Width/Height")
@Description({
        "The length, width, or height of a shape. Changing this will change the size of the shape. Resetting or deleting it will set it back to the default value of 1."
})
@Examples({
        "set {_shape}'s shape length to 5",
        "set {_shape}'s shape width to 5",
        "set {_shape}'s shape height to 5",
        "reset {_shape}'s shape length",
        "reset {_shape}'s shape width",
        "add 6 to {_shape}'s shape height"
})
@Since("1.0.0")
public class ExprShapeLWH extends SimplePropertyExpression<LWHShape, Number> {

    static {
        register(ExprShapeLWH.class, Number.class, "shape (:length|:width|:height)", "lwhshapes");
    }

    private int lwh;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        lwh = parseResult.hasTag("length") ? 0 : parseResult.hasTag("width") ? 1 : 2;
        return super.init(exprs, matchedPattern, isDelayed, parseResult);
    }

    @Override
    @Nullable
    public Number convert(LWHShape lwhShape) {
        return switch (lwh) {
            case 0 -> lwhShape.getLength();
            case 1 -> lwhShape.getWidth();
            case 2 -> lwhShape.getHeight();
            default -> null;
        };
    }

    @Override
    @Nullable
    public Class<?>[] acceptChange(ChangeMode mode) {
        return switch (mode) {
            case SET, DELETE, RESET, ADD, REMOVE -> new Class[]{Number.class};
            default -> new Class[0];
        };
    }

    @Override
    public void change(Event event, Object @Nullable [] delta, ChangeMode mode) {
        double value = 1;
        if (delta != null && delta.length != 0)
            value = ((Number) delta[0]).doubleValue();

        switch (mode) {
            case REMOVE:
                value = -value;
            case ADD:
                for (LWHShape shape : getExpr().getArray(event)) {
                    switch (lwh) {
                        case 0 -> shape.setLength(shape.getLength() + value);
                        case 1 -> shape.setWidth(shape.getWidth() + value);
                        case 2 -> shape.setHeight(shape.getHeight() + value);
                    }
                }
                break;
            case DELETE:
            case RESET:
            case SET:
                for (LWHShape shape : getExpr().getArray(event)) {
                    switch (lwh) {
                        case 0 -> shape.setLength(value);
                        case 1 -> shape.setWidth(value);
                        case 2 -> shape.setHeight(value);
                    }
                }
                break;
            case REMOVE_ALL:
            default:
                assert false;
                break;
        }
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    protected String getPropertyName() {
        return switch (lwh) {
            case 0 -> "length";
            case 1 -> "width";
            case 2 -> "height";
            default -> "unknown";
        };
    }

}
