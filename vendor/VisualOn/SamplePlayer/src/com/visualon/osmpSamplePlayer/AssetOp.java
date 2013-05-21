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

import android.content.Context;

import com.visualon.OSMPBasePlayer.voOSBasePlayer;
import com.visualon.OSMPUtils.voLog;
import com.visualon.OSMPUtils.voOSProgramInfo;
import com.visualon.OSMPUtils.voOSStreamInfo;
import com.visualon.OSMPUtils.voOSTrackInfo;
import com.visualon.OSMPUtils.voOSType;
import com.visualon.OSMPUtils.voOSType.VOOSMP_SOURCE_STREAMTYPE;


public class AssetOp {
	
	 public enum AssetType {
		 Asset_Video,       // BpsQuality 
	     Asset_Audio,       // AudioTrack 
	     Asset_Subtitle,    // External/Internal Subtitle or CloseCaption
	 }
	    
	 public class AssetInfo {
		int        m_nIndex;    // Index of position on UI
	    int        m_nID;       // Stream ID for Video, track ID for Audio, index for Subtitle
	    String     m_strDisplay;// Display name on UI
	    boolean    m_bPlaying;  // If it is playing
	        
	    public AssetInfo(int nIndex, int nID, String strDisplay, boolean bPlaying) {
	        m_nIndex     = nIndex;
	        m_nID        = nID;
	        m_strDisplay = strDisplay;
	        m_bPlaying   = bPlaying;
	    }
	}
	 
	private static final String	 	TAG = "@@@OSMP+ AssetOperation";		// Tag for voLog messages
	
	private static final int        STREAMID_AUTO = 0xffffffff;   // Auto bitrate id
	private static final int 		INDEX_NONE = -1;
	
	private voOSBasePlayer 			m_osbasePlayer	=	null;
	private Context					m_ctx 			= 	null;
	
	ArrayList<AssetInfo> m_lstVideoAsset    = new ArrayList<AssetInfo>();
	ArrayList<AssetInfo> m_lstAudioAsset    = new ArrayList<AssetInfo>();
	ArrayList<AssetInfo> m_lstSubtitleAsset = new ArrayList<AssetInfo>();
	
	public AssetOp(voOSBasePlayer player, Context context) {
		m_osbasePlayer  = player;
		m_ctx 			= context;
	}
	
	public ArrayList<AssetInfo> getAssetList(AssetType type) {
		
		ArrayList<AssetInfo> lstAsset = null;
		
		if (type == AssetType.Asset_Video)
			lstAsset = m_lstVideoAsset;
		else if (type == AssetType.Asset_Audio)
			lstAsset = m_lstAudioAsset;
		else if (type == AssetType.Asset_Subtitle)
			lstAsset = m_lstSubtitleAsset;
		
		return lstAsset;
	}
	
	public boolean queryAsset(AssetType type) {
		
		boolean bRet = false;
		
		if (type == AssetType.Asset_Video)
			bRet = queryVideoAsset();
		else if (type == AssetType.Asset_Audio)
			bRet = queryAudioAsset();
		else if (type == AssetType.Asset_Subtitle)
			bRet = querySubtitleAsset();

		return bRet; 
	}
	
	public void onAssetClick(int nIndex, AssetType type) {
		if (type == AssetType.Asset_Video)
			onVideoAssetClick(nIndex);
		else if (type == AssetType.Asset_Audio)
			onAudioAssetClick(nIndex);
		else if (type == AssetType.Asset_Subtitle)
			onSubtitleAssetClick(nIndex);
	}
	  
	private voOSStreamInfo getCurrentStreamInfo() {
		    
		if (m_osbasePlayer == null || m_osbasePlayer.GetProgramCount() <= 0)
			return null;

		voOSProgramInfo programInfo = (voOSProgramInfo) m_osbasePlayer.GetProgramInfo(0);
		if (programInfo == null)
			return null;

		if (programInfo.getStreamCount() <= 0)
			return null;

		voOSStreamInfo[] streamArr = programInfo.getStreamInfo();
		if (streamArr == null)
			return null;

		for (int i = 0; i < streamArr.length; i++) {
			if ((streamArr[i].getSelInfo() & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0)
				return streamArr[i];
		}

		return null;
	}
	
	private boolean queryVideoAsset() {
	   
	    if (m_lstVideoAsset == null || m_osbasePlayer == null ||
	    		m_osbasePlayer.GetProgramCount() <= 0)
	        return false;
	        
	    voOSProgramInfo info = (voOSProgramInfo)m_osbasePlayer.GetProgramInfo(0);
	    if (info == null || info.getStreamCount() <= 0)
	    	return false;
	        
	    voOSStreamInfo [] streamArr = info.getStreamInfo();
	    if (streamArr == null)
	    	return false;
	        
	    m_lstVideoAsset.clear();
	        
	    if (streamArr.length == 0)
            return false;
	    
	    if (streamArr.length > 1) {
	     
	        m_lstVideoAsset.add(new AssetInfo(0, STREAMID_AUTO,
	                m_ctx.getResources().getString(R.string.Player_BpsQuality_Auto),
	                isVideoAutoMode() ? true : false));
	            
	        for (int i = 0; i < streamArr.length; i++) {
                voOSStreamInfo streamInfo = streamArr[i];

                String strBitrate = CommonFunc.bitrateToString(streamInfo.getBitrate());
                
                boolean bPlaying;
                
                if (isVideoAutoMode())
                    bPlaying = false;
                else
                    bPlaying = ( (streamInfo.getSelInfo()
                            & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0 ) ? 
                            true : false;
             
                m_lstVideoAsset.add(new AssetInfo(i + 1, streamInfo.getStreamID(),
                        strBitrate, bPlaying));
            }

	        
	    } else {
	        
	        voOSStreamInfo streamInfo = streamArr[0];
	        
	        String strBitrate = CommonFunc.bitrateToString(streamInfo.getBitrate());
            
            m_lstVideoAsset.add(new AssetInfo(0, streamInfo.getStreamID(),
                    strBitrate, true));
	    }
	        
	    return true;
	}
	    
	private void onVideoAssetClick(int nIndex) {
		if (m_lstVideoAsset == null || nIndex < 0 || nIndex >= m_lstVideoAsset.size())
			return;

		selectVideo(m_lstVideoAsset.get(nIndex).m_nID);
	}

	void selectVideo(int nStream) {
	        
		if (m_osbasePlayer == null)
			return;

		m_osbasePlayer.SelectStream(nStream);

	}
	    
	public boolean isVideoAutoMode() {
	        
		if (m_osbasePlayer == null)
			return false;

		Object objParam = m_osbasePlayer.GetParam(voOSType.VOOSMP_SRC_PID_BA_WORKMODE);

		if (objParam == null) {
			voLog.e(TAG, "BA work mode return null.");
			return false;
		}

		int nParam = (Integer) objParam;

		return (nParam == voOSType.VOOSMP_SRC_ADAPTIVE_STREAMING_BA_MODE_AUTO);

	}
	
	private boolean queryAudioAsset() {
		if (m_lstAudioAsset == null || m_osbasePlayer == null)
			return false;

		voOSStreamInfo streamInfo = getCurrentStreamInfo();
		if (streamInfo == null)
			return false;

		voOSTrackInfo[] trackInfoArray = streamInfo.getTrackInfo();
		if (trackInfoArray == null)
			return false;

		m_lstAudioAsset.clear();

		int nIndexForName = 0;
		int nIndexForPos = 0;
	        
		for (int i = 0; i < trackInfoArray.length; i++) {
			if ( (trackInfoArray[i].getTrackType() != VOOSMP_SOURCE_STREAMTYPE.VOOSMP_SS_AUDIO)
					&& (trackInfoArray[i].getTrackType() != VOOSMP_SOURCE_STREAMTYPE.VOOSMP_SS_AUDIO_GROUP) )
	            continue;

	        if (trackInfoArray[i].getAudioInfo() == null)
	            continue;

	        String strLanguage = trackInfoArray[i].getAudioInfo().Language();
	        if (strLanguage == null || strLanguage.length() == 0)
	            strLanguage = "A" + Integer.toString(nIndexForName++);
	            
	        strLanguage = strLanguage.trim();
	            
	        boolean bPlaying = (trackInfoArray[i].getSelectInfo() 
	                & voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0 ?
	                true : false;
	            
	        m_lstAudioAsset.add(new AssetInfo(nIndexForPos++, 
	                trackInfoArray[i].getTrackID(), strLanguage, bPlaying));
	            
	        voLog.i(TAG, "Audio Track Info: ID is %d, SelInfo is %d : %s, " +
	        		"typename is  %s, language is %s, Codec is %d: %s.",
	        		trackInfoArray[i].getTrackID(), trackInfoArray[i].getSelectInfo(), 
	        		bPlaying == true ? "SELECTED" : "NOT SELECTED",
	        		trackInfoArray[i].getTrackType().name(), trackInfoArray[i].getAudioInfo().Language(), 
	        		trackInfoArray[i].getCodec(), voOSType.VOOSMP_AUDIO_CODINGTYPE.valueOf(trackInfoArray[i].getCodec()));
	    }
	      
	    if (m_lstAudioAsset.size() == 0)
	    	m_lstAudioAsset.add(new AssetInfo(INDEX_NONE, 0,
	    			m_ctx.getResources().getString(R.string.Player_Audio_None), true));
	            
	    return true;
	}
	    
	private void onAudioAssetClick(int nIndex) {
		if (m_lstAudioAsset == null || nIndex < 0 || nIndex >= m_lstAudioAsset.size())
			return;

		if (nIndex == 0 && m_lstAudioAsset.get(nIndex).m_nIndex == INDEX_NONE)
			return;

		selectAudio(m_lstAudioAsset.get(nIndex).m_nID);
	}

	private void selectAudio(int nAudioTrackID) {

		if (m_osbasePlayer == null)
			return;

		m_osbasePlayer.SelectTrack(nAudioTrackID);

		return;
	}

	private boolean querySubtitleAsset() {

		if (m_lstSubtitleAsset == null || m_osbasePlayer == null)
			return false;

		m_lstSubtitleAsset.clear();

		voOSStreamInfo streamInfo = getCurrentStreamInfo();
		if (streamInfo == null)
			return false;

		voOSTrackInfo[] trackInfoArray = streamInfo.getTrackInfo();
		if (trackInfoArray == null)
			return false;

		int nIndexForName = 0;
		int nIndexForPos = 0;
	        
		for (int i = 0; i < trackInfoArray.length; i++) {

			if ((trackInfoArray[i].getTrackType() != VOOSMP_SOURCE_STREAMTYPE.VOOSMP_SS_SUBTITLE)
					&& (trackInfoArray[i].getTrackType() != VOOSMP_SOURCE_STREAMTYPE.VOOSMP_SS_SUBTITLE_GROUP))
				continue;

			if (trackInfoArray[i].getSubtitleInfo() == null)
				continue;

			String strLanguage = trackInfoArray[i].getSubtitleInfo().Language();
			if (strLanguage == null || strLanguage.length() == 0)
				strLanguage = "Subt" + Integer.toString(nIndexForName++);

			boolean bPlaying = (trackInfoArray[i].getSelectInfo()
					& voOSType.VOOSMP_SRC_TRACK_SELECT_SELECTED) != 0 ? 
					true: false;

			m_lstSubtitleAsset.add(new AssetInfo(nIndexForPos++, trackInfoArray[i]
					.getTrackID(), strLanguage, bPlaying));

			voLog.i(TAG, "Subtitle Track Info: ID is %d, SelInfo is %d : %s, ",
					trackInfoArray[i].getTrackID(), trackInfoArray[i]
					.getSelectInfo(), bPlaying == true ? "SELECTED"
					: "NOT SELECTED");
		}

		if (m_lstSubtitleAsset.size() == 0)
			m_lstSubtitleAsset.add(new AssetInfo(INDEX_NONE, 0, m_ctx.getResources()
							.getString(R.string.Player_CloseCaptionSelector_None), true));

		return true;
	}
	    
	private void onSubtitleAssetClick(int nIndex) {
		if (m_lstSubtitleAsset == null || nIndex < 0 || nIndex >= m_lstSubtitleAsset.size())
			return;

		if (nIndex == 0 && m_lstSubtitleAsset.get(nIndex).m_nIndex == INDEX_NONE)
			return;

		selectSubtitle(m_lstSubtitleAsset.get(nIndex).m_nID);
	}
	    
	private void selectSubtitle(int nSubtitleTrackID) {
		if (m_osbasePlayer == null)
			return;

		m_osbasePlayer.SelectTrack(nSubtitleTrackID);

		return;
	}
	
}
