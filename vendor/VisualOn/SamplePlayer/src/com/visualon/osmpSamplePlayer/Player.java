/************************************************************************
VisualOn Proprietary
Copyright (c) 2013, VisualOn Incorporated. All rights Reserved

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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.LayoutParams;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;

import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.visualon.OSMPPlayer.VOCommonPlayer;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection;
import com.visualon.OSMPPlayer.VOOSMPInitParam;
import com.visualon.OSMPPlayer.VOOSMPOpenParam;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetIndex;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOCommonPlayerListener;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_CB_EVENT_ID;
import com.visualon.OSMPPlayer.VOCommonPlayerListener.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT;
import com.visualon.OSMPPlayer.VOOSMPType.*;
import com.visualon.OSMPPlayer.VOOSMPVerificationInfo;
import com.visualon.OSMPPlayerImpl.VOCommonPlayerImpl;
import com.visualon.OSMPPlayer.*;
import com.visualon.OSMPUtils.voLog;
import com.visualon.appConfig.AppBehaviorManager;
import com.visualon.appConfig.AppBehaviorManagerDelegate;
import com.visualon.appConfig.AppBehaviorManagerImpl;
import com.visualon.appConfig.AppBehaviorManager.APP_BEHAVIOR_EVENT_ID;
import com.visualon.appConfig.AppBehaviorManagerImpl.OPTION_ID;


/* Activity implement SurfaceHolder.Callback */
public class Player extends Activity
{
    private static final String  TAG                  = "@@@OSMP+Player"; // Tag for VOLog messages

    /* Messages for managing the user interface */
    private static final int     MSG_SHOW_CONTROLLER  = 1;
    private static final int     MSG_HIDE_CONTROLLER  = 2;
    private static final int     MSG_UPDATE_UI        = 3;
    private static final int     MSG_PLAYCOMPLETE     = 5;
    private static final int     MSG_STARTPLAY        = 10;
    
   /* SurfaceView must be passed to SDK */
    private SurfaceView          m_svMain             = null;
    private SurfaceHolder        m_shMain             = null;

    /* Media controls and User interface */
    private SeekBar              m_sbMain             = null;            // Seekbar
    private TextView             m_tvCurrentTime      = null;            // Current position
    private TextView             m_tvTotalTime        = null;            // Total duration
    private TextView                        m_tvBps;
    private TextView                        m_tvResolutionW;
    private TextView                        m_tvResolutionH;
    private ImageButton          m_ibPlayPause        = null;            // Play/Pause button
    private ProgressBar          m_pbLoadingProgress  = null;            // Wait icon for buffered or stopped video
    private TextView             m_tvLoadingProgress  = null;            // PD buffering percent 
    private Date                 m_dateUIDisplayStartTime = null;        // Last update of media controls

    private Timer                m_timerMain          = null;            // Timer for display of media controls
    private TimerTask            m_ttMain             = null;            // Timer Task for display of media controls

    private VOCommonPlayer       m_sdkPlayer          = null;            // SDK player
    
    private ImageButton          m_ibDolby            = null;            //Dolby button
    private ImageButton          m_ibHighSpeed        = null;            //Audio Speed button
    private ImageButton          m_ibLowSpeed         = null;
    
    private RelativeLayout          m_rlTop              = null;
    private RelativeLayout       m_rlBottom           = null;
    private RelativeLayout       m_rlRight            = null;      // reserved
    private HorizontalScrollView m_hsvRight           = null;
    private LinearLayout         m_llRight            = null;
    private LinearLayout         m_llSubtitlePopupMenu= null;
    private LinearLayout         m_llVideoPopupMenu   = null;
    private LinearLayout         m_llAudioPopupMenu   = null;
    private HorizontalScrollView m_hsvSubtitlePopupMenu = null;
    private HorizontalScrollView m_hsvVideoPopupMenu  = null; 
    private HorizontalScrollView m_hsvAudioPopupMenu  = null;
    private RelativeLayout       m_rlProgramInfo      = null;
    private RelativeLayout       m_rlProgramInfoArrow = null;
    private TextView             m_tvCommit           = null;
    private TextView             m_tvRevert           = null;
    private GestureDetector      m_gdMain             = null;
    private RelativeLayout       m_rlChannel          = null;
    private ListView             m_lvChannel          = null;

    /* Flags */
    private boolean              m_bStartOption       =false;
    private boolean             isSelect              = false;
    private boolean              isCodecSupport       = true;
    private boolean              m_appStop            = false;
    private boolean              m_bOnError           = false;           //Error flag
    private boolean              m_bTrackProgressing  = false;           // Seekbar flag
    private boolean              m_bPaused            = false;           // Pause flag
    private boolean              m_bStoped            = false;           // Stop flag
    private boolean				 m_bFullScreen		  = false;           // Full screen flag
    
    private boolean              enableOpenmax        = false;      //HW auto test flag
    private boolean              enableIOMX           = false;
    
    
   
    private boolean              m_bShowServerIP_Port = false;   
    private boolean              m_bDrmVerimatrix 	  = false;   
    
    
    private boolean              m_bEnableChannel     = true;
    private boolean              m_bShowDolby         = false;
    private boolean              m_bEnableDolby       = false;      
    private boolean              m_bShowAudioSpeed    = true;
    
    private boolean              m_bEnableVideo       = true; // BpsQuality 
    private boolean              m_bEnableAudio       = true; // AudioTrack
    private boolean              m_bEnableSubtitle    = true; // External/Internal Subtitle or Closed Caption
    private boolean              m_isResume                = false;
    private boolean              m_nAsyncOpen              = true;
    /*Global Variables*/
    private String               m_strVideoPath       = "";              // URL or file path to media source
   

    private int                  m_nVideoWidth        = 0;               // Video width
    private int                  m_nVideoHeight       = 0;               // video height
    private int                  m_nDuration          = 0;               // Total duration
    private int                  m_nPos               = 0;               // Current position
    private VO_OSMP_PLAYER_ENGINE m_eEngineType       = VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;
    private VO_OSMP_DECODER_TYPE                  m_videoDecoderType   = null;
    private VO_OSMP_DECODER_TYPE                  m_audioDecoderType   = null;
    private int                  m_nZoom              = 0;        //0:disable, 1:ZoomIn, 2: Origiral
    private float                m_fAudioSpeed        = 1.0f;
    private VO_OSMP_ASPECT_RATIO  m_nAspectRatio      = VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO;
    
    /* User interface for main view (media source input/selection) */
    private EditText             m_edtInputURL        = null;            // User input URL or file path
    private Dialog          m_adlgMain           = null;            // Dialog for media source
    private Button             m_bSelectURL        = null;            // Media source list selector
    private ArrayList<String>    m_lstSelectURL       = null;            // Media source list from url.txt
    private AlertDialog         m_adlgURL             = null;        
   
    private EditText             m_edtVCASIP          = null;
    private EditText             m_edtVCASPort        = null;
    private Button               m_bOption            = null;
    private Button               m_bStart             = null;
	 /*Bps,Audio,Subtitle tracks feature*/
    private int                  m_nSelectedVideoIndex        = VOCommonPlayerAssetSelection.VO_OSMP_ASSET_AUTO_SELECTED;  // index of selected video, 0 is Auto 
    private int                  m_nSelectedAudioIndex        = 0;
    private int                  m_nSelectedSubtitleIndex     = 0;
    private int                  m_nSelectedVideoPreIndex     = VOCommonPlayerAssetSelection.VO_OSMP_ASSET_AUTO_SELECTED;  // index of playing video, -1 is Auto. 
    private int                  m_nSelectedAudioPreIndex     = 0;
    private int                  m_nSelectedSubtitlePreIndex  = 0;
    
    private int[]                m_AudioIndex                 = new int[100];
    
    private int					 m_FastChannelTimes           = 0;
    
    private static final String  STRING_ASSETPROPERTYNAME_VIDEO         = "V";      //String description for Bps ,Audio,Subtitile tracks.
    private static final String  STRING_ASSETPROPERTYNAME_AUDIO         = "A";
    private static final String  STRING_ASSETPROPERTYNAME_SUBTITLE      = "Subt";
  
    private AppBehaviorManager   abManager                 = null;
    private  VO_OSMP_RENDER_TYPE m_nRenderType             = null; 
    private boolean              m_bReturnSourceWindow           = false;
    private BroadcastReceiver      mInfoReceiver                        = null;
    
    private VOCommonPlayerAssetSelection m_asset           = null;
    private static final String  DOWNLOAD_PATH             = "/sdcard/osmp";
    private AlertDialog          m_adlgDownload           = null;
    private VOOSMPOpenParam openParam                     =null;
    private boolean              m_isDownloadOpenFailed    = false;
    private static final String[] correctFormatSet = new String[] { "m3u8"}; 
    private int                  m_downloadCurrent         = 0;
    private int                  m_downloadTotal           = 0;
    private TextView             m_tvDownloadCurrent;
    private TextView             m_tvDownloadTotal;
   
    private ListView             lvURL                     = null;
    
    ArrayList<String>            lstVideo                  = null;
    private enum AssetType {
        Asset_Video,          // BpsQuality 
		Asset_Audio,          // AudioTrack 
		Asset_Subtitle        // External/Internal Subtitle or CloseCaption
    }
    
    private SurfaceHolder.Callback m_cbSurfaceHolder = new SurfaceHolder.Callback() {
        /* Notify SDK on Surface Change */  
        public void surfaceChanged (SurfaceHolder surfaceholder, int format, int w, int h) {
            voLog.i(TAG, "Surface Changed");
            if (m_sdkPlayer != null)
                m_sdkPlayer.setSurfaceChangeFinished();
        }

        /* Notify SDK on Surface Creation */  
        public void surfaceCreated(SurfaceHolder surfaceholder) {
            voLog.i(TAG, "Surface Created");
            
            if(m_bStoped){
                m_bReturnSourceWindow = false;
                return;
            }
            
            if (m_sdkPlayer !=null && m_isResume) {
                // For handling the situation such as phone calling is coming.
               
                // If SDK player already exists, show media controls
                m_sdkPlayer.resume(m_svMain);
                m_isResume = false;
                return;
            }
            
            if ((m_strVideoPath == null) || (m_strVideoPath.trim().length() <=0))
                return;
            if(m_bStartOption)
                return;
            // Enter from the other APP such as a browser or file explorer
            initPlayer(m_strVideoPath);
            playVideo(m_strVideoPath);
        }
      
        public void surfaceDestroyed(SurfaceHolder surfaceholder) {
            voLog.i(TAG, "Surface Destroyed");
            
            if (m_sdkPlayer == null)
                return;
            
            m_sdkPlayer.setView(null);
        }
    };
    
    private VOCommonPlayerListener m_listenerEvent = new VOCommonPlayerListener() {
        /* SDK event handling */
     
        @SuppressWarnings("incomplete-switch")
        public VO_OSMP_RETURN_CODE onVOEvent(VO_OSMP_CB_EVENT_ID nID, int nParam1, int nParam2, Object obj) {
            int rc = 0;
            switch(nID) { 
            case VO_OSMP_CB_VIDEO_ASPECT_RATIO:
    	    { abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
    	    	m_nAspectRatio = VO_OSMP_ASPECT_RATIO.valueOf(nParam1);
    	    	
				break;					
    	    }
           
            case VO_OSMP_CB_ERROR:  
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL:
            case VO_OSMP_SRC_CB_DRM_FAIL:
            case VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR:
            case VO_OSMP_SRC_CB_CONNECTION_REJECTED:
            case VO_OSMP_SRC_CB_DRM_NOT_SECURE:
            case VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL:
            case VO_OSMP_SRC_CB_CONNECTION_TIMEOUT:
            case VO_OSMP_SRC_CB_CONNECTION_LOSS:
            case VO_OSMP_CB_LICENSE_FAIL: {  // Error
                // Display error dialog and stop player
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                break;
            }
            case VO_OSMP_SRC_CB_CONNECTING:
            case VO_OSMP_SRC_CB_CONNECTION_FINISHED:
            case VO_OSMP_SRC_CB_DOWNLOAD_STATUS:
            case VO_OSMP_CB_SEEK_COMPLETE:
            case VO_OSMP_CB_DEBLOCK:
            case VO_OSMP_SRC_CB_BA_HAPPENED:
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_WAITING_RECOVER:
            case VO_OSMP_SRC_CB_DOWNLOAD_FAIL_RECOVER_SUCCESS:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                break;
            case VO_OSMP_CB_PLAY_COMPLETE: {
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                handler.sendEmptyMessage(MSG_PLAYCOMPLETE);
                break;
            }   
           
            case VO_OSMP_SRC_CB_SEEK_COMPLETE : {   // Seek (SetPos) complete
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                TimeCal.printTime("Receive Source Seek Complete <---");
                break;
            }
            case VO_OSMP_CB_VIDEO_RENDER_START : { 
                TimeCal.printTime("Receive VideoRenderStart <---");
                break;
            }
            case VO_OSMP_CB_VIDEO_SIZE_CHANGED: {   // Video size changed
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                m_nVideoWidth = nParam1;
                m_nVideoHeight = nParam2;
                m_tvResolutionW.setText(Integer.toString(m_nVideoWidth));
                m_tvResolutionH.setText(Integer.toString(m_nVideoHeight));
                
                // Retrieve new display metrics
                DisplayMetrics dm  = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(dm);
                
    			if (m_nZoom == 1)
    			{
    				doPanScan();
    			}else if(m_nZoom == 2)
    			{
    				endPanScan();
    			}
    			
                
                break;
            }   
            case VO_OSMP_CB_VIDEO_STOP_BUFFER:
            case VO_OSMP_CB_AUDIO_STOP_BUFFER:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                m_pbLoadingProgress.setVisibility(View.INVISIBLE);
                m_tvLoadingProgress.setVisibility(View.INVISIBLE);
                break;
                
            case VO_OSMP_CB_VIDEO_START_BUFFER:
            case VO_OSMP_CB_AUDIO_START_BUFFER:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                m_pbLoadingProgress.setVisibility(View.VISIBLE);
                break;
            
            
            case VO_OSMP_SRC_CB_OPEN_FINISHED: {
                if (m_nAsyncOpen)
                {
                
                if (nParam1 == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE.getValue())  {
                    abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                        
                    VO_OSMP_RETURN_CODE nRet;
                    m_asset = m_sdkPlayer;   
                    /* Run (play) media pipeline */
                    TimeCal.printTime("Start --->");
                    nRet = m_sdkPlayer.start();
                    TimeCal.printTime("Start <---");
                    rc = abManager.processReturnCode("Player start", nRet.getValue());
                    if (rc == 1)
                    {
                        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
                    }
                   
                        
                    m_nPos = (int) m_sdkPlayer.getPosition();
                    m_nDuration = (int) m_sdkPlayer.getDuration();
                            
                    updatePosDur();
                    fillProgramInfo();   
                    m_bStoped = false;
                    m_ibPlayPause.setImageResource(R.drawable.selector_btn_pause);
                    //m_sdkPlayer.setInitialBitrate(600000);
                        
                }else {
                    abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                }
                } 
                break;
            }
            case VO_OSMP_SRC_CB_NOT_APPLICABLE_MEDIA:
            	Toast.makeText(getApplicationContext(), "Network is not available now",
            		     Toast.LENGTH_SHORT).show();
            	break;
            case VO_OSMP_CB_SEI_INFO:
            {
            	if (nParam1 == VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_PIC_TIMING.getValue())
            	{
            		if (obj != null)
            		{
            			VOOSMPSEIPicTiming timing = (VOOSMPSEIPicTiming)obj;
            			voLog.v(TAG, "OnEvent VO_OSMP_CB_SEI_INFO VO_OSMP_SEI_INFO_PIC_TIMING");
            		}
            	}
            	
            	break;
            }
            case VO_OSMP_SRC_CB_PD_DOWNLOAD_POSITION:
            {
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
            	if(nParam1>=99){
            	    m_sbMain.setSecondaryProgress(100);
            	}else{
            	m_sbMain.setSecondaryProgress(nParam1);
            	}
            	break;
            }
            
            case VO_OSMP_SRC_CB_PD_BUFFERING_PERCENT:
            {
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                if(nParam1>=99){
                    m_pbLoadingProgress.setVisibility(View.GONE);
                    m_tvLoadingProgress.setVisibility(View.GONE);
                
                }else{
                    m_pbLoadingProgress.setVisibility(View.VISIBLE);
                    m_tvLoadingProgress.setVisibility(View.VISIBLE);
                    m_tvLoadingProgress.setText(Integer.toString(nParam1)+"%");
                }
            	
            	break;
            }
            
            case  VO_OSMP_CB_ANALYTICS_INFO:
            {
                abManager.processEvent(nID.getValue(), nParam1, nParam2, obj);
            	break;
            }
			
            case VO_OSMP_CB_CODEC_NOT_SUPPORT:
                
                if((m_videoDecoderType == VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW)&&(m_audioDecoderType == VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW)
                        &&(m_eEngineType==VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER))
                {
                    abManager.processEvent(0X8A000001 , nParam1, nParam2, null);
                }
                else
                { if(isCodecSupport){
                    voLog.d(TAG,"processEvent(VO_OSMP_CB_CODEC_NOT_SUPPORT)");
                    isCodecSupport=false;
                    abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                    
                }
                    
                }
                break;
			   case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_INFO:
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_ERROR:
            case VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_WARNING:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                break; 
                
            case VO_OSMP_CB_HW_DECODER_STATUS:
                rc = abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                if (rc == 0)
                {
                   stopVideo();
                   uninitPlayer();
                    playVideo(m_strVideoPath);
                }
                break;
            case VO_OSMP_SRC_CB_RTSP_ERROR:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                 break;   
            case VO_OSMP_SRC_CB_PROGRAM_CHANGED:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                m_sdkPlayer.getAudioProperty(0);
                break;
                
            case VO_OSMP_SRC_CB_PROGRAM_RESET:
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
                m_sdkPlayer.getAudioProperty(0);
                break;
            default :
                abManager.processEvent(nID.getValue(), nParam1, nParam2, null);
               break;
            
        }
    
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;       
        }
        
		@Override
		public VO_OSMP_RETURN_CODE onVOSyncEvent(VO_OSMP_CB_SYNC_EVENT_ID arg0,
				int arg1, int arg2, Object arg3) {
			// TODO Auto-generated method stub
			
//			switch(arg0) {
//	          case VO_OSMP_SRC_CB_SYNC_AUTHENTICATION_DRM_SERVER_INFO : {
//	        	VOOSMPVerificationInfo info = new VOOSMPVerificationInfo();
//	            info.setDataFlag(1);
//	    		String s = "0.0.0.0:80";
//	    		if(m_edtVCASIP !=null && m_edtVCASPort!= null){
//	    			if(m_edtVCASIP.getText().toString().length()>0 
//	    					&&m_edtVCASPort.getText().toString().length()>0)
//	    				s = m_edtVCASIP.getText().toString()+":"+m_edtVCASPort.getText().toString();
//	    		}
//	
//	            info.setVerificationString(s); //Please set VCAS server ip address or domain name here
//	            m_sdkPlayer.setDRMVerificationInfo(info);
//	            voLog.i(TAG, "VO_OSMP_SRC_CB_DRM_VERIFICATION, drm callback, ");
//	            
//	            break;
//	          }
//			}
			return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
		}
    };
    
    private OnSeekBarChangeListener m_listenerSeekBar = new OnSeekBarChangeListener() {
        
        /* Seek to new position when Seekbar drag is complete */
        public void onStopTrackingTouch(SeekBar arg0) {
            int nCurrent = arg0.getProgress();
            int nMax = arg0.getMax();
            if (m_sdkPlayer != null){
            m_bTrackProgressing = false;
            int max=(int)m_sdkPlayer.getMaxPosition();
            int min=(int)m_sdkPlayer.getMinPosition();
            m_nDuration = ( max-min);
            if(m_nDuration<=0)
                m_nDuration = (int)m_sdkPlayer.getDuration();
            
            int lNewPosition = (int)nCurrent * (m_nDuration) / nMax + min;
            voLog.v(TAG, "Seek to  nCurrent = "+nCurrent+"  max = " + m_sdkPlayer.getMaxPosition() + " min = " + m_sdkPlayer.getMinPosition()
            		 + "  nMax = " + nMax);
            if (m_sdkPlayer != null) {
                voLog.v(TAG,"Seek To " + lNewPosition);
                TimeCal.printTime("SetPos --->");
            	//m_skipUpdate = 4;
                
                VOOSMPSEIPicTiming timing = m_sdkPlayer.getSEIInfo(m_sdkPlayer.getPosition(), VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_PIC_TIMING);
                if (timing == null)
                {
                	voLog.v(TAG, "getSEIInfo SEI INFO return is null"); 
                	
                }else
                	voLog.v(TAG, "getSEIInfo SEI INFO successful.");

                m_sdkPlayer.setPosition((long)lNewPosition);  // Set new position
                TimeCal.printTime("SetPos <---");
                
            }
            }
 
        }
        
        /* Flag when Seekbar is being dragged */
        public void onStartTrackingTouch(SeekBar arg0) {
            m_bTrackProgressing = true;
        }
        
        public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {}
    };

	private Handler handler = new Handler() {
		/* Handler to manage user interface during playback */
		public void handleMessage(Message msg)
		{
		    if (msg.what == MSG_STARTPLAY) {
                initPlayer(m_strVideoPath);
                playVideo(m_strVideoPath);
                return;
            }
            
			if(m_sdkPlayer == null)
				return;
			
			if (msg.what == MSG_SHOW_CONTROLLER) {
				/* Show media controls */
				showMediaControllerImpl();
			} else if (msg.what == MSG_HIDE_CONTROLLER) {
				/* Hide media controls */
				hideControllerImpl();
			} else if (msg.what == MSG_UPDATE_UI) {
				// Update UI
			    doUpdateUI();
			  
			} else if (msg.what == MSG_PLAYCOMPLETE) {
			  /* Playback in complete, stop player */
			    if(abManager.getOptionItemByID(OPTION_ID.OPTION_LOOP_ID.getValue()).getSelect()==0)
                {
                    voLog.v(TAG, "Seek to , don't loop.");   
			   stopVideo();
			   uninitPlayer();
			   m_bReturnSourceWindow=true;
	           SourceWindow();

                }else
                {
                    m_sdkPlayer.setPosition(0);
                    voLog.v(TAG, "Seek to 0, after play completed, loop.");
                 }
			}
		}
	};
	 private void AppBehaviorPopMsg(String title, String str) {
	        // TODO Auto-generated method stub
	        AlertDialog ad = new AlertDialog.Builder(Player.this)
	        .setIcon(R.drawable.icon)
	        .setTitle(title)
	        .setMessage(str)
	        .setOnKeyListener(new OnKeyListener()
	            {
	                public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) 
	                {
	                    if (arg1 == KeyEvent.KEYCODE_BACK) 
	                    { 
	                        // do something on back. 
	                        arg0.dismiss();
	                        finish();
	                    } 

	                    return false;
	                }
	            })
	            .setPositiveButton(R.string.str_OK, new OnClickListener() 
	                {
	                    public void onClick(DialogInterface a0, int a1)
	                    {
	                        if(m_appStop){
	                          
	                            m_bReturnSourceWindow=true;
	                            SourceWindow();     
	                       
	                        }
	                    }
	                }).create();
	        ad.setCanceledOnTouchOutside(false);
	            ad.show();
	    }
	  AppBehaviorManagerDelegate abManagerDelegate = new AppBehaviorManagerDelegate() {
	        
	        @Override
	        public VO_OSMP_RETURN_CODE handleBehaviorEvent(APP_BEHAVIOR_EVENT_ID nID, String str) {
	            switch(nID)
	            {
	            case APP_BEHAVIOR_STOP_PLAY:
	                m_appStop=true;
	                stopVideo();
	                uninitPlayer();
	                AppBehaviorPopMsg("Error",str);

	                break;
	                
	            case APP_BEHAVIOR_CONTINUE_PLAY:
	                m_appStop=false;
	                AppBehaviorPopMsg("Warning",str);
	                break;
	                
	            case APP_BEHAVIOR_PAUSE_PLAY:
	                m_appStop=false;
	                m_sdkPlayer.pause();
	                showMediaController();
	                m_ibPlayPause.setImageResource(R.drawable.selector_btn_play);
	                m_bPaused = true;
	                AppBehaviorPopMsg("Warning",str);
	                break;
	            
	        case APP_BEHAVIOR_SWITCH_ENGINE:
	            stopVideo();
                uninitPlayer();
                AlertDialog ad = new AlertDialog.Builder(Player.this)
                .setIcon(R.drawable.icon)
                .setTitle("Warning")
                .setMessage(str)
                .setOnKeyListener(new OnKeyListener()
                    {
                        public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) 
                        {
                            if (arg1 == KeyEvent.KEYCODE_BACK) 
                            { 
                                // do something on back. 
                                finish();
                            } 

                            return false;
                        }
                    })
                    .setPositiveButton(R.string.str_OK, new OnClickListener() 
                        {
                            public void onClick(DialogInterface a0, int a1)
                            {
                               
                               
                            m_videoDecoderType = VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW;
                            m_audioDecoderType = VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW;
                            initPlayer(m_strVideoPath);
                            playVideo(m_strVideoPath);
                            a0.dismiss();
                                }
                        })
                        .create();
                ad.setCanceledOnTouchOutside(false);
                        ad.show();
                        break;
                    }
	        
	            return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
	        }
	    };

	/** Called when the activity is first created. */
	//@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
	public void onCreate(Bundle savedInstanceState) {
	
		super.onCreate(savedInstanceState);
		voLog.v(TAG, "Player onCreate");

		/*Screen always on*/
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		CommonFunc.copyfile(this, "appcfg.xml", "appcfg.xml");
		setContentView(R.layout.player);
		abManager = new AppBehaviorManagerImpl(this);
        abManager.init(abManagerDelegate);
        String filePath = CommonFunc.getUserPath(this) + "/" + "appcfg.xml";
        abManager.loadCfgFile(filePath);
		
		init();  
		String downloadPath=DOWNLOAD_PATH;
        File downloadFilePath=new File(downloadPath);
        boolean ret=false;;
        if(!downloadFilePath.exists())
            ret= downloadFilePath.mkdir();
		m_gdMain = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener(){

            public boolean onDoubleTap(MotionEvent e) 
            {
				voLog.e("onDoubleTap", e.toString());
				
                if (m_bFullScreen == false) 	
                	doFullScreen();
                else					
                	endFullScreen();
                
                return true;
            }

//            public boolean onSingleTapConfirmed(MotionEvent e) {
//                voLog.e("onSingleTapConfirmed", e.toString());
//        	    if (e.getAction() == MotionEvent.ACTION_DOWN) {
//		              if (m_rlBottom.getVisibility() != View.VISIBLE) {
//		                  showMediaControllerImpl();
//		              } else {
//		                  hideControllerImpl();
//		              }
//		  		}
//                return true;
//            }
        });

		
		Uri uri = getIntent().getData();
		enableIOMX=getIntent().getBooleanExtra("isIOMX",false);
		enableOpenmax=getIntent().getBooleanExtra("isOpenMax",false);
		
		if (uri != null) {
		// If media source was passed as URI data, use it
              if(uri.toString().startsWith("content"))//change image uri to file path
		    {
		        String[] proj = { MediaStore.Video.Media.DATA };   
		        Cursor actualimagecursor = managedQuery(uri,proj,null,null,null);  
		        int actual_image_column_index = actualimagecursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);   
		        actualimagecursor.moveToFirst();   

		       String img_path = actualimagecursor.getString(actual_image_column_index);  
		       File file = new File(img_path);
		       uri = Uri.fromFile(file);
		    }
			m_strVideoPath = uri.getPath();

			int i = m_strVideoPath.indexOf("/mnt/");
			if (i != -1) {
				//local file
				m_strVideoPath = m_strVideoPath.subSequence(i, m_strVideoPath.length()).toString();
			} else {
				//stream
				m_strVideoPath = uri.toString();
			}

			voLog.v(TAG, "Source URI: " + m_strVideoPath);
		} else {
			// Else bring up main view to input/select media source
			SourceWindow();
		}

		getWindow().setFormat(PixelFormat.UNKNOWN);

		// Find View and UI objects
		m_svMain = (SurfaceView) findViewById(R.id.svMain);
		m_shMain = m_svMain.getHolder();
		m_shMain.addCallback(m_cbSurfaceHolder);
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
		if (Build.VERSION.SDK_INT >=17)
		{
			//m_svMain.setSecure(true);
			voLog.i(TAG, "setSecure(true) , add screenshots secure. sdk level is %d",  Build.VERSION.SDK_INT);
		}else
		{
			voLog.i(TAG, "Do not support setSecure function,  screenshots secure. sdk level is %d",  Build.VERSION.SDK_INT);
		}
		if(abManager.getOptionItemByID(OPTION_ID.OPTION_COLORTYPE_ID.getValue()).getSelect()==1)
        {
            m_shMain.setFormat(PixelFormat.RGBA_8888);
        }else
        {
            m_shMain.setFormat(PixelFormat.RGB_565);
        }
		
		
		m_ibPlayPause = (ImageButton) findViewById(R.id.ibPlayPause); 

		m_sbMain = (SeekBar) findViewById(R.id.sbMain);
		m_tvBps         = (TextView) findViewById(R.id.tvBps);
	    m_tvResolutionW = (TextView) findViewById(R.id.tvResolutionW);
	    m_tvResolutionH = (TextView) findViewById(R.id.tvResolutionH);
		m_tvCurrentTime = (TextView)findViewById(R.id.tvCurrentTime);
		m_tvTotalTime = (TextView)findViewById(R.id.tvTotalTime); 
		m_pbLoadingProgress = (ProgressBar) findViewById(R.id.pbBuffer);
        m_tvLoadingProgress=(TextView)findViewById(R.id.tvBufferValue);
 		m_sbMain.setOnSeekBarChangeListener(m_listenerSeekBar);

		voLog.v(TAG, "Video source is " + m_strVideoPath);

		m_rlTop.setVisibility(View.INVISIBLE);
		m_rlBottom.setVisibility(View.INVISIBLE);
		m_pbLoadingProgress.setVisibility(View.GONE);
		m_tvLoadingProgress.setVisibility(View.GONE);

		// Activate listener for Play/Pause button
		m_ibPlayPause.setOnClickListener(new ImageButton.OnClickListener() {
		    public void onClick(View view) {
		        playerPauseRestart();
			}
		});
		CommonFunc.copyfile(this, "voVidDec.dat", "voVidDec.dat");
		CommonFunc.copyfile(this, "cap.xml", "cap.xml");
		registerLockReceiver();
		fillProgramInfo();
	}
	private void registerLockReceiver() {
        final IntentFilter filter = new IntentFilter(); 
        filter.addAction(Intent.ACTION_USER_PRESENT);   
            
      mInfoReceiver = new BroadcastReceiver() {    
            @Override    
            public void onReceive(final Context context, final Intent intent) {  
                  
  
                String action = intent.getAction();    
  
                  
               if(Intent.ACTION_USER_PRESENT.equals(action))  
               {  
                   if(m_sdkPlayer != null && m_isResume)
                   {
                       m_sdkPlayer.resume(m_svMain);
                       m_isResume = false;
                   }
               }  
                 
            }    
        };    
          
        registerReceiver(mInfoReceiver, filter);  
    }

    /* Interface to input/select media source */
	private void SourceWindow() {
	   if(!m_bReturnSourceWindow || m_adlgMain==null){
	    LayoutInflater inflater;
		View layout;
		inflater = LayoutInflater.from(Player.this);
		layout = inflater.inflate(R.layout.url,null );
		
		m_edtInputURL = (EditText)layout.findViewById(R.id.edtInputURL);
	    m_bSelectURL = (Button)layout.findViewById(R.id.bSelectURL);
	   
	  
	    
	   
		m_edtVCASIP		= (EditText)layout.findViewById(R.id.editTextIP);
		m_edtVCASPort	= (EditText)layout.findViewById(R.id.editTextPort);
		m_bStart        = (Button)layout.findViewById(R.id.bStart);
        m_bOption       =(Button)layout.findViewById(R.id.bOption);
       
        m_bOption.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Intent intent = new Intent();
                intent.setClass(Player.this, AppBehaviorManagerImpl.class);
                intent.putExtra("textView", R.id.privew_text);
                intent.putExtra("previewLayout", R.layout.preview);
                intent.putExtra("singleChoiceLayout", R.layout.simple_list_item_single_choice);
                
                startActivity(intent); 
                m_bStartOption=true;
            }
        });
       
        if(!m_bShowServerIP_Port){
			RelativeLayout rl3		= (RelativeLayout)layout.findViewById(R.id.rlOptions3);
			if(rl3!=null)
				rl3.setVisibility(View.GONE);
			m_edtVCASIP.setVisibility(View.GONE);
			m_edtVCASPort.setVisibility(View.GONE);
		}

	   
		// Dialog to input source URL or file path
		m_adlgMain = new Dialog(Player.this,R.style.Dialog_Fullscreen);
						
		m_adlgMain.setContentView(layout);
		m_adlgMain.setOnKeyListener(new OnKeyListener(){

            @Override
            public boolean onKey(DialogInterface dialog, int keyCode,
                    KeyEvent event) {
                // TODO Auto-generated method stub
                if (keyCode == KeyEvent.KEYCODE_BACK) { 
                 
                    stopAndFinish();
                        
                    return true;
                } 
                return false;
            }
		    
		});
						
   
		
		// Display media source input and selection options
	
		
		// Retrieve URL list
	
	    lvURL =new ListView(Player.this);
        lvURL.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT));
        
        lvURL.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                // TODO Auto-generated method stub
                String url=arg0.getItemAtPosition(arg2).toString();
                m_edtInputURL.setText(url);
               
            }
        });
        ;
        m_bSelectURL.setOnClickListener(new View.OnClickListener() {
          
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                readURL();
           if(!isSelect){
                m_adlgURL= new AlertDialog.Builder(Player.this)
               .setView(lvURL)
                .setPositiveButton("OK", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                       
                    }
                    
                }).create();
                isSelect=true;
           }
               m_adlgURL.show(); 
               
            }
        });
		
		 m_bStart.setOnClickListener(new View.OnClickListener() {
	            
	            @Override
	            public void onClick(View v) {
	                // TODO Auto-generated method stub
	               
	                    m_strVideoPath = m_edtInputURL.getText().toString();
	                m_adlgMain.hide();
	                if(m_sdkPlayer!=null)
                        return;
	                initPlayer(m_strVideoPath);
	                playVideo(m_strVideoPath);
	                m_bStartOption=false;
	            }
	        });
	   }
	   readURL();
		  m_adlgMain.show();
		
	
		
	}
	
	private void updateToDefaultVideoSize()
	{
        // Retrieve new display metrics
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        m_sdkPlayer.setViewSize(dm.widthPixels, dm.heightPixels);

        //if(!m_nAspectRatio.equals(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO))
        	m_sdkPlayer.setVideoAspectRatio(this.m_nAspectRatio);//setVideoAspectRatio(arg0)(nWidth, nHeight);
        
		
	}
	
	/* Notify SDK of configuration change */  
    public void onConfigurationChanged(Configuration newConfig) {
	    
        if (m_sdkPlayer == null ||  m_nVideoHeight == 0 || m_nVideoWidth == 0) {     
	        super.onConfigurationChanged(newConfig);
	        return;
	    }
	     
	    updateToDefaultVideoSize();
        
        super.onConfigurationChanged(newConfig);
    }
	private boolean checkDolbyEffectLib()
	{
		boolean b = true;;
		File f = new File(CommonFunc.getUserPath(this) + "/lib/libvoDolbyEffect_OSMP.so");
		if (!f.exists()) 
		{
			if(this.m_ibDolby!=null)
				m_ibDolby.setEnabled(false);
			b = false;
		}
		return b;
	}
	private void updateDolbyEffectButton()
	{
		if(!m_bShowDolby)
			return;
		if(!checkDolbyEffectLib())
		{
			m_ibDolby.setVisibility(View.GONE);
			return;
		}
		if(m_bEnableDolby)
		{
			m_ibDolby.setImageDrawable(getResources().getDrawable(R.drawable.btn_dolby_on));
			m_sdkPlayer.enableAudioEffect(true);
		}
		else
		{
			m_ibDolby.setImageDrawable(getResources().getDrawable(R.drawable.btn_dolby_off));
			m_sdkPlayer.enableAudioEffect(false);
		}
		
	}
	private void setDolbyEffect()
	{
		if(!checkDolbyEffectLib())
			return;
		if(this.m_sdkPlayer==null) return;
		if(m_bEnableDolby)
			m_bEnableDolby = false;
		else
			m_bEnableDolby = true;
		
		updateDolbyEffectButton();
	}
    
	private void setupParameters() {
        
        // Set view
        m_sdkPlayer.setView(m_svMain);
        DisplayMetrics dm  = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        
        // Set surface view size
        m_sdkPlayer.setViewSize(dm.widthPixels, dm.heightPixels);
        
        // Register SDK event listener
        m_sdkPlayer.setOnEventListener(m_listenerEvent);
        
        /* Processor-specific settings */
        String capFile = CommonFunc.getUserPath(this) + "/" + "cap.xml";
        m_sdkPlayer.setDeviceCapabilityByFile(capFile);
        abManager.updatePlayerOption(m_sdkPlayer);
        /* Set DRM library */
        //Implement setDRMLibrary on VOCommonPlayerImpl.java
        //m_sdkPlayer.setDRMLibrary("voDRM", "voGetDRMAPI");
        
         // Need to point to license file or pass its content, otherwise license failed will be returned.
        InputStream is = null;
        byte[] b = new byte[32*1024];
        try {
            is = getAssets().open("voVidDec.dat");
            is.read(b);
            is.close();
        } catch (IOException e) {
            DebugMode.logE(TAG, "Caught!", e);
        }
        m_sdkPlayer.setLicenseContent(b);
        //m_sdkPlayer.setPreAgreedLicense("ABCDEF");
        m_sdkPlayer.setRenderType(m_nRenderType);
        

        if((abManager.getOptionItemByID(OPTION_ID.OPTION_DOLBY_ID.getValue()).getSelect()==1)
                 && (m_eEngineType == VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER))
         {
            m_bShowDolby=true;
//           m_sdkPlayer.setParameter(voOSType.VOOSMP_PID_LOAD_AUDIO_EFFECT_MODULE, 1);
             m_ibDolby.setVisibility(View.VISIBLE);
             updateDolbyEffectButton();
         }else
         {
             m_bShowDolby=false;
//           m_sdkPlayer.setParameter(voOSType.VOOSMP_PID_LOAD_AUDIO_EFFECT_MODULE, 0);
             m_ibDolby.setVisibility(View.INVISIBLE);
             m_sdkPlayer.enableAudioEffect(false);
         }
      
       
        m_sdkPlayer.setInitialBufferTime(2000);
        m_sdkPlayer.setAnewBufferingTime(5000);
        
        m_sdkPlayer.enableSEI(VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_PIC_TIMING);
        m_sdkPlayer.startSEINotification(5000);
        
      

    }

    /* Initialize SDK player and playback selected media source */
    private void initPlayer(String strPath)
	{ 
       
		m_bOnError = false;
        VO_OSMP_RETURN_CODE nRet;
        
    	m_sdkPlayer = new VOCommonPlayerImpl();
        int nRenderType = abManager.getOptionItemByID(OPTION_ID.OPTION_VIDEORENDERTYPE_ID.getValue()).getSelect();
       
        m_nRenderType = VO_OSMP_RENDER_TYPE.valueOf(nRenderType);
        voLog.v(TAG, "mRenderType type is " + m_nRenderType);
        
       
        

        
        // Location of libraries
		String apkPath = CommonFunc.getUserNativeLibPath(this)+"/";//getUserPath(this) + "/lib/";	
		if(m_bDrmVerimatrix)
			apkPath = CommonFunc.getUserPath(this) + "/lib/";

        // SDK player engine type
		  m_eEngineType = VO_OSMP_PLAYER_ENGINE.valueOf(abManager.getOptionItemByID(OPTION_ID.OPTION_ENGINETYPE_ID.getValue()).getSelect());
		  if(!isCodecSupport)
		      m_eEngineType=VO_OSMP_PLAYER_ENGINE.VO_OSMP_VOME2_PLAYER;
		if(enableOpenmax)
		       m_eEngineType=VO_OSMP_PLAYER_ENGINE.VO_OSMP_OMXAL_PLAYER;
		
		voLog.e(TAG,"enableOpenMax="+enableOpenmax);
		VOOSMPInitParam init = new VOOSMPInitParam();
		init.setContext(this);
		init.setLibraryPath(apkPath);
		
     	// Initialize SDK player
		TimeCal.printTime("Init --->");
		nRet = m_sdkPlayer.init(m_eEngineType, init);
		TimeCal.printTime("Init <---");
	    int rc = abManager.processReturnCode("PlayVideo init", nRet.getValue());
        if (rc == 0) {
           
        } else {
            
            return;
        }
        
        m_sdkPlayer.setDRMLibrary("voDRM", "voGetDRMAPI");
        
	}

 	private void playVideo(String strPath) { 
	    VO_OSMP_RETURN_CODE nRet;
	    if (m_sdkPlayer == null){
	    	this.finish();
	    	return;
	    }
	    setupParameters();
	    updateRightView();
	    
	    strPath=strPath.trim();
		m_bOnError = false;
		/* Open media source */
        VO_OSMP_SRC_FORMAT format = VO_OSMP_SRC_FORMAT.VO_OSMP_SRC_AUTO_DETECT;
        
        VO_OSMP_SRC_FLAG eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
        if(abManager.getOptionItemByID(OPTION_ID.OPTION_ASYNCHRONOUSLY_ID.getValue()).getSelect()==1)
        {
            m_nAsyncOpen = true;
            eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_ASYNC;
            voLog.i(TAG, "PREFERENCE_KEY_ASYNC_OPEN is true.");
        }else
        {
            m_nAsyncOpen = false;
            eSourceFlag = VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC;
            voLog.i(TAG, "PREFERENCE_KEY_ASYNC_OPEN is false.");
        }
       openParam = new VOOSMPOpenParam();
        m_audioDecoderType = VO_OSMP_DECODER_TYPE.valueOf(abManager.getOptionItemByID(OPTION_ID.OPTION_AUDIODECODERTYPE_ID.getValue()).getSelect());
        m_videoDecoderType = VO_OSMP_DECODER_TYPE.valueOf(abManager.getOptionItemByID(OPTION_ID.OPTION_VIDEODECODERTYPE_ID.getValue()).getSelect());
        if(!isCodecSupport){
            m_audioDecoderType=VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW;
            m_videoDecoderType=VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW;
        }
        if(m_eEngineType==VO_OSMP_PLAYER_ENGINE.VO_OSMP_OMXAL_PLAYER){
            m_audioDecoderType=VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW;
            m_videoDecoderType=VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_SW;
        }
        openParam.setDecoderType(m_videoDecoderType.getValue() | m_audioDecoderType.getValue());
       
        if(enableIOMX)
            openParam.setDecoderType(VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_VIDEO_IOMX.getValue() | VO_OSMP_DECODER_TYPE.VO_OSMP_DEC_AUDIO_SW.getValue());
        int rc=0;
        {
            
        TimeCal.printTime("Open --->");
        nRet = m_sdkPlayer.open(strPath, eSourceFlag, format, openParam);
        TimeCal.printTime("Open <---");
        
        rc = abManager.processReturnCode("Player open", nRet.getValue());
        if (rc == 1)
        {
            return;
        }
		
		/* If Sync open */
        if (eSourceFlag == VO_OSMP_SRC_FLAG.VO_OSMP_FLAG_SRC_OPEN_SYNC) {
            
            /* Run (play) media pipeline */
            TimeCal.printTime("Start --->");
            nRet = m_sdkPlayer.start();
            TimeCal.printTime("Start <---");
            
            rc = abManager.processReturnCode("Player start", nRet.getValue());
            if (rc == 1)
            {
                return;
            }
            
            m_nPos = (int) m_sdkPlayer.getPosition();
            m_nDuration = (int) m_sdkPlayer.getDuration();
            
            updatePosDur();
            
            m_bStoped = false;
            m_ibPlayPause.setImageResource(R.drawable.selector_btn_pause);
            //m_sdkPlayer.setInitialBitrate(600000);
      
        }
        
        m_tvCurrentTime.setText("00:00");
        m_sbMain.setProgress(0);
        
       
        
        // Show wait icon
        m_pbLoadingProgress.setVisibility(View.VISIBLE);
        if(abManager.getOptionItemByID(OPTION_ID.OPTION_PERFORMANCE_ID.getValue()).getSelect()==1)
        {
		m_sdkPlayer.enableAnalytics(100000);
		VOOSMPAnalyticsFilter  filter = new VOOSMPAnalyticsFilter(5, 60, 60, 30, 60);
		m_sdkPlayer.startAnalyticsNotification(5000, filter);
        }
        
        updateAudioSpeed();
	}
 	}
    /* Notify SDK on Surface Change */  
	public void surfaceChanged (SurfaceHolder surfaceholder, int format, int w, int h) {
		voLog.i(TAG, "Surface Changed");
		if (m_sdkPlayer != null)
			m_sdkPlayer.setSurfaceChangeFinished();
	}

	/* Notify SDK on Surface Creation */  
	public void surfaceCreated(SurfaceHolder surfaceholder) {
		voLog.i(TAG, "Surface Created");
		if (m_sdkPlayer !=null)	{
			// If SDK player already exists, show media controls
			m_sdkPlayer.resume(m_svMain);
			showMediaController();
			return;
		}
		
		if ((m_strVideoPath == null) || (m_strVideoPath.trim().length() <=0))
			return;
		
		// Else play media
		initPlayer(m_strVideoPath);
		playVideo(m_strVideoPath);
		
	}
  
	public void surfaceDestroyed(SurfaceHolder surfaceholder) {
		voLog.i(TAG, "Surface Destroyed");
		
		if (m_sdkPlayer !=null)
		    m_sdkPlayer.setView(null);

	}
	
	private void stopVideo() {
	    m_bPaused = false;
        m_bStoped = true;
        m_svMain.setVisibility(View.INVISIBLE);
        m_svMain.setVisibility(View.VISIBLE);
    	if (m_sdkPlayer != null) {
    	    if(abManager.getOptionItemByID(OPTION_ID.OPTION_PERFORMANCE_ID.getValue()).getSelect()==1)
			m_sdkPlayer.stopAnalyticsNotification();
			
			TimeCal.printTime("Stop --->");
			m_sdkPlayer.stop();
			TimeCal.printTime("Stop <---");
			
			TimeCal.printTime("Close --->");
			m_sdkPlayer.close();
			TimeCal.printTime("Close <---");
         
            voLog.v(TAG, "MediaPlayer stoped.");
        }

 	}

	/* Stop playback, close media source, and uninitialize SDK player */
	public void uninitPlayer() {
		
		if (m_sdkPlayer != null) {
		    TimeCal.printTime("Destroy --->");
			m_sdkPlayer.destroy();
			TimeCal.printTime("Destroy <---");
			
			m_sdkPlayer = null;
			m_strVideoPath="";
			
			voLog.v(TAG, "MediaPlayer released.");
		}

		//this.finish();
	}

	@Override
	protected void onDestroy() {
	    m_adlgMain.dismiss();
	    unregisterReceiver(mInfoReceiver);
		super.onDestroy();  
		voLog.v(TAG, "Player onDestroy Completed!");
	}

	/* Pause/Stop playback on activity pause */
	protected void onPause() 
	{
		super.onPause();
		voLog.v(TAG, "Player onPause");
		
		if (m_sdkPlayer!= null) {
            
            m_sdkPlayer.suspend(false); // If you want to continue to play music, please input parameter "true"
           
    }
	}
	@Override
    protected void onResume() {
        
       if (m_sdkPlayer !=null)
        {
            m_isResume = true;
        }
        super.onResume();
    }


	/* (non-Javadoc)
	* @see android.app.Activity#onRestart()
	*/
	@Override
	protected void onRestart() {
		voLog.v(TAG, "Player onRestart");
		
		super.onRestart();
		if(m_sdkPlayer == null)
			return;
	}
	
	private void doUpdateUI() {
	     
	    // If the controls are not visible do not update
        if(m_rlBottom.getVisibility() != View.VISIBLE)
            return;
       
        /* If the player is stopped do not update */
        if (m_bStoped)
            return;
       
        /* If the user is dragging the progress bar do not update */
        if (m_bTrackProgressing)
            return;
 
        Date timeNow = new Date(System.currentTimeMillis());
        long timePeriod = (timeNow.getTime() - m_dateUIDisplayStartTime.getTime())/1000;
        if (timePeriod >= 10 && m_bPaused == false
                && m_rlProgramInfo.getVisibility() != View.VISIBLE ) {
            /* If the media is being played back, hide media controls after 3 seconds if unused */
            hideController();
            return;
        }

        VOOSMPAssetIndex j=  m_sdkPlayer.getPlayingAsset ();
        int i=0;
        if(j!=null)
        i=  j.getVideoIndex();
        String strBps="0";
        if(lstVideo!=null && lstVideo.size()>1)
          strBps=lstVideo.get(i+1);
     
       if (strBps != null && !strBps.equals("0 bps"))
          {
           
            m_tvBps.setText(strBps);
          }
        else
            m_tvBps.setText("0");
        
        //updateAudioSpeed();
         
        {
            m_tvDownloadCurrent.setVisibility(View.INVISIBLE);
            m_tvDownloadTotal.setVisibility(View.INVISIBLE);
        }
        /* update the Seekbar and Time display with current position */
        m_nPos = (int)m_sdkPlayer.getPosition();
        int nPos = m_nPos - (int)m_sdkPlayer.getMinPosition();
 		m_nDuration = ( (int)m_sdkPlayer.getMaxPosition()-(int)m_sdkPlayer.getMinPosition());
 		if(m_nDuration<=0)
 			m_nDuration = (int)m_sdkPlayer.getDuration();
	    int nDuration;
	    nDuration = m_nDuration;
	    if(nDuration>0)
	    	m_sbMain.setProgress((int) (100 * nPos/ nDuration));
	    
	    nPos= (int)m_sdkPlayer.getPosition();
        int nMin =(int)m_sdkPlayer.getMinPosition();
        String str;
	    if(m_sdkPlayer.isLiveStreaming()){
	        m_nDuration=(int) m_sdkPlayer.getMaxPosition();
	        nMin=-nMin;
	        nPos=-nPos;
	         str = "-"+formatTime(nMin / 1000)+"/"
                    +"-"+formatTime(nPos / 1000);
	        }
	    else{
	        m_nDuration=(int) m_sdkPlayer.getDuration();
	        str = formatTime(nMin / 1000)+"/"
                    +formatTime(nPos / 1000);
	   }
	   
       
        m_tvCurrentTime.setText(str);
        
        updatePosDur();
        VO_OSMP_STATUS status=m_sdkPlayer.getPlayerStatus();
        if(status==VO_OSMP_STATUS.VO_OSMP_STATUS_PAUSED)
            m_ibPlayPause.setImageResource(R.drawable.selector_btn_play);
        if(status==VO_OSMP_STATUS.VO_OSMP_STATUS_PLAYING)
            m_ibPlayPause.setImageResource(R.drawable.selector_btn_pause);
    }

	/* Show media controller. Implemented by showMediaControllerImpl(), called by handler */
	private void showMediaController() {
		if(m_sdkPlayer == null)
			return;
		
		handler.sendEmptyMessage(MSG_SHOW_CONTROLLER);
	}

    /* Show media controller implementation */
	private void showMediaControllerImpl()	{
		voLog.v(TAG, "Touch screen, layout status is " +m_rlBottom.getVisibility());
		m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());		// Get current system time
		
		if (m_rlBottom.getVisibility() != View.VISIBLE) {
			voLog.v(TAG, "mIsStop is " + m_bStoped);
		
			// Schedule next UI update in 200 milliseconds
			if(m_ttMain != null)
				m_ttMain = null;
			
			m_ttMain= new TimerTask() {
			    public void run() {
					handler.sendEmptyMessage(MSG_UPDATE_UI);
				}
			};
			
			if(m_timerMain == null)
			    m_timerMain = new Timer();
			
			m_timerMain.schedule(m_ttMain, 0, 1000);
					
			// Show controls
			m_rlBottom.setVisibility(View.VISIBLE);
			m_rlTop.setVisibility(View.VISIBLE);
			
			voLog.v(TAG, "m_rlTop show " + m_rlTop.getVisibility());
		}
	}

	/* Show media controls on activity touch event */
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getAction() == MotionEvent.ACTION_DOWN){
	        if (m_rlBottom.getVisibility() != View.VISIBLE) {
	            showMediaControllerImpl();
	        } else {
	            hideControllerImpl();
	        }
		}
        //return super.onTouchEvent(event);
		return m_gdMain.onTouchEvent(event);
	}
//	public boolean onTouchEvent(MotionEvent event) {
//	    if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            
//            if (m_rlBottom.getVisibility() != View.VISIBLE) {
//                showMediaControllerImpl();
//            } else {
//                hideControllerImpl();
//            }
//		
//		}
//		
//		return super.onTouchEvent(event);
//	}

	/* Stop player and exit on Back key */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		voLog.v(TAG, "Key click is " + keyCode);

		if (keyCode ==KeyEvent.KEYCODE_BACK) {
			voLog.v(TAG, "Key click is Back key");
		  stopVideo();
		  uninitPlayer();
		  m_bReturnSourceWindow=true;
		  SourceWindow();
          return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/* (non-Javadoc)
	* @see android.app.Activity#onStart()
	*/
	@Override
	protected void onStart() {
		voLog.v(TAG, "Player onStart");
		super.onStart();
	}

	/* (non-Javadoc)
	* @see android.app.Activity#onStop()
	*/
	@Override
	protected void onStop() {
		voLog.v(TAG, "Player onStop");
		super.onStop();
	}

	/* Hide media controller. Implemented by showMediaControllerImpl(), called by handler */
	public void hideController() {
		handler.sendEmptyMessage(MSG_HIDE_CONTROLLER);
	}

	/* Hide media controller implementation */
	public void hideControllerImpl() {
		if(m_timerMain != null)	{
			m_timerMain.cancel();
			m_timerMain.purge();
			m_timerMain = null;
			m_ttMain = null;
		}
		
		m_rlBottom.setVisibility(View.INVISIBLE);
		m_rlTop.setVisibility(View.INVISIBLE);
	}
	
	private void stopAndFinish() {
        stopVideo();
        uninitPlayer();
        this.finish();
    }

	/* Display error messages and stop player */
	public boolean onError(VOCommonPlayer mp, int what, int extra) 	{
		voLog.v(TAG, "Error message, what is " + what + " extra is " + extra);
	    stopVideo();
	    uninitPlayer();
		if(m_bOnError)
			return true;
		m_bOnError = true;
		
		String errMsg = getString(R.string.VO_OSMP_CB_ERROR);
	    if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_ADAPTIVE_STREAMING_ERROR.getValue()){
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_PLAYLIST_PARSEFAIL.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_PLAYLIST_PARSEFAIL);
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_PLAYLIST_UNSUPPORTED.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_PLAYLIST_UNSUPPORTED);
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_UNSUPPORTED.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_UNSUPPORTED);
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_DOWNLOADFAIL.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_DOWNLOADFAIL);
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_DRMLICENSEERROR.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_DRMLICENSEERROR);
	        	if( extra ==  VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_VOLIBLICENSEERROR.getValue())
	        		errMsg = getString(R.string.VO_OSMP_SRC_ADAPTIVE_STREAMING_ERROR_EVENT_STREAMING_VOLIBLICENSEERROR);
	    }
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_LICENSE_FAIL.getValue()
				|| what == VO_OSMP_RETURN_CODE.VO_OSMP_ERR_LICENSE_FAIL.getValue())
		    errMsg = getString(R.string.VO_OSMP_CB_LICENSE_FAIL);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_CB_ERROR.getValue())
		    errMsg = getString(R.string.VO_OSMP_CB_ERROR);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_FAIL.getValue())
		    errMsg = getString(R.string.VO_OSMP_SRC_CB_DRM_FAIL);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DOWNLOAD_FAIL.getValue())
		    errMsg = getString(R.string.VO_OSMP_SRC_CB_DOWNLOAD_FAIL);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR.getValue())
		    errMsg = getString(R.string.VO_OSMP_SRC_CB_PLAYLIST_PARSE_ERR);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_REJECTED.getValue())
            errMsg = getString(R.string.VO_OSMP_SRC_CB_CONNECTION_REJECTED);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_NOT_SECURE.getValue())
            errMsg = getString(R.string.VO_OSMP_SRC_CB_DRM_NOT_SECURE);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL.getValue())
            errMsg = getString(R.string.VO_OSMP_SRC_CB_DRM_AV_OUT_FAIL);
		
		if(what == VO_OSMP_CB_EVENT_ID.VO_OSMP_SRC_CB_CONNECTION_FAIL.getValue())
		    errMsg = getString(R.string.VO_OSMP_SRC_CB_CONNECTION_FAIL);
		
		String errStr = "";
		errStr = errMsg + "\nError code is " + Integer.toHexString(what);
		
		// Dialog to display error message; stop player and exit on Back key or "OK"
		AlertDialog ad = new AlertDialog.Builder(Player.this)
							.setIcon(R.drawable.icon)
							.setTitle(R.string.str_ErrPlay_Title)
							.setMessage(errStr)
							.setOnKeyListener(new OnKeyListener() {
							    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2)	{
										if (arg1 == KeyEvent.KEYCODE_BACK) { 
										} 
										arg0.dismiss();
										Player.this.finish();
										return false;
									}
								})
							.setPositiveButton(R.string.str_OK, new OnClickListener() {
							    public void onClick(DialogInterface a0, int a1)	{
							        a0.dismiss();
							    	Player.this.finish();
							    }
							}).create();
		ad.show();
		return true;

	}

	/* Toggle Play/Pause button display and functionality */
	private void playerPauseRestart() {
		if (m_sdkPlayer != null) {
			if(m_bPaused == false)	{
				// If playing, pause media pipeline, show media controls, and change button to "Play" icon
			    m_sdkPlayer.pause();
				showMediaController();
				m_ibPlayPause.setImageResource(R.drawable.selector_btn_play);
				m_bPaused = true;
				
//				m_sdkPlayer.stopSEINotification();
			} else {
				// Else, play media pipeline and change button to "Pause" icon
				m_sdkPlayer.start();
				m_ibPlayPause.setImageResource(R.drawable.selector_btn_pause);
				m_bPaused = false;
			}
		}
	}

	

	private void updatePosDur() {
        m_tvTotalTime.setText(formatTime( m_nDuration / 1000));
    }
	
	/* Retrieve list of media sources */
    private void ReadUrlInfo() {
        voLog.i(TAG, "Current external storage directory is %s", Environment.getExternalStorageDirectory().getAbsolutePath());
        String str = Environment.getExternalStorageDirectory().getAbsolutePath() + "/url.txt";
        if (CommonFunc.ReadUrlInfoToList(m_lstSelectURL, str) == false)
            Toast.makeText(this, "Could not find " + str, Toast.LENGTH_LONG).show();
    }
    
    
	/* Manage SDK requests */
	public int onRequest(int arg0, int arg1, int arg2, Object arg3) {
		
		voLog.i(TAG, "onRequest arg0 is "+ arg0);
		return 0;
	}

	private void resetUIDisplayStartTime() {
        m_dateUIDisplayStartTime = new Date(System.currentTimeMillis());
    }
	
	private void updateAudioSpeed(){
		TextView vw = (TextView)findViewById(R.id.tvAudioSpeed);
	    if(!m_bShowAudioSpeed){
	    	if(vw!=null)
	    		vw.setVisibility(View.GONE);
	    }
		Float f = new Float(m_fAudioSpeed);
		Float f2 = new Float(m_fAudioSpeed+0.0001f);
		String s = f2.toString();
		if(s.length()>3)
			s = s.substring(0, 3);
		vw.setText(s);
		if(m_sdkPlayer!=null){
		    VO_OSMP_RETURN_CODE ret;
		    ret=this.m_sdkPlayer.setAudioPlaybackSpeed( f);
			if(VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE != ret && VO_OSMP_RETURN_CODE.VO_OSMP_ERR_RETRY != ret){
				vw.setText("1.0");
				m_fAudioSpeed = 1.0f;
			}
		}

	}

    private void init() {
        initLayout();
        initLayoutRight();
        initLayoutLeft();
        m_tvDownloadCurrent = (TextView) findViewById(R.id.tvDownloaderPercentage);
        m_tvDownloadTotal   = (TextView) findViewById(R.id.tvDownloaderTotal);
	    m_ibHighSpeed    = (ImageButton) findViewById(R.id.ibHighSpeed);
	    m_ibLowSpeed     = (ImageButton) findViewById(R.id.ibLowSpeed);
	    if(!m_bShowAudioSpeed){
	    	m_ibHighSpeed.setVisibility(View.GONE);
	    	m_ibLowSpeed.setVisibility(View.GONE);
	    }
	    
	    m_ibHighSpeed.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            	m_fAudioSpeed += 0.1f;
            	if(m_fAudioSpeed>4.0f)
            		m_fAudioSpeed = 4.0f;
            	updateAudioSpeed();
            }
        });
	    m_ibLowSpeed.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
            	m_fAudioSpeed -= 0.1f;
            	if(m_fAudioSpeed<0.5f)
            		m_fAudioSpeed = 0.5f;
            	updateAudioSpeed();
            	
            }
        });

    }

    private void initLayout() {

        m_rlTop = (RelativeLayout) findViewById(R.id.tlTop);
        m_rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);
    }
    
    private void disablePopupMenuLableClick() {
       
        ((TextView) findViewById(R.id.tvVideo)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { /* do nothing */ }});
        
        ((TextView) findViewById(R.id.tvAudio)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { /* do nothing */ }});
        
        ((TextView) findViewById(R.id.tvSubtitle)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { /* do nothing */ }});
        
    }
    private void updateRightView(){
        if(abManager.getOptionItemByID(OPTION_ID.OPTION_SUBTITLE_ID.getValue()).getSelect()==1) 
            m_bEnableSubtitle=true;
        else
            m_bEnableSubtitle=false;
        if (!m_bEnableVideo && !m_bEnableAudio && !m_bEnableSubtitle)
            m_rlProgramInfoArrow.setVisibility(View.INVISIBLE);
        else {
        	m_rlProgramInfoArrow.setVisibility(View.VISIBLE);
            if (!m_bEnableVideo)
                findViewById(R.id.rlVideo).setVisibility(View.GONE);
            else
            	findViewById(R.id.rlVideo).setVisibility(View.VISIBLE);
            if (!m_bEnableAudio)
                findViewById(R.id.rlAudio).setVisibility(View.GONE);
            else
            	findViewById(R.id.rlAudio).setVisibility(View.VISIBLE);
           
            if (!m_bEnableSubtitle)
                findViewById(R.id.rlSubtitle).setVisibility(View.GONE);
            else
            	findViewById(R.id.rlSubtitle).setVisibility(View.VISIBLE);
        }
    	
    }

    private void initLayoutRight() {

        m_rlRight = (RelativeLayout) findViewById(R.id.rlRight);

        m_hsvRight= (HorizontalScrollView) findViewById(R.id.hsvRight);
        m_llRight = (LinearLayout) findViewById(R.id.llRight);

        m_rlProgramInfo = (RelativeLayout) findViewById(R.id.rlProgramInfo);
        m_rlProgramInfo.setVisibility(View.INVISIBLE);
        
        m_rlProgramInfoArrow = (RelativeLayout) findViewById(R.id.rlProgramInfoArrow);
        m_rlProgramInfoArrow.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                resetUIDisplayStartTime();

                if (m_rlProgramInfo.getVisibility() != View.VISIBLE) {
                    m_rlProgramInfo.setVisibility(View.VISIBLE);
                    m_rlProgramInfoArrow.setBackgroundResource(R.drawable.bg_player_right_programinfo_on);
                    fillProgramInfo();
                } else {
                    m_rlProgramInfo.setVisibility(View.INVISIBLE);
                    m_rlProgramInfoArrow.setBackgroundResource(R.drawable.bg_player_right_programinfo_off);
                }

            }
        });
        updateRightView();
        
        disablePopupMenuLableClick();
        
        m_tvCommit = (TextView) findViewById(R.id.tvCommit);
        m_tvCommit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
               
                VO_OSMP_RETURN_CODE ret = m_sdkPlayer.commitSelection();
                int rc = abManager.processReturnCode("commitSelection", ret.getValue());
                if (rc == 0) {
                    
                    // UI logic
                    resetProgramInfoPopupMenu();

                    resetTextAppearance();

                    m_nSelectedVideoPreIndex = m_nSelectedVideoIndex;
                    m_nSelectedAudioPreIndex = m_nSelectedAudioIndex;
                    m_nSelectedSubtitlePreIndex = m_nSelectedSubtitleIndex;

                    // use SelectedXXXindex
                    initProgramInfoPopupMenu(m_nSelectedVideoIndex+1,
                            m_nSelectedAudioIndex, m_nSelectedSubtitleIndex);
                
                } else {
                   return;
                }
                
            }
        });
        
        m_tvRevert = (TextView) findViewById(R.id.tvRevert);
        m_tvRevert.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // data logic
                VO_OSMP_RETURN_CODE ret = m_sdkPlayer.clearSelection();
                int rc = abManager.processReturnCode("clearSelection", ret.getValue());
                if (rc ==0) {
                    
                    // UI logic
                    resetProgramInfoPopupMenu();
                    
                    resetTextAppearance();
                    
                    //not use PlayingXXXindex
                    m_nSelectedVideoIndex = m_nSelectedVideoPreIndex;
                    m_nSelectedAudioIndex = m_nSelectedAudioPreIndex;
                    m_nSelectedSubtitleIndex = m_nSelectedSubtitlePreIndex;
                    initProgramInfoPopupMenu(m_nSelectedVideoIndex+1,
                            m_nSelectedAudioIndex, m_nSelectedSubtitleIndex);
                
                } else {
                    return;
                }
                
            }
        });
        
        
        initLayoutRightPopupMenu();
        
        // reserved/extended feature
        m_hsvRight.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View arg0, MotionEvent arg1) {
                resetUIDisplayStartTime();
                return false;
            }
        });

        m_llRight.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {

                if (motionevent.getAction() == MotionEvent.ACTION_DOWN) {

                    if (m_rlBottom.getVisibility() == View.VISIBLE) {
                        hideControllerImpl();
                        return true;
                    }
                }

                return false;
            }
        });
        
        return;

    }
  
    private LinearLayout requirePopupMenuLayout(AssetType type) {
        
        if (type == AssetType.Asset_Video)
            return m_llVideoPopupMenu;
        else if (type == AssetType.Asset_Audio)
            return m_llAudioPopupMenu;
        else if (type == AssetType.Asset_Subtitle)
            return m_llSubtitlePopupMenu;
        
        return null;
    }
    
    private void initProgramInfoPopupMenu(AssetType type, int nIndex) {
        
        LinearLayout llParent = requirePopupMenuLayout(type);
        
        if (llParent == null || nIndex >= llParent.getChildCount() || 
                nIndex < 0)
            return;
        
        LinearLayout llChild = (LinearLayout) llParent.getChildAt(nIndex);

        TextView tv = (TextView) llChild.findViewById(R.id.tvContent);
        tv.setSelected(true);
    }
    
    
    private void initProgramInfoPopupMenu(int nVideoIndex, int nAudioIndex, int nSubtitleIndex) {
      
        initProgramInfoPopupMenu(AssetType.Asset_Video, nVideoIndex);
        initProgramInfoPopupMenu(AssetType.Asset_Audio, nAudioIndex);
        initProgramInfoPopupMenu(AssetType.Asset_Subtitle, nSubtitleIndex);
     
    }
    
    private void resetProgramInfoPopupMenu(LinearLayout llParent, int nIndex) {
        
        if (llParent == null || nIndex >= llParent.getChildCount()
                || nIndex < 0)
            return;
        
        LinearLayout llChild = (LinearLayout) llParent.getChildAt(nIndex);

        TextView tv = (TextView) llChild.findViewById(R.id.tvContent);
        tv.setSelected(false);
        tv.setEnabled(true);
        
    }
    
    private void resetProgramInfoPopupMenu(AssetType type) {
        
        LinearLayout llParent = requirePopupMenuLayout(type);
        
        if (llParent == null)
            return;
        
        for (int nIndex = 0; nIndex < llParent.getChildCount(); nIndex++)
            resetProgramInfoPopupMenu(llParent, nIndex);
    }
    
    private void resetProgramInfoPopupMenu() {
        resetProgramInfoPopupMenu(AssetType.Asset_Video);
        resetProgramInfoPopupMenu(AssetType.Asset_Audio);
        resetProgramInfoPopupMenu(AssetType.Asset_Subtitle);
    }
    
    private void disableProgramInfoPopupMenu(AssetType type, int nIndex) {
        
        LinearLayout llParent = requirePopupMenuLayout(type);
        
        if (llParent == null || nIndex >= llParent.getChildCount()
                || nIndex < 0)
            return;
        
        LinearLayout llChild = (LinearLayout) llParent.getChildAt(nIndex);

        TextView tv = (TextView) llChild.findViewById(R.id.tvContent);
        tv.setSelected(false);
        tv.setEnabled(false);
        
    }
    
    private final int INDEX_RESETALL = Integer.MAX_VALUE;
    
    private void resetTextAppearance(AssetType type, int nIndexExcept)  
    {
        LinearLayout llParent = requirePopupMenuLayout(type);
        if (llParent == null)
            return;
        
        if (nIndexExcept != INDEX_RESETALL) 
            if (nIndexExcept >= llParent.getChildCount() || nIndexExcept < 0)
                return;
        
        for (int i = 0; i < llParent.getChildCount(); i++) {
     
            LinearLayout llChild;
            TextView tv;
                
            if (nIndexExcept == INDEX_RESETALL) {
                  
                llChild = (LinearLayout) llParent.getChildAt(i);
                tv = (TextView) llChild.findViewById(R.id.tvContent);
                    
            } else {
                    
                if (i == nIndexExcept)
                    continue;
                  
                llChild = (LinearLayout) llParent.getChildAt(i);
                tv = (TextView) llChild.findViewById(R.id.tvContent);
                  
            }
                
            tv.setTextAppearance(getApplicationContext(),
                    R.style.style_player_textview_general_selector);
        }
       
    }
    
    private void resetTextAppearance() {
        resetTextAppearance(AssetType.Asset_Video, INDEX_RESETALL);
        resetTextAppearance(AssetType.Asset_Audio, INDEX_RESETALL);
        resetTextAppearance(AssetType.Asset_Subtitle, INDEX_RESETALL);
    }
    
    private void setProgramInfoAssetEventListener(final TextView tv, final AssetType type) {
        
        tv.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                
                tv.setTextAppearance(getApplicationContext(),
                        R.style.style_player_textview_general_selector);
                
                tv.setSelected(true);
                
                VO_OSMP_RETURN_CODE ret;
                
                int nIndex = ((Integer) view.getTag()).intValue();
                if (nIndex == -1) {
                	nIndex=0;
                }
                if (type == AssetType.Asset_Video) {

                    int nVideoIndex;
                    
                    if (nIndex == 0)
                        nVideoIndex = VOCommonPlayerAssetSelection.VO_OSMP_ASSET_AUTO_SELECTED;
                    else 
                        nVideoIndex = nIndex - 1; 
                    
                    // data logic
                    ret = m_asset.selectVideo(nVideoIndex);
                    int rc = abManager.processReturnCode("selectVideo", ret.getValue());
                    if (rc == 1)
                    {
                        return;
                    }
                    
                    m_nSelectedVideoIndex = nVideoIndex; // not nVideoIndex;
                    
                    // UI logic
                    resetProgramInfoPopupMenu(AssetType.Asset_Video);
                    tv.setSelected(true);

                    resetTextAppearance(AssetType.Asset_Video, nIndex);
                    
                    for (int i = 0; i < m_asset.getAudioCount(); i++) { 
                        if (!m_asset.isAudioAvailable(i))
                            disableProgramInfoPopupMenu(AssetType.Asset_Audio, i);
                    }
                    
                    for (int i = 0; i < m_asset.getSubtitleCount(); i++) { 
                        if (!m_asset.isSubtitleAvailable(i))
                            disableProgramInfoPopupMenu(AssetType.Asset_Subtitle, i);
                    }
                    
                } else if (type == AssetType.Asset_Audio) {

                    // data logic
                    ret = m_asset.selectAudio(nIndex);
                    int rc = abManager.processReturnCode("selectAudio", ret.getValue());
                    if (rc == 1)
                    {
                        return;
                    }
                    
                    m_nSelectedAudioIndex = nIndex;
                    
                    // UI logic
                    resetProgramInfoPopupMenu(AssetType.Asset_Audio);
                    tv.setSelected(true);
                    resetTextAppearance(AssetType.Asset_Audio, nIndex);
                    
                    for (int i = 0; i < m_asset.getVideoCount(); i++) { 
                        if (!m_asset.isVideoAvailable(i))
                            //  0 is video auto, so use i + 1
                            disableProgramInfoPopupMenu(AssetType.Asset_Video, i + 1);
                    }
                    
                    for (int i = 0; i < m_asset.getSubtitleCount(); i++) { 
                        if (!m_asset.isSubtitleAvailable(i))
                            disableProgramInfoPopupMenu(AssetType.Asset_Subtitle, i);
                    }
                  
                } else if (type == AssetType.Asset_Subtitle) {

                    // data logic
                    ret = m_asset.selectSubtitle(nIndex);
                    int rc = abManager.processReturnCode("selectSubtitle", ret.getValue());
                    if (rc == 1)
                    {
                        return;
                    }
                    
                    m_nSelectedSubtitleIndex = nIndex;
                    
                    // UI logic
                    resetProgramInfoPopupMenu(AssetType.Asset_Subtitle);
                    tv.setSelected(true);
                    resetTextAppearance(AssetType.Asset_Subtitle, nIndex);
                    
                    for (int i = 0; i < m_asset.getVideoCount(); i++) { 
                        if (!m_asset.isVideoAvailable(i))
                            //  0 is video auto, so use i + 1
                            disableProgramInfoPopupMenu(AssetType.Asset_Video, i + 1);
                    }
                    
                    for (int i = 0; i < m_asset.getAudioCount(); i++) { 
                        if (!m_asset.isAudioAvailable(i))
                            disableProgramInfoPopupMenu(AssetType.Asset_Audio, i);
                    }
                  
                }
                
                
            } });
    }
    

    @SuppressWarnings("unchecked")
    private void inflatePopupMenu(Context context, final LinearLayout parentView,
            ArrayList<?> lstT, final AssetType type) {

        if (lstT.size() == 0)
            return;

        LinearLayout llBuffer;
        TextView tvBuffer;

        int size = lstT.size();
        if (size <= 3) {
            // There is a bug in HorizontalScrollView, so we have to use a workaround.
            // http://stackoverflow.com/questions/9031817/android-horizontalscrollview-with-right-layout-gravity-working-wrong
            HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams( 
                    HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.FILL_PARENT); 
            params.gravity = Gravity.RIGHT;
            parentView.setLayoutParams(params);
        }else{
            HorizontalScrollView.LayoutParams params = new HorizontalScrollView.LayoutParams( 
                    HorizontalScrollView.LayoutParams.WRAP_CONTENT, HorizontalScrollView.LayoutParams.FILL_PARENT); 
            params.gravity = Gravity.LEFT;
            parentView.setLayoutParams(params);
        }
        
        for (int i = 0; i < size; i++) {
            if (i == 0) {
                llBuffer = (LinearLayout)LayoutInflater.from(context)
                    .inflate(R.layout.player_popupmenu_left, parentView, false);
                parentView.addView(llBuffer);
                tvBuffer = (TextView) llBuffer.findViewById(R.id.tvContent);
                tvBuffer.setTag(-1);
            }    
             else {
                llBuffer = (LinearLayout)LayoutInflater.from(context)
                    .inflate(R.layout.player_popupmenu_normal, parentView, false);
                parentView.addView(llBuffer);
                tvBuffer = (TextView) llBuffer.findViewById(R.id.tvContent);
                tvBuffer.setTag(i);
            }
            tvBuffer.setText(((ArrayList<String>) lstT).get(i));
            setProgramInfoAssetEventListener(tvBuffer, type);
                    
        }
    }

    // Right layout functions
    private void initLayoutRightPopupMenu() {
        
        m_llVideoPopupMenu = (LinearLayout) findViewById(R.id.llVideoPopupMenu);
        m_llAudioPopupMenu = (LinearLayout) findViewById(R.id.llAudioPopupMenu);
        m_llSubtitlePopupMenu = (LinearLayout) findViewById(R.id.llSubtitlePopupMenu);
        
        m_hsvVideoPopupMenu = (HorizontalScrollView) findViewById(R.id.hsvVideoPopupMenu);
        m_hsvVideoPopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });

        m_hsvAudioPopupMenu = (HorizontalScrollView) findViewById(R.id.hsvAudioPopupMenu);
        m_hsvAudioPopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });
        
        m_hsvSubtitlePopupMenu = (HorizontalScrollView) findViewById(R.id.hsvSubtitlePopupMenu);
        m_hsvSubtitlePopupMenu.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View view, MotionEvent motionevent) {
                if (motionevent.getAction() == MotionEvent.ACTION_MOVE)
                    resetUIDisplayStartTime();

                return false;
            }

        });

    }

    private void getVideoDescription(ArrayList<String> lstString) {
        
        if (lstString == null || m_asset == null)
            return;
        
        int nAssetCount = m_asset.getVideoCount();
        if (nAssetCount == 0) 
            return;
        
        int nDefaultIndex = 0;
        
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
          
            VOOSMPAssetProperty propImpl = m_asset.getVideoProperty(nAssetIndex);
            
            String strDescription;
            
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
             
                strDescription = STRING_ASSETPROPERTYNAME_VIDEO + Integer.toString(nDefaultIndex++);
                
            } else {
                
                final int KEY_DESCRIPTION_INDEX = 2;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
                    
            }
            if(strDescription.length() > 4)
            {
            String str = strDescription.substring(0,(strDescription.length()-4));
            voLog.d(TAG,"getVideoDescription:str = " + str);
            if(str.equals("0"))
            {
                lstString.add("0");
            }
            else
            {
                lstString.add(bitrateToString( Integer.valueOf(str).intValue()));
            }
            }
           

        }
        
    }
    private void getAudioPropertyLog(){
        if ( m_asset == null)
            return;
        
        int nAssetCount = m_asset.getAudioCount();
        if (nAssetCount == 0) 
            return;
        
        int nDefaultIndex = 0;
        
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
          
            VOOSMPAssetProperty propImpl = m_asset.getAudioProperty(nAssetIndex);
            
            
            
            String str = null;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
             
                str = STRING_ASSETPROPERTYNAME_AUDIO + Integer.toString(nDefaultIndex++);
                
            } else {
                
               for(int i=0;i<=nPropertyCount;i++){
                String key = (String) propImpl.getKey(i);
                String value =(String) propImpl.getValue(i);
                
                str="Audio property:"+ key +" - "+ value;
                voLog.d(TAG, str);
               }
            }
       }
  }
    
    private void getAudioDescription(ArrayList<String> lstString) {
        
        if (lstString == null || m_asset == null)
            return;
        
        int nAssetCount = m_asset.getAudioCount();
        if (nAssetCount == 0) 
            return;
        
        int nDefaultIndex = 0;
        int j=0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
          if(! m_asset.isAudioAvailable(nAssetIndex)){
              continue;
          }
          m_AudioIndex[nAssetIndex]=j;
          j++;
            VOOSMPAssetProperty propImpl = m_asset.getAudioProperty(nAssetIndex);
            
            String strDescription;
            
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
             
                strDescription = STRING_ASSETPROPERTYNAME_AUDIO + Integer.toString(nDefaultIndex++);
                
            } else {
                
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
                    
            }
            
            lstString.add(strDescription);

        }
        m_nSelectedAudioIndex = m_asset.getPlayingAsset().getAudioIndex();
        
    }
    
    
    private void getSubtitleDescription(ArrayList<String> lstString) {
        
        if (lstString == null || m_asset == null)
            return;
        
        int nAssetCount = m_asset.getSubtitleCount();
        if (nAssetCount == 0) 
            return;
        
        int nDefaultIndex = 0;
        
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
           
            VOOSMPAssetProperty propImpl = m_asset.getSubtitleProperty(nAssetIndex);
            
            
            String strDescription;
            
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
             
                strDescription = STRING_ASSETPROPERTYNAME_SUBTITLE + Integer.toString(nDefaultIndex++);
                
            } else {
                
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
                    
            }
            
            lstString.add(strDescription);
            
        }
        m_nSelectedSubtitleIndex = m_asset.getPlayingAsset().getSubtitleIndex();
        
    }

    private void fillAssetInfo(ArrayList<String> lstString,
            LinearLayout llPopupMenu, AssetType type) {

        llPopupMenu.removeAllViews();
        inflatePopupMenu(Player.this, llPopupMenu, lstString, type);
        
        int nIndex = 0;
        if (type == AssetType.Asset_Video) {
            if (m_nSelectedVideoIndex == VOCommonPlayerAssetSelection.VO_OSMP_ASSET_AUTO_SELECTED)
                nIndex = 0;
            else 
                nIndex = m_nSelectedVideoIndex + 1;
        }
        else if (type == AssetType.Asset_Audio)
            nIndex = m_AudioIndex[m_nSelectedAudioIndex];
        else if (type == AssetType.Asset_Subtitle)
            nIndex = m_nSelectedSubtitleIndex;
        initProgramInfoPopupMenu(type, nIndex);
        
        llPopupMenu.scheduleLayoutAnimation();
    }

    private void fillProgramInfo() {

        resetUIDisplayStartTime();
        
         lstVideo = new ArrayList<String>();
        getVideoDescription(lstVideo);
        lstVideo.add(0, getResources().getString(R.string.Player_BpsQuality_Auto));

        fillAssetInfo(lstVideo, m_llVideoPopupMenu, AssetType.Asset_Video);

        ArrayList<String> lstAudio = new ArrayList<String>();
        getAudioDescription(lstAudio);

        fillAssetInfo(lstAudio, m_llAudioPopupMenu, AssetType.Asset_Audio);

        ArrayList<String> lstSubtitle = new ArrayList<String>();
        getSubtitleDescription(lstSubtitle);

        fillAssetInfo(lstSubtitle, m_llSubtitlePopupMenu, AssetType.Asset_Subtitle);
        
   
        getAudioPropertyLog();
        
    }
    
   
    
    void initLayoutLeft() {

        m_rlChannel = (RelativeLayout) findViewById(R.id.rlChannel);
        m_lvChannel = (ListView) findViewById(R.id.lvChannel);
	    m_ibDolby      = (ImageButton) findViewById(R.id.ibDolby);
	    if(!m_bShowDolby)
	    	m_ibDolby.setVisibility(View.GONE);
        
	    m_ibDolby.setOnClickListener(new View.OnClickListener() {
            
            public void onClick(View v) {
                resetUIDisplayStartTime();
            	setDolbyEffect();
            }
        });


        if (m_bEnableChannel) {
            m_rlChannel.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    resetUIDisplayStartTime();

                    if (m_lvChannel.getVisibility() == View.INVISIBLE) {
                        m_lvChannel.setVisibility(View.VISIBLE);
                        m_rlChannel.setBackgroundResource(R.drawable.bg_player_left_channel_on);
                        fillChannelListContent();
                    } else {
                        m_lvChannel.setVisibility(View.INVISIBLE);
                        m_rlChannel.setBackgroundResource(R.drawable.bg_player_left_channel_off);
                    }

                }
            });
        } else {
            m_rlChannel.setVisibility(View.GONE);
            m_lvChannel.setVisibility(View.GONE);
        }

    }

    private void fillChannelListContent() {

        if (m_lvChannel.getAdapter() == null) {

			if (m_lstSelectURL == null) {
                m_lstSelectURL= new ArrayList<String>();
                ReadUrlInfo();
            }

            ArrayList<HashMap<String, String>> urlList = new ArrayList<HashMap<String, String>>();
            for (int i = 0; i < m_lstSelectURL.size(); i++) {
                HashMap<String, String> urlHash = new HashMap<String, String>();
                urlHash.put("url", m_lstSelectURL.get(i));
                urlList.add(urlHash);
            }

            m_lvChannel.setAdapter(new SimpleAdapter(this, urlList,
                    R.layout.simplelistitem1, new String[] { "url" },
                    new int[] { R.id.tvUrl }));
            
            m_lvChannel.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                        @Override
                        public void onItemClick(AdapterView<?> arg0, View arg1,
                                int arg2, long arg3) {

                            stopVideo();
                            playVideo(m_lstSelectURL.get(arg2));

                        }
                    });

        }
    }
	
    public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
        //return super.onCreateOptionsMenu(menu);
    }
	private void doFullScreen()
	{
		if (m_sdkPlayer != null) {
			m_bFullScreen = true;
	
			DisplayMetrics dm  = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			int mScreenWidth = dm.widthPixels;
			int mScreenHeight =  dm.heightPixels;
			
			ViewGroup.LayoutParams lp = m_svMain.getLayoutParams();
			lp.width = mScreenWidth;
			lp.height = mScreenHeight;
			m_svMain.setLayoutParams(lp);
			voLog.i(TAG, "Do Zoom, screen widtd is %d, height is %d", mScreenWidth, mScreenHeight);
		}
	}

	private void endFullScreen()
	{
		if (m_sdkPlayer != null) {
			m_bFullScreen = false;
			updateToDefaultVideoSize();
			//m_sdkPlayer.updateVideoAspectRatio(m_nVideoWidth, m_nVideoHeight);
			voLog.i(TAG, "End Zoom");
		}
	}
	private void doPanScan()
	{
		if (m_sdkPlayer != null) {
			 m_nZoom = 1;
			 int w = 0, h= 0;

			 DisplayMetrics dm  = new DisplayMetrics();
			 getWindowManager().getDefaultDisplay().getMetrics(dm);
			 int mScreenWidth = dm.widthPixels;
			 int mScreenHeight =  dm.heightPixels;
			 voLog.i(TAG, "mScreenWidth is %d, mScreenHeight is %d, mVideoWidth is %d, mVideoHeight is %d", dm.widthPixels, dm.heightPixels, m_nVideoWidth, m_nVideoHeight);
			 if (mScreenWidth * m_nVideoHeight > mScreenHeight * m_nVideoWidth)
			 {
				w = m_nVideoWidth;
				h = w * mScreenHeight /mScreenWidth;
				h &= ~0x3;
			 }else
			 {
				h = m_nVideoHeight;
				w = h * mScreenWidth / mScreenHeight;
				w &= ~0x3;
			 }
			
			 w = 160;
			 h = 120;
			 int nLeft = (m_nVideoWidth-160)/2;
			 int nTop = (m_nVideoHeight-120)/2;
			 Rect rect = new Rect(nLeft, nTop, w+nLeft, h+nTop);
			 m_sdkPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_ZOOMIN, rect);
			 voLog.i(TAG, "new Width is %d, height is %d.", w, h);
			 
			 updateToDefaultVideoSize();
//			 m_shMain.setFixedSize(w, h);
//			 ViewGroup.LayoutParams lp = m_svMain.getLayoutParams();
//			 lp.width = mScreenWidth;
//			 lp.height = mScreenHeight;
//			 m_svMain.setLayoutParams(lp);
			 voLog.i(TAG, "Pan & Scan, widtd is %d, height is %d", w, h);
		}
	}

	private void endPanScan()
	{
		if (m_sdkPlayer != null) {
			m_nZoom = 2;
	        int nWidth = m_nVideoWidth;
	        int nHeight = m_nVideoHeight;
	        //if(!m_nAspectRatio.equals(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO))
	        	m_sdkPlayer.setVideoAspectRatio(this.m_nAspectRatio);
	        
	        Rect rect = new Rect(0, 0, m_nVideoWidth, m_nVideoHeight);
			m_sdkPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_ORIGINAL, rect);
			m_shMain.setFixedSize(m_nVideoHeight, m_nVideoHeight);
			voLog.i(TAG, "Pan & Scan END, widtd is %d, height is %d", nWidth, nHeight);
		}
	}

    public boolean onOptionsItemSelected(MenuItem item) {
   
		switch (item.getItemId()) {
		case R.id.O_1:
		{
			doPanScan();
			return true;
		}
		case R.id.O_2:
		{
			endPanScan();
			return true;
		}
		case R.id.O_3:
		{

	        {
	            
	            View layoutVersion = LayoutInflater.from(Player.this).inflate(R.layout.version, null);
	            ListView lvVersion = (ListView) layoutVersion.findViewById(R.id.lvVersion);
	            ArrayList<HashMap<String, String>> lstVersion = new ArrayList<HashMap<String, String>>();
				      HashMap<String, String> hashmapVersion = new HashMap<String, String>();
	            
					try {
					
							PackageManager pm = getPackageManager();
							PackageInfo pinfo = pm.getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
							String versionCode = pinfo.versionName;
							int versionName = pinfo.versionCode;
							String SDKVersion = m_sdkPlayer.getVersion(VO_OSMP_MODULE_TYPE.VO_OSMP_MODULE_TYPE_SDK);
						
							hashmapVersion.put("version", "Build Release: V" + SDKVersion);
							lstVersion.add(hashmapVersion);
					} catch (NameNotFoundException e) {
					}
				 
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
	            
	            lvVersion.setAdapter(new SimpleAdapter(this, lstVersion, R.layout.simplelistitem1,
	                        new String[] { "version" }, new int[] { R.id.tvUrl }));
	            
	            AlertDialog adlgVersion = new AlertDialog.Builder(Player.this)
	                    .setTitle("Version").setView(layoutVersion)
	                    .setPositiveButton(R.string.str_OK, new OnClickListener() {
	                        public void onClick(DialogInterface dialog, int which) {
	                        }
	                    }).create();
	
	            adlgVersion.show();
	        }
        
	        return true;
		}
		default:
			return super.onOptionsItemSelected(item);
		}
    }
    
    private String formatTime(long time){
        long hour=time/60/60%60;
        long minute = time/60%60;
        long second = time%60;
        String strhour=hour<10 ? "0" + hour : "" + hour;
        String strminutes=minute<10 ? "0" + minute : "" + minute;
        String strseconds=second < 10 ? "0" + second : "" + second;
        String format=strhour+":"+strminutes+":"+strseconds;
        return format;
        
    }
    private void fillDownloaderProgramInfo() {
        voLog.i(TAG, "m_downloader Video count is %d, audio count is %d, subtitle count is %d, videx index is %d , audio index is %d , subtitle index is %d .", 
                m_asset.getVideoCount(), m_asset.getAudioCount(), m_asset.getSubtitleCount()
                , m_asset.getCurrentSelection().getVideoIndex(), m_asset.getCurrentSelection().getAudioIndex(), m_asset.getCurrentSelection().getSubtitleIndex());
        for (int i = 0; i < m_asset.getVideoCount(); i ++)
        {
            String videoPro = "Index is ";
            for (int j = 0; j < m_asset.getVideoProperty(i).getPropertyCount(); j++)
            {
                videoPro += "key = " + m_asset.getVideoProperty(i).getKey(j)
                        + ", value = " + (String)m_asset.getVideoProperty(i).getValue(j) + " ; "; 
            
            }
            voLog.i(TAG, "m_downloader " + videoPro);
        }

        LayoutInflater inflater;
        View layout;
        inflater = LayoutInflater.from(Player.this);
        layout = inflater.inflate(R.layout.asset_select, null);
        final Spinner sp_downloadSelectVideo = (Spinner)layout.findViewById(R.id.spDownloadSelectVideo);
        final Spinner sp_downloadSelectAudio = (Spinner)layout.findViewById(R.id.spDownloadSelectAudio);
        final Spinner sp_downloadSelectSubtitle  = (Spinner)layout.findViewById(R.id.spDownloadSelectSubtitle);
        
        TextView tv_downloadVideo = (TextView)layout.findViewById(R.id.tvDownloadSelectVideo);
        TextView tv_downloadAudio = (TextView)layout.findViewById(R.id.tvDownloadSelectAudio);
        TextView tv_downloadSubtitle  = (TextView)layout.findViewById(R.id.tvDownloadSelectSubtitle);
        
        if(m_asset.getVideoCount() == 0)
        {
            tv_downloadVideo.setVisibility(View.GONE);
            sp_downloadSelectVideo.setVisibility(View.GONE);
        }
        
        if(m_asset.getAudioCount() == 0)
        {
            tv_downloadAudio.setVisibility(View.GONE);
            sp_downloadSelectAudio.setVisibility(View.GONE);
        }
        
        if(m_asset.getSubtitleCount() == 0)
        {
            tv_downloadSubtitle.setVisibility(View.GONE);
            sp_downloadSelectSubtitle.setVisibility(View.GONE);
        }
        
        ArrayList<String> lstVideo = new ArrayList<String>();
        getVideoDescription(lstVideo);
        lstVideo.add(0, getResources().getString(R.string.Player_DownloadSelectVideo));

        ArrayAdapter<String> adapterVideo = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lstVideo);

        sp_downloadSelectVideo.setAdapter(adapterVideo);
        sp_downloadSelectVideo.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
              
                int index = sp_downloadSelectVideo.getSelectedItemPosition() - 1;
                VO_OSMP_RETURN_CODE nRet = m_asset.selectVideo(index);
                int rc = abManager.processReturnCode("selectVideo", nRet.getValue());
                if(rc == 1)
                {
                    return;
                }
                sp_downloadSelectAudio.setEnabled(true);
                sp_downloadSelectSubtitle.setEnabled(true);
                for (int i = 0; i < m_asset.getAudioCount(); i++) { 
                    if (!m_asset.isAudioAvailable(i))
                       sp_downloadSelectAudio.setEnabled(false);
                   
                }
                for (int i = 0; i < m_asset.getSubtitleCount(); i++) { 
                    if (!m_asset.isSubtitleAvailable(i))
                        sp_downloadSelectSubtitle.setEnabled(false);
                }
            }
            
            public void onNothingSelected(AdapterView<?> arg0){}
            
        });

        ArrayList<String> lstAudio = new ArrayList<String>();
        getAudioDescription(lstAudio);

        ArrayAdapter<String> adapterAudio = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lstAudio);

        sp_downloadSelectAudio.setAdapter(adapterAudio);
        sp_downloadSelectAudio.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
               
                int index = sp_downloadSelectAudio.getSelectedItemPosition();
                VO_OSMP_RETURN_CODE nRet = m_asset.selectAudio(index);
                int rc = abManager.processReturnCode("selectAudio", nRet.getValue());
                if(rc == 1)
                {
                    return;
                }
               
                sp_downloadSelectVideo.setEnabled(true);
                sp_downloadSelectSubtitle.setEnabled(true);
                for (int i = 0; i < m_asset.getVideoCount(); i++) { 
                    if (!m_asset.isVideoAvailable(i))
                       sp_downloadSelectVideo.setEnabled(false);
                   
                }
                for (int i = 0; i < m_asset.getSubtitleCount(); i++) { 
                    if (!m_asset.isSubtitleAvailable(i))
                        sp_downloadSelectSubtitle.setEnabled(false);
                }
            }
            
            public void onNothingSelected(AdapterView<?> arg0){}
            
        });

        
        
        ArrayList<String> lstSubtitle = new ArrayList<String>();
        getSubtitleDescription(lstSubtitle);

        ArrayAdapter<String> adapterSubtitle = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, lstSubtitle);

        sp_downloadSelectSubtitle.setAdapter(adapterSubtitle);
        sp_downloadSelectSubtitle.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int index =  sp_downloadSelectSubtitle.getSelectedItemPosition();
                m_asset.selectSubtitle(index);
                VO_OSMP_RETURN_CODE nRet = m_asset.selectSubtitle(index);
                int rc = abManager.processReturnCode("selectSubtitle", nRet.getValue());
                if(rc == 1)
                {
                    return;
                }
               
                sp_downloadSelectAudio.setEnabled(true);
                sp_downloadSelectVideo.setEnabled(true);
                for (int i = 0; i < m_asset.getAudioCount(); i++) { 
                    if (!m_asset.isAudioAvailable(i))
                       sp_downloadSelectAudio.setEnabled(false);
                   
                }
                for (int i = 0; i < m_asset.getVideoCount(); i++) { 
                    if (!m_asset.isVideoAvailable(i))
                        sp_downloadSelectVideo.setEnabled(false);
                }
            }
            
            public void onNothingSelected(AdapterView<?> arg0){}
            
        });
        
        
        m_adlgDownload = new AlertDialog.Builder(Player.this)
        .setTitle("Select Asset")
        .setView(layout)
        .setNegativeButton("Cancel", new OnClickListener() {
            // "Cancel" button stops player and exits
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                stopVideo();
                uninitPlayer();
                  m_bReturnSourceWindow=true;
                  
                  
                  SourceWindow();

                
            }
        })
        .setPositiveButton("Commit", new OnClickListener() {
            // "OK" button begins playback of inputted media source
            public void onClick(DialogInterface dialog, int which) {
                VO_OSMP_RETURN_CODE nRet = m_asset.commitSelection();
                int rc = abManager.processReturnCode("commitSelection", nRet.getValue());
                if(rc == 1)
                {
                    return;
                }
            }
        })
        .setOnKeyListener(new OnKeyListener() {
            public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                // "Back" button stops player and exits
                if (arg1 == KeyEvent.KEYCODE_BACK) { 
                    arg0.dismiss();
                    stopVideo();
                    uninitPlayer();
                      m_bReturnSourceWindow=true;
                      
                      
                      SourceWindow();

                    return true;
                } 

                return false;
            }
        })
        .create();
        m_adlgDownload.setCanceledOnTouchOutside(false);
        m_adlgDownload.show();
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
            DebugMode.logE(TAG, "Caught!", e);
        }
    }
    private void readURL() {
        m_lstSelectURL=null;
        m_lstSelectURL= new ArrayList<String>();
        ReadUrlInfo();
        getFiles(m_lstSelectURL,DOWNLOAD_PATH);
        m_lstSelectURL.add(0, getResources().getString(R.string.str_SelURL_FirstLine));
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, m_lstSelectURL);
        // adapter.setDropDownViewResource(R.layout.myspinner);
        lvURL.setAdapter(adapter);
    }

    
    private static boolean isAudioFile(String path) {
        for (String format : correctFormatSet) {
            if (path.contains(format)) { 
                return true;
            }
        }
        return false;
    }
    private String bitrateToString(int nBitr)
    {
        String s;
        nBitr/=1000;
        if(nBitr<1000) {
            s = Integer.toString(nBitr) + "kbps";
        }   
        else {
            String str = Float.toString(nBitr/1000.0f);
            int n = str.indexOf('.');
            if(n>=0 && n<str.length()-2)
                str = str.substring(0, n+2);
            
            s = (str + "mbps");
        }
        return s;
    }

   
}
