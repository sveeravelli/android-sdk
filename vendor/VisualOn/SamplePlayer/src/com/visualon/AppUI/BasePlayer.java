/************************************************************************
 VisualOn Proprietary
 Copyright (c) 2014, VisualOn Incorporated. All Rights Reserved

 VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

 All data and information contained in or disclosed by this document are
 confidential and proprietary information of VisualOn, and all rights
 therein are expressly reserved. By accepting this material, the
 recipient agrees that this material and the information contained
 therein are held in confidence and in trust. The material may only be
 used and/or disclosed as authorized in a license agreement controlling
 such use and disclosure.
 ************************************************************************/

package com.visualon.AppUI;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.visualon.AppBehavior.AppBehaviorManager;
import com.visualon.AppBehavior.AppBehaviorManager.APP_BEHAVIOR_EVENT_ID;
import com.visualon.AppBehavior.AppBehaviorManagerDelegate;
import com.visualon.AppBehavior.AppBehaviorManagerImpl.OPTION_ID;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection.AssetStatus;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerAssetSelection.AssetType;
import com.visualon.AppPlayerCommonFeatures.CAdManager;
import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CPlayer.APPUIEventListener;
import com.visualon.AppPlayerCommonFeatures.CPlayer.APP_UI_EVENT_ID;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;
import com.visualon.AppPlayerCommonFeatures.voLog;
import com.visualon.AppPlayerSpecialFeatures.CSpecialFeatures;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerHDMI;
import com.visualon.OSMPPlayer.VOCommonPlayerHDMI.VO_OSMP_HDMI_CONNECTION_STATUS;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPAdInfo;
import com.visualon.OSMPPlayer.VOOSMPAdPeriod;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ASPECT_RATIO;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DISPLAY_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DRM_KEY_EXPIRED_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayerImpl.VOCommonPlayerHDMIImpl;
import com.visualon.OSMPUtils.voSurfaceView;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class BasePlayer extends FragmentActivity implements APPUIEventListener, VOCommonPlayerHDMI.onHDMIConnectionChangeListener, InputBaseFragment.OnPassingDataEventListener {

    private static final String TAG = "@@@OSMP+Player"; // Tag for VOLog messages

    private static final int MSG_UPDATE_UI = 1;
    private static final int MSG_PLAYCOMPLETE = 5;
    private static final int MSG_AD_START = 6;
    private static final int MSG_AD_END = 7;
    private static final int MSG_AD_SKIPPABLE = 8;
    private static final int MSG_CHECK_SURFACE_CREATED = 9;
    private static final int MSG_MEDIA_SEEK_CONTROL = 11;

    private voSurfaceView m_svMain = null;            //use voSurfaceView for HW Zoom feature
    private SurfaceHolder m_shMain = null;
    private SeekBar m_sbMain = null;            // Seekbar
    private TextView m_tvCurrentTime = null;
    private TextView m_tvUTCPosition = null;
    private TextView m_tvMaxTime = null;
    private TextView m_tvMinTime = null;
    private TextView m_tvHDMI = null;
    private TextView m_tvBps = null;
    private TextView m_tvResolutionW = null;
    private TextView m_tvResolutionH = null;
    private ProgressBar m_pbLoadingProgress = null;
    private TextView m_tvLoadingProgress = null;
    private ImageButton m_btnPause = null;
    private ImageButton m_btnStop = null;
    private View m_btnAsset = null;
    private ImageButton m_btnAudioLow = null;
    private ImageButton m_btnAudioHigh = null;
    private TextView m_tvAduioSpeed = null;
    private CheckBox m_chbSubtitle = null;
    private Button m_btnPrev = null;
    private Button m_btnNext = null;
    private Button m_btnSpecial = null;
    private Button m_btnAnalytics = null;
    private TextView m_tvDownloadCurrent = null;
    private TextView m_tvDownloadTotal = null;
    private SpecialDialog m_dlgSpecial = null;
    private AssetDialog m_dlgAsset = null;
    private LinearLayout m_rlBottom = null;
    private View m_rlTop = null;
    private RelativeLayout m_rlDownloader = null;
    private LinearLayout m_llRightBottom = null;

    private APPCommonPlayerAssetSelection m_asset = null;
    private static VOCommonPlayerHDMIImpl m_HDMIStateCheck = null;
    private CPlayer m_cPlayer = null;
    private CSpecialFeatures m_cSpecialPlayer = null;
    private AppBehaviorManager m_abManager = null;
    private CDownloader m_cDownloader = null;
    private AlertDialog m_adlgDownload = null;
    private SharedPreferences m_spMain = null;
    private Editor m_editor = null;
    private Timer m_timerMain = null;
    private TimerTask m_ttMain = null;
    private ArrayList<String> m_lstSelectURL = new ArrayList<String>();
    private float m_fAudioSpeed = 1.0f;
    private int m_nFastChannelIndex = 0;

    private int m_nVideoWidth = 0;               // Video width
    private int m_nVideoHeight = 0;
    private long m_nMaxTime = 0;               // Total duration
    private long m_nCurrentTime = 0;               // Current position
    private long m_nMinTime = 0;
    private ArrayList<String> m_lstVideo = null;
    private int[] m_audioIndex = new int[100];
    private boolean m_isResumePlayerNeeded = false;
    private boolean m_isSurfaceCreated = false;
    private boolean m_isPlayerRun = false;
    private boolean m_isAppStop = false;
    private boolean m_isPlayerStop = false;
    private VO_OSMP_ASPECT_RATIO m_nAspectRatio = VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO;

    private boolean m_updateADUI = false;
    private int m_ADPlayingTime = 0;
    private int m_currPid = 0;
    private VOOSMPAdInfo mPlaybackInfo = null;
    private Button m_btnADSkip = null;
    private Button m_btnShowADUrl = null;
    private CAdManager m_ADManager = null;

    private void addSurfaceHolderCallback() {
        if(m_cbSurfaceHolder == null) {
            m_cbSurfaceHolder = new SurfaceCallBack();
            m_shMain = m_svMain.getHolder();
            m_shMain.addCallback(m_cbSurfaceHolder);
            m_shMain.setFormat(PixelFormat.RGBA_8888);
        }
    }
    private void removeSurfaceHolderCallback() {
        if(m_cbSurfaceHolder != null) {
            voLog.i(TAG, "remove callback");
            m_shMain = m_svMain.getHolder();
            m_shMain.removeCallback(m_cbSurfaceHolder);
            m_cbSurfaceHolder = null;
        }
    }

    private SurfaceHolder.Callback m_cbSurfaceHolder = null;
    private class SurfaceCallBack implements SurfaceHolder.Callback {
        /* Notify SDK on Surface Change */
        public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
            voLog.i(TAG, "Surface Changed");
            m_cPlayer.setSurfaceChangeFinished();
        }

        /* Notify SDK on Surface Creation */
        public void surfaceCreated(SurfaceHolder surfaceholder) {

            voLog.i(TAG, "Surface Created: " + m_isPlayerStop + "," + m_isAppStop + "," + m_isResumePlayerNeeded);

            if(m_svMain != null && m_cPlayer != null)
                m_cPlayer.setPlayView(m_svMain);

            m_isSurfaceCreated = true;

            if (m_isPlayerStop)
                return;
            if (m_isAppStop)
                return;
            if (m_isResumePlayerNeeded) {
                return;
            }

            playerStart();
        }

        public void surfaceDestroyed(SurfaceHolder surfaceholder) {
            m_isSurfaceCreated = false;
            voLog.i(TAG, "Surface Destroyed");
            m_cPlayer.setPlayView(null);
        }
    };

    private SurfaceHolder.Callback m_cbSurfaceHolder2 = new SurfaceHolder.Callback() {
        /* Notify SDK on Surface Change */
        @Override
        public void surfaceChanged(SurfaceHolder surfaceholder, int format, int w, int h) {
            voLog.i(TAG, "Surface Changed");
            m_secondPlayer.setSurfaceChangeFinished();
        }

        /* Notify SDK on Surface Creation */
        @Override
        public void surfaceCreated(SurfaceHolder surfaceholder) {
            secondPlayerStart();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder surfaceholder) {

            m_secondPlayer.setView(null);
        }
    };
    private OnSeekBarChangeListener m_listenerSeekBar = new OnSeekBarChangeListener() {

        /* Seek to new position when Seekbar drag is complete */
        public void onStopTrackingTouch(SeekBar arg0) {
            voLog.d(TAG, ">> onStopTrackingTouch");
            mIsSeekbarProgressChangedByUser = false;
            long duration = m_nMaxTime - m_nMinTime;
            if (duration == 0) {
                m_sbMain.setProgress(0);
                return;
            }
            float nCurrent = arg0.getProgress();
            float nMax = arg0.getMax();
            float seekPoint = nCurrent / nMax;
            m_cPlayer.seekTo(seekPoint);
        }

        /* Flag when Seekbar is being dragged */
        public void onStartTrackingTouch(SeekBar arg0) {
            voLog.d(TAG, ">> onStartTrackingTouch");
        }

        public void onProgressChanged(SeekBar arg0, int progress, boolean arg2) {
            voLog.d(TAG, ">> onProgressChanged - isFromuser: " + arg2);
            if(arg2 == true) {
                mIsSeekbarProgressChangedByUser = true;
            }
            /*This code is only used for auto test.   */
            if (m_sbMain.getTag() != null && m_sbMain.getTag() == "autoTest") {
                m_sbMain.setTag(null);
                float nMax = arg0.getMax();
                float seekPoint = progress / nMax;
                m_cPlayer.seekTo(seekPoint);
            }
        }
    };

    private Handler handler = new Handler() {

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_MEDIA_SEEK_CONTROL:

                    switch(msg.arg1) {
                        case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:

                            voLog.d(TAG, "+++ KEYCODE_MEDIA_FAST_FORWARD +++" + timeStepOfMediaControl);
                            long targetPos1 = m_cPlayer.getCurrentPosition() + timeStepOfMediaControl;
                            if(targetPos1 < m_cPlayer.getRightPosition()) {
                                voLog.d(TAG, "Seek to " + targetPos1);
                                m_cPlayer.seekTo(targetPos1);
                            }
                            break;
                        case KeyEvent.KEYCODE_MEDIA_REWIND:

                            voLog.d(TAG, "+++ KEYCODE_MEDIA_REWIND +++" + timeStepOfMediaControl);
                            long targetPos2 = m_cPlayer.getCurrentPosition() - timeStepOfMediaControl;
                            if(targetPos2 < 1.0f) {
                                targetPos2 = 0;
                            }
                            if(targetPos2 > m_cPlayer.getLeftPosition()) {
                                voLog.d(TAG, "Seek to " + targetPos2);
                                m_cPlayer.seekTo(targetPos2);
                            }
                            break;
                    }

                    break;
                case MSG_CHECK_SURFACE_CREATED:

                    if(m_isSurfaceCreated == true) {
                        voLog.v(TAG, "MSG_CHECK_SURFACE_CREATED 1");

                        if(m_isResumePlayerNeeded == true) {
                            voLog.v(TAG, "MSG_CHECK_SURFACE_CREATED 2");
                            m_cPlayer.resume(m_svMain);
                        }
                    } else {
                        voLog.v(TAG, "MSG_CHECK_SURFACE_CREATED 3");
                        sendEmptyMessageDelayed(MSG_CHECK_SURFACE_CREATED, 200);
                    }

                    break;

                case MSG_UPDATE_UI:
                    doUpdateUI();
                    break;
                case MSG_PLAYCOMPLETE:
                    if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_LOOP_ID.getValue()).getSelect() == 0) {
                        stopVideo();
                        uninitPlayer();
                        (BasePlayer.this).finish();
                    } else {
                        m_cPlayer.seekTo(0);
                    }
                    break;
                case MSG_AD_START:
                    m_btnShowADUrl.setVisibility(View.VISIBLE);
                    break;
                case MSG_AD_END:
                    m_btnShowADUrl.setVisibility(View.GONE);
                    break;
                case MSG_AD_SKIPPABLE:
                    m_btnADSkip.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

        }
    };

    private AppBehaviorManagerDelegate abManagerDelegate = new AppBehaviorManagerDelegate() {

        @Override
        public VO_OSMP_RETURN_CODE handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID nID, String str) {
            switch (nID) {
                case APP_BEHAVIOR_STOP_PLAY:
                    m_isAppStop = true;
                    stopVideo();
                    uninitPlayer();
                    AppBehaviorPopMsg(Definition.ERROR_MESSAGE, str);
                    break;
                case APP_BEHAVIOR_CONTINUE_PLAY:
                    m_isAppStop = false;
                    AppBehaviorPopMsg(Definition.WARNING_MESSAGE, str);
                    break;
                case APP_BEHAVIOR_PAUSE_PLAY:
                    m_isAppStop = false;
                    m_cPlayer.pause();
                    AppBehaviorPopMsg(Definition.WARNING_MESSAGE, str);
                    break;
                case APP_BEHAVIOR_SWITCH_ENGINE:
                    stopVideo();
                    uninitPlayer();
                    AlertDialog ad = new AlertDialog.Builder(BasePlayer.this)
                            .setIcon(R.drawable.icon)
                            .setTitle("Warning")
                            .setMessage(str)
                            .setOnKeyListener(new OnKeyListener() {
                                @Override
                                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                                    if (arg1 == KeyEvent.KEYCODE_BACK) {
                                        // do something on back.
                                        finish();
                                    }
                                    return false;
                                }
                            })
                            .setPositiveButton(R.string.str_OK, new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface a0, int a1) {

                                    m_cPlayer.createPlayer();
                                    m_cDownloader.createDownloader();
                                    playerStart();
                                    a0.dismiss();
                                }
                            })
                            .create();
                    ad.setCanceledOnTouchOutside(false);
                    ad.show();
                    break;
                default:
                    break;
            }

            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
        }
    };

    private void AppBehaviorPopMsg(String title, String str) {
        AlertDialog ad = new AlertDialog.Builder(BasePlayer.this)
                .setIcon(R.drawable.icon)
                .setTitle(title)
                .setMessage(str)
                .setOnKeyListener(new OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                            // do something on back.
                            arg0.dismiss();
                            finish();
                        }

                        return false;
                    }
                })
                .setPositiveButton(R.string.str_OK, new OnClickListener() {
                    public void onClick(DialogInterface a0, int a1) {
                        if (m_isAppStop) {
                            a0.dismiss();
                            finish();
                        }
                    }
                }).create();
        ad.setCanceledOnTouchOutside(false);
        ad.show();
    }

    private Context mContext = null;

    private String m_2ndPlayerUrl = null;

    @SuppressLint("NewApi")
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        voLog.v(TAG, "Player onCreate");

        mContext = this;

        /*Screen always on*/
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.player);

        m_spMain = PreferenceManager.getDefaultSharedPreferences(this);
        m_editor = m_spMain.edit();

        m_cPlayer = CommonFunc.getCPlayer();
        m_abManager = m_cPlayer.getBehavior();
        m_abManager.setDelegate(abManagerDelegate);
        m_cPlayer.setUIListener(this);

        m_cDownloader = CommonFunc.getCDownloader();
        m_cDownloader.setUIListener(this);

        m_nFastChannelIndex = getIntent().getIntExtra("fastChannleIndex", 0);

        getWindow().setFormat(PixelFormat.UNKNOWN);
        m_svMain = (voSurfaceView) findViewById(R.id.svMain);

        m_svMain2 = (SurfaceView) findViewById(R.id.svMain2);
        m_shMain2 = m_svMain2.getHolder();

        m_rlTop = (View) findViewById(R.id.rlTop);
        m_2ndPlayerUrl = this.getIntent().getStringExtra("KEY_2ND_PLAY_URL");
        m_btnSwitch = (Button) findViewById(R.id.btSwitch);
        if (m_2ndPlayerUrl != null && !m_2ndPlayerUrl.isEmpty()) {
            m_svMain2.getHolder().addCallback(m_cbSurfaceHolder2);
            setupPlayer2Pos();
            m_btnSwitch.setVisibility(View.VISIBLE);
        } else {
            m_svMain2.setVisibility(View.INVISIBLE);
            m_btnSwitch.setVisibility(View.INVISIBLE);
        }

        if (Build.VERSION.SDK_INT >= 17) {
            m_svMain.setSecure(true);
            if (m_svMain2 != null) {
                m_svMain2.setSecure(true);
            }
            voLog.i(TAG, "setSecure(true) , add screenshots secure. sdk level is %d", Build.VERSION.SDK_INT);
        } else {
            voLog.i(TAG, "Do not support setSecure function,  screenshots secure. sdk level is %d", Build.VERSION.SDK_INT);
        }


        initLayout();

        m_HDMIStateCheck = new VOCommonPlayerHDMIImpl();
        m_HDMIStateCheck.enableHDMIDetection(this, true);
        m_HDMIStateCheck.setOnHDMIConnectionChangeListener(this);

        CommonFunc.ReadUrlInfo(this, m_lstSelectURL);
        CommonFunc.getLocalFiles(m_lstSelectURL, Definition.LOCALFILE_PATH);
        CommonFunc.getDownloadFiles(m_lstSelectURL, Definition.DOWNLOAD_PATH);

        if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_ENABLEAD_ID.getValue()).getSelect() == 1) {
            m_ADManager = m_cPlayer.getCAdManager();
            m_ADManager.getGoogleAdvertisingId(this);
        }

        m_dm = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(m_dm);
    }
    // For Remote control - |>|> and <|<| Media Key
    private final static int TIME_STEP_MS_PER_SEEK = 5000;
    private long timeStepOfMediaControl = TIME_STEP_MS_PER_SEEK;

    private void seekByProgressBar() {
        float nMax = m_sbMain.getMax();
        float seekPoint = m_sbMain.getProgress() / nMax;
        m_cPlayer.seekTo(seekPoint);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean handled = false;
        switch(keyCode) {

            case KeyEvent.KEYCODE_DPAD_CENTER:
                if(!m_sbMain.isFocused()) {
                    handled = false;
                    break;
                }
                voLog.v(TAG, "onKeyUp - KEYCODE_DPAD_CENTER -> KEYCODE_MEDIA_PLAY_PAUSE");
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                voLog.v(TAG, "onKeyUp - KEYCODE_MEDIA_PLAY_PAUSE");
                VO_OSMP_RETURN_CODE ret = m_cPlayer.pause();
                if (ret == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    voLog.e(TAG, "player.pause: " + ret);
                }
                checkPlayerStatus();
                handled = true;
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                voLog.v(TAG, "onKeyUp - KEYCODE_DPAD_RIGHT/KEYCODE_DPAD_LEFT");
                if(mIsSeekbarProgressChangedByUser) {
                    mIsSeekbarProgressChangedByUser = false;
                    seekByProgressBar();
                }

                break;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
		voLog.v(TAG, "onKeyUp - KEYCODE_MEDIA_FAST_FORWARD");
                timeStepOfMediaControl = TIME_STEP_MS_PER_SEEK;
                handled = true;
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                voLog.v(TAG, "onKeyUp - KEYCODE_MEDIA_FAST_FORWARD");
                timeStepOfMediaControl = TIME_STEP_MS_PER_SEEK;
                handled = true;
                break;
        }

        return handled || super.onKeyUp(keyCode, event);
    }

    private boolean mIsSeekbarProgressChangedByUser = false;

    /* Stop player and exit on Back key */
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean handled = false;
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                voLog.v(TAG, "onKeyDown: Back key");
                hideMediaControllerImpl();
                stopVideo();
                uninitPlayer();
                m_abManager.setDelegate(null);
                m_cPlayer.setUIListener(null);
                handled = true;
                this.finish();
                break;

            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                voLog.v(TAG, "onKeyDown - KEYCODE_MEDIA_FAST_FORWARD");
                timeStepOfMediaControl += TIME_STEP_MS_PER_SEEK;
                handler.sendMessageDelayed(handler.obtainMessage(MSG_MEDIA_SEEK_CONTROL,
                        KeyEvent.KEYCODE_MEDIA_FAST_FORWARD, 0), 200);
                handled = true;

                break;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
                voLog.v(TAG, "onKeyDown - KEYCODE_MEDIA_REWIND");
                timeStepOfMediaControl += TIME_STEP_MS_PER_SEEK;
                handler.sendMessageDelayed(handler.obtainMessage(MSG_MEDIA_SEEK_CONTROL,
                        KeyEvent.KEYCODE_MEDIA_REWIND, 0), 200);
                handled = true;

                break;
        }

        return handled || super.onKeyDown(keyCode, event);
    }

    /* Notify SDK of configuration change */
    public void onConfigurationChanged(Configuration newConfig) {

        // task 52179
        if (null == m_cPlayer) {
            super.onConfigurationChanged(newConfig);
            return;
        }

        m_cPlayer.updateToDefaultVideoSize(m_nAspectRatio);
        super.onConfigurationChanged(newConfig);
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onStart()
    */
    @Override
    protected void onStart() {
        voLog.v(TAG, "Player onStart");
        super.onStart();
    }

    /* (non-Javadoc)
    * @see android.app.Activity#onStop()
    */
    @Override
    protected void onStop() {
        voLog.v(TAG, "Player onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        hideMediaControllerImpl();
        if (m_HDMIStateCheck != null)
            m_HDMIStateCheck.enableHDMIDetection(this, false);
        if (m_dlgAsset != null)
            m_dlgAsset.dismiss();
        if (m_dlgSpecial != null)
            m_dlgSpecial.dismiss();

        super.onDestroy();
        voLog.v(TAG, "Player onDestroy Completed!");
    }

    /* Pause/Stop playback on activity pause */
    protected void onPause() {
        voLog.v(TAG, "Player onPause");
        super.onPause();

        removeSurfaceHolderCallback();

        if (m_isPlayerRun) {
            m_cPlayer.suspend(); // If you want to continue to play music, please input parameter "true"
            m_isResumePlayerNeeded = true;
        }

        if(m_secondPlayer != null &&
            m_secondPlayer.getPlayerStatus() == VOOSMPType.VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
            m_secondPlayer.pause();
        }
    }

    private boolean isNow2ndPlayerShow() {
        if(m_btnSwitch != null &&
                m_btnSwitch.getText().toString().indexOf("SecondPlayer") != -1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onResume() {
        voLog.v(TAG, "+++ Player onResume +++ ");
        super.onResume();

        addSurfaceHolderCallback();

        if (m_isPlayerRun){
            m_isResumePlayerNeeded = true;
            handler.sendEmptyMessage(MSG_CHECK_SURFACE_CREATED);
        }

        if(isNow2ndPlayerShow()) {
            m_secondPlayer.unmute();
        }


        checkPlayerStatus();
        voLog.v(TAG, "--- Player onResume --- ");
    }


    /* (non-Javadoc)
    * @see android.app.Activity#onRestart()
    */
    @Override
    protected void onRestart() {
        voLog.v(TAG, "Player onRestart");
        super.onRestart();

        if (m_cPlayer == null)
            return;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {

            voLog.d(TAG, "onTouchEvent - MotionEvent.ACTION_DOWN");

            if (m_rlBottom.getVisibility() != View.VISIBLE) {
                showMediaControllerImpl();
            } else {
                hideMediaControllerImpl();
            }
        }
        return super.onTouchEvent(event);

    }

    private Button m_btnSwitch = null;
    private boolean m_bShowMediaController = true;
    private DisplayMetrics m_dm = null;

    @SuppressLint("NewApi")
    private void setupPlayer2Pos() {
        if (m_svMain2 != null) {
            m_svMain2.setTranslationX(5120);
        }
    }

    @SuppressLint("NewApi")
    private void switch2Player(int playerIndex) {

        voLog.d(TAG, "switch2Player: " + playerIndex);

        switch(playerIndex) {
            case 1:

                m_btnSwitch.setText("FirstPlayer");

                m_secondPlayer.pause();
                m_secondPlayer.mute();

                m_svMain2.setTranslationX(5120);
                m_svMain.setTranslationX(0);

                m_bShowMediaController = true;

                m_svMain.setVisibility(View.VISIBLE);

                if( m_isResumePlayerNeeded== true ) {
                    m_cPlayer.pause();
                }

                m_cPlayer.getAPPControl().setViewSize(m_dm.widthPixels, m_dm.heightPixels);

                break;

            case 2:

                m_btnSwitch.setText("SecondPlayer");
                m_svMain.setTranslationX(5120);
                m_svMain2.setTranslationX(0);

                if(m_cPlayer.isNowPlaying() == true) {
                    m_cPlayer.pause();
                    m_isResumePlayerNeeded = true;
                } else {
                    m_isResumePlayerNeeded = false;
                }

                m_bSecondPlayerRun = true;
                m_bShowMediaController = false;
                hideMediaControllerImpl();
                m_btnSwitch.setVisibility(View.VISIBLE);

                m_secondPlayer.start();
                m_secondPlayer.unmute();
                m_secondPlayer.setViewSize(m_dm.widthPixels, m_dm.heightPixels);

                break;
        }

        checkPlayerStatus();
    }


    private void initLayout() {

        m_rlBottom = (LinearLayout) findViewById(R.id.rlBottom);

        m_rlDownloader = (RelativeLayout) findViewById(R.id.rlDownloader);
        m_llRightBottom = (LinearLayout) findViewById(R.id.llRightBottom);
        m_tvDownloadCurrent = (TextView) findViewById(R.id.tvDownloaderPercentage);
        m_tvDownloadTotal = (TextView) findViewById(R.id.tvDownloaderTotal);


        m_btnSwitch.setText("FirstPlayer");
        m_btnSwitch.setOnClickListener(new View.OnClickListener() {

            @SuppressLint("NewApi")
            @Override
            public void onClick(View v) {

                if (!m_bSecondPlayePrepareWell)
                    return;

                if (m_btnSwitch.getText() == "FirstPlayer") {
                    switch2Player(2);
                } else if (m_btnSwitch.getText() == "SecondPlayer") {
                    switch2Player(1);
                }
            }
        });

        // Controls about NTS URL
        m_btnEnterNts = (Button)findViewById(getResources().getIdentifier("btnEnterNTS", "id", mContext.getPackageName()));
        if(m_btnEnterNts != null && m_cPlayer.isEnterNTSEnabled() == true) {
            m_btnEnterNts.setVisibility(View.VISIBLE);
            m_btnEnterNts.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    m_bEnterNTS = true;

                    m_nNTSPlayingTime = m_cPlayer.getUTCPosition();

                    stopVideo();

                    m_cPlayer.setPlayerURL(m_cPlayer.getNTSUrl());
                    playerStart();
                }
            });
        }

        initLayoutTop();
        initLayoutBottom();
    }

    private Button m_btnEnterNts = null;
    private boolean m_bEnterNTS = false;

    private final static int DEFAULT_NO_UTC_POSITION = 10000;
    private int getPositionToSeek(String strNtsStartTime, String strNtsSeekPos){

        int n = -1;
        if(strNtsSeekPos != null && strNtsSeekPos.length() > 0){
            String s = strNtsSeekPos;
            n = Long.valueOf(s).intValue();
        }

        if (m_bEnterNTS) {
            if (strNtsStartTime != null && strNtsStartTime.length() > 0) {

                if(m_nNTSPlayingTime <= 0) {
                    n = DEFAULT_NO_UTC_POSITION;
                } else {
                    n = (int) (m_nNTSPlayingTime - Long.valueOf(strNtsStartTime));
                }

                voLog.d(TAG, "@func: getPositionToSeek - " + m_nNTSPlayingTime + ", " + strNtsStartTime + ", " + n);

            } else {
                n = DEFAULT_NO_UTC_POSITION;
            }
        }
        return n;
    }

    private long m_nNTSPlayingTime = DEFAULT_NO_UTC_POSITION;

    private void initLayoutTop() {
        m_sbMain = (SeekBar) findViewById(R.id.sbMain);
        m_tvBps = (TextView) findViewById(R.id.tvBps);
        m_tvResolutionW = (TextView) findViewById(R.id.tvResolutionW);
        m_tvResolutionH = (TextView) findViewById(R.id.tvResolutionH);
        m_tvCurrentTime = (TextView) findViewById(R.id.tvCurrentTime);
        m_tvUTCPosition   = (TextView)findViewById(getResources().getIdentifier("tvUTCPosition","id", this.getPackageName()));
        m_tvMinTime = (TextView) findViewById(R.id.tvMinTime);
        m_tvMaxTime = (TextView) findViewById(R.id.tvMaxTime);
        m_tvHDMI = (TextView) findViewById(R.id.tvHDMI);
        m_pbLoadingProgress = (ProgressBar) findViewById(R.id.pbBuffer);
        m_tvLoadingProgress = (TextView) findViewById(R.id.tvBufferValue);
        m_sbMain.setOnSeekBarChangeListener(m_listenerSeekBar);

        m_pbLoadingProgress.setVisibility(View.GONE);
        m_tvLoadingProgress.setVisibility(View.GONE);

    }

    private void initLayoutBottom() {
        initPlayerControlFeatureLayout();
        initPlayerAssetFeatureLayout();
        initPlayerAudioSpeedFeatureLayout();
        initPlayerSubtitleFeatureLayout();
        initPlayerFastChannelFeatureLayout();
        initPlayerSpecialFeatureLayout();
        initPlayerAnalyticsDisplayFeatureLayout();
        initPlayerADFeatureLayout();
        initPlayerDolbyFeatureLayout();
    }


    private boolean m_isPlaying = true;

    private void checkPlayerStatus() {
        VOOSMPType.VO_OSMP_STATUS status = m_cPlayer.getPlayerStatus();
        if(status == null || m_btnPause == null) {
            return;
        }

        switch(status) {
            case VO_OSMP_STATUS_PAUSED:
            case VO_OSMP_STATUS_STOPPED:
                m_btnPause.setImageResource(R.drawable.ic_play);
                break;
            case VO_OSMP_STATUS_PLAYING:
            default:
                m_btnPause.setImageResource(R.drawable.ic_pause);
                break;
        }
    }

    private void initPlayerControlFeatureLayout() {
        m_btnPause = (ImageButton) findViewById(R.id.btPause);
        m_btnStop = (ImageButton) findViewById(R.id.btStop);
        //m_btnPause.setText(Definition.PAUSE);
        m_btnPause.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                VO_OSMP_RETURN_CODE ret = m_cPlayer.pause();
                if (ret == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    voLog.e(TAG, "player.pause: " + ret);
                }

                checkPlayerStatus();
            }
        });

        m_btnStop.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideMediaControllerImpl();
                stopVideo();
                uninitPlayer();

                if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCESTOPKEEPLASTFRAME_ID.getValue()).getSelect() == 1) {
                    voLog.d(TAG, "Option - KEEP LAST FRAME ON is enabled!");

                    android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
                    InputFragment fragment = new InputFragment();
                    fragmentManager.beginTransaction().add(R.id.fragment_container, fragment, "inputfragment").commit();

                    return;
                }

                if (m_svMain != null) {
                    m_svMain.setVisibility(View.INVISIBLE);
                }
                if (m_svMain2 != null) {
                    m_svMain2.setVisibility(View.INVISIBLE);
                }
                finish();
            }
        });
    }

    private void initPlayerAssetFeatureLayout() {
        m_btnAsset = (View) findViewById(R.id.btAsset);
        m_btnAsset.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (m_dlgAsset == null)
                    m_dlgAsset = new AssetDialog(BasePlayer.this, R.style.Dialog_GrayWhite);
                if (m_asset != null)
                    m_dlgAsset.updateAssetInfo(m_asset);

                m_dlgAsset.setCanceledOnTouchOutside(true);
                m_dlgAsset.show();

            }
        });
    }

    private void initPlayerAudioSpeedFeatureLayout() {
        m_btnAudioLow = (ImageButton) findViewById(R.id.btSpeedLow);
        m_btnAudioHigh = (ImageButton) findViewById(R.id.btSpeedHigh);
        m_tvAduioSpeed = (TextView) findViewById(R.id.tvSpeedValue);
        m_btnAudioHigh.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                float highSpeed;
                highSpeed = m_fAudioSpeed + 0.1f;
                if (highSpeed > 4.0f)
                    highSpeed = 4.0f;
                updateAudioSpeed(highSpeed);
            }
        });
        m_btnAudioLow.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                float lowSpeed;
                lowSpeed = m_fAudioSpeed - 0.1f;
                if (lowSpeed < 0.5f)
                    lowSpeed = 0.5f;
                updateAudioSpeed(lowSpeed);

            }
        });

    }

    private void updateAudioSpeed(float speed) {
        String s = Float.toString(speed + 0.0001f);
        if (s.length() > 3)
            s = s.substring(0, 3);

        if (m_cPlayer != null) {
            VO_OSMP_RETURN_CODE ret;
            ret = this.m_cPlayer.setAudioSpeed(speed);

            if (VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE == ret) {
                m_tvAduioSpeed.setText(s);
                m_fAudioSpeed = speed;
            }
        }
    }

    private void initPlayerSubtitleFeatureLayout() {
        m_chbSubtitle = (CheckBox) findViewById(R.id.chbEnableST);
        if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue()).getSelect() == 1) {
            if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_SUBTITLE_ID.getValue()).getSelect() == 1)
                m_chbSubtitle.setChecked(true);
            else
                m_chbSubtitle.setChecked(false);
        } else
            m_chbSubtitle.setChecked(false);
        m_chbSubtitle.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    voLog.d(TAG, ">> enableSubtitle(true)");
                    m_cPlayer.enableSubtitle(true);
                    m_editor.putBoolean(String.valueOf(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue()), true);
                    m_editor.putBoolean(String.valueOf(OPTION_ID.OPTION_SUBTITLE_ID.getValue()), true);
                    m_editor.commit();
                } else {
                    voLog.d(TAG, ">> enableSubtitle(false)");
                    m_cPlayer.enableSubtitle(false);
                    m_editor.putBoolean(String.valueOf(OPTION_ID.OPTION_SUBTITLE_ID.getValue()), false);
                    m_editor.commit();
                }
            }

        });
    }

    private void initPlayerFastChannelFeatureLayout() {
        m_btnPrev = (Button) findViewById(R.id.btPre);
        m_btnNext = (Button) findViewById(R.id.btNext);
        m_btnPrev.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String preFastChannelUrl = getPreFastChannelUrl();
                if (preFastChannelUrl == null)
                    return;
                stopVideo();
                m_cPlayer.setPlayerURL(preFastChannelUrl);
                playerStart();

            }
        });
        m_btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String nextFastChannelUrl = getNextFastChannelUrl();
                if (nextFastChannelUrl == null)
                    return;
                stopVideo();
                m_cPlayer.setPlayerURL(nextFastChannelUrl);
                playerStart();

            }
        });
    }

    private void initPlayerSpecialFeatureLayout() {
        m_btnSpecial = (Button) findViewById(R.id.btSpecial);
        m_btnSpecial.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (m_cSpecialPlayer == null)
                    m_cSpecialPlayer = m_cPlayer.getSpecialFeatureFunction();

                if (m_dlgSpecial == null)
                    m_dlgSpecial = new SpecialDialog(BasePlayer.this, R.style.Dialog_Translucent);

                m_cSpecialPlayer.setAPPUrl(m_cPlayer.getPlayerURL());
                m_cSpecialPlayer.setContext(BasePlayer.this);
                m_cSpecialPlayer.setVideoWidth(m_nVideoWidth);
                m_cSpecialPlayer.setVideoHeight(m_nVideoHeight);
                m_dlgSpecial.setCSpecialPlayer(m_cSpecialPlayer);
                m_dlgSpecial.setCanceledOnTouchOutside(true);
                m_dlgSpecial.show();
            }
        });
    }

    private void initPlayerAnalyticsDisplayFeatureLayout() {
        m_btnAnalytics = (Button) findViewById(R.id.btAnalytics);
        m_btnAnalytics.setText(Definition.ANALYTICS_DISPLAY_NULL);
        m_btnAnalytics.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m_btnAnalytics.getText() == Definition.ANALYTICS_DISPLAY_NULL) {
                    m_btnAnalytics.setText(Definition.ANALYTICS_DISPLAY_PLAYER);
                    m_cPlayer.setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE.VO_OSMP_DISPLAY_PLAYER);
                } else if (m_btnAnalytics.getText() == Definition.ANALYTICS_DISPLAY_PLAYER) {
                    m_btnAnalytics.setText(Definition.ANALYTICS_DISPLAY_SOURCE);
                    m_cPlayer.setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE.VO_OSMP_DISPLAY_SOURCE);
                } else if (m_btnAnalytics.getText() == Definition.ANALYTICS_DISPLAY_SOURCE) {
                    m_btnAnalytics.setText(Definition.ANALYTICS_DISPLAY_RENDER);
                    m_cPlayer.setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE.VO_OSMP_DISPLAY_RENDER);
                } else if (m_btnAnalytics.getText() == Definition.ANALYTICS_DISPLAY_RENDER) {
                    m_btnAnalytics.setText(Definition.ANALYTICS_DISPLAY_NULL);
                    m_cPlayer.setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE.VO_OSMP_DISPLAY_NULL);
                }
            }
        });
    }


    private void initPlayerADFeatureLayout() {
        m_btnADSkip = (Button) findViewById(R.id.btnSkipAd);
        m_btnShowADUrl = (Button) findViewById(R.id.btnShowAdUrl);
        m_btnADSkip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                m_ADManager.setADSkipAction();
                m_btnADSkip.setVisibility(View.GONE);
                if (handler != null)
                    handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
            }
        });
        m_btnShowADUrl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String adUrl = (null == m_ADManager) ? null : m_ADManager.getVideoAdClickThru();
                if (adUrl != null) {
                    if (adUrl.length() > 0) {
                        new AlertDialog.Builder(BasePlayer.this)
                                .setTitle("AD URL")
                                .setMessage(adUrl)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                }).show();
                    }
                }
            }
        });
    }

    private void initPlayerDolbyFeatureLayout() {
        DolbyImageView view = new DolbyImageView(this, m_cPlayer);
        if (!view.isViewEnabled())
            return;
        RelativeLayout.LayoutParams dolbyLayout = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dolbyLayout.addRule(RelativeLayout.BELOW, R.id.btAnalytics);
        dolbyLayout.addRule(RelativeLayout.ALIGN_LEFT, R.id.btAnalytics);
        m_rlBottom.addView(view, dolbyLayout);
    }

    private void doUpdateUI() {

        checkPlayerStatus();

        if (!m_isSurfaceCreated)
            return;
        if (m_isPlayerStop)
            return;

        int index = 0;
        index = m_cPlayer.getAssetIndex(AssetType.Asset_Video, AssetStatus.Asset_Playing);
        String strBps = "0";
        index += 1;
        if (m_dlgAsset != null)
            m_lstVideo = m_dlgAsset.getVideoArrayList();
        if (m_lstVideo != null && m_lstVideo.size() > index)
            strBps = m_lstVideo.get(index);

        if (strBps != null && !strBps.equals("0 bps"))
            m_tvBps.setText(strBps);
        else
            m_tvBps.setText("0");

        if (isDownloadEnable() && m_cDownloader != null) {

            int downloadCurrent = m_cDownloader.getDownloadedStreamDuration();
            int downloadTotal = m_cDownloader.getTotalStreamDuration();
            m_tvDownloadCurrent.setText("Download:" + Integer.toString(downloadCurrent));
            m_tvDownloadTotal.setText("Total:" + Integer.toString(downloadTotal));
            m_tvDownloadCurrent.setVisibility(View.VISIBLE);
            m_tvDownloadTotal.setVisibility(View.VISIBLE);

        } else {
            m_tvDownloadCurrent.setVisibility(View.INVISIBLE);
            m_tvDownloadTotal.setVisibility(View.INVISIBLE);
        }

        if (m_updateADUI) {
            int count = mPlaybackInfo.getCount();
            ArrayList<VOOSMPAdPeriod> periodList = mPlaybackInfo.getPeriodList();
            if (periodList == null || 0 >= count) {
                return;
            }
            int size = periodList.size();
            for (int i = 0; (i < count) && i < size; ++i) {
                VOOSMPAdPeriod period = periodList.get(i);
                if (period != null) {
                    if (m_currPid == period.getID() && VOOSMPAdPeriod.VO_ADSMANAGER_PERIODTYPE_ADS == period.getPeriodType()) {
                        m_nMaxTime = period.getEndTime() - period.getStartTime();
                        m_nCurrentTime = m_ADPlayingTime - period.getStartTime();
                        m_nMinTime = 0;

                    }

                }
            }

        } else {
            m_nCurrentTime = m_cPlayer.getCurrentPosition();
            m_nMaxTime = m_cPlayer.getRightPosition();
            m_nMinTime = m_cPlayer.getLeftPosition();
        }

        long nDuration;
        nDuration = m_nMaxTime - m_nMinTime;
        long nPos;
        nPos = m_nCurrentTime - m_nMinTime;
        if (nDuration > 0 && !mIsSeekbarProgressChangedByUser) {
            m_sbMain.setProgress((int) (100 * nPos / nDuration));
        }

        if (m_nMinTime < 0 || m_nCurrentTime < 0) {
            m_nMinTime = -m_nMinTime;
            m_nCurrentTime = -m_nCurrentTime;
            m_tvMinTime.setText("-" + CommonFunc.formatTime(m_nMinTime / 1000) + "/");
            m_tvCurrentTime.setText("-" + CommonFunc.formatTime(m_nCurrentTime / 1000));
        } else {
            m_tvMinTime.setText(CommonFunc.formatTime(m_nMinTime / 1000) + "/");
            m_tvCurrentTime.setText(CommonFunc.formatTime(m_nCurrentTime / 1000));
        }

        m_tvMaxTime.setText(CommonFunc.formatTime(m_nMaxTime / 1000));

        // UTC TIME presentation.
        long UTCPosition = (Long) m_cPlayer.getUTCPosition();

        if (UTCPosition <= 0) {
            m_tvUTCPosition.setText("--");
        }else {
            String UTCFormatTime = formatUTCPosition(UTCPosition).replace(" ", "\n");
            m_tvUTCPosition.setText(UTCFormatTime);
        }
    }

    private String formatUTCPosition(long time){
        Date date = new Date(time);
        SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        return format.format(date);
    }

    private String getPreFastChannelUrl() {
        if (m_lstSelectURL.size() == 0)
            return null;
        if (m_nFastChannelIndex == 0)
            m_nFastChannelIndex = m_lstSelectURL.size() - 1;
        else
            m_nFastChannelIndex -= 1;
        return m_lstSelectURL.get(m_nFastChannelIndex);
    }

    private String getNextFastChannelUrl() {
        if (m_lstSelectURL.size() == 0)
            return null;
        if (m_nFastChannelIndex == m_lstSelectURL.size() - 1)
            m_nFastChannelIndex = 0;
        else
            m_nFastChannelIndex += 1;
        return m_lstSelectURL.get(m_nFastChannelIndex);
    }

    private void getVideoDescription(ArrayList<String> lstString) {

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Video);
        if (nAssetCount == 0)
            return;

        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {

            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Video, nAssetIndex);
            String strDescription;

            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = "V" + Integer.toString(nAssetIndex);
            } else {
                final int KEY_DESCRIPTION_INDEX = 2;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            if (strDescription.length() > 4) {
                String str = strDescription.substring(0, (strDescription.length() - 4));
                voLog.d(TAG, "getVideoDescription:str = " + str);
                if (str.equals("0")) {
                    lstString.add("0");
                } else {
                    lstString.add(CommonFunc.bitrateToString(Integer.valueOf(str).intValue()));
                }
            }
        }
    }

    private void getAudioDescription(ArrayList<String> lstString) {

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Audio);
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            if (!m_asset.isTrackAvailable(AssetType.Asset_Audio, nAssetIndex)) {
                continue;
            }

            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Audio, nAssetIndex);

            String strDescription;

            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = "A" + Integer.toString(nDefaultIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            lstString.add(strDescription);
        }
    }

    private void getSubtitleDescription(ArrayList<String> lstString) {

        if (lstString == null || m_asset == null)
            return;

        int nAssetCount = m_asset.getAssetCount(AssetType.Asset_Subtitle);
        if (nAssetCount == 0)
            return;

        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {

            VOOSMPAssetProperty propImpl = m_asset.getAssetProperty(AssetType.Asset_Subtitle, nAssetIndex);
            String strDescription;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {

                strDescription = "Subt" + Integer.toString(nAssetIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            lstString.add(strDescription);
        }
    }

    private void fillDownloaderProgramInfo() {
        voLog.i(TAG, "m_downloader Video count is %d, audio count is %d, subtitle count is %d, videx index is %d , audio index is %d , subtitle index is %d .",
                m_cDownloader.getAssetCount(AssetType.Asset_Video), m_cDownloader.getAssetCount(AssetType.Asset_Audio), m_cDownloader.getAssetCount(AssetType.Asset_Subtitle)
                , m_cDownloader.getAssetIndex(AssetType.Asset_Video, AssetStatus.Asset_Selected), m_cDownloader.getAssetIndex(AssetType.Asset_Audio, AssetStatus.Asset_Selected), m_cDownloader.getAssetIndex(AssetType.Asset_Subtitle, AssetStatus.Asset_Selected));
        for (int i = 0; i < m_cDownloader.getAssetCount(AssetType.Asset_Video); i++) {
            String videoPro = "Index is ";
            for (int j = 0; j < m_cDownloader.getAssetProperty(AssetType.Asset_Video, i).getPropertyCount(); j++) {
                videoPro += "key = " + m_cDownloader.getAssetProperty(AssetType.Asset_Video, i).getKey(j)
                        + ", value = " + (String) m_cDownloader.getAssetProperty(AssetType.Asset_Video, i).getValue(j) + " ; ";
            }
            voLog.i(TAG, "m_downloader " + videoPro);
        }

        LayoutInflater inflater;
        View layout;
        inflater = LayoutInflater.from(BasePlayer.this);
        layout = inflater.inflate(R.layout.download_asset, null);
        final Spinner sp_downloadSelectVideo = (Spinner) layout.findViewById(R.id.spDownloadSelectVideo);
        final Spinner sp_downloadSelectAudio = (Spinner) layout.findViewById(R.id.spDownloadSelectAudio);
        final Spinner sp_downloadSelectSubtitle = (Spinner) layout.findViewById(R.id.spDownloadSelectSubtitle);

        TextView tv_downloadVideo = (TextView) layout.findViewById(R.id.tvDownloadSelectVideo);
        TextView tv_downloadAudio = (TextView) layout.findViewById(R.id.tvDownloadSelectAudio);
        TextView tv_downloadSubtitle = (TextView) layout.findViewById(R.id.tvDownloadSelectSubtitle);

        if (m_cDownloader.getAssetCount(AssetType.Asset_Video) == 0) {
            tv_downloadVideo.setVisibility(View.GONE);
            sp_downloadSelectVideo.setVisibility(View.GONE);
        }

        if (m_cDownloader.getAssetCount(AssetType.Asset_Audio) == 0) {
            tv_downloadAudio.setVisibility(View.GONE);
            sp_downloadSelectAudio.setVisibility(View.GONE);
        }

        if (m_cDownloader.getAssetCount(AssetType.Asset_Subtitle) == 0) {
            tv_downloadSubtitle.setVisibility(View.GONE);
            sp_downloadSelectSubtitle.setVisibility(View.GONE);
        }

        ArrayList<String> lstVideo = new ArrayList<String>();
        getVideoDescription(lstVideo);
        lstVideo.add(0, getResources().getString(R.string.Player_DownloadSelectVideo));
        ArrayAdapter<String> adapterVideo = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lstVideo);

        sp_downloadSelectVideo.setAdapter(adapterVideo);
        final Context ctx = this;
        sp_downloadSelectVideo.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int index = sp_downloadSelectVideo.getSelectedItemPosition() - 1;
                VO_OSMP_RETURN_CODE nRet = m_cDownloader.selectAsset(AssetType.Asset_Video, index);
                int rc = m_abManager.processReturnCode("Download selectVideo", nRet.getValue());
                if (rc == 1)
                    return;

                ArrayList<String> lstAudio = new ArrayList<String>();
                getAudioDescription(lstAudio);
                ArrayAdapter<String> adapterAudio = new ArrayAdapter<String>(ctx,
                        android.R.layout.simple_spinner_item, lstAudio);

                sp_downloadSelectAudio.setAdapter(adapterAudio);
                sp_downloadSelectAudio.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                        int index = sp_downloadSelectAudio.getSelectedItemPosition();
                        VO_OSMP_RETURN_CODE nRet = m_cDownloader.selectAsset(AssetType.Asset_Audio, index);
                        m_abManager.processReturnCode("Download selectAudio", nRet.getValue());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });

                ArrayList<String> lstSubtitle = new ArrayList<String>();
                getSubtitleDescription(lstSubtitle);

                ArrayAdapter<String> adapterSubtitle = new ArrayAdapter<String>(ctx,
                        android.R.layout.simple_spinner_item, lstSubtitle);

                sp_downloadSelectSubtitle.setAdapter(adapterSubtitle);
                sp_downloadSelectSubtitle.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                        int index = sp_downloadSelectSubtitle.getSelectedItemPosition();

                        VO_OSMP_RETURN_CODE nRet = m_cDownloader.selectAsset(AssetType.Asset_Subtitle, index);
                        m_abManager.processReturnCode("Download selectSubtitle", nRet.getValue());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }
                });
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        m_adlgDownload = new AlertDialog.Builder(BasePlayer.this)
                .setTitle("Select Asset")
                .setView(layout)
                .setPositiveButton("OK", new OnClickListener() {
                    // "OK" button begins playback of inputted media source
                    public void onClick(DialogInterface dialog, int which) {
                        VO_OSMP_RETURN_CODE nRet = m_cDownloader.commitSelection();
                        int rc = m_abManager.processReturnCode("Download commit selection", nRet.getValue());
                        if (rc == 1)
                            return;
                        if (m_cDownloader != null)
                            m_cDownloader.start();
                    }
                })
                .setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                        // "Back" button stops player and exits
                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                            arg0.dismiss();
                            stopVideo();
                            uninitPlayer();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        m_adlgDownload.setCanceledOnTouchOutside(false);
        m_adlgDownload.show();
    }

    private void playerStart() {

        if (m_isAppStop || m_isPlayerRun)
            return;

        voLog.v(TAG, "+++ playerStart +++");

        int rc = 0;
        VO_OSMP_RETURN_CODE nRet;
        m_cPlayer.setPlayView(m_svMain);
        if (isDownloadEnable()) {
            nRet = m_cDownloader.open(m_cPlayer.getPlayerURL(), 0, Definition.DOWNLOAD_PATH);
            rc = m_abManager.processReturnCode("Download open", nRet.getValue());
            if (rc == 1)
                return;
            else
                voLog.v(TAG, "Downloader is opened.");
        } else {
            nRet = m_cPlayer.start();
            rc = m_abManager.processReturnCode("CPlayer start", nRet.getValue());
            if (rc == 1)
                return;
            syncOpenPreparation();
        }
        m_isPlayerRun = true;
        voLog.v(TAG, "--- playerStart ---");
    }
    @Override
    public VO_OSMP_RETURN_CODE onEvent(APP_UI_EVENT_ID event, int nParam1,
                                       int nParam2, Object obj) {
        switch (event) {
            case APP_UI_EVENT_PLAY_COMPLETE:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
                break;
            case APP_UI_EVENT_VIDEO_ASPECT_RATIO:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                m_nAspectRatio = VO_OSMP_ASPECT_RATIO.valueOf(nParam1);
                break;
            case APP_UI_EVENT_VIDEO_SIZE_CHANGED:
                m_nVideoWidth = nParam1;
                m_nVideoHeight = nParam2;
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                m_tvResolutionW.setText(Integer.toString(nParam1) + "x");
                m_tvResolutionH.setText(Integer.toString(nParam2));
                if (m_cSpecialPlayer != null) {
                    m_cSpecialPlayer.setVideoWidth(m_nVideoWidth);
                    m_cSpecialPlayer.setVideoHeight(m_nVideoHeight);
                }
                break;
            case APP_UI_EVENT_VIDEO_STOP_BUFFER:
            case APP_UI_EVENT_AUDIO_STOP_BUFFER:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                m_pbLoadingProgress.setVisibility(View.INVISIBLE);
                m_tvLoadingProgress.setVisibility(View.INVISIBLE);
                break;
            case APP_UI_EVENT_VIDEO_START_BUFFER:
            case APP_UI_EVENT_AUDIO_START_BUFFER:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                m_pbLoadingProgress.setVisibility(View.VISIBLE);
                break;
            case APP_UI_EVENT_PD_DOWNLOAD_POSITION:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                if (nParam1 >= 99) {
                    m_sbMain.setSecondaryProgress(100);
                } else {
                    m_sbMain.setSecondaryProgress(nParam1);
                }
                break;
            case APP_UI_EVENT_PD_BUFFERING_PERCENT:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                if (nParam1 >= 99) {
                    m_pbLoadingProgress.setVisibility(View.GONE);
                    m_tvLoadingProgress.setVisibility(View.GONE);
                } else {
                    m_pbLoadingProgress.setVisibility(View.VISIBLE);
                    m_tvLoadingProgress.setVisibility(View.VISIBLE);
                    m_tvLoadingProgress.setText(Integer.toString(nParam1) + "%");
                }
                break;
            case APP_UI_EVENT_OPEN_FINISHED:
                m_isPlayerStop = false;
                showMediaControllerImpl();
                m_asset = m_cPlayer;

                if(m_cPlayer.isEnabledDVRPositionSetting() == false) {
                    m_cPlayer.enableLiveStreamingDVRPosition(false);
                }

                // For Entering NTS Streaming
                if(m_bEnterNTS || !m_cPlayer.isLiveStreaming()) {

                    String minPos = m_cPlayer.getNTSMinPos();
                    String seekValue = m_cPlayer.getPreviousSeekValue();

                    int n = getPositionToSeek(minPos, seekValue);
                    if(n>=0){
                        m_cPlayer.seekTo(n);
                    }
                    m_bEnterNTS = false;
                }

                break;
            case APP_UI_EVENT_PROGRAM_CHANGED:

                if (m_dlgAsset == null)
                    m_dlgAsset = new AssetDialog(BasePlayer.this, R.style.Dialog_GrayWhite);
                if (m_asset != null) {
                    voLog.d(TAG, ">>> updateAssetInfo from APP_UI_EVENT_PROGRAM_CHANGED");
                    m_dlgAsset.updateAssetInfo(m_asset);
                }
            case APP_UI_EVENT_PROGRAM_RESET:
                m_abManager.processEvent(event.getValue(), nParam1, nParam2, null);
                break;
            case APP_UI_EVENT_AD_START:
                m_currPid = nParam1;
                handler.sendEmptyMessage(MSG_AD_START);
                break;
            case APP_UI_EVENT_AD_END:
                m_updateADUI = false;
                m_currPid = nParam1;
                handler.sendEmptyMessage(MSG_AD_END);
                break;
            case APP_UI_EVENT_VIDEO_PROGRESS:
                m_updateADUI = true;
                m_ADPlayingTime = nParam1;
                break;
            case APP_UI_EVENT_AD_PLAYBACKINFO:
                mPlaybackInfo = (VOOSMPAdInfo) obj;
                break;
            case APP_UI_EVENT_AD_SKIPPABLE:
                handler.sendEmptyMessage(MSG_AD_SKIPPABLE);
                break;
            default:
                break;
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    @Override
    public VO_OSMP_RETURN_CODE onDownloadEvent(
            APP_UI_EVENT_ID event, int arg1, int arg2,
            Object obj) {
        switch (event) {
            case APP_UI_EVENT_STREAMING_DOWNLOADER_OPEN_COMPLETE:
                VO_OSMP_DRM_KEY_EXPIRED_STATUS expiredStatus = m_cDownloader.getDRMKeyExpiredStatus();
                if (expiredStatus == VO_OSMP_DRM_KEY_EXPIRED_STATUS.VO_OSMP_DRM_KEY_EXPIRED_YES ||
                        expiredStatus == VO_OSMP_DRM_KEY_EXPIRED_STATUS.VO_OSMP_DRM_KEY_EXPIRED_ERROR) {
                    abManagerDelegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_STOP_PLAY, "DRM key expired");
                    break;
                }
                m_asset = m_cDownloader;
                fillDownloaderProgramInfo();
                break;
            case APP_UI_EVENT_STREAMING_DOWNLOADER_MANIFEST_OK:
                m_cPlayer.setPlayerURL((String) obj);
                VO_OSMP_RETURN_CODE nRet = m_cPlayer.start();
                m_abManager.processReturnCode("CPlayer start", nRet.getValue());
                syncOpenPreparation();
                break;
            default:
                break;
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    private boolean isDownloadEnable() {
        if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_DOWNLOAD_ID.getValue()).getSelect() == 1)
            return true;
        else
            return false;
    }

    private void showMediaControllerImpl() {

        voLog.d(TAG, "+++ showMediaControllerImpl +++");

        if (m_ttMain != null)
            m_ttMain = null;
        m_ttMain = new TimerTask() {
            public void run() {
                handler.sendEmptyMessage(MSG_UPDATE_UI);
            }
        };

        if (m_timerMain == null)
            m_timerMain = new Timer();

        if (isNow2ndPlayerShow()) {
            return;
        }

        m_timerMain.schedule(m_ttMain, 0, 1000);
        m_rlBottom.setVisibility(View.VISIBLE);
        m_rlDownloader.setVisibility(View.VISIBLE);

        voLog.d(TAG, "--- showMediaControllerImpl ---");
    }

    private void hideMediaControllerImpl() {

        voLog.d(TAG, "+++ hideMediaControllerImpl +++");

        if (m_timerMain != null) {
            m_timerMain.cancel();
            m_timerMain.purge();
            m_timerMain = null;
            m_ttMain = null;
        }

        m_rlBottom.setVisibility(View.INVISIBLE);
        m_rlDownloader.setVisibility(View.INVISIBLE);

        voLog.d(TAG, "--- hideMediaControllerImpl ---");
    }

    private void stopVideo() {
        m_isPlayerStop = true;
        m_isPlayerRun = false;
        if (m_cPlayer != null) {
            m_cPlayer.stop();
        }
        if (m_cDownloader != null) {
            m_cDownloader.stop();
            m_cDownloader.close();
        }
        playerReset();

    }

    private void uninitPlayer() {
        if (m_cPlayer != null) {
            m_cPlayer.destroyPlayer();
        }

        if (m_cDownloader != null) {
            m_cDownloader.destroy();
        }

    }

    private void playerReset() {
        m_sbMain.setSecondaryProgress(0);
        m_sbMain.setProgress(0);
        m_asset = null;
        m_lstVideo = null;
        m_audioIndex = new int[100];
        m_nVideoWidth = 0;
        m_nVideoHeight = 0;
        m_nMaxTime = 0;
        m_nCurrentTime = 0;
        m_nMinTime = 0;
        m_nAspectRatio = VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO;
        m_btnShowADUrl.setVisibility(View.GONE);
        m_btnADSkip.setVisibility(View.GONE);
        if (m_abManager.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCESTOPKEEPLASTFRAME_ID.getValue()).getSelect() == 0) {
            m_svMain.setVisibility(View.INVISIBLE);
            m_svMain.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public VO_OSMP_RETURN_CODE onHDMIStateChangeEvent(
            VO_OSMP_HDMI_CONNECTION_STATUS status) {
        switch (status) {
            case VO_OSMP_HDMISTATE_CONNECT:
                m_tvHDMI.setVisibility(View.VISIBLE);
                break;
            case VO_OSMP_HDMISTATE_DISCONNECT:
                m_tvHDMI.setVisibility(View.INVISIBLE);
                break;
            default:
                break;
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    private void syncOpenPreparation() {
        if (m_cPlayer.getPlayerSourceFlag() == VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC) {
            m_isPlayerStop = false;
            showMediaControllerImpl();
            m_asset = m_cPlayer;
        }
    }


    private VOCommonPlayer m_secondPlayer = null;
    private SurfaceView m_svMain2 = null;
    private SurfaceHolder m_shMain2 = null;

    private static final int MSG_SECONDPLAYER_PLAYCOMPLETE = 6;
    private static final int MSG_SECONDPLAYER_PREPARE_WELL = 7;

    private boolean m_bSecondPlayePrepareWell = false;
    private boolean m_bSecondPlayerRun = false;

    private void secondPlayerStart() {
        m_secondPlayer = m_cPlayer.getAPPControl().creatPlayer(VOOSMPType.VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER);
        m_secondPlayer.setView(m_svMain2);
        m_secondPlayer.setViewSize(m_svMain2.getWidth(), m_svMain2.getHeight());
        m_secondPlayer.setOnEventListener(m_listenerEvent);
        InputStream is = null;
        byte[] b = new byte[32 * 1024];
        try {
            is = getAssets().open("voVidDec.dat");
            is.read(b);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_secondPlayer.setLicenseContent(b);
        VOOSMPType.VO_OSMP_SRC_FORMAT format = m_cPlayer.getPlayerSourceFormat();
        VOOSMPOpenParam openParam = m_cPlayer.getPlayerOpenParam();
        VO_OSMP_SRC_FLAG eSourceFlag = m_cPlayer.getPlayerSourceFlag();
        m_secondPlayer.open(m_2ndPlayerUrl, eSourceFlag, format, openParam);
    }

    private VOCommonPlayerListener m_listenerEvent = new VOCommonPlayerListener() {
        /* SDK event handling */

        public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj) {
            int rc = 0;
            voLog.d(TAG, "CPlayer onVOEvent: " + nID);
            switch (nID) {
                case VO_OSMP_CB_VIDEO_START_BUFFER:

                    break;
                case VO_OSMP_CB_VIDEO_STOP_BUFFER:

                    break;
                case VO_OSMP_CB_PLAY_COMPLETE:
                    handler.sendEmptyMessage(MSG_SECONDPLAYER_PLAYCOMPLETE);
                    break;
                case VO_OSMP_CB_VIDEO_SIZE_CHANGED:
                case VO_OSMP_CB_AUDIO_STOP_BUFFER:
                case VO_OSMP_CB_AUDIO_START_BUFFER:
                case VO_OSMP_CB_VIDEO_ASPECT_RATIO:
                case VO_OSMP_SRC_CB_PD_DOWNLOAD_POSITION:
                case VO_OSMP_SRC_CB_PD_BUFFERING_PERCENT:
                case VO_OSMP_SRC_CB_PROGRAM_CHANGED:
                case VO_OSMP_SRC_CB_PROGRAM_RESET: {

                    break;
                }
                case VO_OSMP_SRC_CB_SEEK_COMPLETE: {   // Seek (SetPos) complete

                    break;
                }

                case VO_OSMP_CB_VIDEO_RENDER_START: {
                    m_bSecondPlayePrepareWell = true;
                    Toast.makeText(mContext, "Player2 is ready!", Toast.LENGTH_LONG).show();
                    if (m_bSecondPlayerRun == false) {
                        m_secondPlayer.pause();
                    } else {
                        switch2Player(2);
                    }
                    break;
                }
                case VO_OSMP_CB_AUDIO_RENDER_START: {
                    voLog.d(TAG, "second player VO_OSMP_CB_AUDIO_RENDER_START");
                    m_bSecondPlayePrepareWell = true;
                    if (m_bSecondPlayerRun == false) {
                        m_secondPlayer.pause();
                        m_secondPlayer.unmute();
                    }
                    break;
                }
                case VO_OSMP_SRC_CB_OPEN_FINISHED: {
                    voLog.d(TAG, "second player open finished");
                    m_secondPlayer.start();
                    if(!isNow2ndPlayerShow()) {
                        m_secondPlayer.mute();
                    }

                    checkPlayerStatus();

                    break;
                }
                case VO_OSMP_CB_CODEC_NOT_SUPPORT:

                    break;
                case VO_OSMP_SRC_CB_PREFERRED_AUDIO_LANGUAGE:

                    break;
                case VO_OSMP_SRC_CB_PREFERRED_SUBTITLE_LANGUAGE:

                    break;
                default:

                    break;

            }
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
        }

        @Override
        public VO_OSMP_RETURN_CODE onVOSyncEvent(VOCommonPlayerListener.VO_OSMP_CB_SYNC_EVENT_ID arg0,
                                                 int arg1, int arg2, Object arg3) {
            // TODO Auto-generated method stub
            return null;
        }

    };

    @Override
    public void startVideo() {
        playerStart();
    }
}
