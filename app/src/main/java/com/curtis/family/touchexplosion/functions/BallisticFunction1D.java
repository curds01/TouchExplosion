package com.curtis.family.touchexplosion.functions;

/**
 * A function that evaluates a 1D ballistics trajectory:
 *
 * f(t) = -g * t^2 + v0 * t + x0
 */
public class BallisticFunction1D extends Function1D {
    /** The value of the function at the time origin: f(t0). */
    private float x0;

    /** The value of the function's first derivative at tim origin: f'(t0). */
    private float x_dot0;

    /** The gravity constant. Gravity constant 4.9 m/s^2 converted to 4.9 m/ms^2. */
    private float mGravity = -4.9e-6f;

    /** Constructor.
     * @param p0    Value of f(t0).
     * @param v0    Value of f'(t0).
     * @param t0    Time origin.
     */
    public BallisticFunction1D(float p0, float v0, long t0) {
        super(t0);
        x0 = p0;
        x_dot0 = v0;
    }

    /** Sets the gravity constant. */
    public void setGravity(float gravity) { mGravity = gravity; }

    /** @inheritDoc */
    @Override
    public float eval(long globalT) {
        long t = getLocalT(globalT);
        long t2 = t * t;
        return mGravity * t2 + x_dot0 * t + + x0;
    }
}
