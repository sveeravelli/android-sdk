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
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;

public class APPCommonPlayerDRM {
    private VOCommonPlayer m_player;
    public void setPlayer(VOCommonPlayer player){
        m_player = player;
    }
    public VO_OSMP_RETURN_CODE setDRMVerificationInfo(VOOSMPVerificationInfo info){
       return m_player.setDRMVerificationInfo(info);
        
    }
    
    public VO_OSMP_RETURN_CODE setDRMAdapter(Object adapter, boolean isLibraryName){
        return m_player.setDRMAdapter(adapter, isLibraryName);
    }
    
    public VO_OSMP_RETURN_CODE setDRMLibrary(String libName, String libApiName){
        return m_player.setDRMLibrary(libName, libApiName );
    }
    
    public String getDRMUniqueIdentifier(){
        return m_player.getDRMUniqueIdentifier();
    }
    
    public VO_OSMP_RETURN_CODE setDRMFilePath(String filePath){
        return m_player.setDRMFilePath(filePath );
    }
}
