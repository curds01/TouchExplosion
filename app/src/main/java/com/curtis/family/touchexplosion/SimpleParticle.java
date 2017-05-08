package com.curtis.family.touchexplosion;

import android.graphics.Color;

import java.util.Random;

abstract class Function {
    private long t0;
    protected long getLocalT(long globalT) { return globalT - t0; }
    public Function(long t0) { this.t0 = t0; }
}

abstract class Function1 extends Function {
    public Function1(long t0) { super(t0); }
    public abstract float eval(long globalT);
}

class ConstFunction1 extends Function1 {
    public ConstFunction1(float value) { super(0); this.value = value; }
    private float value;
    @Override
    public float eval(long globalT) { return value; }
}

class LinearFunction1 extends Function1 {
    public LinearFunction1(float x0, float v0, long t0) {
        super(t0);
        this.x0 = x0;
        this.v0 = v0;
    }

    @Override
    public float eval(long globalT) {
        return x0 + v0 * getLocalT(globalT);
    }

    private float x0;
    private float v0;
}
/** A function of time that returns vector3 values. */
abstract class Function3 extends Function {
    public Function3(long t0) { super(t0); }

    /** Computes the function value for the given global time value, storing the result in the
     given result vector. */
    public abstract void eval(long globalT, Vector3 result);
}

/** Function that always outputs a constant value. */
class ConstFunction3 extends Function3 {
    public ConstFunction3(Vector3 val) { super(0); value = val.clone(); }
    Vector3 value;
    @Override
    public void eval(long globalT, Vector3 result) {
        result.set(value);
    }
}

/** Function that follows a ballistics trajectory. */
class BallisticFunction3 extends Function3 {
    public BallisticFunction3(Vector3 p0, Vector3 v0, long t0) {
        super(t0);
        x0 = p0.clone();
        x_dot0 = v0.clone();
        scratch1 = new Vector3();
    }
    @Override
    public void eval(long globalT, Vector3 result) {
        long t = getLocalT(globalT);
        long t2 = t * t;
//        // 1/2at^2 + v0 t + x0
        result.set(half_gravity.x *t2 +  x_dot0.x * t + x0.x,
                   half_gravity.y * t2 + x_dot0.y * t + x0.y,
                   half_gravity.z * t2 + x_dot0.z * t + x0.z);
    }
    private Vector3 scratch1;
    private Vector3 x0;
    private Vector3 x_dot0;
    private static Vector3 half_gravity = new Vector3(0, -4.9e-6f, 0);
}

/**
 * A simple particle.
 */
public class SimpleParticle {

    private static Random sRandom = new Random();
    private Function1 mOrientFunc;
    private Function3 mPosFunc;
    float mColor[];

    public SimpleParticle(Vector3 pos, Vector3 vel, float orient, long globalT) {
        float angVel = sRandom.nextFloat() * 0.4f - 0.2f;
        mOrientFunc = new LinearFunction1(orient, angVel, globalT);
        mPosFunc = new BallisticFunction3(pos, vel, globalT);
        mColor = new float[4];
        float h = sRandom.nextFloat() * 360;
        float hsl[] = {h, 1.0f, 1.0f};
        int argb = Color.HSVToColor(hsl);
        mColor[0] = (float)((argb >> 16) & 0xff) / 255.0f;
        mColor[1] = (float)((argb >> 8) & 0xff) / 255.0f;
        mColor[2] = (float)(argb & 0xff) / 255.0f;
        mColor[3] = 1.0f;
//        mPosFunc = new ConstFunction3(pos);
    }

    /** Never moves. */
    public void getPosition(long globalT, Vector3 pos) {
        mPosFunc.eval(globalT, pos);
    }

    /** Never rotates. */
    public float getOrient(long globalT) {
        return mOrientFunc.eval(globalT);
    }

    public float[] getColor() { return mColor; }

    /** This never dies. */
    public boolean isAlive(long globalT) {
        return true;
    }
}
