package com.sovdee.shapes.modifiers;

/**
 * A modifier that is driven by a {@link NormalizedInput}.
 */
public interface HasInput {

    /**
     * @return the input that drives this modifier
     */
    NormalizedInput getInput();

    /**
     * @param input the input to drive this modifier with
     */
    void setInput(NormalizedInput input);
}
