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



import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOOSMPSEIPicTiming;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DOWNLOAD_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SEI_INFO_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SOURCE_STREAMTYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_STATUS;

public class APPCommonPlayerStatus {
    private VOCommonPlayer m_player;
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    public VO_OSMP_STATUS getPlayerStatus(){
        return m_player.getPlayerStatus();
    }
    
    public VOOSMPSEIPicTiming getSEIInfo(long time, VO_OSMP_SEI_INFO_FLAG flag) {
        return m_player.getSEIInfo(time, flag);
    }
    
    public boolean isLiveStreaming(){
        return m_player.isLiveStreaming();
    }
    
    public long getPosition(){
        return m_player.getPosition();
    }
    
    public long getMinPosition(){
        return m_player.getMinPosition();
    }
    
    public long getMaxPosition(){
        return m_player.getMaxPosition();
    }
    
    public long getDuration(){
        return m_player.getDuration();
    }
    
    public String getDRMUniqueIdentifier(){
        return m_player.getDRMUniqueIdentifier();
    }
    
    public String getVersion(VO_OSMP_MODULE_TYPE module){
        return m_player.getVersion(module);
    }
    
    public boolean canBePaused(){
        return m_player.canBePaused();
    }
    
    public int[] getAudioDecodingBitrate(){
        return m_player.getAudioDecodingBitrate();
    }
    
    public int[] getVideoDecodingBitrate(){
        return m_player.getVideoDecodingBitrate();
    }
    
    public int getValidBufferDuration() {
    	return m_player.getValidBufferDuration();
    }
    
    public VO_OSMP_DOWNLOAD_STATUS getDownloadStatus(VO_OSMP_SOURCE_STREAMTYPE trackType) {
    	return m_player.getDownloadStatus(trackType);
    }
    
    public boolean canPlayIFrameOnly()
    {
        return m_player.canPlayIframeOnly();
    }
}
