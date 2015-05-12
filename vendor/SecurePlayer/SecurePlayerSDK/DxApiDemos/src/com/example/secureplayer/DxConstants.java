package com.example.secureplayer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;

import com.example.secureplayer.DxContentItem.ECustomDataType;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;

public class DxConstants {
	public static final int CUSTOM_CONTENT_ID = -1;
	public static String TAG = "DxApiDemos";
	public static String CONTENT_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/DxApiDemos_Contents";
	
	
	// ! Personalization Url
	public static final String PERSONALIZATION_URL = "localhost:8000/Personalization";
	// ! App Version name to be transmitted via performPersonalization API.
	public static final String APPLICATION_VERSION = "DxApiDemos";
	// ! SessionID string to be transmitted via performPersonalization API.
	public static final String SESSION_ID = "session";
	
	private static SharedPreferences mSharedPref = null;
	
	public static void init(Context context){
		mSharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		SubtitleUse = mSharedPref.getBoolean((context.getString(R.string.key_checkbox_subtitles)), false);
		SubtitleCustom = mSharedPref.getString((context.getString(R.string.key_custom_subtitles)), null);
	}
	
	private static DxContentItem mUserInput = new DxContentItem.Builder("----Custom Content----")
													.setIsStreaming(true)
													.build();
	//Contents
	private static  DxContentItem[] mContentArry = new DxContentItem[]{
	
		new DxContentItem.Builder("PR Dx3.0 HLS - Avatar")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Avatar_HLS_25FPS_Enc/Avatar.m3u8")
			.setIsStreaming(true)
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Avatar_HLS_25FPS_Enc/LicenseAcquisition_HLS_Avatar.cms")
			.setCustomData("Unlimited",	ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),

        new DxContentItem.Builder("PR Harmonic HLS - Salt")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Salt_HLS_25FPS_Enc/Salt.m3u8")
			.setIsStreaming(true)
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Salt_HLS_25FPS_Enc/LicenseAcquisition_HLS_Salt.cms")
			.build(),
	    
		new DxContentItem.Builder("PR Dx2.1 HLS - Zombie Land")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Zombieland_HLS_25FPS_Enc/Zombieland.m3u8")
	        .setIsStreaming(true)
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/high/Zombieland_HLS_25FPS_Enc/LicenseAcquisition_HLS_Zombieland.cms")
			.setCustomData("ExpireIn2Days", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		
		new DxContentItem.Builder("PR Cisco format HLS - Cars 2")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/High/Cars2_HLS_25FPS_Enc/Cars2.m3u8")
			.setIsStreaming(true)
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/HLS/High/Cars2_HLS_25FPS_Enc/LicenseAcquisition_HLS_Cars2.cms")
			.setCustomData("Unlimited", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		
		new DxContentItem.Builder("PR SMST - Valhalla Rising")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ValhallaRising_SmStr_25FPS_Enc/ValhallaRising.ism/Manifest")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ValhallaRising_SmStr_25FPS_Enc/LicenseAcquisition_SmStr_ValhallaRising.cms")
			.setIsStreaming(true)
			.setCustomData("ExpireIn10Days", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		new DxContentItem.Builder("PR SMST - Toy Story 3")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ToyStory3_SmStr_25FPS_Enc/ToyStory3.ism/Manifest")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ToyStory3_SmStr_25FPS_Enc//LicenseAcquisition_SmStr_ToyStory3.cms")
			.setIsStreaming(true)
			.setCustomData("ExpireIn10Days", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		//---------------------------------------------------------				          
		new DxContentItem.Builder("Local PR Env - Airbender")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/Airbender_ENV_Enc/Airbender_320x136_350KbpsBP25Fps.eny")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/Airbender_ENV_Enc/LicenseAcquisition_ENV_Airbender_320x136_350KbpsBP25Fps.cms")
			.setIsStreaming(false)
			.build(),
		new DxContentItem.Builder("Local PR Env - District 9")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/District9_ENV_Enc/District9_584x312_1000KbpsBP25Fps.eny")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/District9_ENV_Enc/LicenseAcquisition_ENV_District9_584x312_1000KbpsBP25Fps.cms")
			.setIsStreaming(false)
			.setCustomData("ExpireIn2Days", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		new DxContentItem.Builder("Local PR Env - Ice Age")
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/IceAge_ENV_Enc/IceAge_568x304_800KbpsBP25Fps.eny")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/Envelope/IceAge_ENV_Enc/LicenseAcquisition_ENV_IceAge_568x304_800KbpsBP25Fps.cms")
			.setIsStreaming(false)
			.setCustomData("Unlimited", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		new DxContentItem.Builder("Local PR Env - Prince of Persia") 
			.setContentUrl("http://192.116.217.179/PlayReadyDemo/SPDemo/Envelope/_HighBitrate/PrinceofPersia_ENV_Enc/PrinceofPersia_1280x532_2500KbpsBP25Fps.eny")
			.setInitiatorUrl("http://192.116.217.179/PlayReadyDemo/SPDemo/Envelope/_HighBitrate//PrinceofPersia_ENV_Enc/LicenseAcquisition_ENV_PrinceofPersia_1280x532_2500KbpsBP25Fps.cms")
			.setIsStreaming(true)
			.setCustomData("Unlimited", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build(),
		new DxContentItem.Builder("Local PR ISMV - Valhalla Rising") 
			.setContentUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ValhallaRising_SmStr_25FPS_Enc/ValhallaRising_768x324_1000KbpsBP25Fps.ismv")
			.setInitiatorUrl("http://64.57.247.9/PlayReadyDemo/SPDemo/SmStreaming/ValhallaRising_SmStr_25FPS_Enc/LicenseAcquisition_SmStr_ValhallaRising.cms")
			.setIsStreaming(false)
			.setCustomData("ExpireIn10Days", ECustomDataType.CUSTOM_DATA_IS_TEXT)
			.build()

	};
	
	//This is the content that need to linked with the actions.
	private static DxContentItem mActiveContantItem;

	public static DxContentItem getActiveContent() {
		return mActiveContantItem;
	}
	
	public static DxContentItem getConetnt(int id){
		if (CUSTOM_CONTENT_ID == id){
			return mUserInput;
		}
		return mContentArry[id];
	}
	
	public static int getNumberOfContents(){
		return mContentArry.length;
	}
	
	public static void setActiveContent(DxContentItem fromContentItem){
		mActiveContantItem = fromContentItem;
	}
	
	
	public static boolean   bVideoSpecificSet = false;
	
	private static boolean  SubtitleUse = false;
	private static int      SubtitleIndexSelected = -1;
	private static String[] SubtitleArray = null;
	private static String   SubtitleCustom = null;
	
	private static int      AudioIndexSelected = -1;
	private static String[] AudioArray = null;

	public static void setVideoSpecifics(boolean val) {
		bVideoSpecificSet = val;
		
		if (!val) {
			SubtitleUse = false;
			SubtitleArray = null;
			SubtitleCustom = null;
			SubtitleIndexSelected = -1;
			
			AudioArray = null;
			AudioIndexSelected = -1;
		}	
	}
	
	public static boolean isVideoSpecificsSet() {
		return bVideoSpecificSet;
	}

	public static void setSubtitleArray(int count) {
		SubtitleArray = new String[count];
	}
	
	public static void setSubtitle(int index, String subtitle) {
		SubtitleArray[index] = subtitle;
	}
	
	public static void setSubtitleSelected(int index) {
		SubtitleIndexSelected = index;
	}
	
	public static int getSubtitleArrayLength() {
		if (SubtitleArray != null)
			return SubtitleArray.length;
		else 
			return 0;	
	}
	
	public static void setSubtitleUse(boolean val) {
		SubtitleUse = val;
	}
	
	public static boolean getSubtitleUse() {
		return SubtitleUse;
	}

	
	public static String getSubtitleArrayEntry(int index) {
		return SubtitleArray[index];	
	}
	
	public static int getSubtitleSelected() {
		return SubtitleIndexSelected;
	}
	
	public static void setSubtitleCustom(String subtitle) {
		SubtitleCustom = subtitle;
	}
	
	public static String getSubtitleCustom() {
		return SubtitleCustom;
	}
	
	public static void setAudioArray(int count) {
		AudioArray = new String[count];
	}
	
	public static void setAudio(int index, String audio) {
		AudioArray[index] = audio;
	}
	
	public static void setAudioSelected(int index) {
		AudioIndexSelected = index;
	}
	
	public static int getAudioArrayLength() {
		if (AudioArray != null)
			return AudioArray.length;
		else 
			return 0;	
	}
	
	public static String getAudioArrayEntry(int index) {
		return AudioArray[index];	
	}
	
	public static int getAudioSelected() {
		return AudioIndexSelected;
	}
	
	private static boolean HardwareAccelerated = false;
	
	public static boolean isHardwareAccelerated() {
		return HardwareAccelerated;
	}
	
	public static void setHardwareAccelerated(boolean val) {
		HardwareAccelerated = val;
	}
	
    private static boolean DisplayPlaybackInformation = false;
	
	public static boolean getDisplayPlaybackInformation() {
		return DisplayPlaybackInformation;
	}
	
	public static void setDisplayPlaybackInformation(boolean val) {
		DisplayPlaybackInformation = val;
	}
    
	private static String PlaybackInformationBPS =null;
	
	public static String getPlaybackInformationBPS() {
		return PlaybackInformationBPS;
	}
	
	public static void setPlaybackInformationBPS(String val) {
		PlaybackInformationBPS = val;
	}
	
	private static String PlaybackInformationResolution =null;
	
	public static String getPlaybackInformationResolution() {
		return PlaybackInformationResolution;
	}
	
	public static void setPlaybackInformationResolution(String val) {
		PlaybackInformationResolution = val;
	}
	
}
