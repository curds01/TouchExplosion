package com.curtis.family.touchexplosion;

import android.graphics.Color;

import com.curtis.family.touchexplosion.functions.BallisticFunction3D;
import com.curtis.family.touchexplosion.functions.Function1D;
import com.curtis.family.touchexplosion.functions.Function3D;
import com.curtis.family.touchexplosion.functions.LinearFunction1D;

import java.util.Random;

/**
 * A simple particle.
 */
public class SimpleParticle {

    private static Random sRandom = new Random();
    private Function1D mOrientFunc;
    private Function3D mPosFunc;
    float mColor[];
    float mColor2[];

    public SimpleParticle(Vector3 pos, Vector3 vel, float orient, long globalT) {
        float angVel = sRandom.nextFloat() * 0.4f - 0.2f;
        mOrientFunc = new LinearFunction1D(orient, angVel, globalT);
        mPosFunc = new BallisticFunction3D(pos, vel, globalT);

        mColor = new float[4];
        float h = sRandom.nextFloat() * 360;
        float hsl[] = {h, 1.0f, 1.0f};
        int argb = Color.HSVToColor(hsl);
        mColor[0] = (float)((argb >> 16) & 0xff) / 255.0f;
        mColor[1] = (float)((argb >> 8) & 0xff) / 255.0f;
        mColor[2] = (float)(argb & 0xff) / 255.0f;
        mColor[3] = 1.0f;

        h += 180;
        while (h > 360) { h -= 360; }
        hsl[0] = h;
        argb = Color.HSVToColor(hsl);
        mColor2 = new float[4];
        mColor2[0] = (float)((argb >> 16) & 0xff) / 255.0f;
        mColor2[1] = (float)((argb >> 8) & 0xff) / 255.0f;
        mColor2[2] = (float)(argb & 0xff) / 255.0f;
        mColor2[3] = 1.0f;
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

    public float[] getColor2() { return mColor2; }

    /** This never dies. */
    public boolean isAlive(long globalT) {
        return true;
    }
}
