package com.sovdee.shapes.modifiers;

/**
 * Something that has an {@link EasingFunction}.
 */
public interface Easable {

    /**
     * @return the easing function used to distribute rotation along the axis
     */
    EasingFunction getEasing();

    /**
     * @param easing the easing function used to distribute rotation along the axis
     */
    void setEasing(EasingFunction easing);

}
