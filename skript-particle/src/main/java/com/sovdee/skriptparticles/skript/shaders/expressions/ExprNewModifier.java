package com.sovdee.skriptparticles.skript.shaders.expressions;

import ch.njol.skript.Skript;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Example;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.lang.DefaultExpression;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.sovdee.shapes.modifiers.PointModifier;
import com.sovdee.skriptparticles.skript.shaders.constructors.ModifierConstructorExpression;
import com.sovdee.skriptparticles.util.SkriptUtil;
import org.bukkit.event.Event;
import org.jetbrains.annotations.Nullable;
import org.skriptlang.skript.registration.SyntaxInfo;
import org.skriptlang.skript.registration.SyntaxRegistry;

/**
 * The modifier being created by the enclosing modifier constructor section. Also the default point modifier,
 * so modifier properties can leave out {@code of ...} inside such a section.
 */
@Name("New Modifier")
@Description({
        "The modifier being created, inside the section of a modifier constructor (scale, twist, wave, gradient or motion).",
        "Modifier properties can leave out which modifier they are about inside such a section, and will use this one."
})
@Example("""
        set {_wave} to a wave with amplitude 0.3 and frequency 2 along the y axis:
            set wave phase of the new modifier to 90
            set modifier easing to ease in-out sine
        """)
@Since("2.0.0")
public class ExprNewModifier extends SimpleExpression<PointModifier> implements DefaultExpression<PointModifier> {

    public static void register(SyntaxRegistry registry) {
        registry.register(SyntaxRegistry.EXPRESSION, SyntaxInfo.Expression.builder(ExprNewModifier.class, PointModifier.class)
                .supplier(ExprNewModifier::new)
                .addPattern("[the] (new|created|current) [point] modifier")
                .build());
    }

    private static boolean inSection() {
        return SkriptUtil.getCurrentSectionExpression(ModifierConstructorExpression.class, ParserInstance.get()) != null;
    }

    @Override
    public boolean init(Expression<?>[] expressions, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        if (!inSection()) {
            Skript.error("'the new modifier' can only be used inside the section of a modifier constructor, such as 'a wave with amplitude 1 and frequency 2:'");
            return false;
        }
        return true;
    }

    @Override
    public boolean init() {
        // used as the default point modifier: silently unavailable outside a constructor section
        return inSection();
    }

    @Override
    public boolean isDefault() {
        return true;
    }

    @Override
    protected PointModifier @Nullable [] get(Event event) {
        PointModifier<?> modifier = ModifierConstructorExpression.current();
        return modifier == null ? new PointModifier[0] : new PointModifier[]{modifier};
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends PointModifier> getReturnType() {
        return PointModifier.class;
    }

    @Override
    public String toString(@Nullable Event event, boolean debug) {
        return "the new modifier";
    }
}
