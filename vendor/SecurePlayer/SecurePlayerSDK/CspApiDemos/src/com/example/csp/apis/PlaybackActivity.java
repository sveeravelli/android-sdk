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
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.discretix.vodx.VODXPlayer;
import com.discretix.vodx.VODXPlayerImpl;
import com.example.csp.CspConstants;
import com.example.csp.R;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DECODER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_STATUS;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Represents the activity in charge of playing the content. It implements VOCommonPlayerListener
 * interface in order to listen different event from player.
 */
public class PlaybackActivity extends Activity implements VOCommonPlayerListener {

    private String mPlaybackPath = CspConstants.getActiveContent().getPlayBackPath();
    private PlaybackControl mVideoView = null;
    private VODXPlayer mPlayer;
    private boolean mStartedPlayBack = false;
    SurfaceView mPlayerSurfaceView = null;
    private int m_DrmErrorCount = 0;

    /**
     * @deprecated Uses setType() method from SurfaceHolder (which is deprecated but required on
     *             Android versions prior to 3.0.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
        mPlaybackPath = CspConstants.getActiveContent().getPlayBackPath();

        // Get into full screen mode. And prevent it from sleeping
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.main);
        mVideoView = (PlaybackControl) findViewById(R.id.video_view);
        mPlayerSurfaceView = (SurfaceView) findViewById(R.id.dxvo_Surface_view);
        getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);
        SurfaceHolder surfaceHolder = mPlayerSurfaceView.getHolder();
        surfaceHolder.addCallback(mSHCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);
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

        // Load license files
        loadLicenseFile(CspConstants.LICENSE_FILE);

        // Configure Device Capabilities
        setDeviceCapability("Android-cap.xml");

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

        return nRet;
    }

    /**
     * Sets content subtitles.
     */
    private void setSubtitles() {

        String customSubtitle = CspConstants.getSubtitleCustom();

        // If user has defined custom subtitle file, use it
        if (customSubtitle != null) {
            mPlayer.enableSubtitle(true);
            mPlayer.setSubtitlePath(customSubtitle);

            // If the subtitles are from the list of subtitle
        } else if (CspConstants.getSubtitleSelected() != -1) {
            mPlayer.enableSubtitle(true);
            mPlayer.selectSubtitle(CspConstants.getSubtitleSelected());

        } else {
            mPlayer.enableSubtitle(false);
        }
    }

    /**
     * Sets selected audio channel of current content.
     */
    private void setAudioChannel() {
        if (CspConstants.getAudioSelected() != -1) {
            mPlayer.selectAudio(CspConstants.getAudioSelected());
        }
    }

    /**
     * Play video content configured into {@link #mPlaybackPath}.
     */
    private void playVideo() {
        if (mPlaybackPath != null) {
            VO_OSMP_RETURN_CODE nRet;

            // Initialize Player and check it is ok
            nRet = initPlayer();
            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                // Display error dialog and stop player
                onError(mPlayer, nRet.getValue(), 0);
            }

            VOOSMPOpenParam openParam = new VOOSMPOpenParam();
            if (CspConstants.isHardwareAccelerated())
                openParam.setDecoderType(VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_IOMX.getValue());

            nRet = mPlayer.open(mPlaybackPath,
                    VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC,
                    VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT, openParam);
            if (nRet == null) {
                Log.e(CspConstants.TAG,
                        "mPlayer.open returned null - using default onError with 1.");
                onError(mPlayer, 1, 0);
            } else if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                // Display error dialog and stop player
                onError(mPlayer, nRet.getValue(), 0);
            }
        }
    }

    /**
     * Updates Byte Per Seconds (BPS) information into the TextView element.
     */
    private void updatePlaybackInfoBPS() {
        TextView tView = (TextView) findViewById(R.id.playback_info_bps);
        String value = CspConstants.getPlaybackInformationBPS();

        if (value == null) {
            // If Value hasn't been set;null; then use default value
            value = getString(R.string.playback_information_BPS_default_value);
        } else {
            // Set units
            value = value + getString(R.string.playback_information_BPS_tView_units);
        }
        value = getString(R.string.playback_information_BPS_tView_header) + value;
        tView.setText(value);
        tView.invalidate();
    }

    /**
     * Updates Resolution information into the TextView element.
     */
    private void updatePlaybackInfoResolution() {
        TextView tView = (TextView) findViewById(R.id.playback_info_res);
        String value = CspConstants.getPlaybackInformationResolution();

        if (value == null) {
            // If Value hasn't been set;null; then use default value
            value = getString(R.string.playback_information_Resolution_default_value);
        }
        value = getString(R.string.playback_information_Resolution_tView_header) + value;
        tView.setText(value);
        tView.invalidate();
    }

    /**
     * Updates the playback information (BPS, Resolution) depending on the current configuration.
     */
    private void updatePlaybackInfoLayout() {
        updatePlaybackInfoBPS();
        updatePlaybackInfoResolution();
        View playbackInfoLayout = (View) findViewById(R.id.playbackInfoLayout);

        if (CspConstants.getDisplayPlaybackInformation() == true) {
            playbackInfoLayout.setVisibility(android.view.View.VISIBLE);
        } else {
            playbackInfoLayout.setVisibility(android.view.View.INVISIBLE);
        }
    }

    /**
     * Resets all playback information to default values.
     */
    private void resetPlayBackInformation() {
        CspConstants.setPlaybackInformationBPS(
                getString(R.string.playback_information_BPS_default_value));
        CspConstants.setPlaybackInformationResolution(
                getString(R.string.playback_information_Resolution_default_value));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mPlayer != null) {
            mPlayer.destroy();
        }
        resetPlayBackInformation();

    }

    @Override
    public void onResume() {
        super.onResume();
        updatePlaybackInfoLayout();
    }

    @Override
    protected void onPause() {
        if (mPlayer != null) {
            if (mPlayer.getPlayerStatus() == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
                mStartedPlayBack = false;
            }

            if (mPlayer.canBePaused()) {
                mPlayer.pause();
            } else {
                mPlayer.stop();
            }
        }
        super.onPause();
    }

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

    /**
     * SurfaceHolder callback object to manage different surface changes.
     */
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
            if (mPlayer == null) {
                playVideo();
            } else {
                mPlayer.resume(mPlayerSurfaceView);

                if (!mStartedPlayBack) {
                    mStartedPlayBack = true;
                    mPlayer.start();
                }

                setAudioChannel();
                setSubtitles();
                mPlayer.commitSelection();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.i(CspConstants.TAG, "Surface Destroyed");
            if (mPlayer != null)
                mPlayer.setView(null);
        }
    };

    @Override
    public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2,
            Object obj) {

        switch (nID) {
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO: {
                VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT event = VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT
                        .valueOf(nParam1);
                switch (event) {
                    case VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE:
                        Log.v(CspConstants.TAG,
                                "OnEvent VOOSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_BITRATE_CHANGE, param2 is "
                                        + nParam2);
                        CspConstants.setPlaybackInformationBPS(Integer.toString(nParam2) + " ");
                        updatePlaybackInfoBPS();
                        break;

                    default:
                        break;
                }
                break;
            }
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAM_WARNING:
                if (nParam1 == VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DRMERROR
                        .getValue()) {
                    Log.v(CspConstants.TAG,
                            "VO_OSMP_SRC_CB_ADAPTIVE_STREAM_WARNING, nParam1 is VOOSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DRMERROR");
                    m_DrmErrorCount++;
                    if (m_DrmErrorCount == 10)
                        onError(mPlayer, nID.getValue(), nParam1);
                } else {
                    Log.v(CspConstants.TAG,
                            "VOOSMP_SRC_CB_Adaptive_Stream_Warning, nParam1 is " + nParam1);
                }
                break;

            case VO_OSMP_SRC_CB_BA_HAPPENED: {
                break;
            }
            case VO_OSMP_CB_VIDEO_SIZE_CHANGED: {
                int videoWidth = nParam1;
                int videoHeight = nParam2;

                // Retrieve new display metrics
                DisplayMetrics dm = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) getSystemService(
                        Context.WINDOW_SERVICE);
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
                // now update playback info
                CspConstants.setPlaybackInformationResolution(videoWidth + "x" + videoHeight);
                updatePlaybackInfoResolution();
                break;
            }
            case VO_OSMP_CB_PLAY_COMPLETE: {
                stopPlayback();
                Toast.makeText(this, "Playback has finished", Toast.LENGTH_LONG).show();
                // Finish should be called in order to exit the activity after playback completes.
                finish();
                break;
            }
            case VO_OSMP_CB_ERROR:
            case VO_OSMP_SRC_CB_CONNECTION_FAIL:
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
            case VO_OSMP_SRC_CB_DRM_FAIL:
            case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
            case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
            case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
            case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:
            case VO_OSMP_CB_CODEC_NOT_SUPPORT:
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_ERROR: {
                // Display error dialog and stop player
                onError(mPlayer, nID.getValue(), nParam1);
                break;
            }

            default:
                Log.w(CspConstants.TAG, "Unhandled VO_OSMP_CB_EVENT_ID:" + nID);
                break;
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
        String errStr = "Error code is " + Integer.toHexString(what) + "\nExtra(" + extra + ")";

        Log.v(CspConstants.TAG, "Stop Playback");
        stopPlayback();

        // Dialog to display error message; stop player and exit on Back key or <OK>
        AlertDialog ad = new AlertDialog.Builder(this).setIcon(R.drawable.icon)
                .setTitle(R.string.str_ErrPlay_Title)
                .setMessage(errStr).setOnKeyListener(new OnKeyListener() {
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_playback, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mPlayer != null && !CspConstants.isVideoSpecificsSet()) {
            int subtitleCount = mPlayer.getSubtitleCount();
            if (subtitleCount > 0) {
                CspConstants.setSubtitleArray(subtitleCount);
                for (int iIndex = 0; iIndex < subtitleCount; ++iIndex) {
                    VOOSMPAssetProperty property = mPlayer.getSubtitleProperty(iIndex);

                    String propertyDescription = null;
                    String propertyLanguage = null;
                    String propertyCodec = null;
                    String subtitleName = null;

                    int propertyCount = property.getPropertyCount();
                    for (int propertyIndex = 0; propertyIndex < propertyCount; ++propertyIndex) {
                        String propertyKey = property.getKey(propertyIndex);
                        if (propertyKey.equals("description")) {
                            propertyDescription = (String) (property.getValue(propertyIndex));
                        } else if (propertyKey.equals("language")) {
                            propertyLanguage = (String) (property.getValue(propertyIndex));
                        } else if (propertyKey.equals("codec")) {
                            propertyCodec = (String) (property.getValue(propertyIndex));
                        }
                    }

                    if (propertyDescription != null) {
                        propertyDescription = " (" + propertyDescription + ")";
                    }
                    if (propertyCodec != null) {
                        propertyCodec = " - [" + propertyCodec + "]";
                    }
                    subtitleName = propertyLanguage + propertyDescription + propertyCodec;
                    if (subtitleName.equalsIgnoreCase("")) {
                        subtitleName = "Unknown";
                    }
                    CspConstants.setSubtitle(iIndex, subtitleName);
                }

            }

            int audioCount = mPlayer.getAudioCount();
            if (audioCount > 0) {
                CspConstants.setAudioArray(audioCount);
                for (int iIndex = 0; iIndex < audioCount; ++iIndex) {
                    VOOSMPAssetProperty property = mPlayer.getAudioProperty(iIndex);

                    String propertyDescription = null;
                    String propertyLanguage = null;
                    String propertyCodec = null;
                    String audioName = null;

                    int propertyCount = property.getPropertyCount();
                    for (int propertyIndex = 0; propertyIndex < propertyCount; ++propertyIndex) {
                        String propertyKey = property.getKey(propertyIndex);
                        if (propertyKey.equals("description")) {
                            propertyDescription = (String) (property.getValue(propertyIndex));
                        } else if (propertyKey.equals("language")) {
                            propertyLanguage = (String) (property.getValue(propertyIndex));
                        } else if (propertyKey.equals("codec")) {
                            propertyCodec = (String) (property.getValue(propertyIndex));
                        }
                    }

                    if (propertyDescription != null) {
                        propertyDescription = " (" + propertyDescription + ")";
                    }
                    if (propertyCodec != null) {
                        propertyCodec = " - [" + propertyCodec + "]";
                    }
                    audioName = propertyLanguage + propertyDescription + propertyCodec;
                    if (audioName.equalsIgnoreCase("")) {
                        audioName = "Unknown";
                    }
                    CspConstants.setAudio(iIndex, audioName);
                }

            }

            CspConstants.setVideoSpecifics(true);
        }

        switch (item.getItemId()) {
            case R.id.menu_options:
                Intent optionsActivity = new Intent(this, PlaybackOptionsActivity.class);
                startActivity(optionsActivity);
                return true;
        }

        return true;
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
     * Sets device capabilities. It only use the first 4k of a file.
     * 
     * @param fileName File name.
     */
    private void setDeviceCapability(String fileName) {
        try {
            String filePath = getFilesDir().getParent() + "/" + fileName;
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

            if (mPlayer != null)
                mPlayer.setDeviceCapabilityByFile(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0, int arg1, int arg2,
            Object arg3) {

        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
}
