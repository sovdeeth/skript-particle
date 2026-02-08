package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.Cuboid;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.shapes.Shape.Style;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle Cuboid")
@Description({
        "Creates a cuboid from a length, a width, and a height, or from two corners.",
        "The specified length, width, and height must be greater than 0. Length is the x-axis, width is the z-axis, and height is the y-axis.",
        "When defining a cuboid from two corners, the corners can either be vectors or locations/entities. " +
                "You cannot use both vectors and locations/entities, but you can mix and match locations and entities." +
                "When using locations, this is a shape that can be drawn without a specific location. It will be drawn between the two given locations.",
})
@Examples({
        "set {_shape} to a solid cuboid with length 10, width 10, and height 10",
        "set {_shape} to a hollow cuboid from vector(-5, -5, -5) to vector(5, 5, 5)",
        "draw the shape of a cuboid from player to player's target"
})
@Since("1.0.0")
public class ExprCuboid extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprCuboid.class, Shape.class, ExpressionType.COMBINED,
                "[a] [:hollow|:solid] cuboid (with|of) length %number%(,| and) width %number%[,] and height %number%",
                "[a] [:hollow|:solid] cuboid (from|between) %location/entity/vector% (to|and) %location/entity/vector%");

    }

    private Expression<Number> width;
    private Expression<Number> length;
    private Expression<Number> height;
    private Expression<?> corner1;
    private Expression<?> corner2;
    private int matchedPattern = 0;

    private Style style;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        switch (matchedPattern) {
            case 0 -> {
                length = (Expression<Number>) exprs[0];
                width = (Expression<Number>) exprs[1];
                height = (Expression<Number>) exprs[2];
            }
            case 1 -> {
                corner1 = exprs[0];
                corner2 = exprs[1];
            }
        }
        this.matchedPattern = matchedPattern;
        if (parseResult.hasTag("hollow")) {
            style = Style.SURFACE;
        } else if (parseResult.hasTag("solid")) {
            style = Style.FILL;
        } else {
            style = Style.OUTLINE;
        }
        return true;
    }

    @Override
    @Nullable
    protected Shape[] get(Event event) {
        Shape shape;
        // from width, length, height
        if (matchedPattern == 0) {
            if (width == null || length == null || height == null) return null;
            Number width = this.width.getSingle(event);
            Number length = this.length.getSingle(event);
            Number height = this.height.getSingle(event);
            if (width == null || length == null || height == null) return null;
            width = Math.max(width.doubleValue(), MathUtil.EPSILON);
            length = Math.max(length.doubleValue(), MathUtil.EPSILON);
            height = Math.max(height.doubleValue(), MathUtil.EPSILON);
            shape = new Cuboid(length.doubleValue(), width.doubleValue(), height.doubleValue());
            // from location/entity/vector to location/entity/vector
        } else {
            if (corner1 == null || corner2 == null) return null;
            Object corner1 = this.corner1.getSingle(event);
            Object corner2 = this.corner2.getSingle(event);
            if (corner1 == null || corner2 == null) return null;

            // vector check
            if (corner1 instanceof Vector && corner2 instanceof Vector) {
                shape = new Cuboid(VectorConversion.toJOML((Vector) corner1), VectorConversion.toJOML((Vector) corner2));
            } else if (corner1 instanceof Vector || corner2 instanceof Vector) {
                return null;
            } else {
                DynamicLocation dl1 = DynamicLocation.fromLocationEntity(corner1);
                DynamicLocation dl2 = DynamicLocation.fromLocationEntity(corner2);
                if (dl1 == null || dl2 == null)
                    return null;
                // Use Supplier-based Cuboid for dynamic corners
                shape = new Cuboid(
                        () -> VectorConversion.toJOML(dl1.getLocation().toVector()),
                        () -> VectorConversion.toJOML(dl2.getLocation().toVector())
                );
            }
        }
        shape.setStyle(style);
        shape.setDrawContext(new DrawData());
        return new Shape[]{shape};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (style) {
            case FILL -> "filled ";
            case SURFACE -> "hollow ";
            case OUTLINE -> "outlined ";
        } + "cuboid " +
                switch (matchedPattern) {
                    case 0 ->
                            "with width " + width.toString(event, debug) + ", length " + length.toString(event, debug) + ", and height " + height.toString(event, debug);
                    case 1 -> "from " + corner1.toString(event, debug) + " to " + corner2.toString(event, debug);
                    default -> "";
                };

    }
}
