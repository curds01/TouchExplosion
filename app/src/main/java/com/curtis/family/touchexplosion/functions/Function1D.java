package com.curtis.family.touchexplosion.functions;

/**
 * Definition of a one-dimensional (scalar) function of time.
 */
public abstract class Function1D extends FunctionBase {
    /** @inheritDoc */
    public Function1D(long t0) { super(t0); }

    /** Evaluates the function at the *global* wall time. */
    public abstract float eval(long globalT);
}