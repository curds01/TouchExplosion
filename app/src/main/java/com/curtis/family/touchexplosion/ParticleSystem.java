package com.curtis.family.touchexplosion;

// Ideas for particle systems
//  1. Simple fireworks.
//      - Each tap of the screen causes a rocket to be fired from the corner to the target position.
//      - when it reaches the target position, it explodes.
//      - the explosion elements fade to transparent while following simple ballistic trajectories.
//  2. Flower explosion
//      - At each tap, a flower explodes.
//      - the flower center follows ballistic trajectory.
//      - Accelerate outwards, but air friction slows them quiclkly.
//      - they then have a more or less constant downward velocity with sinusoidal back and forth
//          pendulum swings.
//  3. Tinkerbell
//      - a single light sits on the screen, emitting sparks that accelerate ballistically fall.
//      - A tap on the screen causes the light to move to the tap location
//          - However, there are acceleration constraints on it, it must move with C1 continuity.
//  4. fireflies
//      - The view frustum is filled with meandering fireflies. They generally fill the volume
//      uniformly
//      - during contact, the flies swarm to the touching point.
//      - This uses an opensteer flock-style behavior
//  5. Bubbles that work their way around obstacles
//  6. Feeding pool
//      - contact sprinkles food on the surface of water
//      - fish come up to eat the food.
//      - food not eaten eventually moves under the surface and then fades away.

import android.content.Context;

/**
 * Represents a particle system.
 */
public abstract class ParticleSystem {
    Frustum mFrustum;

    /** Constructor */
    public ParticleSystem() { mFrustum = null; }

    /** Initializes the OpenGL resources for this system. */
    public abstract void initGL(Context context);

    /** Draws the particles to the open gl context at the given time stamp. */
    public abstract void drawGL(long globalT, float[] mMVPMatrix);

    /** Reports that there has been a touch and that the touch has the given coordinates in the
     world frame. */
    public abstract void reportTouch(float x, float y, float z, long globalT);

    public void setFrustum(Frustum f) { mFrustum = f; }

    public abstract float[] getBgColor();

    // TODO:
    //  1. I need to know if they exit the view frustum such that they cannot return.
    //  2. I need to handle transparency
    //  3. I need to handle a particle that just declares it has died.
}
