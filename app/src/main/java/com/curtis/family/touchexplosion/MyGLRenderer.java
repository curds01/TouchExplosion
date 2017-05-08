package com.curtis.family.touchexplosion;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.view.MotionEvent;

import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * The OpenGL renderer.
 */
public class MyGLRenderer implements GLSurfaceView.Renderer {
    private ParticleSystem mParticleSystem;
    // mMVPMatrix is an abbreviation for "Model View Projection Matrix"
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    // Window dimensions in pixels
    private int _wWidth;
    private int _wHeight;

    private Random random;

    Frustum mFrustum;

    Context mContext;
    MyGLRenderer(Context ctx) {
        mContext = ctx;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.05f, 0.05f, 0.05f, 1.0f);
        mFrustum = new Frustum(new Vector3(0, 0, 3), new Vector3(0, 0, -1),
                -1, 1, 0.5f, 7.0f, -1, 1);
        mParticleSystem = new SimpleParticleSystem();
        mParticleSystem.initGL(mContext);
        random = new Random();
    }

    public void onSurfaceChanged(GL10 unused, int width, int height) {
        _wWidth = width;
        _wHeight = height;

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        mFrustum = new Frustum(new Vector3(0, 0, 3), new Vector3(0, 0, -1),
                -ratio, ratio, 0.5f, 7.0f, -1, 1);

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, mFrustum.getLeft(), mFrustum.getRight(),
                mFrustum.getBottom(), mFrustum.getTop(), mFrustum.getNear(), mFrustum.getFar());

        // Set the camera position (View matrix)
        Vector3 tgt = mFrustum.getTarget();
        Matrix.setLookAtM(mViewMatrix, 0,
                mFrustum.getEye().x, mFrustum.getEye().y, mFrustum.getEye().z,
                tgt.x, tgt.y, tgt.z, 0f, 1.0f, 0.0f);
        mParticleSystem.setFrustum(mFrustum);
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        long now = getGlobalT();
        mParticleSystem.drawGL(now, mMVPMatrix);
    }

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    protected long getGlobalT() {
        // TODO: This needs to take into account when the system is paused and restarted.
        return SystemClock.uptimeMillis();
    }

    synchronized public boolean handleTouchEvent(MotionEvent e) {
        String TAG = "MyGLRenderer";
        if (e.getActionMasked() == MotionEvent.ACTION_DOWN) {
            // First compute an (x, y) value in canonical space [-1, -1] X [1, 1]
            float x = 2 * (e.getX() / (float) _wWidth - 0.5f);
            float y = 2 * ((_wHeight - e.getY()) / (float) _wHeight - 0.5f);

            Vector3 pos = new Vector3();
            mFrustum.pointInFrustum(x, y, pos);
            mParticleSystem.reportTouch(pos.x, pos.y, pos.z, getGlobalT());
            return true;
        }
        return false;
    }
}
