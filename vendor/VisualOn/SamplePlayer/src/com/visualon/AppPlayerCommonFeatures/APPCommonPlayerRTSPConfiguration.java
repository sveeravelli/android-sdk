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
import com.visualon.OSMPPlayer.VOOSMPRTSPPort;
import com.visualon.OSMPPlayer.VOOSMPRTSPStatistics;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RTSP_CONNECTION_TYPE;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;

public class APPCommonPlayerRTSPConfiguration {
    private VOCommonPlayer m_player;
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    public VO_OSMP_RETURN_CODE setRTSPConnectionType(VO_OSMP_RTSP_CONNECTION_TYPE type){
        return m_player.setRTSPConnectionType(type);
    }
    
    public VO_OSMP_RETURN_CODE setRTSPConnectionPort(VOOSMPRTSPPort port){
        return m_player.setRTSPConnectionPort(port);
    }
    
    public VO_OSMP_RETURN_CODE setRTSPOverHTTPConnectionPort(int portNum){
        return m_player.setRTSPOverHTTPConnectionPort(portNum);
    }
    
    public VO_OSMP_RETURN_CODE enableRTSPOverHTTP(boolean enable){
        return m_player.enableRTSPOverHTTP(enable);
    }
    
    public VO_OSMP_RETURN_CODE setRTSPMaxSocketErrorCount(int count){
        return m_player.setRTSPMaxSocketErrorCount(count);
    }
    
    public VO_OSMP_RETURN_CODE setRTSPConnectionTimeout(int time){
        return m_player.setRTSPConnectionTimeout(time);
    }
    
    public VO_OSMP_RETURN_CODE setHTTPVerificationInfo(VOOSMPVerificationInfo info){
        return m_player.setHTTPVerificationInfo(info);
    }
    
    public VO_OSMP_RETURN_CODE enableLowLatencyVideo(boolean enable){
        return m_player.enableLowLatencyVideo(enable);
    }
    
    public VOOSMPRTSPStatistics getRTSPStatistics() {
        return m_player.getRTSPStatistics();
    }
}
