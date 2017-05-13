package com.curtis.family.touchexplosion;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Simple particle system; an explosion of sprites from the tap location that fall to the ground.
 */
public class SimpleParticleSystem extends ParticleSystem {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "uniform float uFarLimit;" +
                    "attribute vec4 aPosition;" +
                    "attribute vec2 aTextureCoord;" +
                    "varying vec2 vTextureCoord;" +
                    "varying float alpha;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * aPosition;" +
                    "  float dist = 0.125f + 1.0f - (gl_Position.z / uFarLimit) * 0.825f;" +
                    "  alpha = sqrt(dist);" +
                    "  vTextureCoord = aTextureCoord;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "varying vec2 vTextureCoord;" +
                    "uniform sampler2D iconTexture;" +
                    "uniform sampler2D colorTexture;" +
                    "varying float alpha;" +
                    "uniform vec4 uColor;" +
                    "uniform vec4 uCenterColor;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(iconTexture, vTextureCoord);" +
                    "  float color_mask = texture2D(colorTexture, vTextureCoord).x;" +
                    "  gl_FragColor.xyz *= color_mask < 0.75f ? uCenterColor.xyz : uColor.xyz;" +
                    "  gl_FragColor.a = min(alpha, gl_FragColor.a);" +
                    "}";

    private IntBuffer mData;

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private int mProgram;
    private int mFlowerTex;
    private int mColorTex;
    float sBgColor[] = {0.05f, 0.05f, 0.05f};

    /** The time stamp of the last time particles were spawned. In milliseconds.*/
    long mLastSpawn;
    /** The minimum duration which must elapse between spawns (in milliseconds). */
    long mSpawnPeriod;
    /** The number of particles to spawn at a time. */
    int mSpawnCount;
    /** The scale factor on the sprites. */
    float mScale;

    public SimpleParticleSystem() {
        super();
        mParticles = new ArrayList<>();
        mPose = new Vector3();
        mVel = new Vector3();
        scratch = new Vector3();
        mMat = new float[16];
        random = new Random();
        mSync = new Object();
        mLastSpawn = -1;
        mSpawnPeriod = 100;  // 10 spawns / second (100 ms between spawns).
        mSpawnCount = 10;
        mScale = 0.75f;
    }

    @Override
    public void initGL(Context context) {
        int one = 0x10000;
        // DATA: v.x, v.y, v.z, u, v
        // FIXED DATA
        int interleaved[] = {
                -one, -one, 0,0, one,
                one, -one, 0, one, one,
                -one, one, 0, 0, 0,
                one, one, 0, one, 0
        };
        mData = Utils.buildBuffer(interleaved);

        int vertexShader = MyGLRenderer.loadShader(GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = MyGLRenderer.loadShader(GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, vertexShader);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, fragmentShader);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);

        mFlowerTex = loadTexture(context, R.raw.flower);
        mColorTex = loadTexture(context, R.raw.flower_mask);

    }

    @Override
    public void drawGL(long globalT, float[] mMVPMatrix) {
        GLES20.glEnable( GLES20.GL_BLEND );
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        int texLoc = GLES20.glGetUniformLocation(mProgram, "iconTexture");
        GLES20.glUniform1i(texLoc, 0);
        texLoc = GLES20.glGetUniformLocation(mProgram, "colorTexture");
        GLES20.glUniform1i(texLoc, 1);

        GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFlowerTex);

        GLES20.glActiveTexture( GLES20.GL_TEXTURE1 );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mColorTex);

        int stride = 20; // 4 bytes-per int * 5 ints.
        mData.position(0);
        int handle =  GLES20.glGetAttribLocation( mProgram, "aPosition" );
        GLES20.glVertexAttribPointer(handle, 3, GLES20.GL_FIXED, false, stride, mData);
        GLES20.glEnableVertexAttribArray( handle );

        mData.position(3); handle =  GLES20.glGetAttribLocation( mProgram, "aTextureCoord" );
        GLES20.glVertexAttribPointer(handle, 2, GLES20.GL_FIXED, false, stride, mData);
        GLES20.glEnableVertexAttribArray( handle );
        GLES20.glEnableVertexAttribArray( handle );

        int mFarLimitHandle = GLES20.glGetUniformLocation(mProgram, "uFarLimit");
        GLES20.glUniform1f(mFarLimitHandle, 7);

        int originalSize = mParticles.size();
        synchronized (mSync) {
            int count = mParticles.size();
            for (int i = 0; i < count; ++i) {
                SimpleParticle particle = mParticles.get(i);
                if (!drawParticle(particle, globalT, mMVPMatrix) ) {
                    SimpleParticle end = mParticles.remove(count - 1);
                    --count;
                    if ( count > 0 && i != count ) mParticles.set(i, end);
                    --i;
                }
            }
        }
        if (originalSize > 0 && mParticles.size() == 0) notifyActivityStop();
    }

    /** Draws the particle given -- indicates true if it is still alive, false if not. */
    protected boolean drawParticle(SimpleParticle particle, long globalT, float[] mvpMatrix) {
        // Elapsed is a monotonically increasing time.
        float theta = particle.getOrient(globalT);
        particle.getPosition(globalT, scratch);
        if (mFrustum.farthestOut(scratch, 1.0f) > 1.0f) return false;

        Matrix.setIdentityM( mMat, 0 );
        Matrix.translateM( mMat, 0, scratch.x, scratch.y, scratch.z );
        Matrix.rotateM(mMat, 0, theta, 0, 0, 1);
        Matrix.scaleM(mMat, 0, mScale, mScale, mScale);
        Matrix.multiplyMM( mMat, 0, mvpMatrix, 0, mMat, 0 );

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMat, 0);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        GLES20.glUniform4fv(mColorHandle, 1, particle.getColor(), 0);
        int mCenterHandle = GLES20.glGetUniformLocation(mProgram, "uCenterColor");
        GLES20.glUniform4fv(mCenterHandle, 1, particle.getColor2(), 0);

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return true;
    }

    @Override
    public void reportTouch(float x, float y, Frustum frustum, long globalT) {

        if (mLastSpawn < 0 || mLastSpawn + mSpawnPeriod < globalT ) {
            if (mParticles.size() == 0) notifyActivityStart();
            mFrustum.pointInFrustum(x, y, mPose);
            float maxSpeed = 0.0075f;
            synchronized (mSync) {
                for (int i = 0; i < mSpawnCount; ++i) {
                    float orient = random.nextFloat() * 360;
                    mVel.set((random.nextFloat() * 2 - 1) * maxSpeed,
                            (random.nextFloat() * 2 - 1) * maxSpeed,
                            (random.nextFloat() * 2 - 1) * maxSpeed * 0.25f);
                    mParticles.add(new SimpleParticle(mPose, mVel, orient, globalT));
                }
            }
            mLastSpawn = globalT;
        }
    }

    @Override
    public float[] getBgColor() { return sBgColor; }

    private ArrayList<SimpleParticle> mParticles;
    private Vector3 mPose;
    private Vector3 mVel;
    private Vector3 scratch;
    private float mMat[];
    private Random random;
    private Object mSync;

//    // Program
//    private GLProgram mGlProgram = null;
//    private int mPositionHandle = 0;
//    private int mColorHandle = 0;
//    // No textures
//    // Set color with red, green, blue and alpha (opacity) values
//    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };

}
