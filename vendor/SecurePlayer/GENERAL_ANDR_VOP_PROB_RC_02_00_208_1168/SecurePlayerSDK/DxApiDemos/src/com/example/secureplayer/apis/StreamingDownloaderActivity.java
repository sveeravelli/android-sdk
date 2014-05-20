package com.example.secureplayer.apis;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.secureplayer.R;
import com.example.secureplayer.Utils;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloader;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloaderInitParam;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloaderListener;
import com.visualon.VOOSMPStreamingDownloaderImpl.VOOSMPStreamingDownloaderImpl;

public class StreamingDownloaderActivity extends Activity implements VOOSMPStreamingDownloaderListener{

	private AlertDialog          			m_adlgDownload           = null;

	private static final String 			TAG 									= "com.example.secureplayer";
	private static final String  			STRING_ASSETPROPERTYNAME_VIDEO         = "Bps";
	private static final String  			STRING_ASSETPROPERTYNAME_AUDIO         = "Audio";
	private static final String  			STRING_ASSETPROPERTYNAME_SUBTITLE      = "Subt";

	private static VOOSMPStreamingDownloader 		s_streamingDownloader 	= null;
	private static VOCommonPlayerAssetSelection 	s_asset           		= null;
	private static StreamingDownloaderState 		s_currState				= StreamingDownloaderState.IDLE;

	private static String					s_downloadUrl = "";
	private static String 					s_destDirectory = "";
	private static String 					s_destDownloadedPlaylist = "";

	private static int            			s_downloadCurrent		= 0;
	private static int             			s_downloadTotal			= 0;

	private ProgressBar						m_pbDownloaded;								
	private TextView            			m_tvDownloadCurrent;
	private TextView            			m_tvDownloadTotal;
	private EditText            			m_edURL;
	private Button	            			m_btnDownload;
	private Button	            			m_btnPause;
	private Button	            			m_btnResume;
	private Button	            			m_btnCancel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.streaming_downloader);
		m_edURL = (EditText)findViewById(R.id.etDownloadUrl);
		m_tvDownloadCurrent = (TextView)findViewById(R.id.tvDownloadCurrent);
		m_tvDownloadTotal = (TextView)findViewById(R.id.tvDownloadTotal);
		m_pbDownloaded = (ProgressBar)findViewById(R.id.progressBar1);

		m_btnDownload = (Button)findViewById(R.id.btnDownload);
		m_btnDownload.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				VO_OSMP_RETURN_CODE nRet;

				s_downloadUrl = m_edURL.getText().toString();
				if(!TextUtils.isEmpty(s_downloadUrl))
				{
					nRet = initStreamingDownloader();
					if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
					{
						Log.e(TAG, "StreamingDownloader: init failed!! with error = " + nRet);
					}

					nRet = openStreamingDownloader(s_downloadUrl);
					if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
					{
						Log.e(TAG, "StreamingDownloader: open failed!! with error = " + nRet);
					}
				}
			}
		});

		m_btnPause = (Button)findViewById(R.id.btnPause);
		m_btnPause.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {

				VO_OSMP_RETURN_CODE nRet;
				if(s_streamingDownloader != null)
				{
					nRet = s_streamingDownloader.pause();
					if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
					{
						Log.e(TAG, "StreamingDownloader: pause failed!! with error = " + nRet);								
					}
					else
					{
						setState(StreamingDownloaderState.PAUSED);
					}
				}
			}
		});
		
		m_btnResume = (Button)findViewById(R.id.btnResume);
		m_btnResume.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {			

				VO_OSMP_RETURN_CODE nRet;
				if(s_streamingDownloader != null)
				{
					nRet = s_streamingDownloader.resume();
					if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
					{
						Log.v(TAG, "StreamingDownloader: resume failed!! with error = " + nRet);								
					}
					else
					{
						setState(StreamingDownloaderState.DOWNLOADING);
					}
				}
			}
		});
		
		m_btnCancel = (Button)findViewById(R.id.btnCancel);
		m_btnCancel.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {			
				stopDownloader();
			}
		});
		

		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable() {
					@Override
					public void run() {

						if(s_streamingDownloader != null){
							VOOSMPStreamingDownloaderProgressInfo info = s_streamingDownloader.getDuration();
							if(info != null)
							{
								s_downloadCurrent = info.getDownloadedStreamDuration();
								s_downloadTotal = info.getTotalStreamDuration();
							}
						}
						((TextView)findViewById(R.id.tvDownloadCurrent)).setText(Integer.toString(s_downloadCurrent));
						((TextView)findViewById(R.id.tvDownloadTotal)).setText(Integer.toString(s_downloadTotal));
						m_pbDownloaded.setProgress(s_downloadTotal > 0 ? 100*s_downloadCurrent/s_downloadTotal : 0);
					}
				});		
			}
		};
		timer.schedule(timerTask, 0, 200);


		// Copy license file, 
		loadLicenseFile("voVidDec.dat");

	}
	private void loadLicenseFile( String fileName)
	{
		try {
			String filePath = getFilesDir().getParent() + "/" + fileName;
			InputStream InputStreamis  = getAssets().open(fileName);
			File desFile = new File(filePath);
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

	@Override
	protected void onResume() 
	{
		super.onResume();

		setState(s_currState);

		m_edURL.setText(s_downloadUrl);
		m_tvDownloadCurrent.setText(Integer.toString(s_downloadCurrent));
		m_tvDownloadTotal.setText(Integer.toString(s_downloadTotal));	
		m_pbDownloaded.setProgress(s_downloadTotal > 0 ? 100*s_downloadCurrent/s_downloadTotal : 0);
	};

	private VO_OSMP_RETURN_CODE initStreamingDownloader()
	{ 
		VO_OSMP_RETURN_CODE nRet;

		String apkPath = getFilesDir().getParent() + "/lib/";	
		s_streamingDownloader = new VOOSMPStreamingDownloaderImpl();
		VOOSMPStreamingDownloaderInitParam initParam = new VOOSMPStreamingDownloaderInitParam();
		initParam.setContext(this);
		initParam.setLibraryPath(apkPath);
		nRet = s_streamingDownloader.init(this, initParam);

		return nRet;
	}
	private VO_OSMP_RETURN_CODE openStreamingDownloader(String strPath)
	{ 	
		VO_OSMP_RETURN_CODE nRet;
		String dirPath = Utils.GenerateDownloadFolderName(strPath);
		//creates dir if doesn't exists
		File dest = new File(dirPath);
		if(!dest.exists()) {
			if(! dest.mkdirs())
			{
				Log.e(TAG, "StreamingDownloader: dest.mkdirs() FAILED.");
				AlertDialog dialog = new AlertDialog.Builder(StreamingDownloaderActivity.this)
				.setTitle("Create destination folder failed")
				.setNegativeButton("OK", new OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {}
				}).create();
				dialog.show();
				//stop (reset) downloader and return general error.
				stopDownloader();
				return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_STATUS;
			}
		}

		s_destDirectory = dirPath;	
		nRet = s_streamingDownloader.open(strPath, 0, dirPath);
		if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
		{			
			AlertDialog dialog = new AlertDialog.Builder(StreamingDownloaderActivity.this)
			.setTitle("Download open failed")
			.setMessage("Error = " + nRet)
			.setNegativeButton("OK", new OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {        			
				}
			}).create();
			dialog.show();
			return nRet;
		}
		return nRet;        		
	}

	private void fillDownloaderProgramInfo() {
		/* Printouts */
		/*Log.i(TAG, "StreamingDownloader: m_downloader Video count is "+ m_asset.getVideoCount()  +
				", audio count is "+ m_asset.getAudioCount()  +
				", subtitle count is "+  m_asset.getSubtitleCount() +
				", videx index is "+  m_asset.getCurrentSelection().getVideoIndex() +
				", audio index is "+  m_asset.getCurrentSelection().getAudioIndex() +
				", subtitle index is " + m_asset.getCurrentSelection().getSubtitleIndex());
		for (int i = 0; i < m_asset.getVideoCount(); i ++)
		{
			String videoPro = "Index is ";
			for (int j = 0; j < m_asset.getVideoProperty(i).getPropertyCount(); j++)
			{
				videoPro += "key = " + m_asset.getVideoProperty(i).getKey(j) + ", value = " + (String)m_asset.getVideoProperty(i).getValue(j) + " ; "; 
			}
			Log.i(TAG, "StreamingDownloader: m_downloader " + videoPro);
		}*/

		LayoutInflater inflater;
		View layout;
		inflater = LayoutInflater.from(StreamingDownloaderActivity.this);
		layout = inflater.inflate(R.layout.asset_select, null);
		final Spinner sp_downloadSelectVideo = (Spinner)layout.findViewById(R.id.spDownloadSelectVideo);
		final Spinner sp_downloadSelectAudio = (Spinner)layout.findViewById(R.id.spDownloadSelectAudio);
		final Spinner sp_downloadSelectSubtitle  = (Spinner)layout.findViewById(R.id.spDownloadSelectSubtitle);

		TextView tv_downloadVideo = (TextView)layout.findViewById(R.id.tvDownloadSelectVideo);
		TextView tv_downloadAudio = (TextView)layout.findViewById(R.id.tvDownloadSelectAudio);
		TextView tv_downloadSubtitle  = (TextView)layout.findViewById(R.id.tvDownloadSelectSubtitle);

		if(s_asset.getVideoCount() == 0)
		{
			tv_downloadVideo.setVisibility(View.GONE);
			sp_downloadSelectVideo.setVisibility(View.GONE);
		}

		if(s_asset.getAudioCount() == 0)
		{
			tv_downloadAudio.setVisibility(View.GONE);
			sp_downloadSelectAudio.setVisibility(View.GONE);
		}

		if(s_asset.getSubtitleCount() == 0)
		{
			tv_downloadSubtitle.setVisibility(View.GONE);
			sp_downloadSelectSubtitle.setVisibility(View.GONE);
		}

		ArrayList<String> lstVideo = new ArrayList<String>();
		getVideoDescription(lstVideo);
		lstVideo.add(0, getResources().getString(R.string.BpsQuality_Auto));

		ArrayAdapter<String> adapterVideo = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lstVideo);

		sp_downloadSelectVideo.setAdapter(adapterVideo);
		sp_downloadSelectVideo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				int index = sp_downloadSelectVideo.getSelectedItemPosition() - 1;
				VO_OSMP_RETURN_CODE nRet = s_asset.selectVideo(index);
				if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
				{
					Log.e(TAG, "StreamingDownloader: Download module selectVideo FAILED with error = " + nRet);
					return;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0){}

		});

		ArrayList<String> lstAudio = new ArrayList<String>();
		getAudioDescription(lstAudio);

		ArrayAdapter<String> adapterAudio = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lstAudio);

		sp_downloadSelectAudio.setAdapter(adapterAudio);
		sp_downloadSelectAudio.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

				int index = sp_downloadSelectAudio.getSelectedItemPosition();
				VO_OSMP_RETURN_CODE nRet = s_asset.selectAudio(index);
				if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
				{
					Log.e(TAG, "StreamingDownloader: Download module selectAudio FAILED with error = " + nRet);
					return;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0){}

		});

		ArrayList<String> lstSubtitle = new ArrayList<String>();
		getSubtitleDescription(lstSubtitle);

		ArrayAdapter<String> adapterSubtitle = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, lstSubtitle);

		sp_downloadSelectSubtitle.setAdapter(adapterSubtitle);
		sp_downloadSelectSubtitle.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int index =  sp_downloadSelectSubtitle.getSelectedItemPosition();
				s_asset.selectSubtitle(index);
				VO_OSMP_RETURN_CODE nRet = s_asset.selectSubtitle(index);
				if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
				{
					Log.e(TAG, "StreamingDownloader: Download module selectSubtitle FAILED with error = " + nRet);
					return;
				}
			}

			public void onNothingSelected(AdapterView<?> arg0){}

		});


		m_adlgDownload = new AlertDialog.Builder(StreamingDownloaderActivity.this)
		.setTitle("Select Asset")
		.setView(layout)
		.setNegativeButton("Cancel", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {}})
		.setPositiveButton("Start", new OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				VO_OSMP_RETURN_CODE nRet = s_asset.commitSelection();
				if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
				{
					Log.e(TAG, "StreamingDownloader: Download module commitSelection FAILED with error = " + nRet);;
					return;
				}
				if (s_streamingDownloader != null)
				{
					nRet = s_streamingDownloader.start();
					if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE)
					{
						Log.v(TAG, "StreamingDownloader: Start FAILED with error = " + nRet);
					}
					else
					{
						setState(StreamingDownloaderState.DOWNLOADING);
					}
				}
			}
		})
		.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
				if (arg1 == KeyEvent.KEYCODE_BACK) { 
					arg0.dismiss();
					return true;
				} 
				return false;
			}
		})
		.create();
		m_adlgDownload.show();
	}
	private void getVideoDescription(ArrayList<String> lstString) {

		if (lstString == null || s_asset == null)
			return;
		
		int nAssetCount = s_asset.getVideoCount();
		if (nAssetCount == 0) 
			return;

		int nDefaultIndex = 0;
		for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
			VOOSMPAssetProperty propImpl =s_asset.getVideoProperty(nAssetIndex);
			String strDescription;
			int nPropertyCount = propImpl.getPropertyCount();
			if (nPropertyCount == 0) {
				strDescription = STRING_ASSETPROPERTYNAME_VIDEO + Integer.toString(nDefaultIndex++);
			} else {
				final int KEY_DESCRIPTION_INDEX = 2;
				strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
			}
			lstString.add(strDescription);
		}
	}

	private void getAudioDescription(ArrayList<String> lstString) {

		if (lstString == null || s_asset == null)
			return;

		int nAssetCount = s_asset.getAudioCount();
		if (nAssetCount == 0) 
			return;

		int nDefaultIndex = 0;
		for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
			VOOSMPAssetProperty propImpl = s_asset.getAudioProperty(nAssetIndex);
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
	}
	private void getSubtitleDescription(ArrayList<String> lstString) {

		if (lstString == null || s_asset == null)
			return;

		int nAssetCount = s_asset.getSubtitleCount();
		if (nAssetCount == 0) 
			return;

		int nDefaultIndex = 0;
		for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
			VOOSMPAssetProperty propImpl = s_asset.getSubtitleProperty(nAssetIndex);
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
	}

	private void setState(StreamingDownloaderState state)
	{
		s_currState = state;
		switch(s_currState)
		{
		case DOWNLOADING:
			m_btnCancel.setEnabled(true);
			m_btnDownload.setEnabled(false);
			m_btnPause.setEnabled(true);
			m_btnResume.setEnabled(false);
			break;
		case IDLE:
			m_btnCancel.setEnabled(false);
			m_btnDownload.setEnabled(true);
			m_btnPause.setEnabled(false);
			m_btnResume.setEnabled(false);
			break;
		case PAUSED:
			m_btnCancel.setEnabled(true);
			m_btnDownload.setEnabled(false);
			m_btnPause.setEnabled(false);
			m_btnResume.setEnabled(true);
			break;	
		}
	}
	private void stopDownloader()
	{
		VO_OSMP_RETURN_CODE nRet;
		if(s_streamingDownloader != null)
		{
			if(s_currState == StreamingDownloaderState.DOWNLOADING)
			{
				nRet = s_streamingDownloader.stop();
				if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
				{
					Log.e(TAG, "StreamingDownloader: stop failed!! with error = " + nRet);								
				}
			}
			nRet = s_streamingDownloader.close();
			if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
			{
				Log.e(TAG, "StreamingDownloader: close failed!! with error = " + nRet);							
			}
			nRet = s_streamingDownloader.destroy();
			if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) 
			{
				Log.e(TAG, "StreamingDownloader: destroy failed!! with error = " + nRet);								
			}
		}
		
		setState(StreamingDownloaderState.IDLE);
		resetDownloadedParameters();		
	}
	private void resetDownloadedParameters()
	{
		s_streamingDownloader = null;
		s_destDownloadedPlaylist = "";
		s_destDirectory = "";
		s_downloadCurrent = 0;
		s_downloadTotal = 0;
	}

	@Override
	public VO_OSMP_RETURN_CODE onVOStreamingDownloaderEvent( VO_OSMP_CB_STREAMING_DOWNLOADER_EVENT_ID id, int param1, int param2, Object param3)
	{	
		Log.v(TAG, "onVOStreamingDownloaderEvent: id =  " + id + ", param1 = " + param1 + ", param2 = " + param2 + ", param3 = " + param3);
	
		switch(id)
		{
			case VO_OSMP_CB_STREAMING_DOWNLOADER_OPEN_COMPLETE:
			{ 
				s_asset = s_streamingDownloader;
				fillDownloaderProgramInfo();
				break;
			}
			case VO_OSMP_CB_STREAMING_DOWNLOADER_MANIFEST_OK:
			{				
				s_destDownloadedPlaylist = (String)param3;
	
				File playlist = new File(s_destDirectory + File.separator + "MainPlaylist.txt");
				try {
					playlist.createNewFile();
					if(playlist.exists())
					{
						FileWriter fo = new FileWriter(playlist);              
						fo.write(s_destDownloadedPlaylist);
						fo.close();
						//Log.v("file created: %s ", playlist.getAbsolutePath());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
			}
			case VO_OSMP_CB_STREAMING_DOWNLOADER_END  :
			{
				String toDisplay = s_downloadUrl;
				if(s_downloadUrl.length() > 40){
					toDisplay = s_downloadUrl.substring(0, 20) + "..." + s_downloadUrl.substring(s_downloadUrl.length() - 20, s_downloadUrl.length());
				}
	
				Toast.makeText(this, "Download of \"" + toDisplay + "\" completed successfully", Toast.LENGTH_LONG).show();
	
				stopDownloader();			
				break;
			}
		
			//errors	
			case VO_OSMP_CB_STREAMING_DOWNLOADER_DOWNLOAD_MANIFEST_FAIL:
			case VO_OSMP_CB_STREAMING_DOWNLOADER_WRITE_MANIFEST_FAIL:
			case VO_OSMP_CB_STREAMING_DOWNLOADER_DOWNLOAD_CHUNK_FAIL:
			case VO_OSMP_CB_STREAMING_DOWNLOADER_WRITE_CHUNK_FAIL:
			case VO_OSMP_CB_STREAMING_DOWNLOADER_DISK_FULL:
			case VO_OSMP_CB_STREAMING_DOWNLOADER_LIVE_STREAM_NOT_SUPPORT :
			case VO_OSMP_CB_STREAMING_DOWNLOADER_LOCAL_STREAM_NOT_SUPPORT :
			{ 
				Log.e(TAG, "StreamingDownloader: Error = " + id + ", param1 = " + param1 + ", param2 = " + param2 + ", param3 = " + param3);
	
				String toDisplay = s_downloadUrl;
				if(s_downloadUrl.length() > 40){
					toDisplay = s_downloadUrl.substring(0, 20) + "..." + s_downloadUrl.substring(s_downloadUrl.length() - 20, s_downloadUrl.length());
				}	
				Toast.makeText(this, "Download failed with error: "+ id +" and will be stopped. Content: \"" + toDisplay + "\"", Toast.LENGTH_LONG).show();
				
				stopDownloader();
			}
			default:
				break;
		}
		return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE; 
	}

}