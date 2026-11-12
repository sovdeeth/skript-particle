package com.sovdee.skriptparticles.skript.shapes.constructors;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.shapes.BezierCurve;
import com.sovdee.shapes.shapes.Shape;
import com.sovdee.skriptparticles.rendering.DrawData;
import com.sovdee.skriptparticles.util.Point;
import com.sovdee.skriptparticles.util.VectorConversion;
import org.bukkit.Location;
import org.bukkit.util.Vector;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Name("Particle Bezier Curve")
@Description("""
    Creates a bezier curve between the given start and end points, using the given control points to change the curve.
    """)
@Example("set {_shape} to a bezier curve from {a} to {b} with control points {c} and {d}")
@Example("set {_shape} to a curve from player to player's target with control point (location 3 above player)")
@Since("1.3.0")
public class ExprBezierCurve extends ShapeConstructorExpression {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprBezierCurve.class, Shape.class)
                .supplier(ExprBezierCurve::new)
                .addPatterns("[a] [bezier] curve from [start] %vector/entity/location% to [end] %vector/entity/location% (with|using) control point[s] %vectors/entities/locations%")
                .build());
    }

    private Expression<?> start;
    private Expression<?> end;
    private Expression<?> controlPoints;

    @Override
    public boolean initialize(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        start = exprs[0];
        end = exprs[1];
        controlPoints = exprs[2];
        return true;
    }

    @Override
    protected @Nullable List<Shape> getShapes(Event event) {
        @Nullable Point<?> startPt = Point.of(this.start.getSingle(event));
        @Nullable Point<?> endPt = Point.of(this.end.getSingle(event));
        if (startPt == null || endPt == null)
            return null;
        List<Point<?>> controlPts = new ArrayList<>();
        for (Object value : this.controlPoints.getArray(event)) {
            controlPts.add(Point.of(value));
        }
        BezierCurve curve = getBezierCurve(startPt, controlPts, endPt);
        if (curve == null)
            return null;
        return List.of(curve);
    }

    private static @Nullable BezierCurve getBezierCurve(@NotNull Point<?> startPt, List<Point<?>> controlPts, @NotNull Point<?> endPt) {
        List<Point<?>> others = new ArrayList<>(controlPts);
        others.add(endPt);

        // The curve is anchored at its start. A vector start has no place in the world,
        // so the other points cannot be world positions then.
        boolean relative = startPt.getType() == Vector.class;
        boolean dynamic = startPt.isDynamic();
        for (Point<?> point : others) {
            if (point == null || (relative && point.getType() != Vector.class))
                return null;
            dynamic |= point.isDynamic();
        }

        // every point as an offset from the start
        Supplier<List<Vector3d>> points = () -> {
            Location origin = startPt.getLocation();
            List<Vector3d> result = new ArrayList<>(others.size() + 1);
            result.add(VectorConversion.toJOML(startPt.getVector(origin)));
            for (Point<?> point : others)
                result.add(VectorConversion.toJOML(point.getVector(origin)));
            return result;
        };

        // only follow the points if any of them can actually move
        BezierCurve curve = dynamic ? new BezierCurve(points) : new BezierCurve(points.get());
        curve.getPointSampler().setDrawContext(new DrawData());
        if (!relative)
            DrawData.of(curve).setLocation(startPt.getDynamicLocation());
        return curve;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "bezier curve between start " + start.toString(event, debug) + " and end " + end.toString(event, debug) +
                " using control points " + controlPoints.toString(event, debug);
    }
}
