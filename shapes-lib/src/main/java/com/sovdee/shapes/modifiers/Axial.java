package com.sovdee.shapes.modifiers;

/**
 * Something that has a {@link SampleAxis}
 */
public interface Axial {

    /**
     * @return the axis along which taper is measured and applied
     */
    SampleAxis getAxis();

    /**
     * @param axis the axis along which taper is measured and applied
     */
    void setAxis(SampleAxis axis);

}
