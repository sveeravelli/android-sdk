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

package com.visualon.AppBehavior;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.xml.sax.SAXException;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.CheckBoxPreference;
import android.preference.DialogPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.visualon.AppBehavior.OptionItem.ValueListItem;
import com.visualon.AppPlayerCommonFeatures.CDownloader;
import com.visualon.AppPlayerCommonFeatures.CPlayer;
import com.visualon.AppPlayerCommonFeatures.CommonFunc;
import com.visualon.AppPlayerCommonFeatures.Definition;
import com.visualon.AppPlayerCommonFeatures.voLog;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_CB_EVENT_ID;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_SRC_CUSTOMERTAGID;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsInfo;
import com.visualon.OSMPPlayer.VOOSMPChunkInfo;
import com.visualon.OSMPPlayer.VOOSMPPlaylistData;
import com.visualon.OSMPPlayer.VOOSMPSEIPicTiming;
import com.visualon.OSMPPlayer.VOOSMPSEIUserDataUnregistered;
import com.visualon.OSMPPlayer.VOOSMPSessionData;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SEI_INFO_FLAG;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;
import com.visualon.AppUI.FeatureManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class AppBehaviorManagerImpl extends PreferenceActivity implements AppBehaviorManager,OnPreferenceChangeListener
{
    private ArrayList<EventItem> m_appEventItems =null;
    private ArrayList<EventItem> m_eventItems = null;
    private ArrayList<EventItem> m_downloadEventItems = null;
    private ArrayList<ReturnCodeItem> m_returnCodeItems = null;
    private ArrayList<OptionItem> m_optionItems = null;
    private AppBehaviorManagerDelegate m_delegate = null;
    private String param1String = "";
    private String param2String = "";
    private static final String TAG = "@@@OSMP+AppBehaviorManager";
    
    private static final int ACTIONTYPE_LOG = 1;
    private static final int ACTIONTYPE_POPMESSAGE = 2;
    private static final int ACTIONTYPE_BOTH = 3;
    private static final int ACTIONTYPE_PARAM1 = 4;
    private static final int ACTIONTYPE_PARAM2 = 5;
    
    private static final int UITYPE_CHECKBOX = 1;
    private static final int UITYPE_RADIOBOX = 2;
    private static final int UITYPE_EDITBOX = 3;
    private static final int UITYPE_BUTTON  =4 ;
   
    private static final int ANDROID_PLATFORM = 0x01;
  
    private static SharedPreferences m_spMain; 
    private OptionItem m_desOpItem = null;
    private CPlayer    m_cPlayer    = null;
    private CDownloader m_cDownloader = null;
   
    private PreferenceCategory m_inlinePrefCat = null;
    private static Context mContext = null;
    
    private ArrayList<String>        m_downloadList     = null;
    private ListView                 m_listDownloadInfo   = null;
    private int                      m_nSingleChoiceLayout;
    private int                      m_nTextView;
	private int                      m_nPreviewLayout;
	private TextView                 m_textView;
	
	private CheckBoxPreference       chbEnableSubtitleSetting;
	private CheckBoxPreference       chbEnableSubtitlePresrntation;
	private CheckBoxPreference       chbUseDefaultSubtitleSetting;
	private CheckBoxPreference       chbResetSubtitleSetting;
	private CheckBoxPreference       chbEnableFontSize;
	private EditTextPreference       edtSetFontSize;
	private CheckBoxPreference       chbEnableFontColorOpacity;
	private EditTextPreference       edtSetFontColorOpacity;
	private CheckBoxPreference       chbEnableFontColor;
	private ListPreference           listSetFontColor;
	private CheckBoxPreference       chbEnableBackgroundColorOpacity;
	private EditTextPreference       edtSetBackgroundColorOpacity;
	private CheckBoxPreference       chbEnableBackgroundColor;
	private ListPreference           listSetBackgroundColor;
	private CheckBoxPreference       chbEnableEdgeColorOpacity;
	private EditTextPreference       edtSetEdgeColorOpacity;
	private CheckBoxPreference       chbEnableEdgeColor;
	private ListPreference           listSetEdgeColor;
	private CheckBoxPreference       chbEnableEdgeType;
	private ListPreference           listSetEdgeType;
	private CheckBoxPreference       chbEnableFontname;
    private ListPreference           listSetFontname;
	private CheckBoxPreference       chbEnableWindowBackgroundColorOpacity;
	private EditTextPreference       edtSetWindowBackgroundColorOpacity;
	private CheckBoxPreference       chbEnableWindowBackgroundColor;
	private ListPreference           listSetWindowBackgroundColor;
	private CheckBoxPreference       chbEnableUderlineFont;
	private CheckBoxPreference       chbSetUderlineFont;
	private CheckBoxPreference       chbEnableBoldFont;
	private CheckBoxPreference       chbSetBoldFont;
    private CheckBoxPreference       chbEnableItalicFont;
    private CheckBoxPreference       chbSetItalicFont;
    
	
    public enum OPTION_ID
    {
        OPTION_ENGINETYPE_ID                              (0),
        OPTION_VIDEORENDERTYPE_ID                         (1),
        OPTION_AUDIODECODERTYPE_ID                        (2),
        OPTION_VIDEODECODERTYPE_ID                        (3),
        OPTION_DOLBY_ID                                   (4),
        OPTION_DEBLOCK_ID                                 (5),
        OPTION_PERFORMANCE_ID                             (6),
        OPTION_SUBTITLEURL_ID                             (7),
        OPTION_COLORTYPE_ID                               (8),
        OPTION_ASYNCHRONOUSLY_ID                          (9),
        OPTION_MAXBUFFERINGTIME_ID                        (10),
        OPTION_PANSCAN_ID                                 (11),
        OPTION_DOWNLOAD_ID                                (12),
        OPTION_DOWNLOADLIST_ID                            (1201),
        OPTION_CPUADAPTION_ID                             (13),
        OPTION_RTSP_ID                                    (14),
        OPTION_LOOP_ID                                    (15),
        OPTION_SUBTITLESETTINGS_ID                        (16),
        OPTION_SUBTITLE_ID                                (1601),
        OPTION_ST_FONTSIZE_ID                             (1602),
        OPTION_ST_SETFONTSIZE_ID                          (160201),
        OPTION_ST_FONTCOLOROPACITY_ID                     (1603),
        OPTION_ST_SETFONTCOLOROPACITY_ID                  (160301),
        OPTION_ST_COLORLIST_ID                            (1604),
        OPTION_ST_SETCOLORLIST_ID                         (160401),
        OPTION_ST_BGCOLOROPACITY_ID                       (1605),
        OPTION_ST_SETBGCOLOROPACITY_ID                    (160501),
        OPTION_ST_BGCOLORLIST_ID                          (1606),
        OPTION_ST_SETBGCOLORLIST_ID                       (160601),
        OPTION_ST_EDGECOLOROPACITY_ID                     (1607),
        OPTION_ST_SETEDGECOLOROPACITY_ID                  (160701),
        OPTION_ST_EDGECOLORLIST_ID                        (1608),
        OPTION_ST_SETEDGECOLORLIST_ID                     (160801),
        OPTION_ST_EDGETYPELIST_ID                         (1609),
        OPTION_ST_SETEDGETYPELIST_ID                      (160901),
        OPTION_ST_FONTLIST_ID                             (1610),
        OPTION_ST_SETFONTLIST_ID                          (161001),
        OPTION_ST_WINBGCOLOROPACITY_ID                    (1611),
        OPTION_ST_SETWINBGCOLOROPACITY_ID                 (161101),
        OPTION_ST_WINBGCOLORLIST_ID                       (1612),
        OPTION_ST_SETWINBGCOLORLIST_ID                    (161201),
        OPTION_ST_UNDERLINEFONT_ID                        (1613),
        OPTION_ST_SETUNDERLINEFONT_ID                     (161301),
        OPTION_ST_BOLDFONT_ID                             (1614),
        OPTION_ST_SETBOLDFONT_ID                          (161401),
        OPTION_ST_ITALICFONT_ID                           (1615),
        OPTION_ST_SETITALICFONT_ID                        (161501),
        OPTION_ST_USEDEFAULTFONT_ID                       (1616),
        OPTION_ST_RESETTODEFAULTSET_ID                    (1617),
        OPTION_ST_PREVIEWSUBTITLE_ID                      (1618),
        OPTION_ST_FONTPOSITION_ID                         (1619),
        OPTION_ST_SETFONTTOPPOSITION_ID                   (161901),
        OPTION_ST_SETFONTLEFTPOSITION_ID                  (161902),
        OPTION_ST_SETFONTBOTTOMPOSITION_ID                (161903),
        OPTION_ST_SETFONTRIGHTPOSITION_ID                 (161904),
        OPTION_ST_FONTGRAVITY_ID                          (1620),
        OPTION_ST_SETFONTHORIZONTALPOSITION_ID            (162001),
        OPTION_ST_SETFONTVERTICALPOSITION_ID              (162002),
        OPTION_ST_ENABLEFONTTRIM_ID                       (1621),
        OPTION_ST_SETFONTTRIM_ID                          (162101),
        OPTION_ST_ENABLEAUTOADJUSTMENT                       (1622),

        OPTION_INITIALBITRATE_ID                          (18),
        OPTION_SETINITIALBITRATE_ID                       (1801),
        OPTION_BITRATERANGE_ID                            (19),
        OPTION_LOWERBITRATERANGE_ID                       (1901),
        OPTION_UPPERBITRATERANGE_ID                       (1902),
        OPTION_HTTPRETRYTIMEOUT_ID                        (20),
        OPTION_SETHTTPRETRYTIMEOUT_ID                     (2001),
        OPTION_ENABLEDEFAULTAUDIOLANGUAGE_ID              (21),
        OPTION_SETDEFAULTAUDIOLANGUAGE_ID                 (2101),
        OPTION_ENABLEDEFAULTSUBTITLELANGUAGE_ID           (22),
        OPTION_SETDEFAULTSUBTITLELANGUAGE_ID              (2201),
        OPTION_ENABLERTSPHTTPPORT_ID                      (23),
        OPTION_SETRTSPHTTPPORT_ID                         (2301),
        OPTION_ENABLERENDEROPTIMIZATIONFORBA_ID           (24),
        OPTION_ENABLERTSPSOCKETERROR_ID                   (25),
        OPTION_SETRTSPSOCKETERROR_ID                      (2501),
        OPTION_ENABLERTSPCONNECTIONTIMEOUT_ID             (26),
        OPTION_SETRTSPCONNECTIONTIMEOUT_ID                (2601),
        OPTION_ENABLEANALYTICSDISPLAYTIME_ID              (27),
        OPTION_SETANALYTICSDISPLAYTIME_ID                 (2701),
        OPTION_ENABLERTSPHTTPVERIFICATIONINFO_ID          (28),
        OPTION_SETRTSPHTTPVERIFICATIONUSERNAME_ID         (2801),
        OPTION_SETRTSPHTTPVERIFICATIONPASSWORD_ID         (2802),
        OPTION_ENABLELOWLATENCYVIDEO_ID                   (29),
        OPTION_ENABLEHTTPGZIPREQUEST_ID                   (30),
        OPTION_ENABLESEIPOSIPROCESSVIDEO_ID               (31),
        OPTION_ENABLEPUSHPD_ID                            (32),
        OPTION_SETPUSHPDOPENDURATION_ID                   (3201),
        OPTION_ANALYTICSFOUNDATION_ID                     (33),
        OPTION_SETANALYTICSFOUNDATIONCUID_ID              (3301),
        OPTION_ENABLEANALYTICSFOUNDATIONLOCATION_ID       (3302),
        OPTION_ENABLEHTTPPROXY_ID                         (34),
        OPTION_SETHTTPPROXYHOST_ID                        (3401),
        OPTION_SETHTTPPROXYPORT_ID                        (3402),
        OPTION_INITIALBUFFERINGTIME_ID                    (35),
        OPTION_HDCPPOLICY_ID                              (36),
        OPTION_SDKPREFERENCE_ID           	              (38),
        OPTION_SDKPREFERENCESTOPKEEPLASTFRAME_ID          (3801),
        OPTION_SDKPREFERENCESEEKPRECISE_ID                (3802),
        OPTION_SDKPREFERENCEAUDIOSWITCHIMMEDIATELY_ID     (3803),
        OPTION_SDKPREFERENCEBASTARTFASTID                 (3804),
        OPTION_ENABLEPRESENTATIONDELAYTIME_ID             (39),
        OPTION_PRESENTATIONDELAYTIME_ID                   (3901),
        OPTION_ENABLEAD_ID                                (40),
        OPTION_ANALYTICS_EXPORT_ID                        (41),
        OPTION_PLAYBACKBUFFERINGTIME_ID                   (42),
        OPTION_2ND_PLAYER_URL_ENADLED_ID                  (43),
        OPTION_2ND_PLAYER_URL_INPUT_ID                    (4301),

        // For NTS Video URL input field ---------------->
        OPTION_NTS_ENABLED_ID                              (44),
        OPTION_NTS_URL_ID                                 (4401),
        OPTION_NTS_MIN_POSITION_ID                        (4402),
        OPTION_PREVIOUS_SEEK_ENABLED_ID                    (45),
        OPTION_PREVIOUS_VALUE_TO_SEEK_ID                  (4501),
        // ----------------------------------------------<

        // For Extra HTTP Header setup ------------------------>
        OPTION_ENABLEHTTPHEADER_ID                         (46),
        OPTION_SETHTTPHEADER_NAME_ID                       (4601),
        OPTION_SETHTTPHEADER_VALUE_ID                      (4602),
        // ----------------------------------------------<

        // for Enable/Disable DVR Position
        OPTION_ENABLE_DVR_POSITION_ID                     (47),
        
        OPTION_ENABLE_HW_DECODER_MAX_RESOLUTION                    (48),
        OPTION_HW_DECODER_MAX_WIDTH                    (4801),
        OPTION_HW_DECODER_MAX_HEIGHT                    (4802),
        OPTION_ENABLE_URL_QUERY_STRING_ID           (49),
        OPTION_QUERY_STRING_ID                                      (4901),
        OPTION_ANALYTICSAGENT_ID                          (50),
        OPTION_SETANALYTICSAGENTCUID_ID                   (5001),

        OPTION_VERSION_ID                                 (99),
        OPTION_MAX_ID                                     (0xFFFFFFFF);
        
        private int value;
        
        OPTION_ID(int value)
        {
            this.value = value;
        }
           
        public int getValue()
        {
            return value;
        }
        
        public static OPTION_ID valueOf(int value)
        {
            for (int i = 0; i < OPTION_ID.values().length; i ++)
            {
                if (OPTION_ID.values()[i].getValue() == value)
                    return OPTION_ID.values()[i];
            }
            
            return OPTION_MAX_ID;
        }
    }

    public AppBehaviorManagerImpl() {
        super();
    }
    
    public AppBehaviorManagerImpl(Context context) {
        super();
        mContext = context;
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        voLog.d(TAG, "+++ onCreate +++ ");
        m_cPlayer = CommonFunc.getCPlayer();
        m_cPlayer.createPlayer();
        m_cDownloader = CommonFunc.getCDownloader();
        if(m_cDownloader.isImplement())
            m_cDownloader.createDownloader();
        else
            m_cDownloader = null;
        m_nSingleChoiceLayout=getIntent().getIntExtra("singleChoiceLayout", 0);
        m_nTextView=getIntent().getIntExtra("textView", 0);
        m_nPreviewLayout=getIntent().getIntExtra("previewLayout", 0);
        String filePath = getUserPath(this) + "/" + "appcfg.xml";
        loadCfgFile(filePath);
        showOptionPage(true);
        initPreference();
        addPreview();
        voLog.d(TAG, "--- onCreate --- ");
       }

    @Override
    public VO_OSMP_RETURN_CODE setDelegate(AppBehaviorManagerDelegate delegate) {
        this.m_delegate = delegate;
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    @Override
    public ArrayList<OptionItem> loadCfgFile(String filePath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        
        XMLAnalysis analysis = new XMLAnalysis();
        try {
            android.util.Xml.parse(fis, Xml.Encoding.UTF_8, analysis);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        m_appEventItems = analysis.getAppEventItems();
        m_eventItems = analysis.getEventItems();
        m_downloadEventItems = analysis.getDownloadEventItems();
        m_returnCodeItems = analysis.getReturnCodeItems();
        m_optionItems = analysis.getOptionItems();
       
        return m_optionItems;
    }

    @Override
    public int processEvent(int eventId, int param1, int param2, Object param3) {
        String temp = Integer.toHexString(eventId);
        if (temp.length() > 8)
            temp = temp.substring(temp.length()-8,temp.length());
        Long leventId = Long.parseLong(temp, 16);
        ArrayList<EventItem> eventItems = null;
        if (CDownloader.isDownloaderEventId(eventId))
            eventItems = m_downloadEventItems;
        else if(leventId==Long.parseLong("8A000001", 16))
            eventItems = m_appEventItems;
        else if(m_eventItems != null)
            eventItems = m_eventItems;
        else 
            return 0;
        
        
        int nRet = eventAction(eventItems,leventId,param1,param2,param3);
        if(nRet == 0)
            return 0;
        else
            return 1;
        
    }

    private int eventAction(ArrayList<EventItem> eventItems, Long leventId, int param1, int param2, Object param3) {
        int listSize = eventItems.size();
        int j = 0;
        EventItem eventItem = null;
        for (int i=0; i<listSize; i++) {
            eventItem = eventItems.get(i);
            if (leventId == eventItem.getId()) {
                if (!eventItem.getEnable())
                    return 0;

                int param1Size = eventItem.getParam1Count();
                if (param1Size > 0) {
                    for (j=0; j<param1Size; j++) {
                        if (param1 == eventItem.getParam1(j).getParamValue()) {
                            param1String = eventItem.getParam1(j).getDescription();
                            break;
                        }
                    }
                }
                int param2Size = eventItem.getParam2Count();
                if (param2Size > 0) {
                    for (j=0; j<param2Size; j++) {
                        if (param2 == eventItem.getParam2(j).getParamValue()) {
                            param2String = eventItem.getParam2(j).getDescription();
                            break;
                        }
                    }
                }
                int nRet = pressEventLog(eventItem,param1,param2,param3);
                if (nRet == 0)
                    return 0;
                else
                    return 1;
            }
            else {
                if (eventItem.getChildCount()>0) {
                    ArrayList<EventItem> childList = new ArrayList<EventItem>();
                    for (int temp=0; temp<eventItem.getChildCount(); temp++) {
                        childList.add((EventItem) eventItem.getChild(temp));
                    }
                    eventAction(childList,leventId,param1,param2,param3);
                }
            }
            
        }
        
        return 0;
    }

    private int pressEventLog(EventItem eventItem, int param1, int param2, Object param3) {
        String log = eventItem.getDescription();
        long leventId = eventItem.getId();
        int type = eventItem.getType();
        int eventCorrectReturnValue = type/1000;
        type = type%1000;
        APP_BEHAVIOR_EVENT_ID behavior = eventItem.getAppBehavior();
        log = log.replace("param1 is %d", "param1 is " + param1);
        log = log.replace("param2 is %d", "param2 is " + param2);
        log = log.replace("param1 is %x", "param1 is " + Integer.toHexString(param1));
        log = log.replace("param2 is %x", "param2 is " + Integer.toHexString(param2));
        log = log.replace("param1 is %s", "param1 is " + param1String);
        log = log.replace("param2 is %s", "param2 is " + param2String);
        log = log.replace("SEI %s","SEI " + param1String);
        log = log.replace("Output blocked, type %d", "Output blocked, type " + String.valueOf(param1));
        log = log.replace("Resolution downgraded,current mode:%s","Resolution downgraded,current mode:"+ param1String);
        
       
        if (param3 != null) {
            if (leventId == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_ANALYTICS_INFO.getValue()) {
                VOOSMPAnalyticsInfo info = (VOOSMPAnalyticsInfo) param3;
                log = String.format(log, info.getAverageDecodeTime(),info.getAverageRenderTime(),info.getCodecDropNum(),info.getCodecErrorsNum(),info.getCodecTimeNum(),
                        info.getCPULoad(),info.getDecodedNum(),info.getFrequency(),info.getJitterNum(),info.getLastTime(),info.getMaxFrequency(),info.getRenderDropNum(),
                        info.getRenderNum(),info.getRenderTimeNum(),info.getSourceDropNum(),info.getSourceTimeNum(),info.getTotalCPULoad(),info.getWorstDecodeTime(),
                        info.getWorstRenderTime());
            }
            if (leventId == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_WARNING.getValue()) {
                if (param1 == VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_WARNING_EVENT_CHUNK_DRMERROR.getValue()){
                    VOOSMPChunkInfo info = (VOOSMPChunkInfo) param3;
                    log = String.format(log,info.getErrorCode());
                }
            }
            if(leventId == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO.getValue()) {
            	if (param1 == VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_PLAYLIST_DOWNLOADOK.getValue()){
                    
            		VOOSMPPlaylistData info = (VOOSMPPlaylistData) param3;
                    String playlistData = new String(info.getData());
    				
                    try{              	
                        // Decode the input data which is encoded by based64
                    	byte[] data = Base64.decode(playlistData, Base64.DEFAULT);
                    	playlistData = new String(data);
                	}
                	catch(Exception ex){	
                		// if input data is not based64 encode	
                	}
                 
                    if (playlistData.length() > 50) {
                        playlistData = playlistData.substring(0, 50) + "... (truncated)";
                    }
                    log = String.format(log,info.getRootUrl(), info.getNewUrl(), info.getUrl(), playlistData, info.getDataSize(), info.getPlaylistType(), info.getErrorCode());
                }
                if (param1 == VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_INFO_EVENT_SESSION_DATA.getValue()){
                    VOOSMPSessionData info = (VOOSMPSessionData) param3;
                    String tmpLog = log.substring(log.indexOf("Index="));
                    log = log.substring(0, log.indexOf("Index=")) + "{";
                    for (int i = 0; i < info.getCount(); i++)
                    {
                        log += " ["+String.format(tmpLog, i, info.getDataID(i), info.getValue(i), info.getURI(i), info.getLanguage(i))+"] ";
                    }
                    log += "}";
                }
            }
            if (leventId == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_SEI_INFO.getValue()) {
                if (param1 == VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_PIC_TIMING.getValue()) {
                    VOOSMPSEIPicTiming timing = (VOOSMPSEIPicTiming)param3;
                    
                    log = String.format(log, timing.getCpbDpbDelaysPresentFlag(),timing.getCpbRemovalDelay(),timing.getDpbOutputDelay(),timing.getNumClockTs(),
                            timing.getPictureStructure(),timing.getPictureStructurePresentFlag());
                }
                if (param1 == VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_USER_DATA_UNREGISTERED.getValue()) {
                    VOOSMPSEIUserDataUnregistered data = (VOOSMPSEIUserDataUnregistered)param3;
                    log = String.format(log, data.getFieldCount());
                }
            }
            if (leventId == VO_OSMP_CB_EVENT_ID .VO_OSMP_SRC_CB_CUSTOMER_TAG.getValue()) {
                if (param1 == VO_OSMP_SRC_CUSTOMERTAGID.VO_OSMP_SRC_CUSTOMERTAGID_TIMEDTAG.getValue()) {
                    int time=param2;
                    byte[] b=(byte[]) param3;
                    String s=new String(b);
                }
            }
            if (CDownloader.isDownloaderOpenComplete((int)leventId)) {
            	
				String content = new String((byte[])param3);
                
				try{              	
                    // Decode the input data which is encoded by based64
                	byte[] data = Base64.decode((byte[])param3, Base64.DEFAULT);
            		content = new String(data);
            	}
            	catch(Exception ex){	
            		// if input data is not based64 encode	
            	}
                
                content = content.length() > 50 ? content.substring(0, 50) +  "... (truncated)" : content;
        		log = String.format(log, param2, content);
            }
        }
        
        
        if (type == ACTIONTYPE_LOG) {
            printLog(log);
            return 0;
        } else if (type == ACTIONTYPE_POPMESSAGE) {
            popupMsg(log,behavior);
            return 1;
        } else if (type == ACTIONTYPE_BOTH) {
            printLog(log);
            popupMsg(log,behavior);
            return 1;
        } else if (type == ACTIONTYPE_PARAM1) {
            if (param1 == eventCorrectReturnValue) {
                printLog(log);
                return 0;
            }
            else {
                printLog(log);
                popupMsg(log,behavior);
                return 1;
            }
        } else if ( type == ACTIONTYPE_PARAM2) {
            if (param2 == eventCorrectReturnValue) {
                printLog(log);
                return 0;
            }
            else {
                printLog(log);
                popupMsg(log,behavior);
                return 1;
            }
        }
        return 0;
    }

    @Override
    public int processReturnCode(String apiName, int returnCode) {
        String temp = Integer.toHexString(returnCode);
        if(temp.length() > 8)
            temp = temp.substring(temp.length()-8,temp.length());
        Long lreturnCode = Long.parseLong(temp, 16);
        
        if(m_returnCodeItems != null) {
            int nRet = returnCodeAction(m_returnCodeItems, apiName, lreturnCode);
            if(nRet == 0)
                return 0;
            else
                return 1;
        }
        return 0;
    }

    private int returnCodeAction(ArrayList<ReturnCodeItem> mReturnCodeItems2,
            String apiName, Long lreturnCode) {
        
        String log ;
        int i = 0;
        int listSize = m_returnCodeItems.size();
        for(i=0; i<listSize; i++) {
            ReturnCodeItem rcItem = m_returnCodeItems.get(i);
            if(rcItem.getChildCount()>0) {
                ArrayList<ReturnCodeItem> childList = new ArrayList<ReturnCodeItem>();
                for(int temp=0;temp<rcItem.getChildCount();temp++) {
                    childList.add((ReturnCodeItem) rcItem.getChild(temp));
                }
                returnCodeAction(childList,apiName,lreturnCode);
            }
            else {
                if(lreturnCode == rcItem.getId()) {
                    if(!rcItem.getEnable())
                        return 0;
                    log = rcItem.getDescription();
                    log = log.replace("%s", apiName);
                    int type = rcItem.getType();
                    APP_BEHAVIOR_EVENT_ID behavior = rcItem.getAppBehavior();
                    
                    if(type == ACTIONTYPE_LOG) {
                        printLog(log);
                        return 0;
                    } else if(type == ACTIONTYPE_POPMESSAGE) {
                        popupMsg(log,behavior);
                        if (behavior == APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_STOP_PLAY)
                            return 1;
                        else 
                        	return 0;
                    } else if(type == ACTIONTYPE_BOTH) {
                        printLog(log);
                        popupMsg(log,behavior);
                        if (behavior == APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_STOP_PLAY)
                            return 1;
                        else 
                        	return 0;
                    } 
                    break;
                }
            }
        }
        return 0;
    }

    @Override
    public VO_OSMP_RETURN_CODE showOptionPage(boolean show) {
        if(show)
        {
            PreferenceManager preManager = getPreferenceManager();
            PreferenceScreen root = preManager.createPreferenceScreen(this);
            setPreferenceScreen(root);
            m_inlinePrefCat = new PreferenceCategory(this);
            m_inlinePrefCat.setTitle("Options");
            root.addPreference(m_inlinePrefCat);
            showOptionItem(m_optionItems);
           
            /*  
            llWindow1 = new RelativeLayout(this);
            llWindow1.setBackgroundColor(0xff182020);
            int n3 = 180;
           llWindow1.setLayoutParams(new ListView.LayoutParams(ListView.LayoutParams.FILL_PARENT,n3));
          
           TextView title=new TextView(this);
           title.setText("PreviewSubtitle");
           RelativeLayout.LayoutParams rltitle = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,n3);
           rltitle.addRule(RelativeLayout.ALIGN_PARENT_LEFT|RelativeLayout.CENTER_VERTICAL);
           //rltitle.leftMargin = 30;
           llWindow1.addView(title,rltitle);
           title.setTextColor(Color.WHITE);
		   int tvID = 2001;
		   title.setId(tvID);
		   title.setTextScaleX(1.2f);
		   int n1 = title.getWidth();
       
          
           RelativeLayout rlView=new RelativeLayout(this);
           RelativeLayout.LayoutParams rlViewParams = new RelativeLayout.LayoutParams(500,n3);
           
           rlViewParams.addRule(RelativeLayout.RIGHT_OF, tvID);
         
		   
           llWindow1.addView(rlView,rlViewParams);
           n1 = rlView.getWidth();
           
         	textViewOfRows = new TextView(this);
         	textViewOfRows.setTag(new Integer(n3));
         	textViewOfRows.setBackgroundColor(Color.BLUE);
			RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(0,n3);
			
			rlView.addView(textViewOfRows,rlp);
			
			
			
			ListView vw2 =getListView();
			vw2.addFooterView(llWindow1);
			
			
			*/
            
            
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    private void showOptionItem(ArrayList<OptionItem> optionItems) {
        OptionItem opItem = null;
        for(int i=0;i<optionItems.size();i++)
        {
            opItem = optionItems.get(i);
            
            if(Build.VERSION.SDK_INT < 16){
                if(opItem.getId() == OPTION_ID.OPTION_AUDIODECODERTYPE_ID.getValue()){
                    opItem.getValueListItem(2).setPlatform(0);
                }
                if(opItem.getId() == OPTION_ID.OPTION_VIDEODECODERTYPE_ID.getValue()){
                    opItem.getValueListItem(2).setPlatform(0);
                }
            }
            if(Build.VERSION.SDK_INT < 14){
                if(opItem.getId() == OPTION_ID.OPTION_AUDIODECODERTYPE_ID.getValue()){
                    opItem.getValueListItem(1).setPlatform(0);
                }
                if(opItem.getId() == OPTION_ID.OPTION_VIDEODECODERTYPE_ID.getValue()){
                    opItem.getValueListItem(1).setPlatform(0);
                    opItem.getValueListItem(3).setPlatform(0);
                }
            }    
            if(!(((opItem.getPlatform()&ANDROID_PLATFORM) != 0) && opItem.getEnable()))
            {
                continue;
            }
            if(opItem.getUIType() == UITYPE_CHECKBOX)
            {
                CheckBoxPreference togglePref = new CheckBoxPreference(this);
                togglePref.setKey(String.valueOf(opItem.getId()));
                togglePref.setTitle(opItem.getTitle());
                togglePref.setSummary(opItem.getDescription());
                togglePref.setDefaultValue(opItem.getSelect() == 1?true:false);
                 togglePref.setOnPreferenceChangeListener(this);
                 m_inlinePrefCat.addPreference(togglePref);
                if(opItem.getParentItem() != null) {
                    togglePref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                }
                
            } 
            else if(opItem.getUIType() == UITYPE_RADIOBOX)
            {
                List<String> listEntry = new ArrayList<String>(); 
                List<String> listEntryValue = new ArrayList<String>(); 
                for(int j=0; j<opItem.getValueListItemCount();j++)
                {
                    ValueListItem listItem = opItem.getValueListItem(j);
                    if((listItem.getPlatform()&ANDROID_PLATFORM) == 1)
                    {
                        listEntry.add(listItem.getTitle());
                        listEntryValue.add(String.valueOf(listItem.getValue()));
                    }
                }
                String[] entries =  listEntry.toArray(new String[listEntry.size()]);
                String[] entryValues =  listEntryValue.toArray(new String[listEntryValue.size()]);
                
                if(opItem.getId() == OPTION_ID.OPTION_SUBTITLEURL_ID.getValue())
                {
                    addSubtitle(opItem);
                }
                else if(opItem.getId() == OPTION_ID.OPTION_VERSION_ID.getValue())
                {
                    addVersion(opItem);
                }
                else if(opItem.getId() == OPTION_ID.OPTION_DOWNLOADLIST_ID.getValue())
                {
                    addDownloadAlertdialog(opItem);
                }
                else
                {
                    ListPreference listPref = new ListPreference(this);
                    listPref.setKey(String.valueOf(opItem.getId()));
                    listPref.setEntries(entries);
                    listPref.setEntryValues(entryValues);
                    listPref.setDialogTitle(opItem.getTitle());
                    listPref.setTitle(opItem.getTitle());
                    listPref.setSummary(opItem.getDescription());
                   int defaultValue= opItem.getValueListItem(opItem.getSelect()).getValue();
                    listPref.setDefaultValue(String.valueOf(defaultValue).trim());
                    listPref.setOnPreferenceChangeListener(this);
                    m_inlinePrefCat.addPreference(listPref);
                    if(opItem.getParentItem() != null) {
                        listPref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                    }
                }
            } 
            else if(opItem.getUIType() == UITYPE_EDITBOX)
            {
                EditTextPreference editPref = new EditTextPreference(this);
                editPref.setKey(String.valueOf(opItem.getId()));
                if(opItem.getValueListItem(0).getText() != null)
                    editPref.setDefaultValue((opItem.getValueListItem(0).getText()).trim());
                else
                    editPref.setDefaultValue(String.valueOf(opItem.getValueListItem(0).getValue()).trim());
                editPref.setTitle(opItem.getTitle());
                editPref.setSummary(opItem.getDescription());
                editPref.setDialogTitle(opItem.getTitle());
                EditText edit = editPref.getEditText();
                if(opItem.getValueListItem(0).getText() != null)
                	edit.setInputType(InputType.TYPE_CLASS_TEXT);
                else if(opItem.getId() ==OPTION_ID.OPTION_SETHTTPRETRYTIMEOUT_ID.getValue())
                    edit.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
                else
                    edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                editPref.setOnPreferenceChangeListener(this);
                m_inlinePrefCat.addPreference(editPref);
                if(opItem.getParentItem() != null) {
                    editPref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                }
            }else if(opItem.getUIType() == UITYPE_BUTTON)
            {
                CheckBoxPreference togglePref = new CheckBoxPreference(this);
                togglePref.setKey(String.valueOf(opItem.getId()));
                togglePref.setTitle(opItem.getTitle());
                togglePref.setSummary(opItem.getDescription());
                togglePref.setDefaultValue(opItem.getSelect() == 1?true:false);
                m_inlinePrefCat.addPreference(togglePref);
                togglePref.setOnPreferenceChangeListener(this);
                if(opItem.getParentItem() != null) {
                    togglePref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                }
            }
            if(opItem.getChildCount() > 0)
            {
                ArrayList<OptionItem> childList = new ArrayList<OptionItem>();
                for(int temp=0;temp<opItem.getChildCount();temp++)
                {
                    childList.add((OptionItem) opItem.getChild(temp));
                }
                showOptionItem(childList);
            }
         }
    }

    private void addSubtitle(OptionItem item) {
        ArrayList<String> lstUrl = new ArrayList<String>();
        String strPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/subtitle.txt";
        readFileToList(lstUrl, strPath);
        lstUrl.add(0, item.getValueListItem(0).getTitle());
        SharedPreferences shp = PreferenceManager.getDefaultSharedPreferences(this);
        if(shp!=null && lstUrl.size()==1){
            String s = shp.getString(item.getTitle(), "");
            String low = s.toLowerCase();
            if(low.endsWith(".xml") || low.endsWith(".vtt")|| low.endsWith(".smi") || 
                    low.endsWith(".srt") || low.endsWith(".webvtt"))
                lstUrl.add(s);
        }
        if(lstUrl.size()>0)
        {
            ListPreference listPref = new ListPreference(this);
            CharSequence[] contents = new CharSequence[lstUrl.size()];
            lstUrl.toArray(contents);
            listPref.setEntries(contents);
            listPref.setEntryValues(contents);
    
            listPref.setDialogTitle(item.getTitle());
            listPref.setKey(String.valueOf(item.getId()));
            listPref.setTitle(item.getTitle());
            listPref.setSummary(item.getDescription());
            listPref.setDefaultValue(lstUrl.get(0));
            m_inlinePrefCat.addPreference(listPref);
        }
    }
    
    private void addVersion(OptionItem item) {
        ListView lvVersion =new ListView(this);
        lvVersion.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
        
        ArrayList<HashMap<String, String>> lstVersion = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> hashmapVersion = new HashMap<String, String>();
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open("libversions.txt");
            
            byte[] arrContent = new byte[32*1024];
            is.read(arrContent);
            is.close();
            String strVersion = new String(arrContent);
            String[] strlstVersion = strVersion.split("[\\n]");
            
            for (int i = 0; i < strlstVersion.length; i++) {
               if (strlstVersion[i].indexOf("voAbout> ") == -1)
                   continue;
               strlstVersion[i] = strlstVersion[i].substring(strlstVersion[i].indexOf("voAbout> ") + 9);
            }
            
            for (int i = 0; i < strlstVersion.length; i++) {
                strlstVersion[i] = strlstVersion[i].trim();
                hashmapVersion = new HashMap<String, String>();
                hashmapVersion.put("version", strlstVersion[i]);
                lstVersion.add(hashmapVersion);
            }
            } catch (IOException e) {
            }
       
        if(lstVersion.size()>0)
        {
            MyDialogPreference dialogPref = new MyDialogPreference(this,attrs);
            dialogPref.setDialogTitle(item.getTitle());
            dialogPref.setKey(item.getTitle());
            dialogPref.setTitle(item.getTitle());
            dialogPref.setSummary(item.getDescription());
            m_inlinePrefCat.addPreference(dialogPref);
        }

        String v_str = m_cPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SEI_POST_PROCESS_VIDEO);
        HashMap<String, String> v_version=new HashMap<String, String>();
        v_version.put("version", "Post process video module version: "+ v_str);
        lstVersion.add(0, v_version);

        HashMap<String, String> version=new HashMap<String, String>();
        String str=m_cPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SDK);
        String verimatrixVersion=m_cPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_DRM_VENDOR_A);
        if (!checkVDRMExist()) {
        version.put("version", "Build Release: V" + str  + "  API3");
        } else {
            version.put("version", "Build Release: V" + str  + "  API3"+"\n"+"DRM Vendor A:"+verimatrixVersion);  
        }
        lstVersion.add(0, version);
        lvVersion.setAdapter(new SimpleAdapter(this, lstVersion, android.R.layout.simple_list_item_1,
                   new String[] { "version" }, new int[] { android.R.id.text1 }));
        adlgVersion = new AlertDialog.Builder(this)
        .setTitle("Version").setView(lvVersion)
        .setPositiveButton("OK", new OnClickListener() {
             public void onClick(DialogInterface dialog, int which) {}
        }).create();
        
    }

    @Override
    public OptionItem getOptionItemByID(int id) {
        if(m_optionItems != null) {
            OptionItem item = searchOptionItem(m_optionItems, id);
            if(!(((item.getPlatform()&ANDROID_PLATFORM) != 0) && item.getEnable()))
                return null;	
            m_spMain = PreferenceManager.getDefaultSharedPreferences(mContext);
            Editor editor = m_spMain.edit();
            if(item.getUIType() == UITYPE_RADIOBOX) {
                if(id != OPTION_ID.OPTION_SUBTITLEURL_ID.getValue()) {
                    String itemID = String.valueOf(item.getId());
                    String preValue = m_spMain.getString(itemID, "");
                    if (preValue == "") {
                        String defaultValue = String.valueOf(item.getValueListItem(item.getSelect()).getValue());
                        editor.putString(itemID, defaultValue);
                        editor.commit();
                        preValue = defaultValue;
                    }
                    int selectValue = Integer.parseInt(preValue);
                    item.setSelect(selectValue);
                }
            }
            else if(item.getUIType() == UITYPE_EDITBOX) {
            	if (item.getValueListItem(0).getText() != null) {
            		String strText= m_spMain.getString(String.valueOf(item.getId()), item.getValueListItem(0).getText());
                    item.getValueListItem(0).setText(strText); 
            	} else {
            		String strValue= m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                    if(strValue==null || strValue.length()==0)
                        item.getValueListItem(0).setValue(0);
                    else {
                    	 try{
                    		 item.getValueListItem(0).setValue(Integer.parseInt(strValue)); 
                    	 } catch (NumberFormatException e){
                    		 voLog.d(TAG,"Input invalid value.");
                    		 e.printStackTrace();
                    		 return null;
                    	 }
                 	   
                    }
            	}
            }
            else if(item.getUIType() == UITYPE_CHECKBOX) {
                item.setSelect(m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false)==true?1:0);
            }
            return item;
        }
        return null;
    }

    private OptionItem searchOptionItem(ArrayList<OptionItem> optionItems, int id)
    {
        OptionItem opItem = null;
        for(int i=0;i<optionItems.size();i++) {
            opItem = optionItems.get(i);
            if(id == opItem.getId()) {
            	m_desOpItem = opItem;
            	break;
            }
            else {
                if(opItem.getChildCount() > 0) {
                    ArrayList<OptionItem> childList = new ArrayList<OptionItem>();
                    for(int temp=0;temp<opItem.getChildCount();temp++) {
                        childList.add((OptionItem) opItem.getChild(temp));
                    }
                    searchOptionItem(childList,id);
                }
            }
        }
      return m_desOpItem;
    }
    
  

    private static void readFileToList(ArrayList<String> lstUrl, String filePathName)
    {
       String sUrl, line = "";
       sUrl = filePathName;
       File UrlFile = new File(sUrl);
     
       if (!UrlFile.exists()) {
//           PreferenceManager.getDefaultSharedPreferences(this).edit().putString(String.valueOf(OPTION_ID.OPTION_SUBTITLEURL_ID.getValue()), "select external subtitle file or url").commit();
           return;
       }

       try {
           FileReader fileread = new FileReader(UrlFile);
           BufferedReader bfr = new BufferedReader(fileread);
           try {
               while (line != null) {
                   line = bfr.readLine();
                   if (line != null && line.length() != 0) {
                       line = line.trim();
                       lstUrl.add(line);
                   }
               }
               
               bfr.close();
               fileread.close();
               
           } catch (IOException e) {
               e.printStackTrace();
           }
       } catch (FileNotFoundException e) {
           e.printStackTrace();
       } 
       
   }
    
    @Override
    public VO_OSMP_RETURN_CODE uninit() {
        m_cPlayer = null;
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
    
    public void printLog(String log)
    {
        voLog.d(TAG, log);
    }
    
    public void popupMsg(String log, APP_BEHAVIOR_EVENT_ID behavior) 
    {
        if(m_delegate != null)
        {
            switch(behavior)
            {
                case APP_BEHAVIOR_CONTINUE_PLAY:
                    m_delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_CONTINUE_PLAY, log);
                    break;
                
                case APP_BEHAVIOR_STOP_PLAY:
                    m_delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_STOP_PLAY, log);
                    break;
                    
                case APP_BEHAVIOR_PAUSE_PLAY:
                    m_delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_PAUSE_PLAY, log);
                    break;
                case APP_BEHAVIOR_SWITCH_ENGINE:
                    m_delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_SWITCH_ENGINE, log);
                    break;
                    
                default:
                    break; 
            }
        }
    }
    
    AttributeSet attrs = null;
    AlertDialog adlgVersion = null;
    AlertDialog adlgDelete   = null;
    public class MyDialogPreference extends DialogPreference{

        public MyDialogPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onClick() {

            adlgVersion.show();
            //super.onClick();
        }
    }
    public class DeleteDialogPreference extends DialogPreference{

        public DeleteDialogPreference(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        protected void onClick() {

            adlgDelete.show();
            //super.onClick();
        }
    }
    public static String getUserPath(Context context)
    {
        PackageManager m = context.getPackageManager();
        String path = context.getPackageName();
        String userPath = "/data/data/" + path;
        try 
        {
               PackageInfo p = m.getPackageInfo(path, 0);
               userPath = p.applicationInfo.dataDir;
        } catch (NameNotFoundException e) {
        }
        return userPath;
        
    }
    public static void copyfile(Context context, String filename, String desName) {
        try {
            InputStream InputStreamis  = context.getAssets().open(filename);
            File desFile = new File(getUserPath(context) + "/" + desName);
            desFile.createNewFile();             
            FileOutputStream  fos = new FileOutputStream(desFile);
            int bytesRead;
            byte[] buf = new byte[4 * 1024]; //4K buffer
            while((bytesRead = InputStreamis.read(buf)) != -1) {
            fos.write(buf, 0, bytesRead);
            }
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   private void initSubtitle(){
       m_cPlayer.initSubtitle();
   }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        boolean ret;
         if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue()))){
            initSubtitle();
         }
         
         else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_USEDEFAULTFONT_ID.getValue()))){
             
            ret=(Boolean) newValue;
           
           if(ret){
              
               chbEnableFontSize.setChecked(false);
               chbEnableFontColorOpacity.setChecked(false);
               chbEnableFontColor.setChecked(false);
               chbEnableBackgroundColorOpacity.setChecked(false);
               chbEnableBackgroundColor.setChecked(false);
               chbEnableEdgeColorOpacity.setChecked(false);
               chbEnableEdgeColor.setChecked(false);
               chbEnableEdgeType.setChecked(false);
               chbEnableFontname.setChecked(false);
               chbEnableWindowBackgroundColorOpacity.setChecked(false);
               chbEnableWindowBackgroundColor.setChecked(false);
               chbEnableUderlineFont.setChecked(false);
               chbEnableBoldFont.setChecked(false);
               chbEnableItalicFont.setChecked(false);
               initSubtitle();
            } 
          }
         else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_RESETTODEFAULTSET_ID.getValue()))){
             
             ret=(Boolean) newValue;
            
             if(ret){
                 edtSetFontSize.setText("100");
                 edtSetFontColorOpacity.setText("100");
                 listSetFontColor.setValue("16777215");
                 edtSetBackgroundColorOpacity.setText("100");
                 listSetBackgroundColor.setValue("0");
                 edtSetEdgeColorOpacity.setText("100");
                 listSetEdgeColor.setValue("16711680");
                 listSetEdgeType.setValue("1");
                 listSetFontname.setValue("4");
                 edtSetWindowBackgroundColorOpacity.setText("100");
                 listSetWindowBackgroundColor.setValue("16777215");
                 chbSetUderlineFont.setChecked(false);
                 chbSetBoldFont.setChecked(false);
                 chbSetItalicFont.setChecked(false);
                 initSubtitle();
              
             }
            
         
          }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTSIZE_ID.getValue()))){
          initSubtitle();
          }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTSIZE_ID.getValue()))){
           initSubtitle();
        }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTCOLOROPACITY_ID.getValue()))){
           initSubtitle();
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTCOLOROPACITY_ID.getValue()))){
           initSubtitle();
     
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_COLORLIST_ID.getValue()))){
           initSubtitle();
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETCOLORLIST_ID.getValue()))){
           initSubtitle();
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BGCOLOROPACITY_ID.getValue()))){
           initSubtitle();
         }    
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBGCOLOROPACITY_ID.getValue()))){
           initSubtitle();
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BGCOLORLIST_ID.getValue()))){
           initSubtitle();
         } 
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBGCOLORLIST_ID.getValue()))){
           initSubtitle();
         } 
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGECOLOROPACITY_ID.getValue()))){
           initSubtitle();
         } 
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGECOLOROPACITY_ID.getValue()))){
           initSubtitle();
         } 
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGECOLORLIST_ID.getValue()))){
           initSubtitle();
         }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGECOLORLIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGETYPELIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGETYPELIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTLIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTLIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_WINBGCOLOROPACITY_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETWINBGCOLOROPACITY_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_WINBGCOLORLIST_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETWINBGCOLORLIST_ID.getValue()))){
           initSubtitle();

       } 
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_UNDERLINEFONT_ID.getValue()))){
           initSubtitle();
       }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETUNDERLINEFONT_ID.getValue()))){
           initSubtitle();
        } 
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BOLDFONT_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBOLDFONT_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_ITALICFONT_ID.getValue()))){
           initSubtitle();
        }
       else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETITALICFONT_ID.getValue()))){
           initSubtitle();

        }
       return true;
    }
    
  
private void initPreference(){
    chbEnableSubtitleSetting=(CheckBoxPreference) findPreference(String.valueOf(16));
    chbEnableSubtitlePresrntation=(CheckBoxPreference) findPreference(String.valueOf(1601));
    chbUseDefaultSubtitleSetting=(CheckBoxPreference) findPreference(String.valueOf(1616));
    chbResetSubtitleSetting=(CheckBoxPreference) findPreference(String.valueOf(1617));
    chbEnableFontSize=(CheckBoxPreference) findPreference(String.valueOf(1602));
    edtSetFontSize=(EditTextPreference) findPreference(String.valueOf(160201));
    chbEnableFontColorOpacity=(CheckBoxPreference) findPreference(String.valueOf(1603));
    edtSetFontColorOpacity=(EditTextPreference) findPreference(String.valueOf(160301));
    chbEnableFontColor=(CheckBoxPreference) findPreference(String.valueOf(1604));
    listSetFontColor=(ListPreference) findPreference(String.valueOf(160401));
    chbEnableBackgroundColorOpacity=(CheckBoxPreference) findPreference(String.valueOf(1605));
    edtSetBackgroundColorOpacity=(EditTextPreference) findPreference(String.valueOf(160501));
    chbEnableBackgroundColor=(CheckBoxPreference) findPreference(String.valueOf(1606));
    listSetBackgroundColor=(ListPreference) findPreference(String.valueOf(160601));
    chbEnableEdgeColorOpacity=(CheckBoxPreference) findPreference(String.valueOf(1607));
    edtSetEdgeColorOpacity=(EditTextPreference) findPreference(String.valueOf(160701));
    chbEnableEdgeColor=(CheckBoxPreference) findPreference(String.valueOf(1608));
    listSetEdgeColor=(ListPreference) findPreference(String.valueOf(160801));
    chbEnableEdgeType=(CheckBoxPreference) findPreference(String.valueOf(1609));
    listSetEdgeType=(ListPreference) findPreference(String.valueOf(160901));
    chbEnableFontname=(CheckBoxPreference) findPreference(String.valueOf(1610));
    listSetFontname=(ListPreference) findPreference(String.valueOf(161001));
    chbEnableWindowBackgroundColorOpacity=(CheckBoxPreference) findPreference(String.valueOf(1611));
    edtSetWindowBackgroundColorOpacity=(EditTextPreference) findPreference(String.valueOf(161101));
    chbEnableWindowBackgroundColor=(CheckBoxPreference) findPreference(String.valueOf(1612));
    listSetWindowBackgroundColor=(ListPreference) findPreference(String.valueOf(161201));
    chbEnableUderlineFont=(CheckBoxPreference) findPreference(String.valueOf(1613));
    chbSetUderlineFont=(CheckBoxPreference) findPreference(String.valueOf(161301));
    chbEnableBoldFont=(CheckBoxPreference) findPreference(String.valueOf(1614));
    chbSetBoldFont=(CheckBoxPreference) findPreference(String.valueOf(161401));
    chbEnableItalicFont=(CheckBoxPreference) findPreference(String.valueOf(1615));
    chbSetItalicFont=(CheckBoxPreference) findPreference(String.valueOf(161501));
}



private void addPreview(){

    Preference pf = new Preference(this){
        
        @Override 
           protected  void  onBindView(View view) { 
               super.onBindView(view); 
        
               // Set our custom views inside the layout
               m_textView = (TextView) view.findViewById(m_nTextView); 
               if  (m_textView != null ) { 
                   initSubtitle();                 
                   m_cPlayer.previewSubtitle(m_textView );
               } 
           } 
       };
       pf.setKey("key093039983");
       pf.setTitle("PreviewSubtitle");
       pf.setLayoutResource(m_nPreviewLayout);
                 
       m_inlinePrefCat.addPreference(pf); 
}

public boolean onKeyDown(int keyCode, KeyEvent event) {
    voLog.v(TAG, "Key click is " + keyCode);

    if (keyCode ==KeyEvent.KEYCODE_BACK) {
        voLog.v(TAG, "Key click is Back key");
        if(m_cDownloader!=null){
            m_cDownloader.destroy();
            m_cDownloader = null;
        }
        if(m_cPlayer != null){
        m_cPlayer.destroyPlayer();
        m_cPlayer = null;
        }
       try {
        Thread.sleep(100);
    } catch (InterruptedException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
      this.finish();
    
      return true;
    }

    return super.onKeyDown(keyCode, event);
}

    private void tryToSetupDRMForDownloader() {

        // Check if need to setupDRM for Downloader
        boolean isNeeded = false;
        try {
            Method method = FeatureManager.class.getMethod("isNeededSetupDRMForDownloader");
            try {
                Object value = method.invoke(null);
                isNeeded = ((Boolean) value).booleanValue();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        
        if(isNeeded) {
            FeatureManager featureManager = new FeatureManager(mContext);
            featureManager.setupDRM(null, m_cDownloader);    
        }
    }
    
private void addDownloadAlertdialog(OptionItem item){
    DeleteDialogPreference deleteDialogPref = new DeleteDialogPreference(this,attrs);
    
    deleteDialogPref.setDialogTitle(item.getTitle());
    deleteDialogPref.setKey(item.getTitle());
    deleteDialogPref.setTitle(item.getTitle());
    deleteDialogPref.setSummary(item.getDescription());
    m_inlinePrefCat.addPreference(deleteDialogPref);
    
    m_listDownloadInfo =new ListView(this);
    m_listDownloadInfo.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
    m_listDownloadInfo.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
    
    readDownloadInfo();
    
    adlgDelete = new AlertDialog.Builder(this)
    .setTitle("Delete download file.").setView(m_listDownloadInfo)
    .setPositiveButton("Delete", new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        tryToSetupDRMForDownloader();
          long[]  checkdIds=m_listDownloadInfo.getCheckedItemIds();
          String deleteUrl=null;
		  for (int j=0; j<checkdIds.length;j++) {
              int index = (int) checkdIds[j];
              if(index >= 0 && m_downloadList.size()>0)
			      deleteUrl=m_downloadList.get(index);
		      if(deleteUrl != null && m_cDownloader != null)
			      m_cDownloader.deleteContent(deleteUrl);
          }
          
          readDownloadInfo();
        }
    })
    .setNegativeButton("Delete All", new OnClickListener(){

        @Override
        public void onClick(DialogInterface dialog, int which) {
        tryToSetupDRMForDownloader();
            String deleteUrl = null;
            for (int m=0; m<m_downloadList.size();m++) {
                deleteUrl = m_downloadList.get(m);
                if(deleteUrl != null && m_cDownloader != null)
                    m_cDownloader.deleteContent(deleteUrl);
            }
            readDownloadInfo();
        }
        
    }) 
    .create();

}

private void readDownloadInfo(){
    
    m_downloadList=null;
    m_downloadList=new ArrayList<String>();
    CommonFunc.getDownloadFiles(m_downloadList,Definition.DOWNLOAD_PATH);
    MultipleAdapter adapter = new MultipleAdapter(this,
            m_nSingleChoiceLayout , m_downloadList);
    m_listDownloadInfo.setAdapter(adapter);
    
}

private class MultipleAdapter extends ArrayAdapter<String> {

    /* (non-Javadoc)
     * @see android.widget.BaseAdapter#hasStableIds()
     */
    @Override
    public boolean hasStableIds() {
        // TODO Auto-generated method stub
        return true;
    }

    public MultipleAdapter(Context context, int resource) {
        super(context, resource);
        // TODO Auto-generated constructor stub
    }

    public MultipleAdapter(Context context,
            int resource, ArrayList<String> listString) {
        super(context, resource, listString);
        // TODO Auto-generated constructor stub
    }

   
    
}

private boolean checkVDRMExist() {
    File f = new File(CommonFunc.getUserNativeLibPath(mContext) + "libViewRightWebClient.so");
	if (f.exists())
		return true;
	return false;
	  
}


}
