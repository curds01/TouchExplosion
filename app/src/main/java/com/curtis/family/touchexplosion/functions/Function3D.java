package com.curtis.family.touchexplosion.functions;

import com.curtis.family.touchexplosion.Vector3;

/** Defines a function mapping time to three-dimensional vectors (i.e., R -> R^3). */
public abstract class Function3D extends FunctionBase {
    /** Constructor -- initializes the function's time origin. */
    public Function3D(long t0) { super(t0); }

    /** Computes the function value for the given global time value, storing the result in the
     given result vector. */
    public abstract void eval(long globalT, Vector3 result);
}
