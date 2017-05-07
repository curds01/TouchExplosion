package com.curtis.family.touchexplosion;

/**
 * A simple vector in R3.
 */
public class Vector3 {
    public float x;
    public float y;
    public float z;

    public static Vector3 ZERO = new Vector3(0, 0, 0);

    public Vector3() {
        x = y = z = 0;
    }

    public Vector3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 clone() {
        return new Vector3( x, y, z );
    }

    /** Normalizes this vector in place, returning the previous magnitude. */
    public float normalize() {
        float mag = (float) Math.sqrt(x * x + y * y + z * z);
        if ( mag > 1e-6) {
            x /= mag;
            y /= mag;
            z /= mag;
        }
        return mag;
    }

    public void set(Vector3 src) {
        x = src.x;
        y = src.y;
        z = src.z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /** Mathematical operators */

    // mathematical operators which don't create a new vector
    public static void add( Vector3 lhs, Vector3 rhs, Vector3 result ) {
        result.x = lhs.x + rhs.x;
        result.y = lhs.y + rhs.y;
        result.z = lhs.z + rhs.z;
    }

    public static void sub( Vector3 lhs, Vector3 rhs, Vector3 result ) {
        result.x = lhs.x - rhs.x;
        result.y = lhs.y - rhs.y;
        result.z = lhs.z - rhs.z;
    }

    public static void mul( Vector3 lhs, float k, Vector3 result ) {
        result.x = lhs.x * k;
        result.y = lhs.y * k;
        result.z = lhs.z * k;
    }

    public static void div( Vector3 lhs, float k, Vector3 result ) {
        result.x = lhs.x / k;
        result.y = lhs.y / k;
        result.z = lhs.z / k;
    }

    public static void neg( Vector3 u, Vector3 result ) {
        result.x = -u.x;
        result.y = -u.y;
        result.z = -u.z;
    }
}
