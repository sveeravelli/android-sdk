/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp.apis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.csp.CspConstants;
import com.example.csp.R;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Represents the playback scenario for local files that are currently being downloaded by the
 * Client Application to the device. It supports ISMV and enveloped format.<br>
 * ISMV limitation - cannot perform seek until the file is fully downloaded.<br>
 * <br>
 * It implements VOCommonPlayerListener interface in order to listen different event from player.
 */
public class ProgressiveDownload extends Activity implements VOCommonPlayerListener {

    // Start playing after 1 MB download
    public static final long PLAYBACK_BEGIN_THRESHOLD = 1024 * 1024;

    // Pause playback if 10 seconds are not available
    public static final long PLAYBACK_PAUSE_THRESHOLD = 5 * 1000;
    public static final int BUFFER_SIZE = 4 * 1024;
    public static final int CONNECTION_TIMEOUT = 10000;
    public static final int READ_DATA_TIMEOUT = 10000;

    private String mContentUrl = CspConstants.getActiveContent().getContentUrl();
    private String mLocalContentPath = CspConstants.getActiveContent().getTemplocalFile();
    private int mContentLength;
    private PlaybackControl mVideoView = null;
    SurfaceView mPlayerSurfaceView = null;
    private VODXPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
        mLocalContentPath = CspConstants.getActiveContent().getTemplocalFile();

        // Get into full screen mode. And prevent it from sleeping
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        File localFile = new File(mLocalContentPath);
        if (localFile.exists()) {
            localFile.delete();
        }

        initSurfaceHolder();
        play();
    }

    /**
     * Initializes the SurfaceHolder from {@link #mPlayerSurfaceView} element with RGBA PixelFormat.
     * 
     * @deprecated Uses setType() method from SurfaceHolder (which is deprecated since but required
     *             on Android versions prior to 3.0).
     */
    private void initSurfaceHolder() {
        mVideoView = (PlaybackControl) findViewById(R.id.video_view);
        mPlayerSurfaceView = (SurfaceView) findViewById(R.id.dxvo_Surface_view);

        SurfaceHolder surfaceHolder = mPlayerSurfaceView.getHolder();
        surfaceHolder.addCallback(mSHCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
    }

    /**
     * Creates the Thread to perform download and playback actions.
     */
    private void play() {
        Log.i("thread: " + Thread.currentThread().getName(), "play()");

        Thread playthread = new Thread(new DownloadingPlayer());
        playthread.start();
    }

    /**
     * Prepare {@link VODXPlayer} object to playback.
     * 
     * @param contentlength File content size.
     */
    private void prepareVideo(int contentlength) {
        if (mLocalContentPath != null) {
            // Register listeners
            VO_OSMP_RETURN_CODE nRet;

            // Initialize Player and check it is ok
            nRet = initPlayer();
            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                // Display error dialog and stop player and exit
                onError(mPlayer, nRet.getValue(), 0);
                return;
            }

            VOOSMPOpenParam openParam = new VOOSMPOpenParam();
            openParam.setFileSize(contentlength);
            mPlayer.open(mLocalContentPath, VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC,
                    VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);

            mPlayer.start();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Represents the thread that performs download and starts playback.
     */
    class DownloadingPlayer implements Runnable {

        /** Empty constructor */
        public DownloadingPlayer() {
            super();
        }

        @Override
        public void run() {
            try {
                boolean wasPlaybackRequested = false;
                boolean wasForcePaused = false;

                Log.i(CspConstants.TAG,
                        "Downloading url: " + mContentUrl + ", dest: " + mLocalContentPath);
                FileOutputStream fos = new FileOutputStream(mLocalContentPath);

                byte[] buffer = new byte[BUFFER_SIZE];

                URL urlObj = new URL(mContentUrl);
                URLConnection conn = urlObj.openConnection();
                conn.setConnectTimeout(CONNECTION_TIMEOUT);
                conn.setReadTimeout(READ_DATA_TIMEOUT);
                mContentLength = conn.getContentLength();

                InputStream is = conn.getInputStream();

                int bytesRead = 0;
                int bytesReadSoFar = 0;
                int mbsDownloadedSoFar = 0;

                while (true) {
                    bytesRead = is.read(buffer, 0, BUFFER_SIZE);
                    if (bytesRead == -1) {
                        fos.close();
                        if (wasForcePaused)
                            mPlayer.start();
                        break;
                    }

                    bytesReadSoFar += bytesRead;
                    fos.write(buffer, 0, bytesRead);
                    // Important to flush the data since we are currently playing it
                    fos.flush();

                    // Assume that the content is bigger than one MB
                    if ((!wasPlaybackRequested) && (bytesReadSoFar >= PLAYBACK_BEGIN_THRESHOLD)) {

                        boolean failure = false;
                        try {
                            // Get the CSP singleton instance
                            IDxDrmDlc cspApiSingleton = DxDrmDlc
                                    .getDxDrmDlc(ProgressiveDownload.this);

                            // If content is not DRM protected or it has Rights, we play it
                            if (!cspApiSingleton.isDrmContent(mLocalContentPath)
                                    || cspApiSingleton.verifyRights(mLocalContentPath)) {

                                ProgressiveDownload.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        prepareVideo(mContentLength);
                                    }
                                });

                                wasPlaybackRequested = true;
                                Log.i("Download progress:", "Initiated Playback");
                            } else {
                                failure = true;
                            }
                        } catch (DrmGeneralFailureException e) {
                            failure = true;
                            e.printStackTrace();
                        } catch (DrmInvalidFormatException e) {
                            failure = true;
                            e.printStackTrace();
                        } catch (DrmClientInitFailureException e) {
                            failure = true;
                            e.printStackTrace();
                        } catch (DrmAndroidPermissionMissingException e) {
                        	failure = true;
                            e.printStackTrace();
						}
                        if (failure) {
                            Log.e("Download progress:", "No rights to play this file");
                            break;
                        }
                    }

                    // print log after each MB of download
                    if (bytesReadSoFar > mbsDownloadedSoFar * 1024 * 1024) {
                        Log.i("Download progress:", mbsDownloadedSoFar + " Mb (" + bytesReadSoFar
                                + " bytes) Downloaded.");
                        mbsDownloadedSoFar++;
                    }
                }
                Log.i("Download progress:", "Download was completed");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** SurfaceHolder callback object to manage different surface changes. */
    SurfaceHolder.Callback mSHCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Log.i(CspConstants.TAG, "Surface Changed");
            if (mPlayer != null)
                mPlayer.setSurfaceChangeFinished();
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.i(CspConstants.TAG, "Surface Created");
            if (mPlayer != null) {
                mPlayer.setView(mPlayerSurfaceView);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(CspConstants.TAG, "Surface Destroyed");
            if (mPlayer != null)
                mPlayer.setView(null);
        }
    };

    /**
     * Stop player and exit on Back key.
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            stopPlayback();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2,
            Object obj) {
        if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_VIDEO_SIZE_CHANGED) { // Video size changed
            int videoWidth = nParam1;
            int videoHeight = nParam2;

            // Retrieve new display metrics
            DisplayMetrics dm = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(dm);

            if (getResources()
                    .getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {

                // If portrait orientation, scale height as a ratio of the new
                // aspect ratio
                ViewGroup.LayoutParams lp = mPlayerSurfaceView.getLayoutParams();
                lp.width = dm.widthPixels;
                lp.height = dm.widthPixels * videoHeight / videoWidth;
                mPlayerSurfaceView.setLayoutParams(lp);
            }
        } else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_ERROR) { // Error
            // Display error dialog and stop player
            onError(mPlayer, 1, 0);
        } else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_PLAY_COMPLETE) {
            stopPlayback();
            Toast.makeText(this, "Playback has finished", Toast.LENGTH_LONG).show();
            // Finish should be called in order to exit the activity after playback completes.
            finish();
        } else if (nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_FAIL
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DOWNLOAD_FAIL
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_FAIL
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_REJECTED
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_NOT_SECURE
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_CODEC_NOT_SUPPORT
                || nID == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL) { // Errors
            // Display error dialog and stop player
            onError(mPlayer, nID.getValue(), 0);
        }

        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    /**
     * Display error messages into UI threads.
     * 
     * @param mp {@link VODXPlayer} player.
     * @param what Error code.
     * @param extra Extra information code.
     */
    public void onError(final VODXPlayer mp, final int what, final int extra) {
        runOnUiThread(new Runnable() {

            @Override
            public void run() {
                onErrorImpl(mp, what, extra);
            }
        });
    }

    /**
     * Shows Error and stop playback.
     * 
     * @param mp {@link VODXPlayer} player.
     * @param what Error code.
     * @param extra Extra information code.
     */
    private void onErrorImpl(VODXPlayer mp, int what, int extra) {

        Log.v(CspConstants.TAG, "Error message, what is " + what + " extra is " + extra);
        String errStr = getString(R.string.str_ErrPlay_Message) + "\nError code is "
                + Integer.toHexString(what);

        Log.v(CspConstants.TAG, "Stop Playback");
        stopPlayback();

        // Dialog to display error message; stop player and exit on Back key or <OK>
        AlertDialog ad = new AlertDialog.Builder(this).setIcon(R.drawable.icon)
                .setTitle(R.string.str_ErrPlay_Title)
                .setMessage(errStr).setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                            // do something on back.
                            finish();
                        }
                        return false;
                    }
                }).setPositiveButton(R.string.str_OK, new OnClickListener() {
                    public void onClick(DialogInterface a0, int a1) {
                        finish();
                    }
                }).create();
        ad.show();
    }

    /**
     * Stop playback and reinitialize fields.
     */
    private void stopPlayback() {
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.close();
            mPlayer.destroy();
            mPlayer = null;
            mVideoView.setPlayer(null);
        }

        CspConstants.setVideoSpecifics(false);
    }

    /**
     * Creates {@link VODXPlayerImpl} element and configures it.
     */
    private VO_OSMP_RETURN_CODE initPlayer() {

        mPlayer = new VODXPlayerImpl();
        // Location of libraries
        String apkPath = getFilesDir().getParent() + "/lib/";

        // Initialize SDK player
        VOOSMPInitParam initParam = new VOOSMPInitParam();
        initParam.setLibraryPath(apkPath);
        initParam.setContext(this);
        VO_OSMP_RETURN_CODE nRet = mPlayer.init(VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER,
                initParam);

        // Check initialization errors
        if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            Log.e(CspConstants.TAG, "Error " + nRet + " while initializing VODXPlayer");
            return nRet;
        }

        // Load license file
        loadLicenseFile(CspConstants.LICENSE_FILE);

        // Set view
        nRet = mPlayer.setView(mPlayerSurfaceView);

        // Check setViews errors
        if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            Log.e(CspConstants.TAG, "Error " + nRet + " while setView method");
            return nRet;
        }

        DisplayMetrics dm = getResources().getDisplayMetrics();

        // Set surface view size
        nRet = mPlayer.setViewSize(dm.widthPixels, dm.heightPixels);

        // Check error codes
        if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            Log.e(CspConstants.TAG, "Error " + nRet + " while setViewSize method");
            return nRet;
        }

        nRet = mPlayer.setOnEventListener(this);
        if (mVideoView != null) {
            mVideoView.setPlayer(mPlayer);
        }

        setSubtitles();

        return nRet;
    }

    /**
     * Copy file from Assets directory to destination. Used for licenses and processor-specific
     * configurations.
     */
    private void loadLicenseFile(String fileName) {
        try {
            String fileDir = getFilesDir().getParent() + "/";
            String filePath = fileDir + fileName;
            InputStream InputStreamis = getAssets().open(fileName);
            File desFile = new File(filePath);
            desFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(desFile);
            int bytesRead;
            byte[] buf = new byte[4 * 1024]; // 4K buffer
            while ((bytesRead = InputStreamis.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
            fos.flush();
            fos.close();

            if (mPlayer != null) {
                VO_OSMP_RETURN_CODE nRet = mPlayer.setLicenseFilePath(fileDir);
                if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    Log.e(CspConstants.TAG, "setLicenseFilePath failed with error: " + nRet);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Sets content subtitles.
     */
    private void setSubtitles() {
        String customSubtitle = CspConstants.getSubtitleCustom();
        if (customSubtitle != null) {
            mPlayer.enableSubtitle(true);
            mPlayer.setSubtitlePath(customSubtitle);
        } else {
            mPlayer.enableSubtitle(false);
        }
    }

    @Override
    public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0, int arg1, int arg2,
            Object arg3) {

        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
}
