package com.curtis.family.touchexplosion;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * The main GL view in which all interactions are handled.
 */
public class MyGLSurfaceView extends GLSurfaceView implements ParticleSystem.ActivityListener {
    private MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context) {
        super(context);
        init(context);
    }

    public MyGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        setEGLContextClientVersion(2);
        mRenderer = new MyGLRenderer(this, context);
        setRenderer(mRenderer);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setZOrderOnTop(true);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        boolean handled = mRenderer.handleTouchEvent( e );
        if ( handled ) {
            requestRender();
        }
        return handled;
    }

    @Override
    public void startActivity() {
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public void stopActivity() {
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}
