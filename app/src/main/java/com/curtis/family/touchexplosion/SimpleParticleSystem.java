package com.curtis.family.touchexplosion;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Sean on 5/6/2017.
 */
public class SimpleParticleSystem extends ParticleSystem {

    private final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    public SimpleParticleSystem() {
        super();
        mParticles = new ArrayList<SimpleParticle>();
        mPose = new Vector3();
        mVel = new Vector3();
        scratch = new Vector3();
        mMat = new float[16];
        mParticle = new Particle();
        random = new Random();
        mSync = new Object();
    }

    @Override
    public void initGL() {
//        try {
//            mGlProgram = GLProgram.createProgram(vertexShaderCode, fragmentShaderCode);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mPositionHandle = mGlProgram.addAttributeHandle("vPosition");
//        mColorHandle = mGlProgram.addUniformHandle("vColor");
    }

    @Override
    public void drawGL(long globalT, float[] mMVPMatrix) {
//        Quad.preDraw(mGlProgram);
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
//        Quad.draw();
        synchronized (mSync) {
            for (SimpleParticle particle : mParticles) {
                drawParticle(particle, globalT, mMVPMatrix);
            }
        }
    }

    protected void drawParticle(SimpleParticle particle, long globalT, float[] mMVPMatrix) {
        // Elapsed is a monotonically increasing time.
        float theta = particle.getOrient(globalT);
        particle.getPosition(globalT, scratch);

        Matrix.setIdentityM( mMat, 0 );
        Matrix.translateM( mMat, 0, scratch.x, scratch.y, scratch.z );
        Matrix.rotateM(mMat, 0, theta, 0, 0, 1);
        Matrix.multiplyMM( mMat, 0, mMVPMatrix, 0, mMat, 0 );

        mParticle.draw(mMat);
    }

    @Override
    public void reportTouch(float x, float y, float z, long globalT) {
        mPose.set(x, y, z);
        float maxSpeed = 0.0025f;
        synchronized (mSync) {
            for (int i = 0; i < 5; ++i) {
                float orient = random.nextFloat() * 360;
                mVel.set((random.nextFloat() * 2 - 1) * maxSpeed,
                        (random.nextFloat() * 2 - 1) * maxSpeed,
                        (random.nextFloat() * 2 - 1) * maxSpeed);
                mParticles.add(new SimpleParticle(mPose, mVel, orient, globalT));
            }
        }
    }

    private ArrayList<SimpleParticle> mParticles;
    private Vector3 mPose;
    private Vector3 mVel;
    private Vector3 scratch;
    private float mMat[];
    private Particle mParticle;
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
