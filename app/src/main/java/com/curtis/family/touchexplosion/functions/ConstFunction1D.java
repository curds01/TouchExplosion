package com.curtis.family.touchexplosion.functions;

/**
 * Definition of a constant scalar function.
 */
public class ConstFunction1D extends Function1D {
    /** The value which is always evaluated. */
    private float value;

    /** Constructor. Sets the value which is always returned. */
    public ConstFunction1D(float value) { super(0); this.value = value; }

    /** Evaluates to the constant value provided at construction at all time values. */
    @Override
    public float eval(long globalT) { return value; }
}
