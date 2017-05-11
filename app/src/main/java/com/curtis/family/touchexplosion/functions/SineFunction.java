package com.curtis.family.touchexplosion.functions;

/**
 * Defines the sine function.
 */
public class SineFunction extends Function1D {
    /** The frequency of the sine function in cyles per *milliseconds*. */
    private float mFrequency;
    /** The amplitude of the sine function. */
    private float mAmplitude;

    public SineFunction(float freq, float amp, long t0) {
        super(t0);
        mFrequency = freq;
        mAmplitude = amp;
    }

    @Override
    public float eval(long globalT) {
        return mAmplitude * (float)Math.sin(mFrequency * 2 * Math.PI * getLocalT(globalT));
    }
}
