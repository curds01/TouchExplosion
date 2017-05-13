package com.curtis.family.touchexplosion.functions;

/**
 * Base definition of all function classes. The functions all have a simple time domain.
 */
abstract class FunctionBase {
    /** The timestamp (in milliseconds) which serves as the origin. */
    private long t0;

    /** Given the current clock time, reports the time elapsed from the origin (in milliseconds). */
    protected long getLocalT(long globalT) { return globalT - t0; }

    /** Constructor -- sets the origin of the domain to the given value (in milliseconds). */
    public FunctionBase(long t0) { this.t0 = t0; }

    /** Resets the origin time value. */
    public void resetTime(long t0) { this.t0 = t0; }
}