package com.sovdee.shapes.sampling;

/**
 * Thrown when sampling a shape would produce more points than allowed.
 */
public class PointLimitExceededException extends RuntimeException {

    private final int limit;

    public PointLimitExceededException(int limit) {
        super("Sampling this shape would produce more than " + limit + " points.");
        this.limit = limit;
    }

    /**
     * @return the number of points that was exceeded
     */
    public int getLimit() {
        return limit;
    }
}
