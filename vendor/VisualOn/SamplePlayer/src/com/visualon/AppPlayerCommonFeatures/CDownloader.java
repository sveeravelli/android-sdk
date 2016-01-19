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

import com.visualon.AppBehavior.AppBehaviorManager;
import com.visualon.AppPlayerCommonFeatures.CPlayer.APPUIEventListener;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DRM_KEY_EXPIRED_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;



public class CDownloader extends APPCommonPlayerAssetSelection{
  
    private Context m_context;
    private VOCommonPlayer m_player;

    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    
    public CDownloader(Context context){
        m_context = context;
      
    }
    
    public void setBehavior(AppBehaviorManager behavior) {
       
    }
    public VO_OSMP_RETURN_CODE createDownloader(){
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
     }
    public static boolean isDownloaderEventId(int eventId)
    {
        return false;
    }
    public static boolean isDownloaderOpenComplete(int eventId)
    {
        return false;
    }
    public boolean isImplement(){
       return false;
        }
    
    public void setCPlayer(CPlayer cplayer){
       
    }
    public void setUIListener(APPUIEventListener listener) {
     
    }
    public VO_OSMP_RETURN_CODE destroy() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE open(String source, int flag, String localDir) {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE close() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    public VO_OSMP_RETURN_CODE start() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE stop() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE pause() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE resume() {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
 
    public int getDownloadedStreamDuration() {
            return 0;
         }

    public int getTotalStreamDuration() {
           return 0;
        }

    public VO_OSMP_RETURN_CODE deleteContent(String url) {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE setDRMVerificationInfo(
            VOOSMPVerificationInfo info) {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public VO_OSMP_RETURN_CODE setDRMLibrary(String libName, String libApiName) {
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        
    }
    public int getAssetCount(AssetType type){
      
        return 0;
       
    }
    public boolean isTrackAvailable(AssetType type, int index){
     
        return false;
    } 
    
    public int getAssetIndex(AssetType type, AssetStatus status){
       
        return 0;
    }
    
    public VOOSMPAssetProperty getAssetProperty(AssetType type, int index){
       
        return null;
    }
    
    public VO_OSMP_RETURN_CODE selectAsset(AssetType type, int index){
       
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE commitSelection(){
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
    
    public VO_OSMP_RETURN_CODE clearSelection(){
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
      
    public void setVerificationString(String str) {
        
    }
    
    public VO_OSMP_DRM_KEY_EXPIRED_STATUS getDRMKeyExpiredStatus() {
    	return VO_OSMP_DRM_KEY_EXPIRED_STATUS.VO_OSMP_DRM_KEY_EXPIRED_ERROR;
    }
    
    public String getDRMUniqueIdentifier(){
    	return m_player.getDRMUniqueIdentifier();
    }
   
}

