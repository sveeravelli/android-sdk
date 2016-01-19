package com.visualon.AppUI;


import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.visualon.AppPlayerCommonFeatures.Definition;
import com.visualon.AppPlayerCommonFeatures.voLog;
import com.visualon.AppPlayerSpecialFeatures.CSpecialFeatures;
import com.visualon.OSMPPlayer.VOOSMPRTSPStatistics;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ASPECT_RATIO;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_DOWNLOAD_STATUS;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SEI_INFO_FLAG;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_SOURCE_STREAMTYPE;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_ZOOM_MODE;

import java.util.Timer;
import java.util.TimerTask;

public class SpecialDialog extends Dialog implements OnClickListener{
    
   
    private static final String  TAG                          = "@@@OSMP+SpecialPlayer"; // Tag for VOLog messages
    private static final int   MSG_UPDATE__SPECIAL            = 2;
    
    private CSpecialFeatures   m_cSpecialPlayer               = null;
    private SeekBar            m_sbBrightness                 = null;
    private SeekBar            m_sbVolume                     = null;
    private Button             m_btnMute                      = null;
    private Button             m_btnSetStereo                 = null;
    private EditText           m_edtLeftChannel               = null;
    private EditText           m_edtRightChannel              = null;
    private EditText           m_edtUpdateUrl                 = null;
    private Button             m_btnUpdateUrl                 = null;
    private Button             m_btnZoom                      = null;
    private Button             m_btnAspectRatio               = null;
    private Button             m_btnIFrameOnly              = null;
    private CheckBox           m_chbIFrameOnly          = null;
    private CheckBox           m_chbDRMOfflineMode = null;
    private CheckBox           m_chbSEI                       = null;
    private CheckBox           m_chbRTSPStatistics            = null;
    private Timer              m_timerRTSP                    = null;
    private TimerTask          m_timerTaskRTSP                = null;
    private CheckBox           m_chbDownloadStatus            = null;
    private Timer              m_timerDownloadStatus          = null;
    private TimerTask          m_timerTaskDownloadStatus      = null;
    private TextView           m_tvVideoDecodingBitrate       = null;
    private TextView           m_tvAudioDecodingBitrate       = null;
    private Timer              m_timer                        = null;            
    private TimerTask          m_timerTask                    = null;
    private Button             m_btnMultiInstance             = null;
    private SurfaceView        m_svMultiInstance              = null;
    private final Context ctx;
    
    public SpecialDialog(Context context) {
        super(context);
        ctx = context;
    }
    
    public SpecialDialog(Context context, int theme) {
        super(context, theme);
        ctx = context;
        setContentView(R.layout.player_special);
        findViewById(R.id.btnDone).setOnClickListener(this);
        
        initBrightnessFeature();
        initVolumeMuteFeature();
        initStereoChannelFeature();
        initUpdateUrlFeature();
        initZoomFeature();
        initAspectRatioFeature();
        initDRMOfflineModeFeature();
        initSEIFeature();
        initRTSPStatisticsFeature();
        initDownloadStatusFeature();
        initMultiInstancePlayerFeature();
        m_tvVideoDecodingBitrate = (TextView) findViewById(R.id.tvVideoDecodingBitrate);
        m_tvAudioDecodingBitrate = (TextView) findViewById(R.id.tvAudioDecodingBitrate);

    }
    
    public void setCSpecialPlayer(CSpecialFeatures specialPlayer) {
        m_cSpecialPlayer = specialPlayer;

        initIFrameOnlyFeature();
    }
    
    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch(v.getId()){  
        case R.id.btnDone:  
            hide();
            break;  
        }
    }
    
    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        hide();
    }
    
    @Override
    public void hide() {
        // TODO Auto-generated method stub
        super.hide();
        stopTimer();
        if (m_cSpecialPlayer != null)
            m_cSpecialPlayer.stopMultiInstancePlayer();
        if (m_btnMultiInstance != null)
            m_btnMultiInstance.setText(Definition.START);
    }

   
    @Override
    public void dismiss() {
        // TODO Auto-generated method stub
        stopRTSPStatisticsTimer();
        stopDownloadStatusTimer();
        super.dismiss();
        sIFrameChecked = false;
        sIFrameSpeed = 1.0f;
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub
        super.show();
        startTimer();
    }
    
    
    private Handler handler = new Handler() {
        
        public void handleMessage(Message msg)
        {
            if (msg.what == MSG_UPDATE__SPECIAL) {
                doUpdateSpecial();
            }
        }
    };
    
    private void initBrightnessFeature() {
        m_sbBrightness = (SeekBar) findViewById(R.id.sbBrightness);
        m_sbBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                int nCurrent = seekBar.getProgress();
                if(m_cSpecialPlayer != null){
                    m_cSpecialPlayer.setScreenBrightness(nCurrent);
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {}
        });
    }
    
    private void initVolumeMuteFeature() {
        m_sbVolume = (SeekBar) findViewById(R.id.sbVolume);
        m_btnMute = (Button) findViewById(R.id.btnMute);
        m_btnMute.setText(Definition.MUTE);
        m_btnMute.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(m_cSpecialPlayer != null){
                    if(m_btnMute.getText() == Definition.MUTE){
                        m_cSpecialPlayer.mute();
                        m_btnMute.setText(Definition.UNMUTE);
                        m_sbVolume.setEnabled(false);
                    }    
                    else{
                        m_cSpecialPlayer.unmute();
                        m_btnMute.setText(Definition.MUTE);
                        m_sbVolume.setEnabled(true);
                    }    
                } 
            }
        });
        
        m_sbVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                float nCurrent = seekBar.getProgress();
                float nMax = seekBar.getMax();
                if(m_cSpecialPlayer != null){
                    m_cSpecialPlayer.setVolume((nCurrent/nMax));
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                    boolean fromUser) {
                // TODO Auto-generated method stub
                
            }
        });
        
    }
    
    private void initStereoChannelFeature() {
        m_btnSetStereo = (Button) findViewById(R.id.btnSetStereo);
        m_edtLeftChannel = (EditText) findViewById(R.id.edtLeftChannel);
        m_edtRightChannel = (EditText) findViewById(R.id.edtRightChannel);
        m_btnSetStereo.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m_edtLeftChannel.getText() == null || m_edtRightChannel.getText() == null)
                    return;
                try {
                    float leftChannelValue = Float.parseFloat(m_edtLeftChannel.getText().toString());
                    float rightChannelValue = Float.parseFloat(m_edtRightChannel.getText().toString());
                    if(m_cSpecialPlayer != null) 
                        m_cSpecialPlayer.setVolume(leftChannelValue, rightChannelValue);
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
	
    private void initUpdateUrlFeature() {
        m_edtUpdateUrl = (EditText) findViewById(R.id.edtUpdateUrl);
        m_btnUpdateUrl = (Button) findViewById(R.id.btnUpdateUrl);
        m_btnUpdateUrl.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(m_cSpecialPlayer != null){
                    String updateUrl = m_edtUpdateUrl.getText().toString();
                    m_cSpecialPlayer.updateSourceURL(updateUrl);
                }
           }
        });
    }
    
  
    
    private void initZoomFeature() {
        m_btnZoom = (Button) findViewById(R.id.btnZoomMode);
        m_btnZoom.setText(Definition.ZOOM_MODE_LETTERBOX);
        m_btnZoom.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(m_cSpecialPlayer != null){
                    if (m_btnZoom.getText() == Definition.ZOOM_MODE_LETTERBOX) {
                        m_cSpecialPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_PANSCAN, null);
                        m_btnZoom.setText(Definition.ZOOM_MODE_PANSCAN);
                    } else if (m_btnZoom.getText() == Definition.ZOOM_MODE_PANSCAN) {
                        m_cSpecialPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_FITWINDOW, null);
                        m_btnZoom.setText(Definition.ZOOM_MODE_FITWINDOW);
                    } else if (m_btnZoom.getText() == Definition.ZOOM_MODE_FITWINDOW) {
                    	int videoWidth = m_cSpecialPlayer.getVideoWidth();
                    	int videoHeight = m_cSpecialPlayer.getVideoHeight();
                    	if (videoWidth > 0 && videoHeight > 0) {
	                        Rect rect = new Rect(videoWidth / 4, videoHeight / 4, videoWidth * 3 / 4, videoHeight * 3 / 4);
	                        m_cSpecialPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_ZOOMIN, rect);
                    	}
                        m_btnZoom.setText(Definition.ZOOM_MODE_ZOOMIN);
                    } else if (m_btnZoom.getText() == Definition.ZOOM_MODE_ZOOMIN) {
                        m_cSpecialPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_ORIGINAL, null);
                        m_btnZoom.setText(Definition.ZOOM_MODE_ORIGINAL);
                    }  else if(m_btnZoom.getText() == Definition.ZOOM_MODE_ORIGINAL) {
                        m_cSpecialPlayer.setZoomMode(VO_OSMP_ZOOM_MODE.VO_OSMP_ZOOM_LETTERBOX,null);
                        m_btnZoom.setText(Definition.ZOOM_MODE_LETTERBOX);
                    }
                    
                       
                } 
            }
        });
    }
    
    private void initAspectRatioFeature() {
        m_btnAspectRatio = (Button) findViewById(R.id.btnAspectRatio);
        m_btnAspectRatio.setText(Definition.ASPECT_RATIO_AUTO);
        m_btnAspectRatio.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if(m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_AUTO) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_11);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_11);
                } else if (m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_11) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_43);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_43);
                } else if (m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_43) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_169);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_169);
                } else if (m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_169) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_21);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_21);
                } else if (m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_21) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_2331);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_2331);
                } else if (m_btnAspectRatio.getText() == Definition.ASPECT_RATIO_2331) {
                    m_cSpecialPlayer.setVideoAspectRatio(VO_OSMP_ASPECT_RATIO.VO_OSMP_RATIO_AUTO);
                    m_btnAspectRatio.setText(Definition.ASPECT_RATIO_AUTO);
                }
            }
        });
    }
    
    private static boolean sIFrameChecked = false;
    private static float sIFrameSpeed = 1.0f;
    private void initIFrameOnlyFeature()
    {
        if ((null == m_cSpecialPlayer) || !m_cSpecialPlayer.canPlayIFrameOnly())
            return;
        final float SCALE = ctx.getResources().getDisplayMetrics().density; 
        final int DP_WIDTH = (int)(100 * SCALE + 0.5);
        final int DP_HEIGHT = (int)(50 * SCALE + 0.5);
        final RelativeLayout rlIFrameOnly = (RelativeLayout) findViewById(R.id.rlIFrameOnly);
        rlIFrameOnly.removeAllViews();
        
        final CheckBox cbIFrameOnly = new CheckBox(ctx);
        final RelativeLayout.LayoutParams cbLP = new RelativeLayout.LayoutParams(DP_WIDTH, DP_HEIGHT);
        cbIFrameOnly.setLayoutParams(cbLP);
        cbIFrameOnly.setText("IFrame");
        cbIFrameOnly.setChecked(sIFrameChecked);
        
        final EditText edIFrameOnly = new EditText(ctx);
        final RelativeLayout.LayoutParams edLP = new RelativeLayout.LayoutParams(DP_WIDTH, DP_HEIGHT);
        edLP.leftMargin = DP_WIDTH;
        edIFrameOnly.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_DECIMAL | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
        edIFrameOnly.setText(""+sIFrameSpeed);
        edIFrameOnly.setImeOptions(EditorInfo.IME_ACTION_DONE);
        edIFrameOnly.setLayoutParams(edLP);
        
        
        final Button btnIFrameOnly = new Button(ctx);
        final RelativeLayout.LayoutParams btnLP = new RelativeLayout.LayoutParams(DP_WIDTH, DP_HEIGHT);
        btnLP.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, rlIFrameOnly.getId());
        btnIFrameOnly.setText("SetIFrame");
        btnIFrameOnly.setGravity(Gravity.CENTER);
        btnIFrameOnly.setLayoutParams(btnLP);
        
        btnIFrameOnly.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (edIFrameOnly.getText() == null)
                {
                    return;
                }
                sIFrameSpeed = Float.parseFloat(edIFrameOnly.getText().toString());
                sIFrameChecked = cbIFrameOnly.isChecked();
                
                m_cSpecialPlayer.setPlayIFrameOnly(sIFrameChecked, sIFrameSpeed);
            }
        });
        
        rlIFrameOnly.addView(edIFrameOnly);
        rlIFrameOnly.addView(cbIFrameOnly);
        rlIFrameOnly.addView(btnIFrameOnly);
    }
    
    private void initDRMOfflineModeFeature()
    {
        m_chbDRMOfflineMode = (CheckBox) findViewById(R.id.chDRMOfflineMode);
        m_chbDRMOfflineMode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (null != m_cSpecialPlayer)
                {
                    m_cSpecialPlayer.enableDRMOfflineMode(isChecked);
                }
            }
        });
    }
    
    private void initSEIFeature() {
        m_chbSEI = (CheckBox) findViewById(R.id.chbSEI);
        m_chbSEI.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                // TODO Auto-generated method stub
                if(m_cSpecialPlayer != null){
                    if(isChecked){
                        m_cSpecialPlayer.enableSEI(VO_OSMP_SEI_INFO_FLAG.VO_OSMP_SEI_INFO_PIC_TIMING);
                        m_cSpecialPlayer.startSEINotification(5000);
                    }else{
                        m_cSpecialPlayer.stopSEINotification();
                    }
                }
            }
            
        });
    }

    private void initRTSPStatisticsFeature() {
        m_chbRTSPStatistics = (CheckBox) findViewById(R.id.chbRTSPStatistics);
        m_chbRTSPStatistics.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                // TODO Auto-generated method stub
                if(m_cSpecialPlayer != null){
                    if(isChecked){
                       startRTSPStatisticsTimer();
                    }else{
                       stopRTSPStatisticsTimer();
                    }
                }
            }
            
        });
    }
    
    private void startRTSPStatisticsTimer() {
       
        if (m_timerTaskRTSP != null)
            m_timerTaskRTSP = null;
        m_timerTaskRTSP = new TimerTask() {
            public void run() {
                VOOSMPRTSPStatistics RTSPStatistics = m_cSpecialPlayer.getRTSPStatistics();
                if (RTSPStatistics != null) {
                    try {
                        String log ="RTSP Statistics - PacketReceived: %d, PacketDuplicated: %d, PacketLost: %d, PacketSent: %d, AverageJitter: %d, AverageLatency: %d.";
                        log = String.format(log, RTSPStatistics.getPacketReceived(), RTSPStatistics.getPacketDuplicated(), RTSPStatistics.getPacketLost(), RTSPStatistics.getPacketSent(),
                                RTSPStatistics.getAverageJitter(), RTSPStatistics.getAverageLatency());   
                    voLog.i(TAG, log);
                    } catch (Exception e) {}
                }
            }
        };
        
        if(m_timerRTSP == null)
            m_timerRTSP = new Timer();
        
        m_timerRTSP.schedule(m_timerTaskRTSP, 0, 10000);
    }
    
    private void stopRTSPStatisticsTimer() {
        if(m_timerRTSP != null) {
            m_timerRTSP.cancel();
            m_timerRTSP.purge();
            m_timerRTSP = null;
            m_timerTaskRTSP = null;
        }
    }
    
    private void initDownloadStatusFeature() {
    	 m_chbDownloadStatus = (CheckBox) findViewById(R.id.chbDownloadStatus);
    	 m_chbDownloadStatus.setOnCheckedChangeListener(new OnCheckedChangeListener(){

             @Override
             public void onCheckedChanged(CompoundButton buttonView,
                     boolean isChecked) {
                 // TODO Auto-generated method stub
                 if(m_cSpecialPlayer != null){
                     if(isChecked){
                        startDownloadStatusTimer();
                     }else{
                        stopDownloadStatusTimer();
                     }
                 }
             }
             
         });
    }
    
    private void startDownloadStatusTimer() {
        
        if (m_timerTaskDownloadStatus != null)
        	m_timerTaskDownloadStatus = null;
        m_timerTaskDownloadStatus = new TimerTask() {
            public void run() {
            	int validBufferDuation = m_cSpecialPlayer.getValidBufferDuration();
                voLog.i(TAG, "The valid buffer left can be used to playback is "+validBufferDuation);
                VO_OSMP_DOWNLOAD_STATUS audioDownloadStatus = m_cSpecialPlayer.getDownloadStatus(VO_OSMP_SOURCE_STREAMTYPE.VO_OSMP_SS_AUDIO);
                VO_OSMP_DOWNLOAD_STATUS videoDownloadStatus = m_cSpecialPlayer.getDownloadStatus(VO_OSMP_SOURCE_STREAMTYPE.VO_OSMP_SS_VIDEO);
                VO_OSMP_DOWNLOAD_STATUS subtitleDownloadStatus = m_cSpecialPlayer.getDownloadStatus(VO_OSMP_SOURCE_STREAMTYPE.VO_OSMP_SS_SUBTITLE);
                voLog.i(TAG, "The video download status is " + videoDownloadStatus + ", the audio download status is " + audioDownloadStatus + ", the subtitle download status is " + subtitleDownloadStatus + ".");
            }
        };
        
        if(m_timerDownloadStatus == null)
        	m_timerDownloadStatus = new Timer();
        
        m_timerDownloadStatus.schedule(m_timerTaskDownloadStatus, 0, 1000);
    }
    
    private void stopDownloadStatusTimer() {
        if(m_timerDownloadStatus != null) {
        	m_timerDownloadStatus.cancel();
        	m_timerDownloadStatus.purge();
        	m_timerDownloadStatus = null;
        	m_timerTaskDownloadStatus = null;
        }
    }
    
    private void doUpdateSpecial(){
        int[] j=  m_cSpecialPlayer.getVideoDecodingBitrate();
        int[] i=  m_cSpecialPlayer.getAudioDecodingBitrate();
        if (j == null || i == null)
            return;
        Float videoBps=(float)j[0];
        Float audioBps=(float)i[0];
        String strVideoBps=videoBps.toString();
        String strAudioBps=audioBps.toString();
        strVideoBps=strVideoBps.substring(0, strVideoBps.length()-2);
        strAudioBps=strAudioBps.substring(0, strAudioBps.length()-2);
       
        m_tvVideoDecodingBitrate.setText(strVideoBps);
        m_tvAudioDecodingBitrate.setText(strAudioBps);
    }
    
    private void startTimer() {
        if(m_timerTask != null)
            m_timerTask = null;
        m_timerTask = new TimerTask() {
            public void run() {
                handler.sendEmptyMessage(MSG_UPDATE__SPECIAL);
            }
        };
        
        if(m_timer == null)
            m_timer = new Timer();
        
        m_timer.schedule(m_timerTask, 0, 1000);
    }
    
    private void stopTimer(){
        if(m_timer != null) {
            m_timer.cancel();
            m_timer.purge();
            m_timer = null;
            m_timerTask = null;
        }
    }
    
    private void initMultiInstancePlayerFeature(){
        
        m_btnMultiInstance = (Button) findViewById(R.id.btnMultiInstance);
        m_svMultiInstance = (SurfaceView) findViewById(R.id.svMultiInstance);
        m_btnMultiInstance.setText(Definition.START);
        m_btnMultiInstance.setOnClickListener(new View.OnClickListener() {
            
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (m_btnMultiInstance.getText() == Definition.START) {
                    m_svMultiInstance.setVisibility(View.VISIBLE);
                    m_cSpecialPlayer.startMultiInstancePlayer(m_svMultiInstance);
                    m_btnMultiInstance.setText(Definition.STOP);
                } else if (m_btnMultiInstance.getText() == Definition.STOP) {
                    m_cSpecialPlayer.stopMultiInstancePlayer();
                    m_btnMultiInstance.setText(Definition.START);
                    m_svMultiInstance.setVisibility(View.INVISIBLE);
                }
            }
        });
        
    }
   
}
