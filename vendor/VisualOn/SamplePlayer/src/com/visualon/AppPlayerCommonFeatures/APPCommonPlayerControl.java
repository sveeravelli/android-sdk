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



import java.io.File;

import android.content.Context;
import android.graphics.Rect;
import android.view.SurfaceView;
import android.view.View;

import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ASPECT_RATIO;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SRC_FORMAT;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ZOOM_MODE;
import com.visualon.OSMPPlayerImpl.VOCommonPlayerImpl;


public class APPCommonPlayerControl {
     private static final String  TAG                  = "@@@OSMP+APPCommonPlayerControl"; 
     private boolean openAsync;

     private Context mContext;
     
     public APPCommonPlayerControl(Context context){
         mContext = context;
     }
     private VOCommonPlayer m_player;
     public void setPlayer(VOCommonPlayer player){
         m_player = player;
     }
     public VOCommonPlayer creatPlayer(VO_OSMP_PLAYER_ENGINE engineType){
         VO_OSMP_RETURN_CODE nRet;
         String apkPath = CommonFunc.getUserNativeLibPath(mContext);
         VOOSMPInitParam initParam = null;
        initParam = new VOOSMPInitParam();
        initParam.setContext(mContext);
        initParam.setLibraryPath(apkPath);
        VOCommonPlayer player = new VOCommonPlayerImpl();
        nRet = player.init(engineType, initParam);
        if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            voLog.v(TAG, "MediaPlayer is initialized.");
        } else {
            voLog.v(TAG, "MediaPlayer is initialized failed!");
        }

        return player;
    }

    public VOCommonPlayer creatPlayer(VO_OSMP_PLAYER_ENGINE engineType, VOCommonPlayer player){
        VO_OSMP_RETURN_CODE nRet;
        String apkPath = CommonFunc.getApkPath(mContext);
        VOOSMPInitParam initParam = null;
        initParam = new VOOSMPInitParam();
        initParam.setContext(mContext);
        initParam.setLibraryPath(apkPath);
        nRet = player.init(engineType, initParam);
        if (nRet == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            voLog.v(TAG, "MediaPlayer is initialized.");
        } else {
            voLog.e(TAG, "MediaPlayer is initialized failed!");
        }
        return player;
    }
  
  public VO_OSMP_RETURN_CODE open(String url){
       VO_OSMP_RETURN_CODE ret = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
       if (m_player == null) {
           return ret;
       }
       VO_OSMP_SRC_FLAG eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
       if(!openAsync)
           eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC;
       VOOSMPOpenParam openParam = new VOOSMPOpenParam();
       VO_OSMP_SRC_FORMAT format = VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT;
       ret = m_player.open(url, eSourceFlag, format, openParam);
       return ret;
        }

   public VO_OSMP_RETURN_CODE setOnEventListener(VOCommonPlayerListener instance){
      return m_player.setOnEventListener(instance);
   }
  
   public long setPosition(long position){
       return m_player.setPosition(position);
   }
   
   public VO_OSMP_RETURN_CODE setView(View view){
       return m_player.setView(view);
   }
   
   public VO_OSMP_RETURN_CODE start(){
       return m_player.start();
   }
   
   public VO_OSMP_RETURN_CODE pause(){
       return m_player.pause();
   }
   
   public VO_OSMP_RETURN_CODE stop(){
       return m_player.stop();
   }
   
   public VO_OSMP_RETURN_CODE close(){
       return m_player.close();
   }
   
   public VO_OSMP_RETURN_CODE destroy(){
       return m_player.destroy();
   }
   
   public VO_OSMP_RETURN_CODE setVolume(float volume){
       return m_player.setVolume(volume);
   }
   
   public VO_OSMP_RETURN_CODE mute(){
       return m_player.mute();
   }
   
   public VO_OSMP_RETURN_CODE unmute(){
       return m_player.unmute();
   }
   
   public VO_OSMP_RETURN_CODE setSurfaceChangeFinished(){
       return m_player.setSurfaceChangeFinished();
   }
   
   public VO_OSMP_RETURN_CODE setVideoAspectRatio(VO_OSMP_ASPECT_RATIO ratio){
       return m_player.setVideoAspectRatio(ratio);
   }
   
   public VO_OSMP_RETURN_CODE setViewSize(int width, int height){
       return m_player.setViewSize(width, height);
   }
   
   public VO_OSMP_RETURN_CODE suspend(){
       return m_player.suspend(false);
   }
   
   public VO_OSMP_RETURN_CODE resume(SurfaceView view){
       return m_player.resume(view);
   }
   
   public VO_OSMP_RETURN_CODE startSEINotification(int interval){
       return m_player.startSEINotification(interval);
   }
   
   public VO_OSMP_RETURN_CODE stopSEINotification(){
       return m_player.stopSEINotification();  
   }
   
   public VO_OSMP_RETURN_CODE stopAnalyticsNotification(){
       return m_player.stopAnalyticsNotification();
   }
   
   public VO_OSMP_RETURN_CODE updateSourceURL(String url){
       return m_player.updateSourceURL(url);
   }
   
   public VO_OSMP_RETURN_CODE setScreenBrightness(int brightness){
       return m_player.setScreenBrightness(brightness);
   }
   
   public VO_OSMP_RETURN_CODE setZoomMode(VO_OSMP_ZOOM_MODE mode, Rect rect){
       return m_player.setZoomMode(mode, rect);
   }
   
   public VO_OSMP_RETURN_CODE setVolume(float leftVolume, float rightVolume) {
       return m_player.setVolume(leftVolume, rightVolume);
   }
}
