package com.sovdee.skriptparticle.util;

import ch.njol.skript.ScriptLoader;
import ch.njol.skript.config.Node;
import ch.njol.skript.config.SectionNode;
import ch.njol.skript.config.validate.SectionValidator;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ParseContext;
import ch.njol.skript.lang.SkriptParser;

import java.util.LinkedHashMap;
import java.util.Map;

// Authored by Jay (Ankoki) in SkJade

// Validator which automatically retrieves expressions based on the entry types.
// Everytime validate is called, it wipes the stored section node.
// Be sure to cache everything you need in your init if you are reusing the same validator throughout.
public class SectionValidatorPlus extends SectionValidator {

    private final Map<String, Class<?>> definedEntries = new LinkedHashMap<>();
    private SectionNode current = null;

    /**
     * Adds an entry which has a certain type. These will be returned when {@link SectionValidatorPlus#getEntries()} is called.
     * @param name name of the entry.
     * @param type the type of the entry.
     * @param optional whether the entry is optional.
     */
    public SectionValidatorPlus addEntry(String name, Class<?> type, boolean optional) {
        super.addEntry(name, optional);
        this.definedEntries.put(name, type);
        return this;
    }

    @Override
    public SectionValidatorPlus addEntry(String name, boolean optional) {
        super.addEntry(name, optional);
        return this;
    }

    @Override
    public boolean validate(Node node) {
        boolean b = super.validate(node);
        current = b ? (SectionNode) node : null;
        return b;
    }

    /**
     * MUST call {@link SectionValidator#validate(Node)} before using this method.
     * </p>
     * Returns all entries as parsed as their expressions in the order they are added.
     * Optional entries may be null, so make sure to check for that.
     * @return the entries of the given node.
     */
    public Expression<?>[] getEntries() {
        final Expression<?>[] entries = new Expression<?>[definedEntries.size() - 1];
        synchronized (this) {
            int i = 0;
            for (Map.Entry<String, Class<?>> entry : definedEntries.entrySet()) {
                final String unparsed = current.getValue(entry.getKey());
                if (unparsed == null) entries[i] = null;
                else entries[i] = new SkriptParser(ScriptLoader.replaceOptions(unparsed), 3, ParseContext.DEFAULT)
                        .parseExpression(entry.getValue());
                i++;
            }
        } return entries;
    }

    /**
     * MUST call {@link SectionValidator#validate(Node)} before using this method.
     * </p>
     * Returns the entry with the name if it has a registered type.
     * Optional entries may be null, so make sure to check for that.
     * @param name the name of the entry.
     * @return the expression of the given name.
     */
    public Expression<?> getEntry(String name) {
        final String unparsed = current.getValue(name);
        final Class<?> clazz = definedEntries.get(name);
        if (clazz == null) throw new IllegalArgumentException("No defined class for entry: '" + name + "'");
        if (unparsed == null) return null;
        return new SkriptParser(ScriptLoader.replaceOptions(unparsed), 3, ParseContext.DEFAULT)
                .parseExpression(clazz);
    }
}