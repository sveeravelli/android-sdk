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

package com.visualon.AppPlayerCommonFeatures;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.visualon.AppBehavior.AppBehaviorManager;
import com.visualon.AppBehavior.AppBehaviorManagerImpl.OPTION_ID;
import com.visualon.AppBehavior.OptionItem;
import com.visualon.AppPlayerCommonFeatures.TimeCal.API_TIME_TYPE;
import com.visualon.AppPlayerSpecialFeatures.CSpecialFeatures;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsExportListener;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsFilter;
import com.visualon.OSMPPlayer.VOOSMPHTTPProxy;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ASPECT_RATIO;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_AUDIO_EFFECT_ENDPOINT_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DECODER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DISPLAY_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_HDCP_POLICY;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_HORIZONTAL;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PREFERENCE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RENDER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RTSP_CONNECTION_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_VERTICAL;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;

import java.io.IOException;
import java.io.InputStream;


public class CPlayer extends APPCommonPlayerAssetSelection {
    private static final String                       TAG                    = "@@@CPlayer"; // Tag for VOLog messages
    private static SharedPreferences                  m_spMain               = null; 
    
    private APPCommonPlayerDRM                        m_appDRM               = null;
    private APPCommonPlayerAnalytics                  m_appAnalytics         = null;
    private APPCommonPlayerConfiguration              m_appConfiguration     = null;
    private APPCommonPlayerControl                    m_appControl           = null;
    private APPCommonPlayerStatus                     m_appStatus            = null;
    private APPCommonPlayerSubtitle                   m_appSubtitle          = null;
    private APPCommonPlayerRTSPConfiguration          m_appRTSPConfiguration = null;
    private APPCommonPlayerAssetSelection             m_appAssetSelection    = null;
    private CSpecialFeatures                          m_cSpecialFeatures     = null;
    private CAdManager                                m_cAdManager           = null;
    
    private String                                    m_appPlayerURL         = null;
    private VOCommonPlayer                            m_sdkPlayer            = null;
    private AppBehaviorManager                        m_appBehavior          = null;
    private Context                                   m_context              = null;
    private APPUIEventListener                        m_UIListener           = null;
    private String                                    m_verificationString   = null;
  
    private VO_OSMP_DECODER_TYPE                      m_videoDecoderType     = null;
    private VO_OSMP_DECODER_TYPE                      m_audioDecoderType     = null;
    private boolean                                   m_bIsHWCodecNotSupport = false;     
    private boolean                                   m_bisToast             = false;
    
    public CPlayer(Context context) {
        voLog.d(TAG, "Create new instance of CPlayer.");
        m_appDRM = new APPCommonPlayerDRM();
        m_appAnalytics = new APPCommonPlayerAnalytics();
        m_appConfiguration = new APPCommonPlayerConfiguration();
        m_appControl = new APPCommonPlayerControl(context);
        m_appStatus = new APPCommonPlayerStatus();
        m_appSubtitle = new APPCommonPlayerSubtitle();
        m_appRTSPConfiguration = new APPCommonPlayerRTSPConfiguration();
        m_appAssetSelection = new APPCommonPlayerAssetSelection();
        m_context=context;
        m_spMain = PreferenceManager.getDefaultSharedPreferences(context);
    }

    // Need to tell DRM.so to have a writable folder path.
    public VO_OSMP_RETURN_CODE setDRMFilePath(String path) {
        voLog.d(TAG, "setDRMFilePath: " + path);
        return m_appDRM.setDRMFilePath(path);
    }
   
    public void setUIListener(APPUIEventListener listener) {
        m_UIListener = listener;
    }
    
    public void setBehavior(AppBehaviorManager behavior) {
        if (behavior != null) 
            m_appBehavior = behavior;
    }
    
    public AppBehaviorManager getBehavior() {
        return m_appBehavior;
    }

    public APPCommonPlayerControl getAPPControl() {
        return m_appControl;
    }

    public CSpecialFeatures getSpecialFeatureFunction() {
        voLog.d(TAG, "Get CSpecialFeatures from CPlayer.");
        if (m_cSpecialFeatures == null) {
            m_cSpecialFeatures = new CSpecialFeatures();
            m_cSpecialFeatures.setAPPConfiguration(m_appConfiguration);
            m_cSpecialFeatures.setAPPControl(m_appControl);
            m_cSpecialFeatures.setAPPStatus(m_appStatus);
            m_cSpecialFeatures.setAPPRTSPConfiguration(m_appRTSPConfiguration);
            m_cSpecialFeatures.setAPPBehaviorManager(m_appBehavior);
         }
        return m_cSpecialFeatures;
    }
    
    public CAdManager getCAdManager() {
        if (m_cAdManager == null) {
            m_cAdManager = new CAdManager(m_context);
            m_cAdManager.setSDKPlayer(m_sdkPlayer);
        }
        return m_cAdManager;
    }
    
    private VOCommonPlayerListener m_listenerEvent = new VOCommonPlayerListener() {
        /* SDK event handling */
     
        public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj) {
            int rc = 0;
            voLog.d(TAG,"CPlayer onVOEvent: " + nID + " - " + nID.getValue());
            switch(nID) {
            case VO_OSMP_CB_VIDEO_START_BUFFER:
                TimeCal.printStartTime(API_TIME_TYPE.BUFFER_TIME);
                m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                break;
            case VO_OSMP_CB_VIDEO_STOP_BUFFER:
                TimeCal.printUsingTime(API_TIME_TYPE.BUFFER_TIME);
                m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                break;
            case VO_OSMP_CB_PLAY_COMPLETE:
                TimeCal.printUsingTime(API_TIME_TYPE.PLAY_COMPLETE);
                m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                break;
            case VO_OSMP_CB_VIDEO_SIZE_CHANGED: 
            case VO_OSMP_CB_AUDIO_STOP_BUFFER:
            case VO_OSMP_CB_AUDIO_START_BUFFER:
            case VO_OSMP_CB_VIDEO_ASPECT_RATIO:
            case VO_OSMP_SRC_CB_PD_DOWNLOAD_POSITION:
            case VO_OSMP_SRC_CB_PD_BUFFERING_PERCENT:
            case VO_OSMP_SRC_CB_PROGRAM_CHANGED:
            case VO_OSMP_SRC_CB_PROGRAM_RESET: {
                m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                break;
            }
            case VO_OSMP_SRC_CB_SEEK_COMPLETE : {   // Seek (SetPos) complete
                TimeCal.printUsingTime(API_TIME_TYPE.SEEK_COMPLETE_TIME);
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, null);
                break;
            }
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_SEEK2LASTCHUNK : {   // Seek (SetPos) complete
                TimeCal.printUsingTime(API_TIME_TYPE.SEEK_TO_LATEST_TRUNK);
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, null);
                break;
            }
            case VO_OSMP_CB_VIDEO_RENDER_START : { 
                TimeCal.printUsingTime(API_TIME_TYPE.OPEN_RENDER_TIME);
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, null);
                break;
            }
            case VO_OSMP_SRC_CB_OPEN_FINISHED: {
                TimeCal.printUsingTime(API_TIME_TYPE.OPEN_COMPLETE_TIME);
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, null);
                if (nParam1 == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE.getValue()) {
                    VO_OSMP_RETURN_CODE nRet;
                     
                    m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                    /* Run (play) media pipeline */
                    TimeCal.printStartTime(API_TIME_TYPE.RUN_TIME);
                    nRet =   m_appControl.start();
                    TimeCal.printUsingTime(API_TIME_TYPE.RUN_TIME);
                    rc = m_appBehavior.processReturnCode("Player async start", nRet.getValue());
                    if (rc == 1)
                        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
                }
                break;
            }
            case VO_OSMP_CB_CODEC_NOT_SUPPORT :
                if ((m_videoDecoderType == VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW && m_audioDecoderType == VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW)
                        || Build.VERSION.SDK_INT < 14) {
                    if (nParam1 == 0) {
                        if(!m_bisToast)
                            m_bisToast=true;
                        Toast.makeText(m_context, "Audio not supported", Toast.LENGTH_LONG).show();
                    } else
                        m_appBehavior.processEvent(0X8A000001 , nParam1, nParam2, null);
                } else {
                    if (nParam1 == 0) {
                        if(!m_bisToast)
                            m_bisToast=true;
                        Toast.makeText(m_context, "Audio not supported", Toast.LENGTH_LONG).show();
                    } else {
                        if (!m_bIsHWCodecNotSupport) {
                            m_bIsHWCodecNotSupport = true;
                            m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, null);
                        }
                    }
                  
                }
                break;
            case VO_OSMP_SRC_CB_PREFERRED_AUDIO_LANGUAGE:
                voLog.d("@@@AppBehaviorManager","[APP][EVENT]Received the preferred audio language, language is %s",obj);
                break;
            case VO_OSMP_SRC_CB_PREFERRED_SUBTITLE_LANGUAGE:
                voLog.d("@@@AppBehaviorManager","[APP][EVENT]Received the preferred subtitle language, language is %s",obj);
                break;
            case VO_OSMP_SRC_CB_DRM_FAIL:
                voLog.d(TAG, "[APP][EVENT]Received the drm error, error code is %d", nParam1);
                break;
           
            case VO_OSMP_AD_CB_PLAYBACKINFO:
            case VO_OSMP_AD_CB_AD_START:
            case VO_OSMP_AD_CB_AD_END:
            case VO_OSMP_AD_CB_VIDEO_PROGRESS:
            case VO_OSMP_AD_CB_SKIPPABLE:
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, obj);
                m_UIListener.onEvent(APP_UI_EVENT_ID.valueOf(nID.getValue()), nParam1, nParam2, obj);
                
                break;
           default :
                m_appBehavior.processEvent(nID.getValue(), nParam1, nParam2, obj);
               break;
            
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;       
        }

        @Override
        public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
                int arg1, int arg2, Object arg3) {
            // TODO Auto-generated method stub
            return null;
        }
       
    };    
   

    private final VOOSMPAnalyticsExportListener m_analyticsListener = new VOOSMPAnalyticsExportListener() {
        @Override
        public void onVOAnalyticsEvent() {
            Log.i(TAG, "[APP]Analytics Export Packet: " + m_appAnalytics.getAnalyticsExportPacket());
        }
    };
    
    private VOCommonPlayer m_voCommonPlayer = null;
    public void setCommplayer(VOCommonPlayer player) {
        m_voCommonPlayer = player;
    }
    public VOCommonPlayer getCommplayer() {
        return m_voCommonPlayer;
    }

    public void createPlayer() {
        voLog.d(TAG, "Create new instance of VOCommonPlayer");
        VO_OSMP_PLAYER_ENGINE playerEngine;
        OptionItem item = new OptionItem();
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENGINETYPE_ID.getValue());
        if (item == null)
            playerEngine =VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;
        else
         playerEngine = VO_OSMP_PLAYER_ENGINE.valueOf(item.getSelect());
        voLog.d(TAG, "The player engine type is " + playerEngine);
        TimeCal.printStartTime(API_TIME_TYPE.INIT_TIME);
        
        if(m_voCommonPlayer != null) {
            voLog.d(TAG, "CreatePlayer SP");
            m_sdkPlayer = m_appControl.creatPlayer(playerEngine, m_voCommonPlayer);
        } else {
            voLog.d(TAG, "CreatePlayer Normally");
            m_sdkPlayer = m_appControl.creatPlayer(playerEngine);
        }

        TimeCal.printUsingTime(API_TIME_TYPE.INIT_TIME);
        if (m_sdkPlayer != null) {
            m_appDRM.setPlayer(m_sdkPlayer);
            m_appAnalytics.setPlayer(m_sdkPlayer);
            m_appConfiguration.setPlayer(m_sdkPlayer);
            m_appControl.setPlayer(m_sdkPlayer);
            m_appStatus.setPlayer(m_sdkPlayer);
            m_appSubtitle.setPlayer(m_sdkPlayer);
            m_appRTSPConfiguration.setPlayer(m_sdkPlayer);
            m_appAssetSelection.setPlayer(m_sdkPlayer);
        }
        DisplayMetrics dm  = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(dm);
        m_appControl.setViewSize(dm.widthPixels, dm.heightPixels);
        m_appControl.setOnEventListener(m_listenerEvent);
        String capFile = CommonFunc.getUserPath(m_context) + "/" + "cap.xml";
        m_appConfiguration.setDeviceCapabilityByFile(capFile);
        InputStream is = null;
        byte[] b = new byte[32*1024];
        try {
            is = m_context.getAssets().open("voVidDec.dat");
            is.read(b);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        m_appConfiguration.setLicenseContent(b);
    }

    public void destroyPlayer() {
        voLog.d(TAG, "CPlayer destroyPlayer().");
        if (m_sdkPlayer != null && m_appControl != null) {
            final String lastAnalyticsExportPacket = m_sdkPlayer.getAnalyticsExportPacket();
            if (lastAnalyticsExportPacket.length() > 0) {
                Log.i(TAG, "[APP]Analytics Export Packet: " + lastAnalyticsExportPacket);
            }
            m_appControl.destroy();
            m_sdkPlayer = null;
			m_voCommonPlayer = null;
        }
        m_cAdManager = null;
    }
    
    public VO_OSMP_RETURN_CODE setPlayView(View playView) {
        voLog.d(TAG, "CPlayer setPlayView " + playView);
        if (m_sdkPlayer != null && m_appControl != null)
			return m_appControl.setView(playView);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setPlayerURL(String url) {
        m_appPlayerURL = url;
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
    
    public String getPlayerURL() {
        return m_appPlayerURL;
    }
    
    public VO_OSMP_RETURN_CODE start() {
        voLog.d(TAG, "CPlayer start().");
        VO_OSMP_RETURN_CODE ret = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        if (m_sdkPlayer != null && m_appConfiguration != null && m_appBehavior != null) { 
             checkDRMSetting();
			 
             ret = updatePlayerOption();
             if (ret != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE){
            	 return ret;
             }
			 
             if (playerStartWithAD()) {
                ret = getCAdManager().start(m_appPlayerURL);
             }else {
                 VO_OSMP_SRC_FORMAT format = getPlayerSourceFormat();
                 voLog.d(TAG, "The open source format is " + format);
                 VOOSMPOpenParam openParam = getPlayerOpenParam();
                 VO_OSMP_SRC_FLAG eSourceFlag = getPlayerSourceFlag();
                 voLog.d(TAG, "The open source flag is " + eSourceFlag);
                 TimeCal.printStartTime(API_TIME_TYPE.OPEN_TIME);
                 ret = m_sdkPlayer.open(m_appPlayerURL, eSourceFlag, format, openParam);
                 TimeCal.printUsingTime(API_TIME_TYPE.OPEN_TIME);
                 voLog.i(TAG, "The open url is : " + m_appPlayerURL);
                 if (eSourceFlag == VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC)
                     playerSyncStart();
             }
            
         }
         return ret;  
    }
    
    private boolean playerStartWithAD() {
        OptionItem item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEAD_ID.getValue());
        return (item != null && item.getSelect() == 1);
    }

    public VO_OSMP_STATUS getPlayerStatus() {
        if(m_appStatus != null) {
            return m_appStatus.getPlayerStatus();
        } else {
            return null;
        }
    }

    public boolean isNowPlaying() {
        VO_OSMP_STATUS status=m_appStatus.getPlayerStatus();
        if (status == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
            return true;
        } else {
            return false;
        }
    }
    
    public VO_OSMP_RETURN_CODE pause() {
        voLog.d(TAG, "CPlayer pause().");
        VO_OSMP_RETURN_CODE ret = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        if (m_appControl != null && m_sdkPlayer != null && m_appStatus != null) {
            VO_OSMP_STATUS status=m_appStatus.getPlayerStatus();
            voLog.d(TAG, "The player playing status is " + status);
            if (status == VO_OSMP_STATUS.VO_OSMP_STATUS_PAUSED) {
                TimeCal.printStartTime(API_TIME_TYPE.RUN_TIME);
                ret = m_appControl.start();
                TimeCal.printUsingTime(API_TIME_TYPE.RUN_TIME);
            }
            if (status == VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING) {
                TimeCal.printStartTime(API_TIME_TYPE.PAUSE_TIME);
                ret = m_appControl.pause();
                TimeCal.printUsingTime(API_TIME_TYPE.PAUSE_TIME);
            }
        }
        return ret;
    }
    
    public void stop() {
        voLog.d(TAG, "CPlayer stop()");
        if (m_appAnalytics != null && m_sdkPlayer != null) {
            if(m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PERFORMANCE_ID.getValue()).getSelect()==1)
                m_appAnalytics.stopAnalyticsNotification();
        }
        if (m_appControl != null && m_sdkPlayer != null) {
            m_bisToast = false;
            TimeCal.printStartTime(API_TIME_TYPE.STOP_TIME);
            m_appControl.stop();
            TimeCal.printUsingTime(API_TIME_TYPE.STOP_TIME);
            m_appControl.close();
        }
    }
    
    public VO_OSMP_RETURN_CODE suspend() {
        voLog.d(TAG, "CPlayer suspend()");
        if (m_appControl != null && m_sdkPlayer != null)
            return m_appControl.suspend();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE resume(SurfaceView view) {
        voLog.d(TAG, "CPlayer resume, the view is " + view );
        if (m_appControl != null && m_sdkPlayer != null)
            return m_appControl.resume(view);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setSurfaceChangeFinished() {
        voLog.d(TAG, "CPlayer setSurfaceChangeFinished()");
        if (m_sdkPlayer!=null && m_appControl != null)
            return m_appControl.setSurfaceChangeFinished();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public void updateToDefaultVideoSize(VO_OSMP_ASPECT_RATIO ratio) {
        voLog.d(TAG, "CPlayer updateToDefaultVideoSize()");
        if (m_appControl != null && m_sdkPlayer != null ) {
            DisplayMetrics dm  = new DisplayMetrics();
            WindowManager windowManager = (WindowManager) m_context.getSystemService(Context.WINDOW_SERVICE);
            windowManager.getDefaultDisplay().getMetrics(dm);
            m_appControl.setViewSize(dm.widthPixels, dm.heightPixels);
        }
    }


    public void seekTo(long lNewPosition) {
        if (m_appControl != null) {
            voLog.i(TAG, "CPlayer seek to " + lNewPosition);
            TimeCal.printStartTime(API_TIME_TYPE.SEEK_TIME);
            m_appControl.setPosition(lNewPosition);
            TimeCal.printUsingTime(API_TIME_TYPE.SEEK_TIME);
        }
    }
    
    public void seekTo(float percent) {
         voLog.d(TAG, "CPlayer seekTo the  " + percent + " of video");
        if (m_sdkPlayer!=null && m_appControl != null && m_appStatus != null) {
           
            long max = m_appStatus.getMaxPosition();
            long min = m_appStatus.getMinPosition();
            long duration = max - min;
            if(duration<=0)
                duration = m_appStatus.getDuration();
            if(duration<=0)
                return;
            long lNewPosition = (long) (percent * duration + min);
            voLog.d(TAG, "CPlayer seek to " + lNewPosition);
            TimeCal.printStartTime(API_TIME_TYPE.SEEK_TIME);
            m_appControl.setPosition(lNewPosition);
           
            TimeCal.printUsingTime(API_TIME_TYPE.SEEK_TIME);
        }
    }
    
    public long getCurrentPosition() {
        if (m_sdkPlayer != null && m_appStatus != null)
            return m_appStatus.getPosition();
        else
            return 0;
    }
    
    public long getLeftPosition() {
        if (m_sdkPlayer != null && m_appStatus != null)
            return m_appStatus.getMinPosition();
        else
            return 0;
    }
    
    public String getVersion(VO_OSMP_MODULE_TYPE module) {
        if (m_sdkPlayer != null && m_appStatus != null)
            return m_appStatus.getVersion(module);
        else
            return "";
    }

    public long getUTCPosition() {
        return m_sdkPlayer.getUTCPosition();
    }

    public boolean isLiveStreaming() {
        return m_appStatus.isLiveStreaming();
    }

    public long getRightPosition() {
        if (m_sdkPlayer != null && m_appStatus != null) {
            if (m_appStatus.isLiveStreaming())
                return m_appStatus.getMaxPosition();
            else
                return m_appStatus.getDuration();
        }
        else
            return 0;
    }
    
    public VO_OSMP_RETURN_CODE enableAudioEffect(boolean enable) {
        if (m_sdkPlayer != null && m_appConfiguration != null)
            return m_appConfiguration.enableAudioEffect(enable);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE enableSubtitle(boolean enable) {
        if (m_sdkPlayer != null && m_appSubtitle != null)
            return m_appSubtitle.enableSubtitle(enable);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE resetSubtitleParameter() {
        if (m_sdkPlayer != null && m_appSubtitle != null)
            return m_appSubtitle.resetSubtitleParameter();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE previewSubtitle(View view) {
        if (m_sdkPlayer != null && m_appSubtitle != null)
            return m_appSubtitle.previewSubtitle(view);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public boolean isTrackAvailable(AssetType type, int index) {
        if (m_sdkPlayer != null && m_appAssetSelection != null)
            return m_appAssetSelection.isTrackAvailable(type, index);
        else
            return false;
    }
    
    public int getAssetCount(AssetType type) {
        if (m_sdkPlayer != null && m_appAssetSelection != null)
            return m_appAssetSelection.getAssetCount(type);
        else
            return 0;
    }
    
    public int getAssetIndex(AssetType type, AssetStatus status) {
        if (m_sdkPlayer != null && m_appAssetSelection != null)
            return m_appAssetSelection.getAssetIndex(type, status);
        else
            return 0;
    }
    
    public VOOSMPAssetProperty getAssetProperty(AssetType type, int index) {
        if (m_sdkPlayer != null && m_appAssetSelection != null)
            return m_appAssetSelection.getAssetProperty(type, index);
        else
            return null;
    }
    
    public VO_OSMP_RETURN_CODE selectAsset(AssetType type, int index) {
     if (m_sdkPlayer != null && m_appAssetSelection != null)
         return  m_appAssetSelection.selectAsset(type, index);
     else
         return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE commitSelection() {
      if (m_sdkPlayer != null && m_appAssetSelection != null) {
          TimeCal.resetStartTime(API_TIME_TYPE.SEEK_TIME);
          return m_appAssetSelection.commitSelection();
      }
      else
          return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE clearSelection() {
     if (m_sdkPlayer != null && m_appAssetSelection != null)
         return m_appAssetSelection.clearSelection();
     else
         return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setAudioSpeed(float speed) {
        if (m_sdkPlayer != null && m_appConfiguration != null)
            return m_appConfiguration.setAudioPlaybackSpeed(speed);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setDRMVerificationInfo(VOOSMPVerificationInfo info) {
        if (m_sdkPlayer != null && m_appDRM != null)
            return m_appDRM.setDRMVerificationInfo(info);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setDRMLibrary(String libName, String libApiName) {
        if (m_sdkPlayer != null && m_appDRM != null)
            return m_appDRM.setDRMLibrary(libName, libApiName);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public String getDRMUniqueIdentifier() {
        if (m_sdkPlayer != null && m_appDRM != null)
            return m_appDRM.getDRMUniqueIdentifier();
        else
            return null;
        
    }
    
    public VO_OSMP_RETURN_CODE setDRMAdapter(Object adapter, boolean isLibraryName) {
        if (m_sdkPlayer != null && m_appDRM != null)
            return m_appDRM.setDRMAdapter(adapter, isLibraryName);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public void setVerificationString(String string) {
        m_verificationString = string;
    }
   
    
    public VO_OSMP_RETURN_CODE setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE type) {
        if (m_sdkPlayer != null && m_appAnalytics != null)
            return m_appAnalytics.setAnalyticsDisplayType(type);
        else 
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    public VO_OSMP_RETURN_CODE updatePlayerOption() {
		VO_OSMP_RETURN_CODE Ret = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
        voLog.i(TAG, "CPlayer updatePlayerOption().");
        if (m_sdkPlayer != null) {
            boolean value;
          
           OptionItem hdcpPolicyTypeItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_HDCPPOLICY_ID.getValue());
           if (hdcpPolicyTypeItem != null) {
               VO_OSMP_HDCP_POLICY hdcpPolicyType = VO_OSMP_HDCP_POLICY.valueOf(hdcpPolicyTypeItem.getSelect());
               m_appConfiguration.setHDCPPolicy(hdcpPolicyType);
                voLog.d(TAG, "CPlayer setHDCPPolicy:"+ hdcpPolicyType);
           }
           
          OptionItem videoRenderTypeItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_VIDEORENDERTYPE_ID.getValue());
            if (videoRenderTypeItem != null) {
                VO_OSMP_RENDER_TYPE renderType = VO_OSMP_RENDER_TYPE.valueOf(videoRenderTypeItem.getSelect());
                m_appConfiguration.setRenderType(renderType);
                voLog.d(TAG, "CPlayer setRenderType:"+ renderType);
            }
            
            OptionItem adptivePlaybackItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLERENDEROPTIMIZATIONFORBA_ID.getValue());
            if (adptivePlaybackItem != null) {
                value = (adptivePlaybackItem.getSelect() == 1);
                m_appConfiguration.enableVOAdaptivePlayback(value);
                voLog.d(TAG, "CPlayer enableVOAdaptivePlayback:"+ value);
            }
            
            OptionItem audioEffectItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_DOLBY_ID.getValue());
            if (audioEffectItem != null) {
                value = (audioEffectItem.getSelect() == 1);
                m_appConfiguration.enableAudioEffect(value);
                if (value)
                    m_appConfiguration.setAudioEffectEndpointType(VO_OSMP_AUDIO_EFFECT_ENDPOINT_TYPE.VO_OSMP_AUDIO_EFFECT_ENDPOINT_HEADPHONE);
                voLog.d(TAG, "CPlayer enableAudioEffect:"+ value);
            } 
            
            OptionItem deblockItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_DEBLOCK_ID.getValue());
            if (deblockItem != null) {
                value = (deblockItem.getSelect() == 1);
                m_appConfiguration.enableDeblock(value);
                voLog.d(TAG, "CPlayer enableDeblock:"+ value);
            }
            
            OptionItem analyticsPerformanceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PERFORMANCE_ID.getValue());
            if (analyticsPerformanceItem != null) {
                if(analyticsPerformanceItem.getSelect() == 1) {
                    m_appAnalytics.enableAnalytics(60);
                    VOOSMPAnalyticsFilter filter = new VOOSMPAnalyticsFilter(5, 60, 60, 30, 60);
                    m_appAnalytics.startAnalyticsNotification(filter);
                    voLog.d(TAG, "CPlayer startAnalyticsNotification");
                }
            }
           
            
            OptionItem subtitleUrlItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SUBTITLEURL_ID.getValue());
            if (subtitleUrlItem != null) {
                String subtitleUrl = m_spMain.getString(String.valueOf(subtitleUrlItem.getId()), "0");
                if(subtitleUrl.indexOf(".")>0) {
                    m_appSubtitle.setSubtitlePath(subtitleUrl);
                    voLog.d(TAG, "CPlayer setSubtitlePath:"+ subtitleUrl);
                }
            }
            
            OptionItem maxBufferingItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_MAXBUFFERINGTIME_ID.getValue());
            if (maxBufferingItem != null) {
                int maxBufferingTime = maxBufferingItem.getValueListItem(0).getValue();
                m_appConfiguration.setMaxBufferingTime(maxBufferingTime);
                voLog.d(TAG, "CPlayer setMaxBufferingTime:"+ maxBufferingTime);
            }
            
            OptionItem initialBufferingItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_INITIALBUFFERINGTIME_ID.getValue());
            if (initialBufferingItem != null) {
                int initialBufferingTime = initialBufferingItem.getValueListItem(0).getValue();
                m_appConfiguration.setInitialBufferingTime(initialBufferingTime);
                voLog.d(TAG, "CPlayer setInitialBufferingTime:"+ initialBufferingTime);
            }
            
            OptionItem playbackBufferingItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PLAYBACKBUFFERINGTIME_ID.getValue());
            if (playbackBufferingItem != null) {
                int playbackBufferingTime = playbackBufferingItem.getValueListItem(0).getValue();
                m_appConfiguration.setPlaybackBufferingTime(playbackBufferingTime);
                voLog.d(TAG, "CPlayer setPlaybackBufferingTime:"+ playbackBufferingTime);
            }
            
            OptionItem CPUAdapttionItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_CPUADAPTION_ID.getValue());
            if (CPUAdapttionItem != null) {
                value = (CPUAdapttionItem.getSelect() == 1);
                m_appConfiguration.enableCPUAdaptation(value);
                voLog.d(TAG, "CPlayer enableCPUAdaptation:"+ value);
            }
            
            OptionItem RTSPConnectionTypeItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_RTSP_ID.getValue());
            if (RTSPConnectionTypeItem != null) {
                VO_OSMP_RTSP_CONNECTION_TYPE rstpConectionType = VO_OSMP_RTSP_CONNECTION_TYPE.valueOf(RTSPConnectionTypeItem.getSelect());
                m_appRTSPConfiguration.setRTSPConnectionType(rstpConectionType);
                voLog.d(TAG, "CPlayer setRTSPConnectionType:"+ rstpConectionType);
            }
            
            OptionItem RTSPOverHTTPItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLERTSPHTTPPORT_ID.getValue());
            if (RTSPOverHTTPItem != null) {
                if(RTSPOverHTTPItem.getSelect() == 1){
                    m_appRTSPConfiguration.enableRTSPOverHTTP(true);
                    voLog.d(TAG, "CPlayer enableRTSPOverHTTP:true");
                    OptionItem RTSPOverHTTPPortItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETRTSPHTTPPORT_ID.getValue());
                    if (RTSPOverHTTPPortItem != null) {
                        int RTSPOverHTTPPort = RTSPOverHTTPPortItem.getValueListItem(0).getValue();
                        m_appRTSPConfiguration.setRTSPOverHTTPConnectionPort(RTSPOverHTTPPort);
                        voLog.d(TAG, "CPlayer setRTSPOverHTTPConnectionPort:" + RTSPOverHTTPPort);
                    }
                }else {
                    m_appRTSPConfiguration.enableRTSPOverHTTP(false); 
                    voLog.d(TAG, "CPlayer enableRTSPOverHTTP:false");
                }
            }
            
            OptionItem HTTPVerificationInfoItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLERTSPHTTPVERIFICATIONINFO_ID.getValue());
            if (HTTPVerificationInfoItem != null) {
               if(HTTPVerificationInfoItem.getSelect() == 1){
                    String userName = null;
                    String password = null;
                    VOOSMPVerificationInfo verif = new VOOSMPVerificationInfo();
                    OptionItem HTTPVerificationInfoUserNameItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETRTSPHTTPVERIFICATIONUSERNAME_ID.getValue());
                    if (HTTPVerificationInfoUserNameItem != null)
                        userName = HTTPVerificationInfoUserNameItem.getValueListItem(0).getText();
                    OptionItem HTTPVerificationInfoPasswordItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETRTSPHTTPVERIFICATIONPASSWORD_ID.getValue());
                    if (HTTPVerificationInfoPasswordItem != null)
                        password = HTTPVerificationInfoPasswordItem.getValueListItem(0).getText();
                    String HTTPVerificationInfo = userName+":"+password;
                    verif.setVerificationString(HTTPVerificationInfo);
                    verif.setDataFlag(1);
                    m_appRTSPConfiguration.setHTTPVerificationInfo(verif);
                    voLog.d(TAG, "CPlayer setHTTPVerificationInfo,userName: " + userName + ", password: " + password);
                }
            }
            
            OptionItem lowLatencyVideoItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLELOWLATENCYVIDEO_ID.getValue());
            if (lowLatencyVideoItem != null) {
                value = (lowLatencyVideoItem.getSelect() == 1);
                m_appRTSPConfiguration.enableLowLatencyVideo(value);
                voLog.d(TAG, "CPlayer enableLowLatencyVideo:"+ value);
            }   
               
            OptionItem eanbleRTSPMaxSocketErrorCountItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLERTSPSOCKETERROR_ID.getValue());
            if (eanbleRTSPMaxSocketErrorCountItem != null) {
                if(eanbleRTSPMaxSocketErrorCountItem.getSelect() == 1) {
                    OptionItem  RTSPMaxSocketErrorCountItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETRTSPSOCKETERROR_ID.getValue());
                    if (RTSPMaxSocketErrorCountItem != null) {
                        int RTSPMaxSocketErrorCount = RTSPMaxSocketErrorCountItem.getValueListItem(0).getValue();
                        m_appRTSPConfiguration.setRTSPMaxSocketErrorCount(RTSPMaxSocketErrorCount);
                        voLog.d(TAG, "CPlayer setRTSPMaxSocketErrorCount:"+ RTSPMaxSocketErrorCount);
                    }
                }
            }
            
            OptionItem eanbleRTSPConnectionTimeoutItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLERTSPCONNECTIONTIMEOUT_ID.getValue());
            if (eanbleRTSPConnectionTimeoutItem != null) {
                if(eanbleRTSPConnectionTimeoutItem.getSelect() == 1) {
                    OptionItem RTSPConnectionTimeoutItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETRTSPCONNECTIONTIMEOUT_ID.getValue());
                    if (RTSPConnectionTimeoutItem != null) {
                        int RTSPConnectionTimeout = RTSPConnectionTimeoutItem.getValueListItem(0).getValue();
                        m_appRTSPConfiguration.setRTSPConnectionTimeout(RTSPConnectionTimeout);
                        voLog.d(TAG, "CPlayer setRTSPConnectionTimeout:"+ RTSPConnectionTimeout);
                    }
                }
            }
            
            OptionItem enableInitialBitrateItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_INITIALBITRATE_ID.getValue());
            if (enableInitialBitrateItem != null) {
                if(enableInitialBitrateItem.getSelect() == 1) {
                    OptionItem initialBitrateItem= m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETINITIALBITRATE_ID.getValue());
                    if (initialBitrateItem != null) {
                        int initialBitrate = initialBitrateItem.getValueListItem(0).getValue();
                        initialBitrate *= 1000;
                        m_appConfiguration.setInitialBitrate(initialBitrate);
                        voLog.d(TAG, "CPlayer setInitialBitrate:"+ initialBitrate);
                    }
                }
            }
            
            OptionItem enableBitrateThresholdItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_BITRATERANGE_ID.getValue());
            if (enableBitrateThresholdItem != null) {
                if(enableBitrateThresholdItem.getSelect() == 1) {
                    int lower = 0;
                    int upper = 0;
                    OptionItem lowerBitrateThresholdItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_LOWERBITRATERANGE_ID.getValue());
                    if (lowerBitrateThresholdItem != null)
                        lower = lowerBitrateThresholdItem.getValueListItem(0).getValue();
                    OptionItem upperBitrateThresholdItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_UPPERBITRATERANGE_ID.getValue());
                    if (upperBitrateThresholdItem != null)
                        upper = upperBitrateThresholdItem.getValueListItem(0).getValue();
                    lower *= 1000;
                    upper *= 1000;
                    m_appConfiguration.setBitrateThreshold(upper, lower);
                    voLog.d(TAG, "CPlayer setBitrateThreshold,upper birate: "+ upper + ", lower: " + lower);
                }
            }
            
            OptionItem enableHTTPRetryTimeoutItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_HTTPRETRYTIMEOUT_ID.getValue());
            if (enableHTTPRetryTimeoutItem != null) {
                if(enableHTTPRetryTimeoutItem.getSelect() == 1) {
                    OptionItem HTTPRetryTimeoutItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETHTTPRETRYTIMEOUT_ID.getValue());
                    if (HTTPRetryTimeoutItem != null) {
                        int retryTime = HTTPRetryTimeoutItem.getValueListItem(0).getValue();

                        // Default value is 2. Setting this value to -1 to continue retrying until {@link VOCommonPlayerControl#stop} be called.
                        if(retryTime == -1) {
                            m_appConfiguration.setPDConnectionRetryCount(-1);
                        } else {
                            m_appConfiguration.setPDConnectionRetryCount(2);
                        }

                        m_appConfiguration.setHTTPRetryTimeout(retryTime);
                        voLog.d(TAG, "CPlayer setHTTPRetryTimeout:"+ retryTime);
                    }
                }
            }
            
            OptionItem HTTPGzipRequestItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEHTTPGZIPREQUEST_ID.getValue());
            if (HTTPGzipRequestItem != null) {
                value = (HTTPGzipRequestItem.getSelect() == 1);
                m_appConfiguration.enableHTTPGzipRequest(HTTPGzipRequestItem.getSelect() == 1? true:false);
                voLog.d(TAG, "CPlayer enableHTTPGzipRequest:"+ value);
            }
            
            OptionItem HTTPProxyItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEHTTPPROXY_ID.getValue());
            if (HTTPProxyItem != null) {
                if (HTTPProxyItem.getSelect() == 1) {
                    OptionItem HTTPProxyHostItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETHTTPPROXYHOST_ID.getValue());
                    String proxyHost = HTTPProxyHostItem.getValueListItem(0).getText();
                    OptionItem HTTPProxyPortItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETHTTPPROXYPORT_ID.getValue());
                    int proxyPort = HTTPProxyPortItem.getValueListItem(0).getValue();
                    VOOSMPHTTPProxy proxy = new VOOSMPHTTPProxy(proxyHost, proxyPort);
                    m_appConfiguration.setHTTPProxy(proxy);
                    voLog.d(TAG, "CPlayer setHTTPProxy: host = "+ proxyHost + ", port = " + proxyPort);
                }
            }

            OptionItem HTTPHeaderItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEHTTPHEADER_ID.getValue());
            if (HTTPHeaderItem != null) {
                if (HTTPHeaderItem.getSelect() == 1) {
                    OptionItem HTTPHeaderNameItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETHTTPHEADER_NAME_ID.getValue());
                    String headerName = HTTPHeaderNameItem.getValueListItem(0).getText();
                    OptionItem HTTPHeaderValueItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETHTTPHEADER_VALUE_ID.getValue());
                    String headerValue = HTTPHeaderValueItem.getValueListItem(0).getText();
                    m_appConfiguration.setHTTPHeader(headerName, headerValue);
                    voLog.d(TAG, "CPlayer setHTTPHeader: ItemName = "+ headerName + ", value = " + headerValue);
                }
            }

            OptionItem SEIPostProcessVideoItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLESEIPOSIPROCESSVIDEO_ID.getValue());
            if (SEIPostProcessVideoItem != null) {
                value = (SEIPostProcessVideoItem.getSelect() == 1);
				voLog.d(TAG, "CPlayer enableSEIPostProcessVideo:"+ value);
				
				Ret = m_appConfiguration.enableSEIPostProcessVideo(value);
				// if there is any error when enableSEIPostProcessVideo, return directly
                if( Ret != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE ){
                	return Ret;
                }
               
            }
            
            OptionItem enablePreferredAudioLanguageItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEDEFAULTAUDIOLANGUAGE_ID.getValue());
            if (enablePreferredAudioLanguageItem != null) {
                if(enablePreferredAudioLanguageItem.getSelect() == 1) {
                    OptionItem preferredAudioLanguageItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETDEFAULTAUDIOLANGUAGE_ID.getValue());
                    if (preferredAudioLanguageItem != null) {
                        String preferredAudioLanguage = preferredAudioLanguageItem.getValueListItem(0).getText();
                        String[] audioList=preferredAudioLanguage.split("\\;");
                        m_appAssetSelection.setPreferredAudioLanguage(audioList);
                        voLog.d(TAG, "CPlayer setPreferredAudioLanguage:"+ audioList);
                    }
                }
            }
            
            OptionItem  enablePreferredSubtitleLanguageItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEDEFAULTSUBTITLELANGUAGE_ID.getValue());
            if (enablePreferredSubtitleLanguageItem != null) {
                if(enablePreferredSubtitleLanguageItem.getSelect() == 1) {
                    OptionItem preferredSubtitleLanguageItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETDEFAULTSUBTITLELANGUAGE_ID.getValue());
                    if (preferredSubtitleLanguageItem != null) {
                        String preferredSubtitleLanguage = preferredSubtitleLanguageItem.getValueListItem(0).getText();
                        String[] subtitleList=preferredSubtitleLanguage.split("\\;");
                        m_appAssetSelection.setPreferredSubtitleLanguage(subtitleList);
                        voLog.d(TAG, "CPlayer setPreferredSubtitleLanguage:"+ subtitleList);
                    }
                }
            }
            
            OptionItem enableAnalyticsDisplayItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEANALYTICSDISPLAYTIME_ID.getValue());
            if (enableAnalyticsDisplayItem != null) {
                if(enableAnalyticsDisplayItem.getSelect() == 1) {
                    OptionItem analyticsDisplayItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETANALYTICSDISPLAYTIME_ID.getValue());
                    if (analyticsDisplayItem != null) {
                        int analyticsRefreshTime = analyticsDisplayItem.getValueListItem(0).getValue();
                        m_appAnalytics.enableAnalyticsDisplay(analyticsRefreshTime);
                        voLog.d(TAG, "CPlayer enableAnalyticsDisplay:"+ analyticsRefreshTime);
                    }
                }
            }
            
            OptionItem enableAnalyticsFoundationItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ANALYTICSFOUNDATION_ID.getValue());
            if (enableAnalyticsFoundationItem != null) {
                if (enableAnalyticsFoundationItem.getSelect() == 1) {
                    m_appAnalytics.enableAnalyticsFoundation(true);
                    voLog.d(TAG, "CPlayer enableAnalyticsFoundation: true");
                    OptionItem analyticsFoundationCUIDItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETANALYTICSFOUNDATIONCUID_ID.getValue());
                    if (analyticsFoundationCUIDItem != null) {
                        String CUID = analyticsFoundationCUIDItem.getValueListItem(0).getText();
                        m_appAnalytics.setAnalyticsFoundationCUID(CUID);
                        voLog.d(TAG, "CPlayer setAnalyticsFoundationCUID: " + CUID);
                    }
                    OptionItem analyticsFoundationLocationItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEANALYTICSFOUNDATIONLOCATION_ID.getValue());
                    if (analyticsFoundationLocationItem != null) {
                        value = (analyticsFoundationLocationItem.getSelect() == 1);
                        m_appAnalytics.enableAnalyticsFoundationLocation(value);
                        voLog.d(TAG, "CPlayer enableAnalyticsFoundationLocation: " + value);
                    }
                } else {
                    m_appAnalytics.enableAnalyticsFoundation(false);
                }
            }

            OptionItem enableAnalyticsAgentItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ANALYTICSAGENT_ID.getValue());
            if (null != enableAnalyticsAgentItem) {
                if (enableAnalyticsAgentItem.getSelect() != 0) {
                    m_appAnalytics.enableAnalyticsAgent(true);
                    voLog.d(TAG, "CPlayer enableAnalyticsAgent: true");
                    m_appAnalytics.setAnalyticsAgentAppID(m_context.getPackageName());
                    OptionItem analyticsAgentCUIDItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETANALYTICSAGENTCUID_ID.getValue());
                    if (null != analyticsAgentCUIDItem) {
                        String CUID = analyticsAgentCUIDItem.getValueListItem(0).getText();
                        m_appAnalytics.setAnalyticsAgentCUID(null != CUID ? CUID : "");
                        voLog.d(TAG, "CPlayer setAnalyticsAgentCUID: " + CUID);
                    }
                } else {
                    m_appAnalytics.enableAnalyticsAgent(false);
                }
            }

            OptionItem enableAnalyticsExportItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ANALYTICS_EXPORT_ID.getValue());
            if (enableAnalyticsExportItem != null ) {
                if (enableAnalyticsExportItem.getSelect() == 1) {
                    m_appAnalytics.enableAnalyticsExport(true);
                    m_appAnalytics.setAnalyticsExportListener(m_analyticsListener);
                }else 
                    m_appAnalytics.enableAnalyticsExport(false);
                
            }
            
            OptionItem enablePresentationDelayTimeItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEPRESENTATIONDELAYTIME_ID.getValue());
            if (enablePresentationDelayTimeItem != null) {
                if (enablePresentationDelayTimeItem.getSelect() == 1) {
                    OptionItem presentationDelayTimeItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PRESENTATIONDELAYTIME_ID.getValue());
                    if (presentationDelayTimeItem != null) {
                        int delayTime = presentationDelayTimeItem.getValueListItem(0).getValue();
                        m_appConfiguration.setPresentationDelay(delayTime);
                    }
                }
            }
            
            OptionItem enableHWDecoderMaxResolutionItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLE_HW_DECODER_MAX_RESOLUTION.getValue());
            if (enableHWDecoderMaxResolutionItem != null) {
                if (enableHWDecoderMaxResolutionItem.getSelect() == 1) {
                    OptionItem hwDecoderMaxWidthItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_HW_DECODER_MAX_WIDTH.getValue());
                    OptionItem hwDecoderMaxHeightItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_HW_DECODER_MAX_HEIGHT.getValue());
                    if (hwDecoderMaxWidthItem != null && hwDecoderMaxHeightItem != null) {
                        int maxWidth = hwDecoderMaxWidthItem.getValueListItem(0).getValue();
                        int maxHeight = hwDecoderMaxHeightItem.getValueListItem(0).getValue();
                        m_appConfiguration.setHWDecoderMaxResolution(maxWidth, maxHeight);
                    }
                }
            }
            
            OptionItem enableURLQueryItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLE_URL_QUERY_STRING_ID.getValue());
            if (enableURLQueryItem != null) {
                if (enableURLQueryItem.getSelect() == 1) {
                    OptionItem queryStringItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_QUERY_STRING_ID.getValue());
                    if (queryStringItem != null) {
                        String str = queryStringItem.getValueListItem(0).getText();
                        m_appConfiguration.setURLQueryString(str);
                    }
                }
            }
            
           updateSDKPreferenceSettings();
           updateSubtitleSettings();
            
        }
		return Ret;
    }
    
    private void updateSDKPreferenceSettings() {
        OptionItem SDKPreferenceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCE_ID.getValue());
        if (SDKPreferenceItem != null && SDKPreferenceItem.getSelect() == 1) {
            OptionItem stopKeepLastFramePreferenceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCESTOPKEEPLASTFRAME_ID.getValue());
            OptionItem seekPrecisePreferenceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCESEEKPRECISE_ID.getValue());
            OptionItem audioSwitchImmediatelyPreferenceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCEAUDIOSWITCHIMMEDIATELY_ID.getValue());
            OptionItem baStartFastPreferenceItem = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SDKPREFERENCEBASTARTFASTID.getValue());
            
            if (stopKeepLastFramePreferenceItem != null) {
                if (stopKeepLastFramePreferenceItem.getSelect() == 1) {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_STOP_KEEP_LAST_FRAME);
                } else {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_NO_STOP_KEEP_LAST_FRAME);
                }
                    
            }
            
            if (seekPrecisePreferenceItem != null) {
                if (seekPrecisePreferenceItem.getSelect() == 1) {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_SEEK_PRECISE);
                } else {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_NO_SEEK_PRECISE);
                }
            }
            
            if (audioSwitchImmediatelyPreferenceItem != null) {
                if (audioSwitchImmediatelyPreferenceItem.getSelect() == 1) {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_SELECT_AUDIO_SWITCH_IMMEDIATELY);
                } else {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_NO_SELECT_AUDIO_SWITCH_IMMEDIATELY);
                }
            }
            
            if (baStartFastPreferenceItem != null) {
                if (baStartFastPreferenceItem.getSelect() == 1) {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_BA_START_FAST);
                } else {
                    m_appConfiguration.setPreference(VO_OSMP_PREFERENCE.VO_OSMP_PREF_BA_START_DEFAULT);
                }
            }
        }
    }
    
    private void updateSubtitleSettings() {
        voLog.d(TAG, "CPlayer updateSubtitleSettings().");
        String str = null;
        boolean b = false;
        OptionItem item;
        int num;
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SUBTITLE_ID.getValue());
        {
            b = item.getSelect()==1 ? true:false;
            m_appSubtitle.enableSubtitle(b);
            voLog.d(TAG,"CPlayer enableSubtitle: " + b);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue());
        if (item.getSelect()!=1)
            return;
       
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_USEDEFAULTFONT_ID.getValue());
        if (item.getSelect()==1) {
            m_appSubtitle.resetSubtitleParameter();
            voLog.d(TAG,"CPlayer resetSubtitleParameter.");
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_FONTPOSITION_ID.getValue());
        if (item.getSelect()==1) {
            int topPercent = getOptionItemIntValue(OPTION_ID.OPTION_ST_SETFONTTOPPOSITION_ID);
            int leftPercent = getOptionItemIntValue(OPTION_ID.OPTION_ST_SETFONTLEFTPOSITION_ID);
            int bottomPercent = getOptionItemIntValue(OPTION_ID.OPTION_ST_SETFONTBOTTOMPOSITION_ID);
            int rightPercent = getOptionItemIntValue(OPTION_ID.OPTION_ST_SETFONTRIGHTPOSITION_ID);
            if (topPercent != -1 && leftPercent != -1 && bottomPercent != -1 && rightPercent != -1) {
                m_appSubtitle.setSubtitleBoundingBox(topPercent, leftPercent, bottomPercent, rightPercent);
                voLog.d(TAG, "CPlayer setSubtileFontPosition,topPercetn: " + topPercent + " ,leftPercent: " + leftPercent + " ,bottomPercent: " + bottomPercent 
                        + " ,rightPercent: " + rightPercent);
            }   
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_ENABLEFONTTRIM_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTTRIM_ID.getValue());
            str = (item.getValueListItem(item.getSelect())).getText();
            if (str.length() == 0) 
                str = " ";
            m_appSubtitle.setSubtitleTrim(str);
            voLog.d(TAG,"CPlayer setSubtitleTrim: " + str);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_ENABLEAUTOADJUSTMENT.getValue());
        if (item != null) {
            m_appSubtitle.enableSubtitleAutoAdjustment(item.getSelect() == 1);
            voLog.d(TAG,"CPlayer enableSubtitleAutoAdjustment: " + (item.getSelect() == 1));
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_FONTGRAVITY_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTHORIZONTALPOSITION_ID.getValue());
            VO_OSMP_HORIZONTAL horizontalPosition = VO_OSMP_HORIZONTAL.valueOf(item.getSelect());
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTVERTICALPOSITION_ID.getValue());
            VO_OSMP_VERTICAL verticalPosition = VO_OSMP_VERTICAL.valueOf(item.getSelect());
            m_appSubtitle.setSubtitleGravity(horizontalPosition, verticalPosition);
            voLog.d(TAG, "CPlayer setSubtitleGravity, horizontal position is :" + horizontalPosition + " ,vertical position is : " + verticalPosition);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_FONTSIZE_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTSIZE_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try {
                num=Integer.parseInt(str);
                m_appSubtitle.setSubtitleFontSizeScale(num);
                voLog.d(TAG,"CPlayer setSubtitleFontSizeScale: " + num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
            
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_FONTCOLOROPACITY_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try { 
            num = Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontOpacity(num);
            voLog.d(TAG, "CPlayer setSubtitleFontOpacity: " + num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
    
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_COLORLIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontColor(nclr);
            voLog.d(TAG,"CPlayer setSubtitleFontColor: " + nclr);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_BGCOLOROPACITY_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETBGCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try { 
            num = Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontBackgroundOpacity(num);
            voLog.d(TAG, "CPlayer setSubtitleFontBackgroundOpacity: " + num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
    
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_BGCOLORLIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETBGCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontBackgroundColor(nclr);
            voLog.d(TAG,"CPlayer setSubtitleFontBackgroundColor: " + nclr);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_EDGECOLOROPACITY_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGECOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try { 
            num = Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontEdgeOpacity(num);
            voLog.d(TAG, "CPlayer setSubtitleFontEdgeOpacity: " + num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_EDGECOLORLIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGECOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            m_appSubtitle.setSubtitleFontEdgeColor(nclr);
            voLog.d(TAG,"CPlayer setSubtitleFontEdgeColor: " + nclr);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_EDGETYPELIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGETYPELIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            m_appSubtitle.setSubtitleFontEdgeType(Integer.parseInt(str));
            voLog.d(TAG,"CPlayer setSubtitleFontEdgeType: " + str);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_FONTLIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTLIST_ID.getValue());
            str = (item.getValueListItem(item.getSelect())).getTitle();
            m_appSubtitle.setSubtitleFontName(str);
            voLog.d(TAG,"CPlayer setSubtitleFontName: " + str);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_WINBGCOLOROPACITY_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETWINBGCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{ 
            num = Integer.parseInt(str);
            m_appSubtitle.setSubtitleWindowBackgroundOpacity(num);
            voLog.d(TAG, "CPlayer setSubtitleWindowBackgroundOpacity: " + num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_WINBGCOLORLIST_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETWINBGCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            m_appSubtitle.setSubtitleWindowBackgroundColor(nclr);
            voLog.d(TAG, "CPlayer setSubtitleWindowBackgroundColor: " + nclr);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_UNDERLINEFONT_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETUNDERLINEFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            m_appSubtitle.setSubtitleFontUnderline(b);
            voLog.d(TAG, "CPlayer setSubtitleFontUnderline: " + b);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_BOLDFONT_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETBOLDFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            m_appSubtitle.setSubtitleFontBold(b);
            voLog.d(TAG, "CPlayer setSubtitleFontBold: " + b);
        }
        
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_ITALICFONT_ID.getValue());
        if (item.getSelect()==1) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ST_SETITALICFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            m_appSubtitle.setSubtitleFontItalic(b);
            voLog.d(TAG, "CPlayer setSubtitleFontItalic: " + b);
        }
    }

    public boolean isEnterNTSEnabled() {

        OptionItem itemEnabler = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_NTS_ENABLED_ID.getValue());
        if (itemEnabler != null) {
            if (itemEnabler.getSelect() == 1) {

                return true;

            } else {
                return false;
            }
        }

        return false;
    }

    public String getNTSUrl() {

        OptionItem itemEnabler = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_NTS_ENABLED_ID.getValue());
        if (itemEnabler != null) {
            if (itemEnabler.getSelect() == 1) {
                voLog.d(TAG, "NTS URL is enabled");
                OptionItem item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_NTS_URL_ID.getValue());
                if (item != null) {

                    String url = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));

                    if(url != null && url.length()> 5) {
                        return url;
                    }

                    return null;
                }

            } else {
                voLog.d(TAG, "CPlayer NTS URL is disabled");
            }
        }

        return null;
    }


    public boolean isEnabledDVRPositionSetting() {
        OptionItem itemEnabler = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLE_DVR_POSITION_ID.getValue());
        if (itemEnabler != null && (itemEnabler.getSelect() == 0)) {
            voLog.d(TAG, "EnableDVRPosition is disable");
            return false;
        } else {
            voLog.d(TAG, "EnableDVRPosition is enabled");
            return true;
        }
    }

    public String getNTSMinPos() {

        OptionItem itemEnabler = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_NTS_ENABLED_ID.getValue());
        if (itemEnabler != null) {
            if (itemEnabler.getSelect() == 1) {
                voLog.d(TAG, "NTS URL is enabled");
                OptionItem item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_NTS_MIN_POSITION_ID.getValue());
                if (item != null) {

                    String value = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));

                    if(value.length() > 0 ) {
                        return value;
                    }

                    return null;
                }

            } else {
                voLog.d(TAG, "CPlayer NTS URL is disabled");
            }
        }

        return null;
    }

    public String getPreviousSeekValue() {

        OptionItem itemEnabler = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PREVIOUS_SEEK_ENABLED_ID.getValue());
        if (itemEnabler != null) {
            if (itemEnabler.getSelect() == 1) {
                voLog.d(TAG, "Seek to position for Preview is enabled");
                OptionItem item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_PREVIOUS_VALUE_TO_SEEK_ID.getValue());
                if (item != null) {

                    String value = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));

                    if(value.length() > 0 ) {
                        return value;
                    }

                    return null;
                }

            } else {
                voLog.d(TAG, "CPlayer NTS URL is disabled");
            }
        }

        return null;
    }

    public String get2ndPlayerUrl() {
        OptionItem item2ndPlayerEnabled = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_2ND_PLAYER_URL_ENADLED_ID.getValue());
        if (item2ndPlayerEnabled != null) {
            if (item2ndPlayerEnabled.getSelect() == 1) {
                voLog.d(TAG, "CPlayer 2ndPlayer URL is enabled");
                OptionItem urlOf2ndPlayer = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_2ND_PLAYER_URL_INPUT_ID.getValue());
                if (urlOf2ndPlayer != null) {

                    String url = m_spMain.getString(String.valueOf(urlOf2ndPlayer.getId()), String.valueOf(urlOf2ndPlayer.getValueListItem(0).getValue()));

                    if(url != null && url.length()> 5) {
                        return url;
                    }

                    return null;
                }

            } else {
                voLog.d(TAG, "CPlayer 2ndPlayer URL is disabled");
            }
        }

        return null;
    }
  
    public void initSubtitle() {
        updateSubtitleSettings();
    }
    
    public interface APPUIEventListener {
        VO_OSMP_RETURN_CODE onEvent(APP_UI_EVENT_ID event, int nParam1, int nParam2, Object obj);
            
        
        
        VO_OSMP_RETURN_CODE onDownloadEvent(APP_UI_EVENT_ID event, int arg1, int arg2,
                Object obj);
            
        
    }

    public enum APP_UI_EVENT_ID {
        APP_UI_EVENT_PLAY_COMPLETE                                             (0X00000001),
        APP_UI_EVENT_VIDEO_ASPECT_RATIO                                        (0X0000000E),
        APP_UI_EVENT_VIDEO_SIZE_CHANGED                                        (0X0000000F),
        APP_UI_EVENT_VIDEO_STOP_BUFFER                                         (0X00000004),
        APP_UI_EVENT_AUDIO_STOP_BUFFER                                         (0X00000006),
        APP_UI_EVENT_VIDEO_START_BUFFER                                        (0X00000003),
        APP_UI_EVENT_AUDIO_START_BUFFER                                        (0X00000005),
        APP_UI_EVENT_PD_DOWNLOAD_POSITION                                      (0x02000075),
        APP_UI_EVENT_PD_BUFFERING_PERCENT                                      (0x02000076),
        APP_UI_EVENT_OPEN_FINISHED                                             (0X02000010), 
        APP_UI_EVENT_PROGRAM_CHANGED                                           (0X02000071),
        APP_UI_EVENT_PROGRAM_RESET                                             (0x02000072),
        
        APP_UI_EVENT_STREAMING_DOWNLOADER_OPEN_COMPLETE                        (0X10000001),
        APP_UI_EVENT_STREAMING_DOWNLOADER_MANIFEST_OK                          (0X10000002),

        APP_UI_EVENT_AD_START                                                  (0X03000005),
        APP_UI_EVENT_AD_END                                                    (0X03000006),
        APP_UI_EVENT_VIDEO_PROGRESS                                            (0X03000007),
        APP_UI_EVENT_AD_PLAYBACKINFO                                           (0X0300000A),
        APP_UI_EVENT_AD_SKIPPABLE                                              (0X83000010),
        APP_UI_EVENT_ID_UNDEFINED                                              (0xFFFFFFFF);
        
        private int value;
        
        APP_UI_EVENT_ID(int value) {
            this.value = value;
        }
        
        public int getValue() {
            return value;
        }
        
        public static APP_UI_EVENT_ID valueOf(int value) {
            for (int i = 0; i < APP_UI_EVENT_ID.values().length; i ++) {
                if (APP_UI_EVENT_ID.values()[i].getValue() == value)
                    return APP_UI_EVENT_ID.values()[i];
            }
            
            voLog.e("@@@APPUIListener", "@@@APP_UI_EVENT_ID isn't match. id = " + Integer.toHexString(value));
            return APP_UI_EVENT_ID_UNDEFINED;
        }
    }
    
    private void checkDRMSetting() {
        if(m_verificationString != null && m_appDRM != null) {
            String path = CommonFunc.getUserNativeLibPath(m_context);
            m_appDRM.setDRMFilePath(path);
            m_appDRM.setDRMVerificationInfo(CommonFunc.creatVerificationInfo(m_verificationString));
            voLog.i(TAG,"App set DRM verificationString= "+m_verificationString); 
        }
    }
    
    public VO_OSMP_SRC_FORMAT getPlayerSourceFormat() {
        OptionItem item;
        item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENABLEPUSHPD_ID.getValue());
        if (item != null) {
            if(item.getSelect() == 1) {
                voLog.d(TAG, "CPlayer enable pushPD:true");
                return VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_FFSTREAMING_PUSHPD;
            }
        }
        return VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT;
    }
    
    public VO_OSMP_SRC_FLAG getPlayerSourceFlag() {
        OptionItem item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ASYNCHRONOUSLY_ID.getValue());
        if (item != null) {
            if (item.getSelect() == 1)
                return VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
            if (item.getSelect() == 0)
                return VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC;
        }
        return VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
    }
    
    public VOOSMPOpenParam getPlayerOpenParam() {
        VOOSMPOpenParam openParam = new VOOSMPOpenParam();
        checkPlayerDecodertype();
        openParam.setDecoderType(m_videoDecoderType.getValue() | m_audioDecoderType.getValue());
        voLog.d(TAG, "CPlayer set videoDecoderType: " + m_videoDecoderType + ", set audioDecoderType: " + m_audioDecoderType);
        long openDuration = getPlayerOpenDuration();
        if (openDuration != -1) {                                   //If the value is -1,that means no need to call setDuration this api.  
             openParam.setDuration(openDuration);
             voLog.d(TAG,"CPlayer setOpenDuration:" + openDuration);
        }
        return openParam;
    }
    
    private void checkPlayerDecodertype() {
        m_audioDecoderType = VO_OSMP_DECODER_TYPE.valueOf(m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_AUDIODECODERTYPE_ID.getValue()).getSelect());
        m_videoDecoderType = VO_OSMP_DECODER_TYPE.valueOf(m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_VIDEODECODERTYPE_ID.getValue()).getSelect());
        if (m_bIsHWCodecNotSupport) {
            m_audioDecoderType = VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW;
            m_videoDecoderType = VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW;
            m_bIsHWCodecNotSupport = false;
        }
    }
    
    private long getPlayerOpenDuration() {
        String strOpenDuration;
        OptionItem item;
        if (getPlayerSourceFormat() == VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_FFSTREAMING_PUSHPD) {
            item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_SETPUSHPDOPENDURATION_ID.getValue());
            strOpenDuration = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            if(strOpenDuration != null && strOpenDuration.length() != 0) {
                try{
                   return Long.parseLong(strOpenDuration);
                } catch (NumberFormatException e){
                    voLog.d(TAG,"Input invalid value.");
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }
    
    private int getOptionItemIntValue(OPTION_ID optionID) {
        OptionItem item;
        String str;
        int value = -1;
        item = m_appBehavior.getOptionItemByID(optionID.getValue());
        str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
        try {
            value=Integer.parseInt(str);
        }catch(NumberFormatException e){
             e.printStackTrace();
         }
        return value;
    }
    
    private void playerSyncStart() {
        TimeCal.printStartTime(API_TIME_TYPE.RUN_TIME);
        VO_OSMP_RETURN_CODE  nRet = m_appControl.start();
        TimeCal.printUsingTime(API_TIME_TYPE.RUN_TIME);
        m_appBehavior.processReturnCode("Player sync start", nRet.getValue());
    }

    public VO_OSMP_RETURN_CODE enableLiveStreamingDVRPosition(boolean isEnabled) {
        return m_sdkPlayer.enableLiveStreamingDVRPosition(isEnabled);
    }
}
