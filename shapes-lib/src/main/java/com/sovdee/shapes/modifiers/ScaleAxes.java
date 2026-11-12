package com.sovdee.shapes.modifiers;

/**
 * Specifies which coordinate axes are scaled by a {@link ScalingModifier}.
 */
public enum ScaleAxes {
    /**
     * Scales the X axis only.
     */
    X   (true,  false, false),

    /**
     * Scales the Y axis only.
     */
    Y   (false, true,  false),

    /**
     * Scales the Z axis only.
     */
    Z   (false, false, true),

    /**
     * Scales the X and Y axes.
     */
    XY  (true,  true,  false),

    /**
     * Scales the X and Z axes.
     */
    XZ  (true,  false, true),

    /**
     * Scales the Y and Z axes.
     */
    YZ  (false, true,  true),

    /**
     * Scales all three axes.
     */
    XYZ (true,  true,  true);

    /** Whether the X axis is scaled. */
    public final boolean x;
    /** Whether the Y axis is scaled. */
    public final boolean y;
    /** Whether the Z axis is scaled. */
    public final boolean z;

    ScaleAxes(boolean x, boolean y, boolean z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
