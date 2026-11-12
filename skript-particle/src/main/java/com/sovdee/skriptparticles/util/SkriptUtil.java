package com.sovdee.skriptparticles.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionSection;
import ch.njol.skript.lang.parser.ParserInstance;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SkriptUtil {

    @SuppressWarnings({"UnstableApiUsage", "unchecked"})
    public static <T> @Nullable T getCurrentSectionExpression(Class<T> exprSectionClass, ParserInstance parser) {
        T expression = null;
        List<ExpressionSection> list = parser.getCurrentSections(ExpressionSection.class);
        for (ExpressionSection candidateSection : list) {
            Expression<?> candidate = candidateSection.getAsExpression();
            if (exprSectionClass.isInstance(candidate))
                expression = (T) candidate; // overwriting is good since last should be only one.
        }
        return expression;
    }
}
