package com.curtis.family.touchexplosion;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.curtis.family.touchexplosion.functions.BallisticFunction1D;
import com.curtis.family.touchexplosion.functions.ConstFunction1D;
import com.curtis.family.touchexplosion.functions.ConstFunction3D;
import com.curtis.family.touchexplosion.functions.Function1D;
import com.curtis.family.touchexplosion.functions.Function3D;
import com.curtis.family.touchexplosion.functions.HermiteFunction3D;
import com.curtis.family.touchexplosion.functions.LinearFunction1D;
import com.curtis.family.touchexplosion.functions.SineFunction;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Random;

interface Particle {
    void getPosition(long globalT, Vector3 pos);
    float getOrient(long globalT);
    float getScale();
    float[] getColor();
    boolean isAlive(long globalT);
}

/** The spark particle that the tinkerbell particle gives off. It is emitted at a particular
 location and then simply falls due to gravity -- decaying and eventually dying out. */
class SparkParticle implements Particle {
    /** The x-position of the spark. */
    private float mX;
    /** The time varying y-position of the spark. */
    private BallisticFunction1D mY;
    /** The z-position of the spark. */
    private float mZ;
    /** The age function -- it ages from 0 to 1. */
    private Function1D mAgeFunction;
    /** The particle color. */
    private float mColor[] = {1.0f, 1.0f, 0.1f, 1.0f};


    /** Construtor.
     * @param x             The x-position at t0.
     * @param y             The y-position at t0.
     * @param z             The z-position at t0.
     * @param duration      The duration this particle lives (in milliseconds).
     * @param t0            The simulator time stamp of its creation (in milliseconds).
     */
    public SparkParticle(float x, float y, float z, long duration, long t0) {
        mX = x;
        mY = new BallisticFunction1D(y, 0, t0);
        mY.setGravity(-1e-6f);
        mZ = z;
        mAgeFunction = new LinearFunction1D(0, 1.0f / (float)duration, t0);
    }

    @Override
    public void getPosition(long globalT, Vector3 pos) {
        pos.set(mX, mY.eval(globalT), mZ);
    }

    @Override
    public float getOrient(long globalT) {
        return 0;
    }

    public float getScale() { return 0.05f; }

    @Override
    public float[] getColor() {
        return mColor;
    }

    @Override
    public boolean isAlive(long globalT) {
        return mAgeFunction.eval(globalT) < 1;
    }
}

/** The main tinkerbell particle. */
class TinkerBellParticle implements Particle {
    static final String TAG = TinkerBellParticle.class.getSimpleName();

    /** The scalar function that provides a bobbing displacement in the y-direction. */
    private Function1D mBobbing;
    /** The vector function that provides the position of the particle. */
    private HermiteFunction3D mPosition;
    /** The scalar function specifying the orientation of the particle. */
    private Function1D mOrient;
    /** The scale of the sprite from radius 1 to scale radius. */
    private float mRadius;
    /** The particle color. */
    private float mColor[] = {1.0f, 1.0f, 0.1f, 1.0f};
    /** The time stamp of the last emitted spark. */
    private long mLastEmit;
    /** The minimum number of milliseconds that must pass before emitting a new spark. */
    private long mEmitPeriod;
    private static Random sRandom = new Random();

    /** Constructor.
     *  @param globalT      The time stamp at which this is created (sets the origin for the
     *                      functions.
     *  @param emitPeriod   The amount of time (in milliseconds) which must elapse before a new
     *                      spark can be emitted.
     */
    public TinkerBellParticle(long globalT, long emitPeriod) {
        // Frequency: 3Hz ==> 3/1000 cycles / ms.
        mBobbing = new SineFunction(2.0f / 1000.0f, 0.0625f, globalT);
        mPosition = new HermiteFunction3D(Vector3.ZERO, Vector3.ZERO, Vector3.ZERO, Vector3.ZERO, globalT, 1000);
        mOrient = new ConstFunction1D(0);
        mLastEmit = 0;
        mEmitPeriod = emitPeriod;
        mRadius = 0.25f;
    }

    /** Causes tinkerbell to fly to the given position. */
    public void flyTo(float x, float y, float z, long globalT) {
        Vector3 currPos = new Vector3();
        Vector3 currVel = new Vector3();
        Vector3 tgtPos = new Vector3(x, y, z);
        mPosition.eval(globalT, currPos);
        mPosition.deriv(globalT, currVel);
        float dist = currPos.distance(tgtPos);
        long speed_inv = 100; // 200 ms/m.
        long duration = (long)(dist * speed_inv);
        mPosition.set(currPos, currVel, tgtPos, Vector3.ZERO, globalT, duration);
    }

    /** Evaluate the position of the agent at the given time, setting the position into the given
     vector. */
    public void getPosition(long globalT, Vector3 pos) {
        float bob = mBobbing.eval(globalT);
        mPosition.eval(globalT, pos);
        pos.set(pos.x, pos.y + bob, pos.z);
    }

    /** Evaluates the orientation of the agent at the given time. */
    public float getOrient(long globalT) { return mOrient.eval(globalT); }

    public float getScale() { return mRadius; }

    /** Returns the sprite color. */
    public float[] getColor() { return mColor; }

    /** Reports if the sprite is alive. */
    public boolean isAlive(long globalT) { return true; }

    /** Emits a new spark. Will be null if it is too soon to emit. */
    public SparkParticle emit(long globalT) {
        if (globalT - mLastEmit > mEmitPeriod ) {
            // TODO: Emit multiple particles based on the time that has passed and *advance* them.
            //  This would happen automatically if I set their t0 value to the "correct" one
            //  retroactively.
            Vector3 pos = new Vector3();
            mPosition.eval(globalT, pos);
            mLastEmit = globalT;
            float x, y, z;
            synchronized(sRandom) {
                x = (sRandom.nextFloat() - 0.5f) * mRadius + pos.x;
                y = (sRandom.nextFloat() - 0.5f) * mRadius + pos.y;
                z = (sRandom.nextFloat() - 0.5f) * mRadius + pos.z;
            }
            // TODO: Come up with a more interesting positioning
            // TODO: Initial position should be an offset from the tinker bell particle.
            // TODO: Come up with some random lifespan.
            long life = 1500 + (long)(sRandom.nextFloat() * 1000);
            return new SparkParticle(x, y, z, life, globalT);
        }
        return null;
    }
}

/**
 * This system has a single *main* particle that attempts to fly to the contact point which
 * perpetually emits fading particles from itself.
 */
public class TinkerBellSystem extends ParticleSystem {

    static final String TAG = TinkerBellSystem.class.getSimpleName();

    // Consts --------------------------------------------------------------------------------

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
                    "uniform sampler2D uTexture0;" +
                    "varying float alpha;" +
                    "uniform vec4 uColor;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D(uTexture0, vTextureCoord) * uColor;" +
                    "  gl_FragColor.a = min(alpha, gl_FragColor.a);" +
                    "}";

    // Members --------------------------------------------------------------------------------

    private TinkerBellParticle mTinkerBell;
    private ArrayList<SparkParticle> mSparks;
    private IntBuffer mData;
    private int mProgram;
    private int mTinkerTex;
    private int mSparkTex;
    private Object mSync;
    private Vector3 mScratch;
    private float mMat[];



    // Methods ---------------------------------------------------------------------------------
    public TinkerBellSystem(long globalT) {
        super();
        mTinkerBell = new TinkerBellParticle(globalT, 10);
        mSparks = new ArrayList<>();
        mScratch = new Vector3();
        mSync = new Object();
        mMat = new float[16];
    }

    @Override
    public void initGL(Context context) {
        notifyActivityStart();
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

        mTinkerTex = loadTexture(context, R.raw.yin_yang);
        mSparkTex = loadTexture(context, R.raw.target);
    }

    @Override
    public void drawGL(long globalT, float[] mMVPMatrix) {

        GLES20.glEnable( GLES20.GL_BLEND );
        GLES20.glBlendFunc( GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA );

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        int texLoc = GLES20.glGetUniformLocation(mProgram, "uTexture0");
        GLES20.glUniform1i(texLoc, 0);

        GLES20.glActiveTexture( GLES20.GL_TEXTURE0 );
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTinkerTex);

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

        drawParticle(mTinkerBell, globalT, mMVPMatrix);

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mSparkTex);

        synchronized (mSync) {
            SparkParticle newSpark = mTinkerBell.emit(globalT);
            if ( newSpark != null) mSparks.add(newSpark);
            int count = mSparks.size();
            for (int i = 0; i < count; ++i) {
                SparkParticle particle = mSparks.get(i);
                if (!drawParticle(particle, globalT, mMVPMatrix) ) {
                    SparkParticle end = mSparks.remove(count - 1);
                    --count;
                    if ( count > 0 && i != count ) mSparks.set(i, end);
                    --i;
                }
            }
        }
    }

    /** Draws the particle given -- indicates true if it is still alive, false if not. */
    public boolean drawParticle(Particle particle, long globalT, float[] mvpMatrix) {
        // Elapsed is a monotonically increasing time.
        if (!particle.isAlive(globalT)) return false;
        float theta = particle.getOrient(globalT);
        particle.getPosition(globalT, mScratch);
        if (mFrustum.farthestOut(mScratch, 1.0f) > 1.0f) return false;

        Matrix.setIdentityM( mMat, 0 );
        Matrix.translateM( mMat, 0, mScratch.x, mScratch.y, mScratch.z );
        Matrix.rotateM(mMat, 0, theta, 0, 0, 1);
        float scale = particle.getScale();
        Matrix.scaleM(mMat, 0, scale, scale, scale);
        Matrix.multiplyMM( mMat, 0, mvpMatrix, 0, mMat, 0 );

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMat, 0);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "uColor");
        GLES20.glUniform4fv(mColorHandle, 1, particle.getColor(), 0);

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return true;
    }

    @Override
    public void reportTouch(float x, float y, float z, long globalT) {
        mTinkerBell.flyTo(x, y , z, globalT);
    }

    @Override
    public float[] getBgColor() {
        return new float[0];
    }
}
