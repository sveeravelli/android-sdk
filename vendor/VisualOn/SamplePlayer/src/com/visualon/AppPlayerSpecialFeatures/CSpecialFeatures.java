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

package com.visualon.AppPlayerSpecialFeatures;

import android.content.Context;
import android.graphics.Rect;
import android.view.SurfaceView;

import com.visualon.AppBehavior.AppBehaviorManager;
import com.visualon.AppBehavior.AppBehaviorManagerImpl.OPTION_ID;
import com.visualon.AppBehavior.OptionItem;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerConfiguration;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerControl;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerRTSPConfiguration;
import com.visualon.AppPlayerCommonFeatures.APPCommonPlayerStatus;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPRTSPStatistics;
import com.visualon.OSMPPlayer.VOOSMPSEIPicTiming;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ASPECT_RATIO;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DECODER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DOWNLOAD_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SEI_INFO_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SOURCE_STREAMTYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ZOOM_MODE;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;

import java.io.IOException;
import java.io.InputStream;

public class CSpecialFeatures {
    private APPCommonPlayerConfiguration              m_appConfiguration     = null;
    private APPCommonPlayerControl                    m_appControl           = null;
    private APPCommonPlayerStatus                     m_appStatus            = null;
    private APPCommonPlayerRTSPConfiguration          m_appRTSPConfiguration = null;
    private AppBehaviorManager                        m_appBehavior          = null;
    private String                                    m_appUrl               = null;
    private VOCommonPlayer                            m_multiInstancePlayer  = null;
    private int                                       m_videoWidth           = 0;
    private int                                       m_videoHeight          = 0;
    private Context                                   m_context              = null;
    public void setAPPConfiguration(APPCommonPlayerConfiguration config){
        m_appConfiguration = config;
    }
    
    public void setAPPControl(APPCommonPlayerControl control){
        m_appControl = control;
    }
    
    public void setAPPStatus(APPCommonPlayerStatus status){
        m_appStatus = status;
    }
    
    public void setAPPRTSPConfiguration(APPCommonPlayerRTSPConfiguration config){
        m_appRTSPConfiguration = config;
    }
    
    public void setAPPBehaviorManager(AppBehaviorManager behavior){
        m_appBehavior = behavior;
    }
    
    public void setContext(Context context){
        m_context = context;
    }
    
    public void setAPPUrl(String url){
        m_appUrl = url;
    }
    
    public void setVideoWidth(int width) {
    	m_videoWidth = width;
    }
    
    public int getVideoWidth() {
    	return m_videoWidth;
    }
    
    public void setVideoHeight(int height) {
    	m_videoHeight = height;
    }
    
    public int getVideoHeight() {
    	return m_videoHeight;
    }
    public VO_OSMP_RETURN_CODE setScreenBrightness(int brightness){
        if (m_appControl != null)
            return m_appControl.setScreenBrightness(brightness);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setVolume(float volume){
        if (m_appControl != null)
            return m_appControl.setVolume(volume);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE mute(){
        if (m_appControl != null)
            return m_appControl.mute();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE unmute(){
        if (m_appControl != null)
            return m_appControl.unmute();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setVolume(float leftVolume, float rightVolume) {
        if (m_appControl != null)
            return m_appControl.setVolume(leftVolume, rightVolume);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE updateSourceURL(String url){
        if (m_appControl != null)
            return m_appControl.updateSourceURL(url);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE setZoomMode(VO_OSMP_ZOOM_MODE mode, Rect rect){
        if (m_appControl != null)
            return m_appControl.setZoomMode(mode, rect);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    
    public VO_OSMP_RETURN_CODE setVideoAspectRatio(VO_OSMP_ASPECT_RATIO ratio){
        if (m_appControl != null)
            return m_appControl.setVideoAspectRatio(ratio);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }

    
    public VO_OSMP_RETURN_CODE enableDRMOfflineMode(boolean enable) {
        return (null == m_appConfiguration) ? VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT : m_appConfiguration.enableDRMOfflineMode(enable);
    }

    
    public boolean canPlayIFrameOnly()
    {
        return (null == m_appStatus) ? false : m_appStatus.canPlayIFrameOnly();
    }
    
    public VO_OSMP_RETURN_CODE setPlayIFrameOnly(boolean enable, float speed)
    {
        return (null == m_appConfiguration) ? VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT : m_appConfiguration.setPlayIFrameOnly(enable, speed);
    }
    
    public VOOSMPSEIPicTiming getSEIInfo(long time, VO_OSMP_SEI_INFO_FLAG flag){
        if (m_appStatus != null)
            return m_appStatus.getSEIInfo(time, flag);
        else
            return null;
    }
    
    public VO_OSMP_RETURN_CODE enableSEI(VO_OSMP_SEI_INFO_FLAG flag){
        if (m_appConfiguration != null)
            return m_appConfiguration.enableSEI(flag);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE startSEINotification(int interval){
        if (m_appControl != null)
            return m_appControl.startSEINotification(interval);
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE stopSEINotification(){
        if (m_appControl != null)
            return m_appControl.stopSEINotification();
        else
            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VOOSMPRTSPStatistics getRTSPStatistics() {
        if (m_appRTSPConfiguration == null)
            return null;
        return m_appRTSPConfiguration.getRTSPStatistics();
    }
    
    public int[] getAudioDecodingBitrate(){
        if (m_appStatus != null)
            return m_appStatus.getAudioDecodingBitrate();
        else
            return null;
    }
    
    public int[] getVideoDecodingBitrate(){
        if (m_appStatus != null)
            return m_appStatus.getVideoDecodingBitrate();
        else
            return null;
    }
    
    public int getValidBufferDuration(){
        if (m_appStatus != null)
            return m_appStatus.getValidBufferDuration();
        else
            return -1;
    }
    
    public VO_OSMP_DOWNLOAD_STATUS getDownloadStatus(VO_OSMP_SOURCE_STREAMTYPE trackType){
        if (m_appStatus != null)
            return m_appStatus.getDownloadStatus(trackType);
        else
            return null;
    }
    
    public void startMultiInstancePlayer(SurfaceView view) {
    	creatMultiInstancePlayer(view);
    	checkDRMSetting();
    	
        VO_OSMP_SRC_FORMAT format = VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT;
        VO_OSMP_SRC_FLAG eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
        if (m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ASYNCHRONOUSLY_ID.getValue()).getSelect() == 0)
        	eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC;
        VOOSMPOpenParam openParam = new VOOSMPOpenParam();
        VO_OSMP_DECODER_TYPE audioDecoderType = VO_OSMP_DECODER_TYPE.valueOf(m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_AUDIODECODERTYPE_ID.getValue()).getSelect());
        VO_OSMP_DECODER_TYPE videoDecoderType = VO_OSMP_DECODER_TYPE.valueOf(m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_VIDEODECODERTYPE_ID.getValue()).getSelect());
        openParam.setDecoderType(videoDecoderType.getValue() | audioDecoderType.getValue());
        m_multiInstancePlayer.open(m_appUrl, eSourceFlag, format, openParam);
        if (eSourceFlag == VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC)
        	m_multiInstancePlayer.start();
    }
    
    public void stopMultiInstancePlayer() {
        if (m_multiInstancePlayer != null) {
            m_multiInstancePlayer.stop();
            m_multiInstancePlayer.close();
            m_multiInstancePlayer.destroy();
            m_multiInstancePlayer = null;
        }
    }
    
    private void creatMultiInstancePlayer(SurfaceView view ) {
    	 VO_OSMP_PLAYER_ENGINE playerEngine;
         OptionItem item = new OptionItem();
         item = m_appBehavior.getOptionItemByID(OPTION_ID.OPTION_ENGINETYPE_ID.getValue());
         if (item == null)
             playerEngine =VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;
         else
          playerEngine = VO_OSMP_PLAYER_ENGINE.valueOf(item.getSelect());
         m_multiInstancePlayer = m_appControl.creatPlayer(playerEngine);
         m_multiInstancePlayer.setView(view);
         m_multiInstancePlayer.setViewSize(view.getWidth(), view.getHeight());
         m_multiInstancePlayer.setOnEventListener(m_listenerEvent);
         String capFile = CommonFunc.getUserPath(m_context) + "/" + "cap.xml";
         m_multiInstancePlayer.setDeviceCapabilityByFile(capFile);
         InputStream is = null;
         byte[] b = new byte[32*1024];
         try {
             is = m_context.getAssets().open("voVidDec.dat");
             is.read(b);
             is.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         m_multiInstancePlayer.setLicenseContent(b);
    }
    private void checkDRMSetting() {
    	String drmType = CommonFunc.getStringPreferenceValue(Definition.PREFERENCE_KEY_DRM_TYPE);
    	if (drmType.equals(Definition.DRM_TYPE_A)) {
    		m_multiInstancePlayer.setDRMAdapter("libvoDRMCommonAES128.so", true);
    	} else if (drmType.equals(Definition.DRM_TYPE_V)) {
    		String serverIP = CommonFunc.getStringPreferenceValue(Definition.PREFERENCE_KEY_VCAS_IP);
        	String serverPort = CommonFunc.getStringPreferenceValue(Definition.PREFERENCE_KEY_VCAS_PORT);
        	String UUID = CommonFunc.getStringPreferenceValue(Definition.PREFERENCE_KEY_VCAS_UUID);
        	
        	if (UUID.length() > 0) {
        		m_multiInstancePlayer.setDRMLibrary("voDRM", "voGetDRMAPI");
        		String path = CommonFunc.getUserNativeLibPath(m_context);
        		m_multiInstancePlayer.setDRMFilePath(path);
        		if (serverIP.length() > 0 && serverPort.length() > 0) {
        		    String verificationString = serverIP + ":" + serverPort;
                    m_multiInstancePlayer.setDRMVerificationInfo(CommonFunc.creatVerificationInfo(verificationString));
        		}
            }
    	}
    	
    }
    private VOCommonPlayerListener m_listenerEvent = new VOCommonPlayerListener() {

        @Override
        public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID,
                int arg1, int arg2, Object arg3) {
            // TODO Auto-generated method stub
            switch(nID) {
            case VO_OSMP_SRC_CB_OPEN_FINISHED: {
                m_multiInstancePlayer.start();
                break;
            }
            default:
                break;
            }
            return null;
        }

        @Override
        public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
                int arg1, int arg2, Object arg3) {
            return null;
        }
        
    };
       
}
