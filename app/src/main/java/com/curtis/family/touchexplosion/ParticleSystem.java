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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * Represents a particle system.
 */
public abstract class ParticleSystem {
    /** Definition of listener notified when the particle system stops and starts simulating
     particles, giving other entities a chance to respond. */
    public  interface ActivityListener {
        /** Called when the particle system starts simulating particles -- from previously having
         no active particles. */
        void startActivity();
        /** Called when the particle system no longer has active particles. */
        void stopActivity();
    }
    protected Frustum mFrustum;
    private ArrayList<ActivityListener> mListeners;

    /** Constructor */
    public ParticleSystem() {
        mFrustum = null;
        mListeners = new ArrayList<>();
    }

    /** Adds an activity listener to the system. */
    public void addListener(ActivityListener listener) {
        mListeners.add(listener);
    }

    /** Removes an activity listener from the system. */
    public void removeListener(ActivityListener listener) {
        mListeners.remove(listener);
    }

    /** Initializes the OpenGL resources for this system. */
    public abstract void initGL(Context context);

    /** Draws the particles to the open gl context at the given time stamp. */
    public abstract void drawGL(long globalT, float[] mMVPMatrix);

    /** Reports that there has been a touch on the screen. Provides the (x, y) coordinates of the
     touch in *canonical* coordinates (i.e., both lie in the range [-1, 1]. The particle system
     can select a point in the world frame from the frustum. */
    public abstract void reportTouch(float x, float y, Frustum frustum, long globalT);

    /** Sets the active frustum for the system. It represents the visible volume. */
    public void setFrustum(Frustum f) { mFrustum = f; }

    // TODO: Consider deprecating this.
    /** Set the background color for the system. */
    public abstract float[] getBgColor();

    /** Derived classes can call this method when they go from having no active particles to having
     some. */
    protected void notifyActivityStart() {
        for (ActivityListener l : mListeners) {
            l.startActivity();
        }
    }

    /** Derived classes can invoke this when they no longer have active particles. */
    protected void notifyActivityStop() {
        for (ActivityListener l : mListeners) {
            l.stopActivity();
        }
    }

    /** Loads a resource image as an OpenGL texture. */
    protected int loadTexture(Context context, int resource) {
        // Initialize texture
        InputStream is = context.getResources().openRawResource( resource );
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeStream(is);
        } finally {
            try {
                is.close();
            } catch(IOException e) {
                Log.e("GLTextures", e.getMessage());
                // Ignore.
            }
        }
        String TAG = "SimpleParticleSystem";

        int[] tmp_tex = new int[ 1 ];
        GLES20.glGenTextures( 1, tmp_tex, 0 );
        Utils.checkGlError( TAG, "glGenTextures" );
        int texId = tmp_tex[0];
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //TODO: control whether I want to wrap or clamp the image
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
        Utils.checkGlError( TAG, "ERROR CHECK - 2" );
        bitmap.recycle();
        return texId;
    }

    // TODO:
    //  2. I need to handle transparency
    //  3. I need to handle a particle that just declares it has died.
}
