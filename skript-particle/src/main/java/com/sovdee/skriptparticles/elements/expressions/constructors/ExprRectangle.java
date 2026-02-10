package com.sovdee.skriptparticles.elements.expressions.constructors;

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
import com.sovdee.shapes.shapes.Rectangle;
import com.sovdee.shapes.shapes.Rectangle.Plane;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.shapes.sampling.SamplingStyle;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.DynamicLocation;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

@Name("Particle Rectangle")
@Description({
        "Creates a rectangle from a length and a width, or from two corners. The length and width must be greater than 0.",
        "When defining a rectangle from two corners, the corners can either be vectors or locations/entities. " +
                "You cannot use both vectors and locations/entities, but you can mix and match locations and entities. " +
                "When using locations, this is a shape that can be drawn without a specific location. It will be drawn between the two given locations.",
        "Note that the rectangle defaults to the xz plane, or parallel to the ground, with x being width and z being length. " +
                "You can change this to the xy or yz plane by using the 'xy' or 'yz'. In all cases, the first axis is length and the second is width."
})
@Examples({
        "set {_shape} to rectangle with length 10 and width 5",
        "set {_shape} to a yz rectangle from vector(0, 0, 0) to vector(10, 10, 10)",
        "draw the shape of a rectangle with length 10 and width 5 at player",
        "",
        "# note that the following does not require a location to be drawn at",
        "draw the shape of a rectangle from player to player's target"
})
@Since("1.0.0")
public class ExprRectangle extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprRectangle.class, Shape.class, ExpressionType.COMBINED,
                "[a[n]] [solid:(solid|filled)] [:xz|:xy|:yz] rectangle (with|of) length %number% and width %number%",
                "[a[n]] [solid:(solid|filled)] [:xz|:xy|:yz] rectangle (from|with corners [at]) %location/entity/vector% (to|and) %location/entity/vector%"
        );
    }

    private Expression<Number> lengthExpr;
    private Expression<Number> widthExpr;
    private SamplingStyle style;
    private Expression<?> corner1Expr;
    private Expression<?> corner2Expr;
    private int matchedPattern;
    private Plane plane;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        style = parseResult.hasTag("solid") ? SamplingStyle.SURFACE : SamplingStyle.OUTLINE;
        this.matchedPattern = matchedPattern;
        if (matchedPattern == 0) {
            lengthExpr = (Expression<Number>) exprs[0];
            widthExpr = (Expression<Number>) exprs[1];
        } else {
            corner1Expr = exprs[0];
            corner2Expr = exprs[1];
        }
        plane = Plane.XZ;
        if (parseResult.hasTag("xy")) plane = Plane.XY;
        else if (parseResult.hasTag("yz")) plane = Plane.YZ;
        return true;
    }

    @Override
    protected @Nullable Shape[] get(Event event) {
        Shape shape;
        if (matchedPattern == 0) {
            if (lengthExpr == null || widthExpr == null) return null;
            Number length = lengthExpr.getSingle(event);
            Number width = widthExpr.getSingle(event);
            if (length == null || width == null) return null;
            length = Math.max(length.doubleValue(), MathUtil.EPSILON);
            width = Math.max(width.doubleValue(), MathUtil.EPSILON);
            shape = new Rectangle(length.doubleValue(), width.doubleValue(), plane);
        } else {
            if (corner1Expr == null || corner2Expr == null) return null;
            Object corner1 = corner1Expr.getSingle(event);
            Object corner2 = corner2Expr.getSingle(event);
            if (corner1 == null || corner2 == null) return null;

            if (corner1 instanceof Vector && corner2 instanceof Vector) {
                shape = new Rectangle(VectorConversion.toJOML((Vector) corner1), VectorConversion.toJOML((Vector) corner2), plane);
            } else if (corner1 instanceof Vector || corner2 instanceof Vector) {
                return null;
            } else {
                DynamicLocation dl1 = DynamicLocation.fromLocationEntity(corner1);
                DynamicLocation dl2 = DynamicLocation.fromLocationEntity(corner2);
                if (dl1 == null || dl2 == null)
                    return null;
                // Use Supplier-based Rectangle for dynamic corners
                shape = new Rectangle(
                        () -> VectorConversion.toJOML(dl1.getLocation().toVector()),
                        () -> VectorConversion.toJOML(dl2.getLocation().toVector()),
                        plane
                );
            }
        }
        shape.getPointSampler().setStyle(style);
        shape.getPointSampler().setDrawContext(new DrawData());
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
        return (style == SamplingStyle.SURFACE ? "solid " : "") +
                switch (plane) {
                    case XZ -> " xz ";
                    case XY -> " xy ";
                    case YZ -> " yz ";
                } +
                switch (matchedPattern) {
                    case 0 ->
                            "rectangle with length " + lengthExpr.toString(event, debug) + " and width " + widthExpr.toString(event, debug);
                    case 1 ->
                            "rectangle from " + corner1Expr.toString(event, debug) + " to " + corner2Expr.toString(event, debug);
                    default -> "";
                };
    }
}
