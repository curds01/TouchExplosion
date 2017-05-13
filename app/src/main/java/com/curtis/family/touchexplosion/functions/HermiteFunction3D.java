package com.curtis.family.touchexplosion.functions;

import com.curtis.family.touchexplosion.Vector3;

/**
 * The definition of a hermite interpolation in R3.
 *
 * Given x(0), x'(0), x(T), and x'(T), provides a cubic interpolation between those two states.
 * For values of t < 0, or t > T, x(t) and x'(t) is held at the constant value.
 */
public class HermiteFunction3D extends Function3D {
    private Vector3 x0;
    private Vector3 xdot0;
    private Vector3 xT;
    private Vector3 xdotT;
    private long h;

    /** Constructor
     * @param p0        The value of the function at t = 0: f(0).
     * @param v0        The value of the function derivative at t = 0: f'(0).
     * @param pT        The value of the function at t = T: f(T).
     * @param vT        The value of the function derivative at t = T: f'(T).
     * @param t0        The time origin.
     * @param duration  The value of T - t0.
     */
    public HermiteFunction3D(Vector3 p0, Vector3 v0, Vector3 pT, Vector3 vT, long t0, long duration) {
        super(t0);
        x0 = p0.clone();
        xdot0 = v0.clone();
        xT = pT.clone();
        xdotT = vT.clone();
        h = duration;
    }

    /** Reset the function to a new set of values. */
    public void set(Vector3 p0, Vector3 v0, Vector3 pT, Vector3 vT, long t0, long duration) {
        resetTime(t0);
        x0 = p0.clone();
        xdot0 = v0.clone();
        xT = pT.clone();
        xdotT = vT.clone();
        h = duration;
    }

    @Override
    public void eval(long globalT, Vector3 result) {
        float t = getLocalT(globalT) / (float)h;
        if ( t < 0 ) {
            result.set(x0);
            return;
        } else if ( t > 1 ) {
            result.set(xT);
            return;
        }
        float t2 = t * t;
        float t3 = t2 * t;

        float h1 = 2 * t3 - 3 * t2 + 1;
        float h2 = -2 * t3 + 3 * t2;
        float h3 = (t3 - 2 * t2 + t);// * h;
        float h4 = (t3 - t2);// * h;
        float x = interpolate(x0.x, xdot0.x, xT.x, xdotT.x, h1, h2, h3, h4);
        float y = interpolate(x0.y, xdot0.y, xT.y, xdotT.y, h1, h2, h3, h4);
        float z = interpolate(x0.z, xdot0.z, xT.z, xdotT.z, h1, h2, h3, h4);
        result.set(x, y, z);
    }

    /** Compute the derivative of the function at the given time. */
    public void deriv(long globalT, Vector3 result) {
        float t = getLocalT(globalT) / (float)h;
        if ( t < 0 ) {
            result.set(xdot0);
            return;
        } else if ( t > 1 ) {
            result.set(xdotT);
            return;
        }
        float t2 = t * t;

        float h1 = 6 * (t2 - t) ;
        float h2 = 6 * (t - t2);
        float h3 = (3 * t2 - 4 * t + 1);
        float h4 = (3 * t2 - 2 * t);
        float x = interpolate(x0.x, xdot0.x, xT.x, xdotT.x, h1, h2, h3, h4);
        float y = interpolate(x0.y, xdot0.y, xT.y, xdotT.y, h1, h2, h3, h4);
        float z = interpolate(x0.z, xdot0.z, xT.z, xdotT.z, h1, h2, h3, h4);
        result.set(x, y, z);
    }

    private float interpolate(float x0, float xdot0, float xT, float xdotT,
                              float h1, float h2, float h3, float h4) {
        return x0 * h1 + xdot0 * h3 + xT * h2 + xdotT * h4;
    }
}
