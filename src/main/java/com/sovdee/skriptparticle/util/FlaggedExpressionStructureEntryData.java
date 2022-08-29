package com.sovdee.skriptparticle.util;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;
import ch.njol.skript.lang.parser.ParserInstance;
import ch.njol.util.Kleenean;
import org.bukkit.event.Event;
import org.skriptlang.skript.lang.structure.KeyValueStructureEntryData;

import javax.annotation.Nullable;

public class FlaggedExpressionStructureEntryData <T> extends KeyValueStructureEntryData<Expression<? extends T>> {

    private final Class<T> returnType;

    private final Class<? extends Event>[] events;

    private final int parserFlag;

    /**
     * @param returnType The expected return type of the matched expression.
     * @param events Events to be present during parsing and Trigger execution.
     *               This allows the usage of event-restricted syntax and event-values.
     * @see ParserInstance#setCurrentEvents(Class[])
     */
    @SafeVarargs
    public FlaggedExpressionStructureEntryData(
            String key, @Nullable Expression<T> defaultValue, boolean optional,
            Class<T> returnType, int parserFlag, Class<? extends Event>... events)
    {
        super(key, defaultValue, optional);
        this.returnType = returnType;
        this.events = events;
        this.parserFlag = parserFlag;
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    protected Expression<? extends T> getValue(String value) {
        ParserInstance parser = ParserInstance.get();

        Class<? extends Event>[] oldEvents = parser.getCurrentEvents();
        Kleenean oldHasDelayBefore = parser.getHasDelayBefore();

        parser.setCurrentEvents(events);
        parser.setHasDelayBefore(Kleenean.FALSE);

        Expression<? extends T> expression = new SkriptParser(value, parserFlag, ParseContext.DEFAULT).parseExpression(returnType);

        parser.setCurrentEvents(oldEvents);
        parser.setHasDelayBefore(oldHasDelayBefore);

        return expression;
    }

}
