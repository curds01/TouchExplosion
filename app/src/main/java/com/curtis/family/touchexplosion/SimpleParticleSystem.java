package com.curtis.family.touchexplosion;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Random;

/**
 * Simple particle system; an explosion of sprites from the tap location that fall to the ground.
 */
public class SimpleParticleSystem extends ParticleSystem {

    private final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;

    static final int COORDS_PER_VERTEX = 3;
    static float squareCoords[] = {
            -0.5f,  0.5f, 0.0f,   // top left
            -0.5f, -0.5f, 0.0f,   // bottom left
            0.5f, 0.5f, 0.0f,   // bottom right
            0.5f, -0.5f, 0.0f }; // top right

    private final int vertexCount = squareCoords.length / COORDS_PER_VERTEX;
    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = { 1.0f, 1.0f, 1.0f, 1.0f };

    private short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    private int mProgram;

    public SimpleParticleSystem() {
        super();
        mParticles = new ArrayList<SimpleParticle>();
        mPose = new Vector3();
        mVel = new Vector3();
        scratch = new Vector3();
        mMat = new float[16];
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
        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                squareCoords.length * 4);
        bb.order(ByteOrder.nativeOrder());
        vertexBuffer = bb.asFloatBuffer();
        vertexBuffer.put(squareCoords);
        vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        drawListBuffer = dlb.asShortBuffer();
        drawListBuffer.put(drawOrder);
        drawListBuffer.position(0);

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
    }

    @Override
    public void drawGL(long globalT, float[] mMVPMatrix) {
//        Quad.preDraw(mGlProgram);
//        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
//        Quad.draw();

        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);

        // get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        synchronized (mSync) {
            for (SimpleParticle particle : mParticles) {
                drawParticle(particle, globalT, mMVPMatrix);
            }
        }

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    protected void drawParticle(SimpleParticle particle, long globalT, float[] mvpMatrix) {
        // Elapsed is a monotonically increasing time.
        float theta = particle.getOrient(globalT);
        particle.getPosition(globalT, scratch);

        Matrix.setIdentityM( mMat, 0 );
        Matrix.translateM( mMat, 0, scratch.x, scratch.y, scratch.z );
        Matrix.rotateM(mMat, 0, theta, 0, 0, 1);
        Matrix.multiplyMM( mMat, 0, mvpMatrix, 0, mMat, 0 );

        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMat, 0);

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, vertexCount);
    }

    @Override
    public void reportTouch(float x, float y, float z, long globalT) {
        mPose.set(x, y, z);
        float maxSpeed = 0.0025f;
        synchronized (mSync) {
            for (int i = 0; i < 10; ++i) {
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
