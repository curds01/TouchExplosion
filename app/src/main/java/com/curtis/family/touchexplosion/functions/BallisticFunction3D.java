package com.curtis.family.touchexplosion.functions;

import com.curtis.family.touchexplosion.Vector3;

/**
 * A function that evaluates a 3D ballistics trajectory:
 *
 * f(t) = -g * t^2 + v0 * t + x0
 */
public class BallisticFunction3D extends Function3D {
    /** A scratch vector for evaluation, to avoid garbage collection. */
    private Vector3 scratch1;

    /** The value of the function at the time origin: f(t0). */
    private Vector3 x0;

    /** The value of the function's first derivative at tim origin: f'(t0). */
    private Vector3 x_dot0;

    /** The gravity constant. Gravity contstant 4.9 m/s^2 converted to 4.9 m/ms^2. */
    private static Vector3 half_gravity = new Vector3(0, -4.9e-6f, 0);

    /** Constructor.
     * @param p0    Value of f(t0).
     * @param v0    Value of f'(t0).
     * @param t0    Time origin.
     */
    public BallisticFunction3D(Vector3 p0, Vector3 v0, long t0) {
        super(t0);
        x0 = p0.clone();
        x_dot0 = v0.clone();
        scratch1 = new Vector3();
    }

    /** @inheritDoc */
    @Override
    public void eval(long globalT, Vector3 result) {
        long t = getLocalT(globalT);
        long t2 = t * t;
        result.set(half_gravity.x *t2 +  x_dot0.x * t + x0.x,
                half_gravity.y * t2 + x_dot0.y * t + x0.y,
                half_gravity.z * t2 + x_dot0.z * t + x0.z);
    }
}
