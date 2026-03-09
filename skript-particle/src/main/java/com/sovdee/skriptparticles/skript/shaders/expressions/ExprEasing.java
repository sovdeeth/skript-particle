package com.sovdee.skriptparticles.skript.shaders.expressions;

import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;
import ch.njol.skript.lang.function.FunctionEvent;
import ch.njol.skript.lang.function.Functions;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.EasingFunction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;

@Name("Easing Function")
@Description({
        "Creates an easing function that reshapes the normalised input of modifiers and gradients.",
        "Built-in curves: linear, quadratic, cubic, and sine — each available as ease in, ease out, or ease in-out.",
        "Power curves accept a custom exponent (e.g. 4 for quartic, 0.5 for square-root).",
        "Custom: provide the name of a Skript function that takes one number and returns one number.",
        "  The function should be a pure math transformation; it runs on whatever thread the modifier runs on.",
})
@Examples({
        "set {_e} to linear easing",
        "set {_e} to ease in quadratic",
        "set {_e} to ease out cubic",
        "set {_e} to ease in-out sine",
        "set {_e} to power ease in with exponent 4",
        "set {_e} to power ease out with exponent 0.5",
        "add taper from 0 to 1 eased with ease in quadratic to modifiers of {_shape}",
        "add gradient from red to blue along y eased with ease in-out sine to modifiers of {_shape}",
        "function myEase(t: number) :: number:",
        "    return {_t} ^ 3",
        "set {_e} to custom easing from function \"myEase\"",
})
@Since("2.0.0")
public class ExprEasing extends SimpleExpression<EasingFunction> {

    // Pattern 0: linear
    // Patterns 1-3: ease in/out/in-out [(quadratic|cubic|sine)]
    //   mark: 0=quadratic (default), 1=quadratic explicit, 2=cubic, 3=sine
    // Pattern 4: power ease (in|out|in-out) with exponent %number%
    //   mark: 0=in, 1=out, 2=in-out
    // Pattern 5: custom Skript function name

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprEasing.class, EasingFunction.class)
                .supplier(ExprEasing::new)
                .addPatterns(
                        "linear [easing]",
                        "(ease in|in ease) [(0:quadratic|1:quadratic|2:cubic|3:sine)] [easing]",
                        "(ease out|out ease) [(0:quadratic|1:quadratic|2:cubic|3:sine)] [easing]",
                        "(ease in[- ]out|in[- ]out ease) [(0:quadratic|1:quadratic|2:cubic|3:sine)] [easing]",
                        "[a] power ease (0:in|1:out|2:in[- ]out) with exponent %number%",
                        "[a] custom easing [from [function]] %string%"
                )
                .build());
    }

    private int pattern;
    private int parseMark;
    private @Nullable Expression<Number> exponent;   // pattern 4
    private @Nullable Expression<String> funcName;   // pattern 5

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, SkriptParser.ParseResult parseResult) {
        this.pattern = matchedPattern;
        this.parseMark = parseResult.mark;
        if (matchedPattern == 4) this.exponent = (Expression<Number>) exprs[0];
        if (matchedPattern == 5) this.funcName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    protected EasingFunction @Nullable [] get(Event event) {
        return new EasingFunction[]{ build(event) };
    }

    private EasingFunction build(Event event) {
        return switch (pattern) {
            case 1 -> curveFromMark(parseMark, EasingFunction.Mode.IN);
            case 2 -> curveFromMark(parseMark, EasingFunction.Mode.OUT);
            case 3 -> curveFromMark(parseMark, EasingFunction.Mode.IN_OUT);
            case 4 -> {
                Number n = exponent != null ? exponent.getSingle(event) : null;
                double exp = (n != null) ? n.doubleValue() : 2.0;
                EasingFunction.Mode mode = switch (parseMark) {
                    case 1 -> EasingFunction.Mode.OUT;
                    case 2 -> EasingFunction.Mode.IN_OUT;
                    default -> EasingFunction.Mode.IN;
                };
                yield new EasingFunction.PowerEasing(exp, mode);
            }
            case 5 -> {
                String name = funcName != null ? funcName.getSingle(event) : null;
                yield name != null ? new SkriptFunctionEasing(name) : EasingFunction.LINEAR;
            }
            default -> EasingFunction.LINEAR;
        };
    }

    /**
     * Selects the right built-in curve for the parse mark (0/1=quad, 2=cubic, 3=sine).
     */
    private static EasingFunction curveFromMark(int mark, EasingFunction.Mode mode) {
        return switch (mark) {
            case 2 -> new EasingFunction.PowerEasing(3.0, mode); // cubic
            case 3 -> new EasingFunction.SineEasing(mode);        // sine
            default -> new EasingFunction.PowerEasing(2.0, mode); // quadratic (0 or 1)
        };
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends EasingFunction> getReturnType() {
        return EasingFunction.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return switch (pattern) {
            case 0 -> "linear easing";
            case 1 -> "ease in";
            case 2 -> "ease out";
            case 3 -> "ease in-out";
            case 4 -> "power ease";
            case 5 -> "custom easing";
            default -> "easing";
        };
    }

    // -------------------------------------------------------------------------
    // Custom Skript-function-backed easing
    // -------------------------------------------------------------------------

    /**
     * Calls a user-defined Skript function {@code f(t: number) :: number} as an easing curve.
     * The function should be pure math — it runs on whatever thread the modifier does.
     */
    private record SkriptFunctionEasing(String name) implements EasingFunction {
        @Override
        @SuppressWarnings({"unchecked", "rawtypes"})
        public double apply(double t) {
            var fn = Functions.getGlobalFunction(name);
            if (fn == null) return t;
            try {
                FunctionEvent<?> fe = new FunctionEvent(null);
                Object[] result = fn.execute(fe, new Object[][]{{t}});
                if (result != null && result.length > 0 && result[0] instanceof Number n)
                    return n.doubleValue();
            } catch (Exception ignored) {}
            return t;
        }

        @Override
        public int easingHash() {
            return name.hashCode();
        }
    }
}
