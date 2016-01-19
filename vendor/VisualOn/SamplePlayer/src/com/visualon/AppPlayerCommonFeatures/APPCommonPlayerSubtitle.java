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



import android.view.View;

import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_HORIZONTAL;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_VERTICAL;

public class APPCommonPlayerSubtitle  {
    private VOCommonPlayer m_player;
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    public VO_OSMP_RETURN_CODE setSubtitlePath(String filePath){
        return m_player.setSubtitlePath(filePath);
    }
    
    public VO_OSMP_RETURN_CODE enableSubtitle(boolean value){
        return m_player.enableSubtitle(value);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontColor(int color){
        return m_player.setSubtitleFontColor(color);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontOpacity(int alpha){
        return m_player.setSubtitleFontOpacity(alpha);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontSizeScale(int scale){
        return m_player.setSubtitleFontSizeScale(scale);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontBackgroundColor(int color){
        return m_player.setSubtitleFontBackgroundColor(color);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontBackgroundOpacity(int alpha){
        return m_player.setSubtitleFontBackgroundOpacity(alpha);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleWindowBackgroundColor(int color){
        return m_player.setSubtitleWindowBackgroundColor(color);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleWindowBackgroundOpacity(int alpha){
        return m_player.setSubtitleWindowBackgroundOpacity(alpha);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontItalic(boolean value){
        return m_player.setSubtitleFontItalic(value);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontBold(boolean value){
        return m_player.setSubtitleFontBold(value);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontUnderline(boolean value){
        return m_player.setSubtitleFontUnderline(value);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontName(String name){
        return m_player.setSubtitleFontName(name);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontEdgeType(int type){
        return m_player.setSubtitleFontEdgeType(type);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontEdgeColor(int color){
        return m_player.setSubtitleFontEdgeColor(color);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleFontEdgeOpacity(int alpha){
        return m_player.setSubtitleFontEdgeOpacity(alpha);
    }
    
    public VO_OSMP_RETURN_CODE resetSubtitleParameter(){
        return m_player.resetSubtitleParameter();
    }
    
    public VO_OSMP_RETURN_CODE previewSubtitle(View view){
        return m_player.previewSubtitle("Sample", view);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleBoundingBox(int topPercent, int leftPercent, int bottomPercent, int rightPercent) {
    	return m_player.setSubtitleBoundingBox(topPercent, leftPercent, bottomPercent, rightPercent);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleGravity(VO_OSMP_HORIZONTAL horizontal, VO_OSMP_VERTICAL vertical) {
    	return m_player.setSubtitleGravity(horizontal, vertical);
    }
    
    public VO_OSMP_RETURN_CODE setSubtitleTrim(String trimStr){
        return m_player.setSubtitleTrim(trimStr);
    }
    
    public VO_OSMP_RETURN_CODE enableSubtitleAutoAdjustment(boolean value) {
        return m_player.enableSubtitleAutoAdjustment(value);
    }
    
}
