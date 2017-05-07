package com.curtis.family.touchexplosion;

import java.util.ArrayList;
import java.util.Random;

class Plane {
    final Vector3 n;
    final float D;

    public Plane(Vector3 n, float d) {
        this.n = n.clone();
        this.n.normalize();
        D = d;
    }

    public float signedDistance(Vector3 p) {
        return p.dot(n) + D;
    }

    /** Creates a plane from three points. The points are assumed to be ordered such that
     (p1 - p0) x (p2 - p0) lies in the direction of the normal.  */
    public static Plane makePlane(Vector3 p0, Vector3 p1, Vector3 p2) {
        Vector3 a = p1.sub(p0);
        Vector3 b = p2.sub(p0);
        Vector3 N = a.cross(b);
        N.normalize();
        float d = -N.dot(p0);
        return new Plane(N, d);
    }

    public String toString() {
        return "Plane - N: " + n.toString() + ", d: " + D;
    }
}

/**
 * Definition of a frustum.
 */
public class Frustum {

    Vector3 mEye;
    Vector3 mForward;
    float mNear;
    float mFar;
    float mLeft;
    float mRight;
    float mBottom;
    float mTop;

    ArrayList<Plane> mPlanes;

    Random random;

    /** Defines the view frustum based on an eye position and normal, and then the extents of the
     near plane and the distance to the far plane. Assumes that the forward vector is unit length.
     It assumes the frustum's up vector is <0, 1, 0>.
     */
    public Frustum(Vector3 eye, Vector3 forward,
                   float left, float right,
                   float near, float far,
                   float bottom, float top) {
        mEye = eye.clone();
        mForward = forward.clone();
        mNear = near;
        mFar = far;
        mLeft = left;
        mRight = right;
        mBottom = bottom;
        mTop = top;

        mPlanes = new ArrayList<Plane>();

        // Planes are defined so that negative value are *inside* the frustum.
        // Front plane - Normal is in the negative direction of the forward view.
        mPlanes.add(new Plane(forward.negate(), eye.dot(forward) + near));
        // Back plane - Normal is in the forward direction.
        mPlanes.add(new Plane(forward, -eye.dot(forward) - far));

        // Compute the points on the corners of the front clipping plane.
        Vector3 rightDir = forward.cross(new Vector3(0, 1, 0));
        rightDir.normalize();
        Vector3 upDir = rightDir.cross(forward);
        upDir.normalize();
        Vector3 bottomLeft = eye.add(forward.mul(near)).add(rightDir.mul(left)).add(upDir.mul(bottom));
        Vector3 bottomRight = eye.add(forward.mul(near)).add(rightDir.mul(right)).add(upDir.mul(bottom));
        Vector3 topLeft = eye.add(forward.mul(near)).add(rightDir.mul(left)).add(upDir.mul(top));
        Vector3 topRight = eye.add(forward.mul(near)).add(rightDir.mul(right)).add(upDir.mul(top));

        // bottom plane; eye, bottom left, bottom right
        mPlanes.add(Plane.makePlane(eye, bottomLeft, bottomRight));
        // Top plane: eye, top left, top right
        mPlanes.add(Plane.makePlane(eye, topRight, topLeft));
        // Left plane: eye, top left, bottom left
        mPlanes.add(Plane.makePlane(eye, topLeft, bottomLeft));
        // Right plane: eye, top right, bottom right
        mPlanes.add(Plane.makePlane(eye, bottomRight, topRight));

        random = new Random();
    }

    public float getNear() { return mNear; }
    public float getFar() { return mFar; }
    public float getLeft() { return mLeft; }
    public float getRight() { return mRight; }
    public float getBottom() { return mBottom; }
    public float getTop() { return mTop; }
    public Vector3 getEye() { return mEye; }
    public Vector3 getForward() { return mForward; }
    public Vector3 getTarget() { return mEye.add(mForward); }

    /** Reports the distance outside the frustum the point is. If the frustum is inside, it returns
     a negative value. It will stop testing if it counters a distance >= `threshold`. */
    public float farthestOut(Vector3 pos, float threshold) {
        float farthest = (float)Double.NEGATIVE_INFINITY;
        for (Plane plane : mPlanes) {
            float dist = plane.signedDistance(pos);
            if ( dist > farthest ) {
                farthest = dist;
                if (farthest > threshold) break;
            }
        }
        return farthest;
    }

    /** Defines a point inside the frustum with a random distance to the eye, but passing through
     the line that passes through the canonical point (cX, cY) (where cX, cY \in [-1, +1]. The
     point values are set in the provided vector. */
    public void pointInFrustum(float cX, float cY, Vector3 p) {
        float rand = random.nextFloat();
        float depth = 0.5f;// 0.1f + 0.8f * rand;

        float dist = mNear + ((depth - 1.0f) * mNear + depth * mFar);
        float s = dist / mNear;
        float h = s;
        float width = mRight - mLeft;
        float height = mTop - mBottom;
        float w = s * width / height;
        p.set( cX * w, cY * h, mEye.z - dist);
    }
}
