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
import com.sovdee.shapes.shapes.BezierCurve;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.shapes.DrawData;
import com.sovdee.skriptparticles.util.Point;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.Location;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

@Name("Particle Bezier Curve")
@Description({
        "Creates a bezier curve between the given start and end points, using the given control points to change the curve."
})
@Examples({
        "set {_shape} to a bezier curve from {a} to {b} with control points {c} and {d}",
        "set {_shape} to a curve from player to player's target with control point (location 3 above player)"
})
@Since("1.3.0")
public class ExprBezierCurve extends SimpleExpression<Shape> {

    static {
        Skript.registerExpression(ExprBezierCurve.class, Shape.class, ExpressionType.COMBINED,
            "[a] [bezier] curve from [start] %vector/entity/location% to [end] %vector/entity/location% (with|using) control point[s] %vectors/entities/locations%");
    }

    private Expression<?> start;
    private Expression<?> end;
    private Expression<?> controlPoints;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, @NonNull Kleenean isDelayed, @NonNull ParseResult parseResult) {
        start = exprs[0];
        end = exprs[1];
        controlPoints = exprs[2];
        return true;
    }

    @Override
    protected Shape @Nullable [] get(@NonNull Event event) {
        @Nullable Point<?> startPt = Point.of(this.start.getSingle(event));
        @Nullable Point<?> endPt = Point.of(this.end.getSingle(event));
        if (startPt == null || endPt == null)
            return null;
        List<Point<?>> controlPts = new ArrayList<>();
        for (Object value : this.controlPoints.getArray(event)) {
            controlPts.add(Point.of(value));
        }

        // Use Supplier-based BezierCurve for dynamic control points
        BezierCurve curve = getBezierCurve(startPt, controlPts, endPt);
        return new Shape[]{curve};
    }

    private static @NonNull BezierCurve getBezierCurve(@NonNull Point<?> startPt, List<Point<?>> controlPts, @NonNull Point<?> endPt) {
        BezierCurve curve = new BezierCurve(() -> {
            Location origin = startPt.getLocation();
            List<Vector3d> result = new ArrayList<>();
            result.add(VectorConversion.toJOML(startPt.getVector(origin)));
            for (Point<?> cp : controlPts)
                result.add(VectorConversion.toJOML(cp.getVector(origin)));
            result.add(VectorConversion.toJOML(endPt.getVector(origin)));
            return result;
        });
        curve.setDrawContext(new DrawData());
        return curve;
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @NonNull
    public Class<? extends Shape> getReturnType() {
        return Shape.class;
    }

    @Override
    @NonNull
    public String toString(@Nullable Event event, boolean debug) {
        return "bezier curve between start " + start.toString(event, debug) + " and end " + end.toString(event, debug) +
                " using control points " + controlPoints.toString(event, debug);
    }
}
