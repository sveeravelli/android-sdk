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
import com.visualon.OSMPPlayer.VOOSMPAnalyticsExportListener;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsFilter;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsInfo;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DISPLAY_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;

public class APPCommonPlayerAnalytics {
    private VOCommonPlayer m_player;
    
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    
    public VO_OSMP_RETURN_CODE startAnalyticsNotification(VOOSMPAnalyticsFilter filter) {
        return m_player.startAnalyticsNotification(5000, filter);
    }
    
    public VO_OSMP_RETURN_CODE stopAnalyticsNotification() {
        return m_player.stopAnalyticsNotification();
    }
    
    public VOOSMPAnalyticsInfo getAnalytics(VOOSMPAnalyticsFilter filter) {
        return m_player.getAnalytics(filter);
    }
    
    public VO_OSMP_RETURN_CODE enableAnalytics(int cacheTime) {
        return m_player.enableAnalytics(cacheTime);
    }
    
    public VO_OSMP_RETURN_CODE setAnalyticsDisplayType(VO_OSMP_DISPLAY_TYPE type) {
        return m_player.setAnalyticsDisplayType(type);
    }
    
    public VO_OSMP_RETURN_CODE enableAnalyticsDisplay(int time) {
        return m_player.enableAnalyticsDisplay(time);
    }
    
    public VO_OSMP_RETURN_CODE enableAnalyticsFoundation(boolean value) {
        return m_player.enableAnalyticsFoundation(value);
    }
    
    public VO_OSMP_RETURN_CODE setAnalyticsFoundationCUID(String cuid) {
    	return m_player.setAnalyticsFoundationCUID(cuid);
    }
    
    public VO_OSMP_RETURN_CODE enableAnalyticsFoundationLocation(boolean value) {
        return m_player.enableAnalyticsFoundationLocation(value);
    }

    public VO_OSMP_RETURN_CODE enableAnalyticsAgent(boolean value) {
        return m_player.enableAnalyticsAgent(value);
    }

    public VO_OSMP_RETURN_CODE setAnalyticsAgentAppID(String appID) {
        return m_player.setAnalyticsAgentAppID(appID);
    }

    public VO_OSMP_RETURN_CODE setAnalyticsAgentCUID(String cuid) {
        return m_player.setAnalyticsAgentCUID(cuid);
    }

    public VO_OSMP_RETURN_CODE enableAnalyticsExport(boolean value) {
        return m_player.enableAnalyticsExport(value);
    }
    
    public String getAnalyticsExportPacket() {
    	return m_player.getAnalyticsExportPacket();
    }
    
    public VO_OSMP_RETURN_CODE setAnalyticsExportListener(VOOSMPAnalyticsExportListener listener) {
        return m_player.setAnalyticsExportListener(listener);
    }
}
