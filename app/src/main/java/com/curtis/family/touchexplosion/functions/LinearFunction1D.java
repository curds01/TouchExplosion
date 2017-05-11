package com.curtis.family.touchexplosion.functions;

/**
 * Created by Sean on 5/10/2017.
 */
public class LinearFunction1D extends Function1D {
    /** The value of the function at f(t0). */
    private float x0;
    /** The derivative of the function: f'(t0). */
    private float v0;

    /** Constructor
     * @param x0    The value of f(t0).
     * @param v0    The value of f'(t0).
     * @param t0    The value of t0.
     */
    public LinearFunction1D(float x0, float v0, long t0) {
        super(t0);
        this.x0 = x0;
        this.v0 = v0;
    }

    /** @inheritDoc */
    @Override
    public float eval(long globalT) {
        return x0 + v0 * getLocalT(globalT);
    }
}
