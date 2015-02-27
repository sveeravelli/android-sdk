/************************************************************************
VisualOn Proprietary
Copyright (c) 2012, VisualOn Incorporated. All Rights Reserved

VisualOn, Inc., 4675 Stevens Creek Blvd, Santa Clara, CA 95051, USA

All data and information contained in or disclosed by this document are
confidential and proprietary information of VisualOn, and all rights
therein are expressly reserved. By accepting this material, the
recipient agrees that this material and the information contained
therein are held in confidence and in trust. The material may only be
used and/or disclosed as authorized in a license agreement controlling
such use and disclosure.
************************************************************************/

package com.visualon.appConfig;

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
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import android.content.res.AssetManager;
import android.graphics.Color;
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

import android.util.Xml;

import android.view.KeyEvent;
import android.view.View;



import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RadioGroup.LayoutParams;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOOSMPAnalyticsInfo;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_COLORTYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_MODULE_TYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_PLAYER_ENGINE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RTSP_CONNECTION_TYPE;
import com.visualon.OSMPPlayerImpl.VOCommonPlayerImpl;
import com.visualon.OSMPUtils.voLog;

import com.visualon.appConfig.OptionItem.ValueListItem;




public class AppBehaviorManagerImpl extends PreferenceActivity implements AppBehaviorManager,OnPreferenceChangeListener
{
    private ArrayList<EventItem> mAppEventItems =null;
    private ArrayList<EventItem> mEventItems = null;
    private ArrayList<EventItem> mDownloadEventItems = null;
    private ArrayList<ReturnCodeItem> mReturnCodeItems = null;
    private ArrayList<OptionItem> mOptionItems = null;
    private AppBehaviorManagerDelegate delegate = null;
    private String param1String = "";
    private String param2String = "";
    private static final String TAG = "@@@AppBehaviorManager";
    
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
    private OptionItem desOpItem = null;
    private VOCommonPlayer  m_sdkPlayer = null;
    private PreferenceCategory inlinePrefCat = null;
    Context mContext = null;
    private ArrayList<String>         m_downloadList     = null;
    private static final String  DOWNLOAD_PATH             = "/sdcard/osmp";
    private ListView                  m_listDownloadInfo   = null;
  
    private boolean                  m_enableDownload      = true;
    private int                      singleChoiceLayout;
    private int                      inttextView;
	private int                      intpreviewLayout;
	private TextView                 textView;
	private boolean                  bEnableSubtitleStting;
	private CheckBoxPreference       chbEnableSubtitleSetting;
	private CheckBoxPreference       chbEnableSubtitlePresrntation;
	private boolean                  bEnableSubtitlePresrntation;
	private CheckBoxPreference       chbUseDefaultSubtitleSetting;
	private boolean                  bUseDefaultSubtitleSetting;
	private CheckBoxPreference       chbResetSubtitleSetting;
	private boolean                  bResetSubtitleSetting;
	private CheckBoxPreference       chbEnableFontSize;
	private boolean                  bEnableFontSize;
	private EditTextPreference       edtSetFontSize;
	private int                      fontSize;
	private CheckBoxPreference       chbEnableFontColorOpacity;
	private boolean                  bEnableFontColorOpacity;
	private EditTextPreference       edtSetFontColorOpacity;
	private int                      fontColorOpacity;
	private CheckBoxPreference       chbEnableFontColor;
	private boolean                  bEnableFontColor;
	private ListPreference           listSetFontColor;
	private int                      fontColor;
	private CheckBoxPreference       chbEnableBackgroundColorOpacity;
	private boolean                  bEnableBackgroundColorOpacity;
	private EditTextPreference       edtSetBackgroundColorOpacity;
	private int                      backgroundColorOpacity;
	private CheckBoxPreference       chbEnableBackgroundColor;
	private boolean                  bEnableBackgroundColor;
	private ListPreference           listSetBackgroundColor;
	private int                      backgroundColor;
	private CheckBoxPreference       chbEnableEdgeColorOpacity;
	private boolean                  bEnableEdgeColorOpacity;
	private EditTextPreference       edtSetEdgeColorOpacity;
	private int                      edgeColorOpacity;
	private CheckBoxPreference       chbEnableEdgeColor;
	private boolean                  bEnableEdgeColor;
	private ListPreference           listSetEdgeColor;
	private int                      edgeColor;
	private CheckBoxPreference       chbEnableEdgeType;
	private boolean                  bEnableEdgeType;
	private ListPreference           listSetEdgeType;
	private int                      edgeType;
	private CheckBoxPreference       chbEnableFontname;
	private boolean                  bEnableFontname;
	private ListPreference           listSetFontname;
	private int                      fontName;
	private CheckBoxPreference       chbEnableWindowBackgroundColorOpacity;
	private boolean                  bEnableWindowBackgroundColorOpacity;
	private EditTextPreference       edtSetWindowBackgroundColorOpacity;
	private int                      windowBackgroundColorOpacity;
	private CheckBoxPreference       chbEnableWindowBackgroundColor;
	private boolean                  bEnableWindowBackgroundColor;
	private ListPreference           listSetWindowBackgroundColor;
	private int                      windowBackgroundColor;
	private CheckBoxPreference       chbEnableUderlineFont;
	private boolean                  bEnableUderlineFont;
	private CheckBoxPreference       chbSetUderlineFont;
	private boolean                  bSetUderlineFont;
	private CheckBoxPreference       chbEnableBoldFont;
	private boolean                  bEnableBoldFont;
	private CheckBoxPreference       chbSetBoldFont;
	private boolean                  bSetBoldFont;
	private CheckBoxPreference       chbEnableItalicFont;
	private boolean                  bEnableItalicFont;
	private CheckBoxPreference       chbSetItalicFont;
	private boolean                  bSetItalicFont;
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
        OPTION_MAXBUFFERTIME_ID                           (10),
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
        OPTION_ENABLERTSPHTTPPORT                         (23),
        OPTION_SETRTSPHTTPPORT                            (2301),
        OPTION_ENABLERENDEROPTIMIZATIONFORBA_ID           (24),
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
        copyfile(this, "appcfg.xml", "appcfg.xml");
        m_sdkPlayer = new VOCommonPlayerImpl();
        String apkPath = getUserPath(this) + "/lib/";

        singleChoiceLayout=getIntent().getIntExtra("singleChoiceLayout", 0);
        inttextView=getIntent().getIntExtra("textView", 0);
        intpreviewLayout=getIntent().getIntExtra("previewLayout", 0);
        String filePath = getUserPath(this) + "/" + "appcfg.xml";
        loadCfgFile(filePath);
        showOptionPage(true);
       initPreference();
       try{
       initPreferenceValue();}
       catch(NumberFormatException e){
           e.printStackTrace();
       }
      addPreview();
       
        
    }

    @Override
    public VO_OSMP_RETURN_CODE init(AppBehaviorManagerDelegate delegate) {
        this.delegate = delegate;
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    @Override
    public VO_OSMP_RETURN_CODE loadCfgFile(String filePath) {
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
        mAppEventItems = analysis.getAppEventItems();
        mEventItems = analysis.getEventItems();
        mDownloadEventItems = analysis.getDownloadEventItems();
        mReturnCodeItems = analysis.getReturnCodeItems();
        mOptionItems = analysis.getOptionItems();
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

    @Override
    public int processEvent(int eventId, int param1, int param2, Object param3) {
        String temp = Integer.toHexString(eventId);
        if(temp.length() > 8)
        {
            temp = temp.substring(temp.length()-8,temp.length());
        }
        Long leventId = Long.parseLong(temp, 16);
        ArrayList<EventItem> eventItems = null;
        int downloadStartId = 0X80000001;
        int downloadFailStartId = 0X90000001;
        String temp2 = Integer.toHexString(downloadFailStartId);
        if(temp2.length() > 8)
        {
            temp2 = temp2.substring(temp2.length()-8,temp2.length());
        }
        Long ldownloadFailStartId = Long.parseLong(temp2, 16);
        
        if(((leventId >= downloadStartId && leventId <= downloadStartId + 100) 
                || (leventId >= ldownloadFailStartId && leventId <= ldownloadFailStartId + 100))
                && mDownloadEventItems != null)
        {
            eventItems = mDownloadEventItems;
        }
        else if(leventId==Long.parseLong("8A000001", 16)){
            eventItems = mAppEventItems;
            
        }
        else if(mEventItems != null)
        {
            eventItems = mEventItems;
        }
        else 
        {
            return 0;
        }
        
        int nRet = eventAction(eventItems,leventId,param1,param2,param3);
        if(nRet == 0)
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }

    private int eventAction(ArrayList<EventItem> eventItems, Long leventId, int param1, int param2, Object param3) {
        String log = "";
        int index = 0;
        int listSize = eventItems.size();
        int j = 0;
        EventItem eventItem = null;
        for(int i=0; i<listSize; i++)
        {
            eventItem = eventItems.get(i);

            if(leventId == eventItem.getId())
            {
                if(!eventItem.getEnable())
                {
                    return 0;
                }
                index = i;
                log = eventItem.getDescription();
                int param1Size = eventItem.getParam1Count();
                if(param1Size > 0) 
                {
                    for(j=0;j<param1Size;j++)
                    {
                        if(param1 == eventItem.getParam1(j).getParamValue())
                        {
                            param1String = eventItem.getParam1(j).getDescription();
                            break;
                        }
                    }
                }
                int param2Size = eventItem.getParam2Count();
                if(param2Size > 0) 
                {
                    for(j=0;j<param2Size;j++)
                    {
                        if(param2 == eventItem.getParam2(j).getParamValue())
                        {
                            param2String = eventItem.getParam2(j).getDescription();
                            break;
                        }
                    }
                }
                int nRet = pressEventLog(leventId,eventItems,param1,param2,param3,index,log);
                if(nRet == 0)
                {
                    return 0;
                }
                else
                {
                    return 1;
                }
            }
            else
            {
                if(eventItem.getChildCount()>0)
                {
                    ArrayList<EventItem> childList = new ArrayList<EventItem>();
                    for(int temp=0;temp<eventItem.getChildCount();temp++)
                    {
                        childList.add((EventItem) eventItem.getChild(temp));
                    }
                    eventAction(childList,leventId,param1,param2,param3);
                }
            }
            
        }
        
        return 0;
    }

    private int pressEventLog(Long leventId, ArrayList<EventItem> eventItems, int param1, int param2, Object param3, int index, String log) {
        log = log.replace("param1 is %d", "param1 is " + String.valueOf(param1));
        log = log.replace("param2 is %d", "param2 is " + String.valueOf(param2));
        log = log.replace("param1 is %s", "param1 is " + param1String);
        log = log.replace("param2 is %s", "param2 is " + param2String);
        
        if(param3 != null)
        {
            if(leventId == 0x00001021)
            {
                VOOSMPAnalyticsInfo info = (VOOSMPAnalyticsInfo) param3;
                log = log.replace("AverageDecodeTime is %d", "AverageDecodeTime is " + String.valueOf(info.getAverageDecodeTime()));
                log = log.replace("AverageRender is %d", "AverageRender is " + String.valueOf(info.getAverageRenderTime()));
                log = log.replace("CodecDropNum is %d", "CodecDropNum is " + String.valueOf(info.getCodecDropNum()));
//                if(info.getCodecErrors() == null)
//                {
//                    log = log.replace("CodecErrors is %d", "CodecErrors is null");
//                }
//                else
//                {
//                    log = log.replace("CodecErrors is %d", "CodecErrors is " + String.valueOf(info.getCodecErrors()));
//                }
                log = log.replace("CodecErrorsNum is %d", "CodecErrorsNum is " + String.valueOf(info.getCodecErrorsNum()));
                log = log.replace("CodecTimeNum is %d", "CodecTimeNum is " + String.valueOf(info.getCodecTimeNum()));
                log = log.replace("CPULoad is %d", "CPULoad is " + String.valueOf(info.getCPULoad()));
                log = log.replace("DecodedNum is %d", "DecodedNum is " + String.valueOf(info.getDecodedNum()));
                log = log.replace("Frequency is %d", "Frequency is " + String.valueOf(info.getFrequency()));
                log = log.replace("JitterNum is %d", "JitterNum is " + String.valueOf(info.getJitterNum()));
                log = log.replace("LastTime is %d", "LastTime is " + String.valueOf(info.getLastTime()));
                log = log.replace("MaxFrequency is %d", "MaxFrequency is " + String.valueOf(info.getMaxFrequency()));
                log = log.replace("RenderDropNum is %d", "RenderDropNum is " + String.valueOf(info.getRenderDropNum()));
                log = log.replace("RenderNum is %d", "RenderNum is " + String.valueOf(info.getRenderNum()));
                log = log.replace("RenderTimeNum is %d", "RenderTimeNum is " + String.valueOf(info.getRenderTimeNum()));
                log = log.replace("SourceTimeNum is %d", "SourceTimeNum is " + String.valueOf(info.getSourceTimeNum()));
                log = log.replace("TotalCPULoad is %d", "TotalCPULoad is " + String.valueOf(info.getTotalCPULoad()));
                log = log.replace("WorstDecodeTime is %d", "WorstDecodeTime is " + String.valueOf(info.getWorstDecodeTime()));
                log = log.replace("WorstRenderTime is %d", "WorstRenderTime is " + String.valueOf(info.getWorstRenderTime()));            
            }
        }
        
        int type = eventItems.get(index).getType();
        APP_BEHAVIOR_EVENT_ID behavior = eventItems.get(index).getAppBehavior();;
        if(type == ACTIONTYPE_LOG)
        {
            printLog(log);
            return 0;
        } else if(type == ACTIONTYPE_POPMESSAGE)
        {
            popupMsg(log,behavior);
            return 1;
        } else if(type == ACTIONTYPE_BOTH)
        {
            printLog(log);
            popupMsg(log,behavior);
            return 1;
        } else if(type == ACTIONTYPE_PARAM1)
        {
            if(param1 == 0)
            {
                printLog(log);
                return 0;
            }
            else
            {
                printLog(log);
                popupMsg(log,behavior);
                return 1;
            }
        } else if(type == ACTIONTYPE_PARAM2)
        {
            if(param2 == 0)
            {
                printLog(log);
                return 0;
            }
            else
            {
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
        {
            temp = temp.substring(temp.length()-8,temp.length());
        }
        Long lreturnCode = Long.parseLong(temp, 16);
        
        if(mReturnCodeItems != null)
        {
            int nRet = returnCodeAction(mReturnCodeItems, apiName, lreturnCode);
            if(nRet == 0)
            {
                return 0;
            }
            else
            {
                return 1;
            }
        }
        return 0;
    }

    private int returnCodeAction(ArrayList<ReturnCodeItem> mReturnCodeItems2,
            String apiName, Long lreturnCode) {
        int index = 0;
        String log = "Unknow Error";
        int i = 0;
        int listSize = mReturnCodeItems.size();
        for(i=0; i<listSize; i++)
        {
            ReturnCodeItem rcItem = mReturnCodeItems.get(i);
            if(rcItem.getChildCount()>0)
            {
                ArrayList<ReturnCodeItem> childList = new ArrayList<ReturnCodeItem>();
                for(int temp=0;temp<rcItem.getChildCount();temp++)
                {
                    childList.add((ReturnCodeItem) rcItem.getChild(temp));
                }
                returnCodeAction(childList,apiName,lreturnCode);
            }
            else
            {
                if(lreturnCode == rcItem.getId())
                {
                    if(!rcItem.getEnable())
                    {
                        return 0;
                    }
                    index = i;
                    log = rcItem.getDescription();
                    log = log.replace("%s", apiName);
                    int type = mReturnCodeItems.get(index).getType();
                    APP_BEHAVIOR_EVENT_ID behavior = mReturnCodeItems.get(index).getAppBehavior();
                    
                    if(i == listSize)  // deal with the unknow return code 
                    {
                        printLog(log);
                        popupMsg(log,behavior);
                        return 1;
                    }
                    if(type == ACTIONTYPE_LOG)
                    {
                        printLog(log);
                        return 0;
                    } else if(type == ACTIONTYPE_POPMESSAGE)
                    {
                        popupMsg(log,behavior);
                        return 1;
                    } else if(type == ACTIONTYPE_BOTH)
                    {
                        printLog(log);
                        popupMsg(log,behavior);
                        return 1;
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
            inlinePrefCat = new PreferenceCategory(this);
            inlinePrefCat.setTitle("Options");
            root.addPreference(inlinePrefCat);
            showOptionItem(mOptionItems);
           
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
            
                if(opItem.getId() == OPTION_ID.OPTION_DOWNLOAD_ID.getValue())
                {
                    opItem.setEnable(true);
                }
            if(!m_enableDownload){
                if(opItem.getId() == OPTION_ID.OPTION_DOWNLOAD_ID.getValue())
                {
                    opItem.setEnable(false);
                }
                if(opItem.getId() == OPTION_ID.OPTION_DOWNLOADLIST_ID.getValue())
                {
                    opItem.setEnable(false);
                }
            }
            if(!(((opItem.getPlatform()&ANDROID_PLATFORM) == 1) && opItem.getEnable()))
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
                inlinePrefCat.addPreference(togglePref);
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
                    inlinePrefCat.addPreference(listPref);
                    if(opItem.getParentItem() != null) {
                        listPref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                    }
                }
            } 
            else if(opItem.getUIType() == UITYPE_EDITBOX)
            {
                EditTextPreference editPref = new EditTextPreference(this);
                editPref.setKey(String.valueOf(opItem.getId()));
                if(opItem.getId() ==OPTION_ID.OPTION_SETDEFAULTAUDIOLANGUAGE_ID.getValue()||opItem.getId() ==OPTION_ID.OPTION_SETDEFAULTSUBTITLELANGUAGE_ID.getValue())
                    editPref.setDefaultValue((opItem.getValueListItem(0).getText()).trim());
                else
                editPref.setDefaultValue(String.valueOf(opItem.getValueListItem(0).getValue()).trim());
                editPref.setTitle(opItem.getTitle());
                editPref.setSummary(opItem.getDescription());
                editPref.setDialogTitle(opItem.getTitle());
                EditText edit = editPref.getEditText();
                if(opItem.getId() ==OPTION_ID.OPTION_SETHTTPRETRYTIMEOUT_ID.getValue()){
                    edit.setRawInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
                }else if(opItem.getId() ==OPTION_ID.OPTION_SETDEFAULTAUDIOLANGUAGE_ID.getValue()){
                    edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }else if(opItem.getId() ==OPTION_ID.OPTION_SETDEFAULTSUBTITLELANGUAGE_ID.getValue()){
                    edit.setInputType(InputType.TYPE_CLASS_TEXT);
                }
                else{
                    edit.setInputType(InputType.TYPE_CLASS_NUMBER);
                    
                }
                  editPref.setOnPreferenceChangeListener(this);
               
                inlinePrefCat.addPreference(editPref);
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
                inlinePrefCat.addPreference(togglePref);
                togglePref.setOnPreferenceChangeListener(this);
                if(opItem.getParentItem() != null) {
                    togglePref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                }
            }/*else if (opItem.getUIType() == UITYPE_VIEW)
            {
                Preference     textView= new  Preference(this);
                
                textView.setKey(String.valueOf(opItem.getId()));
                textView.setTitle(opItem.getTitle());
                textView.setSummary(opItem.getDescription());
                
                inlinePrefCat.addPreference(textView);
                if(opItem.getParentItem() != null) {
                    togglePref.setDependency(String.valueOf(opItem.getParentItem().getId()));
                }
       
                
            }*/
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
            inlinePrefCat.addPreference(listPref);
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
            String strVersion = new String(arrContent);
            String[] strlstVersion = strVersion.split("[\\n]");
            
            for (int i = 0; i < strlstVersion.length; i++) {
               if (strlstVersion[i].indexOf("voAbout> ") == -1)
                   continue;
               strlstVersion[i] = strlstVersion[i].substring(strlstVersion[i].indexOf("voAbout> ") + 9);
            }
            
            for (int i = 0; i < strlstVersion.length; i++) {
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
            inlinePrefCat.addPreference(dialogPref);
        }
        VOCommonPlayer versionPlayer = new VOCommonPlayerImpl();
        String apkPath = getUserPath(this) + "/lib/";
        VOOSMPInitParam init = new VOOSMPInitParam();
        init.setContext(this);
        init.setLibraryPath(apkPath);
        versionPlayer.init(VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER, init);
        HashMap<String, String> version=new HashMap<String, String>();
        String str=versionPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SDK);
        String verimatrixVersion=versionPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_DRM_VENDOR_A);
        if(verimatrixVersion==null){
        version.put("version", "Build Release: V" + str  + "  API3");
        }else{
            version.put("version", "Build Release: V" + str  + "  API3"+"\n"+"DRM Vendor A:"+verimatrixVersion);  
        }
        lstVersion.add(0, version);
        
        if(versionPlayer != null)
        {
            versionPlayer = null;
        }
           lvVersion.setAdapter(new SimpleAdapter(this, lstVersion, android.R.layout.simple_list_item_1,
                   new String[] { "version" }, new int[] { android.R.id.text1 }));
           adlgVersion = new AlertDialog.Builder(this)
                   .setTitle("Version").setView(lvVersion)
                   .setPositiveButton("OK", new OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                       }
                   }).create();
        
    }

    @Override
    public OptionItem getOptionItemByID(int id) {
        if(mOptionItems != null)
        {
            OptionItem item = searchOptionItem(mOptionItems, id);
            m_spMain = PreferenceManager.getDefaultSharedPreferences(mContext);
            
            if(item.getUIType() == UITYPE_RADIOBOX)
            {
                if(id != OPTION_ID.OPTION_SUBTITLEURL_ID.getValue())
                {
                    item.setSelect(Integer.parseInt(m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()))));
                }
            }
            else if(item.getUIType() == UITYPE_EDITBOX)
            {
               String str= m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
               if(str==null || str.length()==0){
                   item.getValueListItem(0).setValue(0);
               }else{
                   if(item.getId()==OPTION_ID.OPTION_SETDEFAULTAUDIOLANGUAGE_ID.getValue() || item.getId()==OPTION_ID.OPTION_SETDEFAULTSUBTITLELANGUAGE_ID.getValue()){
                       str= m_spMain.getString(String.valueOf(item.getId()), item.getValueListItem(0).getText());
                       item.getValueListItem(0).setText(str); 
                   }else
                   item.getValueListItem(0).setValue(Integer.parseInt(str)); 
               }
               
            }
            else if(item.getUIType() == UITYPE_CHECKBOX)
            {
                item.setSelect(m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false)==true?1:0);
            }
            return item;
        }
        return null;
    }

    private OptionItem searchOptionItem(ArrayList<OptionItem> optionItems, int id)
    {
        OptionItem opItem = null;
        for(int i=0;i<optionItems.size();i++)
        {
            opItem = optionItems.get(i);
            if(id == opItem.getId())
            {
                desOpItem = opItem;
                break;
            }
            else
            {
                if(opItem.getChildCount() > 0)
                {
                    ArrayList<OptionItem> childList = new ArrayList<OptionItem>();
                    for(int temp=0;temp<opItem.getChildCount();temp++)
                    {
                        childList.add((OptionItem) opItem.getChild(temp));
                    }
                    searchOptionItem(childList,id);
                }
            }
        }
        return desOpItem;
    }
    
    @Override
    public VO_OSMP_RETURN_CODE updatePlayerOption(VOCommonPlayer player) {
        
        if(player != null)
        {
            
            String str = null;
            OptionItem item = new OptionItem();
            int num;
            item = getOptionItemByID(OPTION_ID.OPTION_DEBLOCK_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                player.enableDeblock(true);
            }
            else
            {
                player.enableDeblock(false);
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_COLORTYPE_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                player.setColorType(VO_OSMP_COLORTYPE.VO_OSMP_COLOR_ARGB32_PACKED);
            }
            else
            {
                player.setColorType(VO_OSMP_COLORTYPE.VO_OSMP_COLOR_RGB565_PACKED);
            }
            }
            
//            item = getOptionItemByID(OPTION_ID.OPTION_SUBTITLEURL_ID.getValue());
            str = m_spMain.getString(String.valueOf(OPTION_ID.OPTION_SUBTITLEURL_ID.getValue()), "0");
//            str = m_spMain.getString(String.valueOf(item.getId()), item.getValueListItem(item.getSelect()).getTitle());
            if(str.indexOf(".")>0)
            {
                player.setSubtitlePath(str);
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_MAXBUFFERTIME_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{
                num=Integer.parseInt(str);
                if(num>20000)
                    num=20000;
                player.setMaxBufferTime(num);
            }catch(NumberFormatException e){
                voLog.d(TAG,"Input invalid value.");
                e.printStackTrace();
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_CPUADAPTION_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                player.enableCPUAdaptation(true);
            }
            else
            {
                player.enableCPUAdaptation(false);
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_RTSP_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==0)
            {
                player.setRTSPConnectionType(VO_OSMP_RTSP_CONNECTION_TYPE.VO_OSMP_RTSP_CONNECTION_AUTOMATIC);
            }
            else if(item.getSelect()==1)
            {
                player.setRTSPConnectionType(VO_OSMP_RTSP_CONNECTION_TYPE.VO_OSMP_RTSP_CONNECTION_TCP);
            }
            else if(item.getSelect()==2)
            {
                player.setRTSPConnectionType(VO_OSMP_RTSP_CONNECTION_TYPE.VO_OSMP_RTSP_CONNECTION_UDP);
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_ENABLERTSPHTTPPORT.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1){
                player.enableRTSPOverHTTP(true);
                item = getOptionItemByID(OPTION_ID.OPTION_SETRTSPHTTPPORT.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                try{
                    num=Integer.parseInt(str);
                  
                    player.setRTSPOverHTTPConnectionPort(num);
                }catch(NumberFormatException e){
                    voLog.d(TAG,"Input invalid value.");
                    e.printStackTrace();
                }
           
            }else{
                player.enableRTSPOverHTTP(false); 
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_INITIALBITRATE_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                item = getOptionItemByID(OPTION_ID.OPTION_SETINITIALBITRATE_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                int initialBitrate;
                if(str==null || str.length()==0){
                    initialBitrate=0;
                }else{
                    initialBitrate=Integer.parseInt(str);
                }
                initialBitrate*=1000;
                player.setInitialBitrate(initialBitrate);
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_BITRATERANGE_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                item = getOptionItemByID(OPTION_ID.OPTION_LOWERBITRATERANGE_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                int lower ;
                if(str==null || str.length()==0){
                    lower = 0;
                }else{
                    lower = Integer.parseInt(str);
                }
                item = getOptionItemByID(OPTION_ID.OPTION_UPPERBITRATERANGE_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                int upper ;
                if(str==null || str.length()==0){
                    upper = 0;
                }else{
                    upper = Integer.parseInt(str);
                }
                lower*=1000;
                upper*=1000;
                player.setBitrateThreshold(upper,lower);
            }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_HTTPRETRYTIMEOUT_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                item = getOptionItemByID(OPTION_ID.OPTION_SETHTTPRETRYTIMEOUT_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
                int retryTime;
              try{
                    retryTime=Integer.parseInt(str);
                    player.setHTTPRetryTimeout(retryTime);}
                catch(NumberFormatException e){
                    voLog.d(TAG, "Input invalid value");
                    e.printStackTrace();
                }
             }
            }
            item = getOptionItemByID(OPTION_ID.OPTION_ENABLEDEFAULTAUDIOLANGUAGE_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                item = getOptionItemByID(OPTION_ID.OPTION_SETDEFAULTAUDIOLANGUAGE_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), (item.getValueListItem(0).getText()));
                String[] audioList=str.split("\\;");
                player.setPreferredAudioLanguage(audioList);
             }
            }
            
            item = getOptionItemByID(OPTION_ID.OPTION_ENABLEDEFAULTSUBTITLELANGUAGE_ID.getValue());
            if((item.getPlatform()&ANDROID_PLATFORM)==1){
            if(item.getSelect()==1)
            {
                item = getOptionItemByID(OPTION_ID.OPTION_SETDEFAULTSUBTITLELANGUAGE_ID.getValue());
                str = m_spMain.getString(String.valueOf(item.getId()), (item.getValueListItem(0).getText()));
                String[] subtitleList=str.split("\\;");
                player.setPreferredSubtitleLanguage(subtitleList);
             }
            }
            
            updateSubtitleSettings(player);
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
    
    private void updateSubtitleSettings(VOCommonPlayer player) {
        String str = null;
        boolean b = false;
        OptionItem item = new OptionItem();
        int num;
        item = getOptionItemByID(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue());
        if(item.getSelect()!=1)
            return;
        
        item = getOptionItemByID(OPTION_ID.OPTION_SUBTITLE_ID.getValue());
        if(item.getSelect()==1)
        {
            player.enableSubtitle(true);
        }
        else
        {
            player.enableSubtitle(false);
        }
        item = getOptionItemByID(OPTION_ID.OPTION_ST_USEDEFAULTFONT_ID.getValue());
        if(item.getSelect()==1)
        {
            player.resetSubtitleParameter();
        }
        item = getOptionItemByID(OPTION_ID.OPTION_ST_FONTSIZE_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTSIZE_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{
                num=Integer.parseInt(str);
                player.setSubtitleFontSizeScale(num);
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
            
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_FONTCOLOROPACITY_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{ num = Integer.parseInt(str);
            if(num > item.getValueListItem(0).getMaxValue())
            {
                player.setSubtitleFontOpacity(item.getValueListItem(0).getMaxValue());
            }
            else if(num < item.getValueListItem(0).getMinValue())
            {
                player.setSubtitleFontOpacity(item.getValueListItem(0).getMinValue());
            }
            else
            {
                player.setSubtitleFontOpacity(num);
            }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }

        item = getOptionItemByID(OPTION_ID.OPTION_ST_COLORLIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            int clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
            player.setSubtitleFontColor(clr);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_BGCOLOROPACITY_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETBGCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{ num = Integer.parseInt(str);
            if(num > item.getValueListItem(0).getMaxValue())
            {
                player.setSubtitleFontBackgroundOpacity(item.getValueListItem(0).getMaxValue());
            }
            else if(num < item.getValueListItem(0).getMinValue())
            {
                player.setSubtitleFontBackgroundOpacity(item.getValueListItem(0).getMinValue());
            }
            else
            {
                player.setSubtitleFontBackgroundOpacity(num);
            }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }

        item = getOptionItemByID(OPTION_ID.OPTION_ST_BGCOLORLIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETBGCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            int clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);

            player.setSubtitleFontBackgroundColor(clr);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_EDGECOLOROPACITY_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGECOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{ num = Integer.parseInt(str);
            if(num > item.getValueListItem(0).getMaxValue())
            {
                player.setSubtitleFontEdgeOpacity(item.getValueListItem(0).getMaxValue());
            }
            else if(num < item.getValueListItem(0).getMinValue())
            {
                player.setSubtitleFontEdgeOpacity(item.getValueListItem(0).getMinValue());
            }
            else
            {
                player.setSubtitleFontEdgeOpacity(num);
            }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_EDGECOLORLIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGECOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            int clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
            player.setSubtitleFontEdgeColor(clr);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_EDGETYPELIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETEDGETYPELIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            player.setSubtitleFontEdgeType(Integer.parseInt(str));
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_FONTLIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETFONTLIST_ID.getValue());
            str = (item.getValueListItem(item.getSelect())).getTitle();
           
            player.setSubtitleFontName(str);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_WINBGCOLOROPACITY_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETWINBGCOLOROPACITY_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getValueListItem(0).getValue()));
            try{ num = Integer.parseInt(str);
            if(num > item.getValueListItem(0).getMaxValue())
            {
                player.setSubtitleWindowBackgroundOpacity(item.getValueListItem(0).getMaxValue());
            }
            else if(num < item.getValueListItem(0).getMinValue())
            {
                player.setSubtitleWindowBackgroundOpacity(item.getValueListItem(0).getMinValue());
            }
            else
            {
                player.setSubtitleWindowBackgroundOpacity(num);
            }
            }catch(NumberFormatException e){
                e.printStackTrace();
            }
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_WINBGCOLORLIST_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETWINBGCOLORLIST_ID.getValue());
            str = m_spMain.getString(String.valueOf(item.getId()), String.valueOf(item.getSelect()));
            int nclr=Integer.parseInt(str);
            int clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
            player.setSubtitleWindowBackgroundColor(clr);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_UNDERLINEFONT_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETUNDERLINEFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            player.setSubtitleFontUnderline(b);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_BOLDFONT_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETBOLDFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            player.setSubtitleFontBold(b);
        }
        
        item = getOptionItemByID(OPTION_ID.OPTION_ST_ITALICFONT_ID.getValue());
        if(item.getSelect()==1)
        {
            item = getOptionItemByID(OPTION_ID.OPTION_ST_SETITALICFONT_ID.getValue());
            b = m_spMain.getBoolean(String.valueOf(item.getId()), item.getSelect()==1?true:false);
            player.setSubtitleFontItalic(b);
        }
    }

    private void readFileToList(ArrayList<String> lstUrl, String filePathName)
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
                       String low = line.toLowerCase();
                       int n = low.lastIndexOf(".");
                       boolean bAdd = false;
                       if(n>0){
                           if(low.substring(n).startsWith(".xml"))
                               bAdd = true;
                       }
                       if(bAdd || low.endsWith(".xml") 
                               || low.endsWith(".vtt")
                               || low.endsWith(".smi") 
                               || low.endsWith(".srt")
                               || low.endsWith(".webvtt"))
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
        m_sdkPlayer = null;
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }
    
    public void printLog(String log)
    {
        voLog.d(TAG, log);
    }
    
    public void popupMsg(String log, APP_BEHAVIOR_EVENT_ID behavior) 
    {
        if(delegate != null)
        {
            switch(behavior)
            {
                case APP_BEHAVIOR_CONTINUE_PLAY:
                    delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_CONTINUE_PLAY, log);
                    break;
                
                case APP_BEHAVIOR_STOP_PLAY:
                    delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_STOP_PLAY, log);
                    break;
                    
                case APP_BEHAVIOR_PAUSE_PLAY:
                    delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_PAUSE_PLAY, log);
                    break;
                case APP_BEHAVIOR_SWITCH_ENGINE:
                    delegate.handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID.APP_BEHAVIOR_SWITCH_ENGINE, log);
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
      
       int clr;
       int nclr;
     
    
           m_sdkPlayer.resetSubtitleParameter();
           if(!bEnableSubtitleStting)
               
               return;
       if(bEnableFontSize)
       m_sdkPlayer.setSubtitleFontSizeScale(fontSize);
       
      if(bEnableFontColorOpacity)
      m_sdkPlayer.setSubtitleFontOpacity(fontColorOpacity);
       
      if(bEnableFontColor){
        nclr=fontColor;
        clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
       m_sdkPlayer.setSubtitleFontColor(clr);}
      
       if(bEnableBackgroundColorOpacity)
       m_sdkPlayer.setSubtitleFontBackgroundOpacity(backgroundColorOpacity);
       
       if(bEnableBackgroundColor){
        nclr=backgroundColor;
       clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
       m_sdkPlayer.setSubtitleFontBackgroundColor(clr);}
       
       if(bEnableEdgeColorOpacity)
       m_sdkPlayer.setSubtitleFontEdgeOpacity(edgeColorOpacity);
       
       if(bEnableEdgeColor){
       nclr=edgeColor;
       clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
       m_sdkPlayer.setSubtitleFontEdgeColor(clr);}
       
      if(bEnableEdgeType)
       m_sdkPlayer.setSubtitleFontEdgeType(edgeType);
       
      if(bEnableFontname){
       String fontname=null;
       int font;
       font = fontName;
       voLog.e(TAG,"font="+font);
       if(font==0)fontname="Default";
       if(font==1)fontname="Courier";
       if(font==2)fontname="Times New Roman";
       if(font==3)fontname="Helvetica";
       if(font==4)fontname="Arial";
       if(font==5)fontname="Dom";
       if(font==6)fontname="Coronet";
       if(font==7)fontname="Gothic";
       m_sdkPlayer.setSubtitleFontName(fontname);
      }
      
      if(bEnableWindowBackgroundColorOpacity)
       m_sdkPlayer.setSubtitleWindowBackgroundOpacity(windowBackgroundColorOpacity);
       
      if(bEnableWindowBackgroundColor){
       nclr=windowBackgroundColor;
       clr = Color.argb(255, nclr&0x000000ff, (nclr&0x0000ff00)/256,nclr/256/256);
       m_sdkPlayer.setSubtitleWindowBackgroundColor(clr);}
       
      if(bEnableUderlineFont)
       m_sdkPlayer.setSubtitleFontUnderline(bSetUderlineFont);
       
     if( bEnableBoldFont)
       m_sdkPlayer.setSubtitleFontBold(bSetBoldFont);
       
     if(bEnableItalicFont)
      m_sdkPlayer.setSubtitleFontItalic(bSetItalicFont);
   }
    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        // TODO Auto-generated method stub
        boolean ret;
         String str = null;
         if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_SUBTITLESETTINGS_ID.getValue()))){
             ret=(Boolean) newValue;
             bEnableSubtitleStting=ret;
             initSubtitle();
         }
         if(preference.getKey().equals(1601)){
             
             ret=(Boolean) newValue;
             bEnableSubtitlePresrntation=ret;
         
          }
         else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_USEDEFAULTFONT_ID.getValue()))){
             
            ret=(Boolean) newValue;
            bUseDefaultSubtitleSetting=ret;
           if(ret){
              
                chbEnableFontSize.setChecked(false);
                bEnableFontSize=false;
                chbEnableFontColorOpacity.setChecked(false);
                bEnableFontColorOpacity=false;
                chbEnableFontColor.setChecked(false);
                bEnableFontColor=false;
                chbEnableBackgroundColorOpacity.setChecked(false);
                bEnableEdgeColorOpacity=false;
                chbEnableBackgroundColor.setChecked(false);
                bEnableBackgroundColor=false;
                chbEnableEdgeColorOpacity.setChecked(false);
                bEnableEdgeColorOpacity=false;
                chbEnableEdgeColor.setChecked(false);
                bEnableEdgeColor=false;
                chbEnableEdgeType.setChecked(false);
                bEnableEdgeType=false;
                chbEnableFontname.setChecked(false);
                bEnableFontname=false;
                chbEnableWindowBackgroundColorOpacity.setChecked(false);
                bEnableWindowBackgroundColorOpacity=false;
                chbEnableWindowBackgroundColor.setChecked(false);
                bEnableWindowBackgroundColor=false;
                chbEnableUderlineFont.setChecked(false);
                bEnableUderlineFont=false;
                chbEnableBoldFont.setChecked(false);
                bEnableBoldFont=false;
                chbEnableItalicFont.setChecked(false);
                bEnableItalicFont=false;
                m_sdkPlayer.resetSubtitleParameter();
            } 
           
         }
         else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_RESETTODEFAULTSET_ID.getValue()))){
             
             ret=(Boolean) newValue;
             bResetSubtitleSetting=ret;
             if(ret){
                 
                 
                 edtSetFontSize.setText("100");
                 fontSize=100;
                 edtSetFontColorOpacity.setText("100");
                 fontColorOpacity=100;
                 listSetFontColor.setValue("16777215");
                 fontColor=16777215;
                 edtSetBackgroundColorOpacity.setText("100");
                 backgroundColorOpacity=100;
                 listSetBackgroundColor.setValue("0");
                 backgroundColor=0;
                 edtSetEdgeColorOpacity.setText("100");
                 edgeColorOpacity=100;
                 listSetEdgeColor.setValue("16711680");
                 edgeColor=16711680;
                 listSetEdgeType.setValue("1");
                 edgeType=1;
                 listSetFontname.setValue("4");
                 fontName=4;
                 edtSetWindowBackgroundColorOpacity.setText("100");
                 windowBackgroundColorOpacity=100;
                 listSetWindowBackgroundColor.setValue("16777215");
                 windowBackgroundColor=16777215;
                 chbSetUderlineFont.setChecked(false);
                 bSetUderlineFont=false;
                 chbSetBoldFont.setChecked(false);
                 bSetBoldFont=false;
                 chbSetItalicFont.setChecked(false);
               
                 bSetItalicFont=false;
                 initSubtitle();
              
             }
            
         
          }
 else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTSIZE_ID.getValue()))){
             
             ret=(Boolean) newValue;
             bEnableFontSize=ret;
            
             initSubtitle();
          }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTSIZE_ID.getValue()))){
             str=(String) newValue;
             int num ;
             try{
                 num = Integer.parseInt(str);
                 if(num<50)
                     num=50;
                 if(num>200)
                     num=200;
                 fontSize=num;}
             catch(NumberFormatException e){
                 voLog.d(TAG, "Input invalid value");
                 e.printStackTrace();
             }
             
             initSubtitle();
        }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTCOLOROPACITY_ID.getValue()))){
            
            ret=(Boolean) newValue;
            bEnableFontColorOpacity=ret;
            initSubtitle();
         }
        else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTCOLOROPACITY_ID.getValue()))){
            str=(String) newValue;
            int num ;
            try{
                num = Integer.parseInt(str);
                if(num<50)
                    num=50;
                if(num>100)
                    num=100;
                fontColorOpacity=num;}
            catch(NumberFormatException e){
                voLog.d(TAG, "Input invalid value");
                e.printStackTrace();
            }
           
            initSubtitle();
     
      }
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_COLORLIST_ID.getValue()))){
            
            ret=(Boolean) newValue;
            bEnableFontColor=ret;
            initSubtitle();
      }
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETCOLORLIST_ID.getValue()))){
          str=(String) newValue;
          int nclr=Integer.parseInt(str);
         
          fontColor=nclr;
          
          initSubtitle();
   
    }
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BGCOLOROPACITY_ID.getValue()))){
      
      ret=(Boolean) newValue;
      bEnableBackgroundColorOpacity=ret;
      
      initSubtitle();
}    
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBGCOLOROPACITY_ID.getValue()))){
        str=(String) newValue;
        int num ;
        try{
            num = Integer.parseInt(str);
            if(num<0)
                num=0;
            if(num>100)
                num=100;
            backgroundColorOpacity=num;}
        catch(NumberFormatException e){
            voLog.d(TAG, "Input invalid value");
            e.printStackTrace();
        }
       
        initSubtitle();
 
  }
 else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BGCOLORLIST_ID.getValue()))){
      
      ret=(Boolean) newValue;
      bEnableBackgroundColor=ret;
      initSubtitle();
} 
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBGCOLORLIST_ID.getValue()))){
      str=(String) newValue;
      int nclr=Integer.parseInt(str);
     
      backgroundColor=nclr;
    
      initSubtitle();
} else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGECOLOROPACITY_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableEdgeColorOpacity=ret;
    initSubtitle();
} 
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGECOLOROPACITY_ID.getValue()))){
    str=(String) newValue;
    int num ;
    try{
        num = Integer.parseInt(str);
        if(num<0)
            num=0;
        if(num>100)
            num=100;
        edgeColorOpacity=num;}
    catch(NumberFormatException e){
        voLog.d(TAG, "Input invalid value");
        e.printStackTrace();
    }
    
    initSubtitle();
}else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGECOLORLIST_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableEdgeColor=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGECOLORLIST_ID.getValue()))){
    str=(String) newValue;
    int nclr=Integer.parseInt(str);
   
    edgeColor=nclr;
   
    initSubtitle();
}else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_EDGETYPELIST_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableEdgeType=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETEDGETYPELIST_ID.getValue()))){
    str=(String) newValue;
    edgeType=Integer.parseInt(str);
    
    initSubtitle();
}else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_FONTLIST_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableFontname=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETFONTLIST_ID.getValue()))){
    str=(String) newValue;
    int font=Integer.parseInt(str);
   
    fontName=font;
   
    initSubtitle();
}else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_WINBGCOLOROPACITY_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableWindowBackgroundColorOpacity=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETWINBGCOLOROPACITY_ID.getValue()))){
    str=(String) newValue;
    int num ;
    try{
        num = Integer.parseInt(str);
        if(num<0)
            num=0;
        if(num>100)
            num=100;
        windowBackgroundColorOpacity=num;}
    catch(NumberFormatException e){
        voLog.d(TAG, "Input invalid value");
        e.printStackTrace();
    }
   
    initSubtitle();
  }
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_WINBGCOLORLIST_ID.getValue()))){
      
      ret=(Boolean) newValue;
      bEnableWindowBackgroundColor=ret;
      initSubtitle();
  }
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETWINBGCOLORLIST_ID.getValue()))){
    str=(String) newValue;
    int nclr=Integer.parseInt(str);
   
    windowBackgroundColor=nclr;
    voLog.d(TAG, "windowBackgroundColor="+windowBackgroundColor);
    initSubtitle();

} else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_UNDERLINEFONT_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableUderlineFont=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETUNDERLINEFONT_ID.getValue()))){
    
   ret=(Boolean) newValue;
   bSetUderlineFont=ret;
   
   initSubtitle();

} else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_BOLDFONT_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableBoldFont=ret;
    
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETBOLDFONT_ID.getValue()))){
    
   ret=(Boolean) newValue;
   bSetBoldFont=ret;
  
   initSubtitle();

}else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_ITALICFONT_ID.getValue()))){
    
    ret=(Boolean) newValue;
    bEnableItalicFont=ret;
    initSubtitle();
}
  else if(preference.getKey().equals(String.valueOf(OPTION_ID.OPTION_ST_SETITALICFONT_ID.getValue()))){
    
   ret=(Boolean) newValue;
   bSetItalicFont=ret;
   voLog.d(TAG,"bSetItalicFont="+bSetItalicFont);
   
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
private void initPreferenceValue() throws NumberFormatException{
    bEnableSubtitleStting=chbEnableSubtitleSetting.isChecked();
    bEnableSubtitlePresrntation=chbEnableSubtitlePresrntation.isChecked();
    bUseDefaultSubtitleSetting=chbUseDefaultSubtitleSetting.isChecked();
    bResetSubtitleSetting=chbResetSubtitleSetting.isChecked();
    bEnableFontSize=chbEnableFontSize.isChecked();
    fontSize=Integer.parseInt(edtSetFontSize.getText());
    bEnableFontColorOpacity=chbEnableFontColorOpacity.isChecked();
    fontColorOpacity=Integer.parseInt(edtSetFontColorOpacity.getText());
    bEnableFontColor=chbEnableFontColor.isChecked();
    fontColor=Integer.parseInt(listSetFontColor.getValue());
    bEnableBackgroundColorOpacity=chbEnableBackgroundColorOpacity.isChecked();
    backgroundColorOpacity=Integer.parseInt(edtSetBackgroundColorOpacity.getText());
    bEnableBackgroundColor=chbEnableBackgroundColor.isChecked();
    backgroundColor=Integer.parseInt(listSetBackgroundColor.getValue());
    bEnableEdgeColorOpacity=chbEnableEdgeColorOpacity.isChecked();
    edgeColorOpacity=Integer.parseInt(edtSetEdgeColorOpacity.getText());
    bEnableEdgeColor=chbEnableEdgeColor.isChecked();
    edgeColor=Integer.parseInt(listSetEdgeColor.getValue());
    bEnableEdgeType=chbEnableEdgeType.isChecked();
    edgeType=Integer.parseInt(listSetEdgeType.getValue());
    bEnableFontname=chbEnableFontname.isChecked();
    fontName=Integer.parseInt(listSetFontname.getValue());
    bEnableWindowBackgroundColorOpacity=chbEnableWindowBackgroundColorOpacity.isChecked();
    windowBackgroundColorOpacity=Integer.parseInt(edtSetWindowBackgroundColorOpacity.getText());
    bEnableWindowBackgroundColor=chbEnableWindowBackgroundColor.isChecked();
    windowBackgroundColor=Integer.parseInt(listSetWindowBackgroundColor.getValue());
    bEnableUderlineFont=chbEnableUderlineFont.isChecked();
    bSetUderlineFont=chbSetUderlineFont.isChecked();
    bEnableBoldFont=chbEnableBoldFont.isChecked();
    bSetBoldFont=chbSetBoldFont.isChecked();
    bEnableItalicFont=chbEnableItalicFont.isChecked();
    bSetItalicFont=chbSetItalicFont.isChecked();
}


private void addPreview(){

    Preference pf = new Preference(this){
        
        @Override 
           protected  void  onBindView(View view) { 
               super.onBindView(view); 
        
               // Set our custom views inside the layout
               textView = (TextView) view.findViewById(inttextView); 
               if  (textView != null ) { 
                 
                   m_sdkPlayer.previewSubtitle("Sample",textView );
                   initSubtitle();
                  
                   
               } 
           } 
       };
       pf.setKey("key093039983");
       pf.setTitle("PreviewSubtitle");
       pf.setLayoutResource(intpreviewLayout);
                 
       inlinePrefCat.addPreference(pf); 
}

public boolean onKeyDown(int keyCode, KeyEvent event) {
    voLog.v(TAG, "Key click is " + keyCode);

    if (keyCode ==KeyEvent.KEYCODE_BACK) {
        voLog.v(TAG, "Key click is Back key");
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
private void addDownloadAlertdialog(OptionItem item){
    DeleteDialogPreference deleteDialogPref = new DeleteDialogPreference(this,attrs);
    
    deleteDialogPref.setDialogTitle(item.getTitle());
    deleteDialogPref.setKey(item.getTitle());
    deleteDialogPref.setTitle(item.getTitle());
    deleteDialogPref.setSummary(item.getDescription());
    inlinePrefCat.addPreference(deleteDialogPref);
    
    m_listDownloadInfo =new ListView(this);
    m_listDownloadInfo.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
    m_listDownloadInfo.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
    readDownloadInfo();
    
    adlgDelete = new AlertDialog.Builder(this)
    .setTitle("Delete download file.").setView(m_listDownloadInfo)
    .setPositiveButton("Delete", new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
          int  i=m_listDownloadInfo.getCheckedItemPosition();
          String deleteUrl=null;
          if(i>=0 &&m_downloadList.size()>0)
            deleteUrl=m_downloadList.get(i);
          readDownloadInfo();
        }
    }).create();

}

private void readDownloadInfo(){
    
    m_downloadList=null;
    m_downloadList=new ArrayList<String>();
    getFiles(m_downloadList,DOWNLOAD_PATH);
    ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
           singleChoiceLayout , m_downloadList);
    m_listDownloadInfo.setAdapter(adapter);
    
}
private void getFiles(ArrayList<String> list,String url) {
    File files = new File(url); 
    File[] file = files.listFiles();
    if(file==null){
        return;
    }
    try {
        for (File f : file) { 
        if (f.isDirectory()) { 
           File[] videoFile=f.listFiles();
           int i=0;
           for(File vf: videoFile){
               if(vf.getPath().contains("Master.m3u8")){
                   list.add(vf.getPath());
                   i=1;
               }
           }
           if(i==0){
               for(File vf:videoFile){
                   if(vf.getPath().contains("Video.m3u8"))
                       list.add(vf.getPath());
               }
           }
            }
        }
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
