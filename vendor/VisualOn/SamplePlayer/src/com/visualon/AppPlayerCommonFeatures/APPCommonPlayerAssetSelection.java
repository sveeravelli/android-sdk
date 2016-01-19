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



import com.visualon.AppPlayerCommonFeatures.TimeCal.API_TIME_TYPE;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetIndex;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;



public class APPCommonPlayerAssetSelection{
    private VOCommonPlayer m_player;
    public  enum AssetType {
        Asset_Video,          // BpsQuality 
        Asset_Audio,          // AudioTrack 
        Asset_Subtitle        // External/Internal Subtitle or CloseCaption
    }
    public enum AssetStatus {
        Asset_Playing,
        Asset_Selected
    }
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    
    public int getAssetCount(AssetType type){
        int count = 0;
       if(type == AssetType.Asset_Video)
           count = m_player.getVideoCount();
       if(type == AssetType.Asset_Audio)
           count = m_player.getAudioCount();
       if(type == AssetType.Asset_Subtitle)
           count = m_player.getSubtitleCount();
        return count;
       
    }
    
    public boolean isTrackAvailable(AssetType type, int index){
        boolean ret = false;
        if (m_player == null)
        	return false;
        if(type == AssetType.Asset_Video)
            return m_player.isVideoAvailable(index);
        if(type == AssetType.Asset_Audio)
            return m_player.isAudioAvailable(index);
        if(type == AssetType.Asset_Subtitle)
            return m_player.isSubtitleAvailable(index);
        return ret;
    } 
    
    public int getAssetIndex(AssetType type, AssetStatus status){
        int assetIndex = -1;
        VOOSMPAssetIndex asset = null;
        if(status == AssetStatus.Asset_Playing)
            asset = m_player.getPlayingAsset();
        if(status == AssetStatus.Asset_Selected)
            asset = m_player.getCurrentSelection();
        if(asset == null)
            return assetIndex;
        if(type == AssetType.Asset_Video)
            assetIndex = asset.getVideoIndex();
        if(type == AssetType.Asset_Audio)
            assetIndex = asset.getAudioIndex();
        if(type == AssetType.Asset_Subtitle)
            assetIndex = asset.getSubtitleIndex();
        return assetIndex;
    }
    
    public VOOSMPAssetProperty getAssetProperty(AssetType type, int index){
        if(type == AssetType.Asset_Video)
            return m_player.getVideoProperty(index);
        if(type == AssetType.Asset_Audio)
            return m_player.getAudioProperty(index);
        if(type == AssetType.Asset_Subtitle)
            return m_player.getSubtitleProperty(index);
        return null;
    }
    
    public VO_OSMP_RETURN_CODE selectAsset(AssetType type, int index){
        VO_OSMP_RETURN_CODE ret = VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
        if(type == AssetType.Asset_Video) {
            TimeCal.printStartTime(API_TIME_TYPE.SELECT_VIDEO);
            ret = m_player.selectVideo(index);
            TimeCal.printUsingTime(API_TIME_TYPE.SELECT_VIDEO_COMPLETE);
        }
        if(type == AssetType.Asset_Audio) {
            TimeCal.printStartTime(API_TIME_TYPE.SELECT_AUDIO);
            ret = m_player.selectAudio(index);
            TimeCal.printUsingTime(API_TIME_TYPE.SELECT_AUDIO_COMPLETE);
        }
        if(type == AssetType.Asset_Subtitle) {
            TimeCal.printStartTime(API_TIME_TYPE.SELECT_SUBTITLE);
            ret = m_player.selectSubtitle(index);
            TimeCal.printUsingTime(API_TIME_TYPE.SELECT_SUBTITLE_COMPLETE);
        }
            
        return ret;
    }
    
    public VO_OSMP_RETURN_CODE commitSelection(){
        TimeCal.printStartTime(API_TIME_TYPE.COMMIT_SELECTION);
        VO_OSMP_RETURN_CODE ret = m_player.commitSelection();
        TimeCal.printUsingTime(API_TIME_TYPE.COMMIT_SELECTION);
        return ret;
    }
    
    public VO_OSMP_RETURN_CODE clearSelection(){
        return m_player.clearSelection();
    }
    
    public VO_OSMP_RETURN_CODE setPreferredAudioLanguage(String[] languageList){
        return m_player.setPreferredAudioLanguage(languageList);
    }
    
    public VO_OSMP_RETURN_CODE setPreferredSubtitleLanguage(String[] languageList){
        return m_player.setPreferredSubtitleLanguage(languageList);
    }
    
}
