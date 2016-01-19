package com.visualon.AppPlayerCommonFeatures;



import android.content.Context;
import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;


public class CAdManager {
	
	private static final String  TAG                    = "@@@CAdManager";
	
	public CAdManager(Context context) {
	}
	
	public void setSDKPlayer(VOCommonPlayer player) {
	}
	
    public VO_OSMP_RETURN_CODE start(String path) {
		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
	}
	
    public VO_OSMP_RETURN_CODE setADSkipAction() {
    	return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_IMPLEMENT;
    }
	
    public String getVideoAdClickThru() {
    	return null;
    }
	public  static void getGoogleAdvertisingId(final Context context) {
		
	}
	
	
}
