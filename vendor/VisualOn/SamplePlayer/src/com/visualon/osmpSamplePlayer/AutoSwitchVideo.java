/************************************************************************
VisualOn Proprietary
Copyright (c) 2013, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.osmpSamplePlayer;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.visualon.OSMPUtils.voLog;
import com.visualon.osmpSamplePlayer.AssetOp.AssetInfo;
import com.visualon.osmpSamplePlayer.AssetOp.AssetType;

public class AutoSwitchVideo {
    
    private static final         String TAG                = "Auto Switch Video";

    private static final boolean AUTOSWITCH_VIDEO_ENABLE   = false;
    
    private static final int     AUTOSWITCH_VIDEO_INTERVAL = 30000;   // ms
    private Timer                m_timerAutoSwitchVideo    = null;    // Timer for auto-switch video/bps
    private TimerTask            m_ttAutoSwitchVideo       = null;    // TimeTask for auto-switch video/bps
    
    private AssetOp              m_assetOp                 = null;
    
    AutoSwitchVideo(AssetOp assetOp) {
        m_assetOp = assetOp;
    }
    
    public void startAutoSwitchVideo() {
        
        if (!AUTOSWITCH_VIDEO_ENABLE) 
            return;
        
        voLog.i(TAG, "Start Auto Switch Video");
            
        if (m_ttAutoSwitchVideo != null)
            m_ttAutoSwitchVideo = null;

        m_ttAutoSwitchVideo = new TimerTask() {
            public void run() {
                m_assetOp.queryAsset(AssetType.Asset_Video);
                ArrayList<AssetInfo> lstVideoInfo = m_assetOp.getAssetList(AssetType.Asset_Video);

                if (lstVideoInfo != null) {
                    int nSize = lstVideoInfo.size();
                    voLog.i(TAG, "Video list size: " + nSize +" (Index 0 is AutoMode)");
                    
                    if (nSize <= 1)
                        return;

                    for (int i = 0; i < nSize; i++) {
                        if (lstVideoInfo.get(i).m_bPlaying == true) {
                            
                            voLog.i(TAG, "Current playing video index: " + i + ", video stream ID: " + lstVideoInfo.get(i).m_nID);
                            
                            if (i + 1 < nSize) {
                                m_assetOp.selectVideo(lstVideoInfo.get(i + 1).m_nID);
                                voLog.i(TAG, "Auto Select video stream ID: " + lstVideoInfo.get(i+1).m_nID);
                            } else {
                                m_assetOp.selectVideo(lstVideoInfo.get(1).m_nID); // 0 is AutoMode, skip
                                voLog.i(TAG, "Auto Select video stream ID: " + lstVideoInfo.get(1).m_nID);
                            }
                                    
                            break;
                        }
                    }
                }
            }
        };

        if (m_timerAutoSwitchVideo == null) {
            m_timerAutoSwitchVideo = new Timer();
        }
        m_timerAutoSwitchVideo.schedule(m_ttAutoSwitchVideo,
                AUTOSWITCH_VIDEO_INTERVAL, AUTOSWITCH_VIDEO_INTERVAL);

    }
    
    public void stopAutoSwitchVideo() {
       
        if (!AUTOSWITCH_VIDEO_ENABLE)
            return;
        
        voLog.i(TAG, "Stop Auto Switch Video");
        
        if(m_timerAutoSwitchVideo != null) {
            m_timerAutoSwitchVideo.cancel();
            m_timerAutoSwitchVideo = null;
            m_ttAutoSwitchVideo = null;
        }
    }
}
