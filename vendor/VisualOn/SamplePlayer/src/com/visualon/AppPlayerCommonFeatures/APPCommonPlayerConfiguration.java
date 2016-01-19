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



import android.graphics.Rect;

import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOOSMPHTTPProxy;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_AUDIO_EFFECT_ENDPOINT_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_COLORTYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_HDCP_POLICY;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PREFERENCE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RENDER_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SEI_INFO_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ZOOM_MODE;

public class APPCommonPlayerConfiguration {
    private VOCommonPlayer m_player;
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    public VO_OSMP_RETURN_CODE enableDeblock(boolean value){
        return m_player.enableDeblock(value);
    }
    
    public VO_OSMP_RETURN_CODE enableSEI(VO_OSMP_SEI_INFO_FLAG flag){
        return m_player.enableSEI(flag);
    }
    
    public VO_OSMP_RETURN_CODE setLicenseFilePath(String path){
        return m_player.setLicenseFilePath(path);
    }
    
    public VO_OSMP_RETURN_CODE setLicenseContent(byte[] data){
        return m_player.setLicenseContent(data);
    }
    
    public VO_OSMP_RETURN_CODE setPreAgreedLicense(String str){
        return m_player.setPreAgreedLicense(str);
    }
    
    public VO_OSMP_RETURN_CODE setInitialBitrate(int bitrate){
        return m_player.setInitialBitrate(bitrate);
    }
    
    public VO_OSMP_RETURN_CODE setZoomMode(VO_OSMP_ZOOM_MODE mode, Rect rect){
        return m_player.setZoomMode(mode, rect);
    }
    
    public VO_OSMP_RETURN_CODE setDeviceCapabilityByFile(String filepath){
        return m_player.setDeviceCapabilityByFile(filepath);
    }
    
    public VO_OSMP_RETURN_CODE setInitialBufferingTime(int time){
        return m_player.setInitialBufferingTime(time);
    }
    
    public VO_OSMP_RETURN_CODE setPlaybackBufferingTime(int time) {
    	return m_player.setPlaybackBufferingTime(time);
    }
    
    public VO_OSMP_RETURN_CODE setMaxBufferingTime(int time){
        return m_player.setMaxBufferingTime(time);
    }
    
    public VO_OSMP_RETURN_CODE enableLowLatencyVideo(boolean value){
        return m_player.enableLowLatencyVideo(value);
    }
    
    public VO_OSMP_RETURN_CODE enableAudioEffect(boolean value){
        return m_player.enableAudioEffect(value);
    }
    
    public VO_OSMP_RETURN_CODE setAudioEffectEndpointType(
    		VO_OSMP_AUDIO_EFFECT_ENDPOINT_TYPE type) {
    	return m_player.setAudioEffectEndpointType(type);
    }
    
    public VO_OSMP_RETURN_CODE setAudioPlaybackSpeed(float speed){
        return m_player.setAudioPlaybackSpeed(speed);
    }
    
    public VO_OSMP_RETURN_CODE enableCPUAdaptation(boolean value){
        return m_player.enableCPUAdaptation(value);
    }
    
    public VO_OSMP_RETURN_CODE setBitrateThreshold(int upper, int lower){
        return m_player.setBitrateThreshold(upper, lower);
    }
    
    public VO_OSMP_RETURN_CODE setColorType(VO_OSMP_COLORTYPE type){
        return m_player.setColorType(type);
    }
    
    public VO_OSMP_RETURN_CODE setRenderType(VO_OSMP_RENDER_TYPE type){
        return m_player.setRenderType(type);
    }
    
    public VO_OSMP_RETURN_CODE setHDCPPolicy(VO_OSMP_HDCP_POLICY type){
        return m_player.setHDCPPolicy(type);
    }
    
    public VO_OSMP_RETURN_CODE setHTTPRetryTimeout(int time){
        return m_player.setHTTPRetryTimeout(time);
    }

    public VO_OSMP_RETURN_CODE setPDConnectionRetryCount(int times){
        return m_player.setPDConnectionRetryCount(times);
    }
    
    public VO_OSMP_RETURN_CODE setHTTPProxy(VOOSMPHTTPProxy proxy){
        return m_player.setHTTPProxy(proxy);
    }

    public VO_OSMP_RETURN_CODE setHTTPHeader(String headerName, String headerValue){
        return m_player.setHTTPHeader(headerName, headerValue);
    }
    
    public VO_OSMP_RETURN_CODE setPreference(VO_OSMP_PREFERENCE configuration) {
        return m_player.setPreference(configuration);
    }
    
    public VO_OSMP_RETURN_CODE setPresentationDelay(int time) {
        return m_player.setPresentationDelay(time);
    }
    
    public VO_OSMP_RETURN_CODE setHWDecoderMaxResolution(int maxWidth, int maxHeight) {
        return m_player.setHWDecoderMaxResolution(maxWidth, maxHeight);
    }
    
    public VO_OSMP_RETURN_CODE enableVOAdaptivePlayback(boolean value){
        return m_player.enableVOAdaptivePlayback(value);
    }
    
    public VO_OSMP_RETURN_CODE enableHTTPGzipRequest(boolean value) {
        return m_player.enableHTTPGzipRequest(value);
    }
    
    public VO_OSMP_RETURN_CODE enableSEIPostProcessVideo(boolean value) {
        return m_player.enableSEIPostProcessVideo(value);
    }
    
    public VO_OSMP_RETURN_CODE enableDRMOfflineMode(boolean enable) {
        return m_player.enableDRMOfflineMode(enable);
    }

    
    public VO_OSMP_RETURN_CODE setPlayIFrameOnly(boolean enable, float speed)
    {
        return m_player.setPlayIFrameOnly(enable, speed);
    }
    
    public VO_OSMP_RETURN_CODE setURLQueryString(String string) {
        return m_player.setURLQueryString(string);
    }
}
