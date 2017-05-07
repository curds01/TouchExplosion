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
/**
 * Represents a particle system.
 */
public abstract class ParticleSystem {
    /** Constructor */
    public ParticleSystem() {}

    /** Initializes the OpenGL resources for this system. */
    public abstract void initGL();

    /** Draws the particles to the open gl context at the given time stamp. */
    public abstract void drawGL(long globalT, float[] mMVPMatrix);

    /** Reports that there has been a touch and that the touch has the given coordinates in the
     world frame. */
    public abstract void reportTouch(float x, float y, float z, long globalT);

    // TODO:
    //  1. I need to know if they exit the view frustum such that they cannot return.
    //  2. I need to handle transparency
    //  3. I need to handle a particle that just declares it has died.
}
