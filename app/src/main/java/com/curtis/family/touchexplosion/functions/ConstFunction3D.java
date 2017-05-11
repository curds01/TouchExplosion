package com.curtis.family.touchexplosion.functions;

import com.curtis.family.touchexplosion.Vector3;

/**
 * Created by Sean on 5/10/2017.
 */
public class ConstFunction3D extends Function3D {
    /** The value of the function. */
    Vector3 value;

    /** Constructor -- sets the value of the function. */
    public ConstFunction3D(Vector3 val) { super(0); value = val.clone(); }

    /** @inheritDoc */
    @Override
    public void eval(long globalT, Vector3 result) {
        result.set(value);
    }
}
