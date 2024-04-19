package com.sovdee.skriptparticles.elements.expressions.constructors;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.Literal;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.skriptparticles.shapes.BezierCurve;
import com.sovdee.skriptparticles.shapes.Circle;
import com.sovdee.skriptparticles.shapes.Shape;
import com.sovdee.skriptparticles.util.MathUtil;
import com.sovdee.skriptparticles.util.Point;
import org.bukkit.event.Event;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ExprBezierCurve extends SimpleExpression<BezierCurve> {

    static {
        Skript.registerExpression(ExprBezierCurve.class, BezierCurve.class, ExpressionType.COMBINED,
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
    protected BezierCurve @Nullable [] get(@NonNull Event event) {
        @Nullable Point<?> start = Point.of(this.start.getSingle(event));
        @Nullable Point<?> end = Point.of(this.end.getSingle(event));
        if (start == null || end == null)
            return null;
        List<Point<?>> controlPoints = new ArrayList<>();
        for (Object value : this.controlPoints.getArray(event)) {
            controlPoints.add(Point.of(value));
        }

        return new BezierCurve[]{new BezierCurve(start, end, controlPoints)};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    @NonNull
    public Class<? extends BezierCurve> getReturnType() {
        return BezierCurve.class;
    }

    @Override
    @NonNull
    public String toString(@Nullable Event event, boolean debug) {
        return "bezier curve between start " + start.toString(event, debug) + " and end " + end.toString(event, debug) +
                " using control points " + controlPoints.toString(event, debug);
    }
}
