package com.sovdee.shapes.modifiers;

/**
 * The plane in which a {@link TwistModifier} rotates points.
 */
public enum RotationPlane {
    /**
     * rotates around the Y axis, acts on X and Z
     */
    XZ,

    /**
     * rotates around the Z axis, acts on X and Y
     */
    XY,

    /**
     * rotates around the X axis, acts on Y and Z
     */
    YZ
}
