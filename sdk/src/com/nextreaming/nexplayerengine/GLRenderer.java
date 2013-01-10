package com.nextreaming.nexplayerengine;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.opengles.GL10;

import com.nextreaming.nexplayerengine.NexPlayer.IListener;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.Log;

/**
 * \brief   This is the view for displaying NexPlayer&trade;&nbsp;video output when using 
 *          the OpenGL renderer.
 *
 * For details see the \ref glrenderer section of the
 * general NexPlayer&trade;&nbsp;Engine documentation.
 */
public class GLRenderer extends GLSurfaceView{
    private static final String LOG_TAG = "NexPlayerAPP_Player_GLClass";
    private NexPlayer mNexPlayer = null;
    private Renderer render = null;
    
    /**
     * \brief   This causes the next rendering pass to clear the video image.
     *
     * If this is set to \c true, the next rendering pass will clear the video
     * image instead of displaying the most recently rendered frame.  After the
     * next rendering pass, this is automatically reset to \c false.
     *
     * The typical method of erasing the current video image is to set this
     * to \c true and then request a rendering pass, as follows:
     *
     * \code
     * glRenderer.mClearScreen = true;
     * glRenderer.requestRender();
     * \endcode
     *
     */
    public boolean mClearScreen = false;
    
    private static IListener    mListener;
    
    /**
     * \brief   The sole constructor.
     *
     * \param   context     The Android context object (if the caller is an activity,
     *                      passing \c this is normal).
     *
     * \param   np          The NexPlayer&trade;&nbsp;object that will provide the video frames
     *                      to display in this view.
     *
     * \param   listener    An object that implements GLRenderer.IListener, for receiving
     *                      notifications about changes to the size of the surface.
     *
     * \param   colorDepth  Video output image color depth (this must be the same value
     *                      passed when initializing NexPlayer&trade;).
     *                          - <b>1</b> : RGBA_8888
     *                          - <b>4</b> : RGB_565
     *                         
     */
    public GLRenderer(Context context, NexPlayer np, IListener listener, int colorDepth) {
        super(context);
        mNexPlayer = np;
        setEGLContextFactory(new ContextFactory());

        /* We need to choose an EGLConfig that matches the format of
         * our surface exactly. This is going to be done in our
         * custom config chooser. See ConfigChooser class definition
         * below.
         */
        Log.w(LOG_TAG,"before setEGLConfigChooser " + colorDepth);
        
        if(colorDepth == 1)//32
        {
            this.getHolder().setFormat(PixelFormat.RGBA_8888);
            setEGLConfigChooser(new ConfigChooser(8, 8, 8, 8, 0, 0));
        }
        else//4. 565
        {
            this.getHolder().setFormat(PixelFormat.RGB_565);
            setEGLConfigChooser(new ConfigChooser(5, 6, 5, 0, 0, 0));
        }
        
        
        /* Set the renderer responsible for frame rendering */ 
        render = new Renderer();
        setRenderer(render);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
        Log.w(LOG_TAG,"before setEGLConfigChooser done " + colorDepth);
        mListener = listener;        
    }
    
    private class Renderer implements GLSurfaceView.Renderer {

        @Override
        public void onDrawFrame(GL10 gl) {
            if(mClearScreen)
            {
                mNexPlayer.GLDraw(1);
                
                mClearScreen = false;
            }
            else
            {
                mNexPlayer.GLDraw(0);
            }   
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            Log.d(LOG_TAG, "Change!  w:" + width + "  h:" + height );
            mNexPlayer.GLInit(width, height);
            Log.d(LOG_TAG, "Change! Done.");
            mListener.onGLChangeSurfaceSize(width, height);
        } 

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            Log.d(LOG_TAG, "Create!");
            mNexPlayer.GLInit(0, 0);
        }
    }
    
    
    private static class ContextFactory implements GLSurfaceView.EGLContextFactory {
        private static int EGL_CONTEXT_CLIENT_VERSION = 0x3098;
        
        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display,
                EGLConfig eglConfig) {
            Log.w(LOG_TAG, "creating OpenGL ES 2.0 context");
            checkEglError("Before eglCreateContext", egl);
            int[] attrib_list = {EGL_CONTEXT_CLIENT_VERSION, 2, EGL10.EGL_NONE };
            EGLContext context = egl.eglCreateContext(display, eglConfig, EGL10.EGL_NO_CONTEXT, attrib_list);
            checkEglError("After eglCreateContext", egl);
            Log.w(LOG_TAG, "eglCreateContext Done.");
            return context;
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display,
                EGLContext context) {
            egl.eglDestroyContext(display, context);
            
        }

    }
        
    private static void checkEglError(String prompt, EGL10 egl) {
        int error;
        while ((error = egl.eglGetError()) != EGL10.EGL_SUCCESS) {
            Log.e(LOG_TAG, String.format("%s: EGL error: 0x%x", prompt, error));
        }
    }
    private static class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
        private int[] mValue = new int[1];
        
        public ConfigChooser(int r, int g, int b, int a, int depth, int stencil) {
            mRedSize = r;
            mGreenSize = g;
            mBlueSize = b;
            mAlphaSize = a;
            mDepthSize = depth;
            mStencilSize = stencil;
        }

        /* This EGL config specification is used to specify 2.0 rendering.
         * We use a minimum size of 4 bits for red/green/blue, but will
         * perform actual matching in chooseConfig() below.
         */
        private static int EGL_OPENGL_ES2_BIT = 4;
        private static int[] s_configAttribs2 =
        {
            EGL10.EGL_RED_SIZE, 4,
            EGL10.EGL_GREEN_SIZE, 4,
            EGL10.EGL_BLUE_SIZE, 4,
            EGL10.EGL_RENDERABLE_TYPE, EGL_OPENGL_ES2_BIT,
            EGL10.EGL_NONE
        };

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {

            /* Get the number of minimally matching EGL configurations
             */
            int[] num_config = new int[1];
            egl.eglChooseConfig(display, s_configAttribs2, null, 0, num_config);

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException("No configs match configSpec");
            }

            /* Allocate then read the array of minimally matching EGL configs
             */
            EGLConfig[] configs = new EGLConfig[numConfigs];
            egl.eglChooseConfig(display, s_configAttribs2, configs, numConfigs, num_config);

            /* Now return the "best" one
             */
            return chooseConfig(egl, display, configs);
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                EGLConfig[] configs) {
            for(EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);

                // We need at least mDepthSize and mStencilSize bits
                if (d < mDepthSize || s < mStencilSize)
                    continue;

                // We want an *exact* match for red/green/blue/alpha
                int r = findConfigAttrib(egl, display, config,
                        EGL10.EGL_RED_SIZE, 0);
                int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                int a = findConfigAttrib(egl, display, config,
                        EGL10.EGL_ALPHA_SIZE, 0);

                if (r == mRedSize && g == mGreenSize && b == mBlueSize && a == mAlphaSize)
                    return config;
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }
    }

    /**
     * \brief   The application must implement this interface in order to receive
     *          notification when the size of the OpenGL surface (the video
     *          rendering surface) changes.
     *
     * This is specfified in the constructor for GLRenderer.
     *
     * Do not confuse this with NexPlayer.IListener, a separate interface.
     */
    public interface IListener
    {
        /**
         * \brief   This method is called when the size of the OpenGL surface
         *          has changed.
         *          
         *   Whenever the width or height in GLRenderer changes, the changed values are
         *   passed through the IListener.
         *
         * \param   width   New surface width, in pixels.
         * \param   height  New surface height, in pixels.
         */
        void onGLChangeSurfaceSize( int width, int height);
    }
}

