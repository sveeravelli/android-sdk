/*******************************************************************************
 * Copyright
 *  This code is strictly confidential and the receiver is obliged to use it
 *  exclusively for his or her own purposes. No part of Viaccess Orca code may be
 *  reproduced or transmitted in any form or by any means, electronic or
 *  mechanical, including photocopying, recording, or by any information storage
 *  and retrieval system, without permission in writing from Viaccess Orca.
 *  The information in this code is subject to change without notice. Viaccess Orca
 *  does not warrant that this code is error free. If you find any problems
 *  with this code or wish to make comments, please report them to Viaccess Orca.
 *  
 *  Trademarks
 *  Viaccess Orca is a registered trademark of Viaccess S.A in France and/or other
 *  countries. All other product and company names mentioned herein are the
 *  trademarks of their respective owners.
 *  Viaccess S.A may hold patents, patent applications, trademarks, copyrights
 *  or other intellectual property rights over the code hereafter. Unless
 *  expressly specified otherwise in a Viaccess Orca written license agreement, the
 *  delivery of this code does not imply the concession of any license over
 *  these patents, trademarks, copyrights or other intellectual property.
 *******************************************************************************/

package com.example.csp.apis;

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

import com.example.csp.CspConstants;
import com.example.csp.R;
import com.example.csp.Utils;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection;
import com.visualon.OSMPPlayer.VOCommonPlayerAssetSelection.VOOSMPAssetProperty;
import com.visualon.OSMPPlayer.VOOSMPType.VO_OSMP_RETURN_CODE;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloader;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloaderInitParam;
import com.visualon.VOOSMPStreamingDownloader.VOOSMPStreamingDownloaderListener;
import com.visualon.VOOSMPStreamingDownloaderImpl.VOOSMPStreamingDownloaderImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Represents the activity to download content to local device using VisualOn OnStream MediaPlayer
 * Download Manager. This functionality has known limitation, Streaming protocol must be HLS and DRM
 * must be PlayReady or none.
 */
public class StreamingDownloaderActivity extends Activity
        implements VOOSMPStreamingDownloaderListener {

    /** Video asset propertie Bits Per Second. */
    private static final String STRING_ASSETPROPERTYNAME_VIDEO = "Bps";
    /** Audio asset property. */
    private static final String STRING_ASSETPROPERTYNAME_AUDIO = "Audio";
    /** Subtitle asset property. */
    private static final String STRING_ASSETPROPERTYNAME_SUBTITLE = "Subt";

    /** VisualOn OnStream MediaPlayer Download Manager. */
    private static VOOSMPStreamingDownloader sStreamingDownloader = null;
    /** VisualOn assets selection. */
    private static VOCommonPlayerAssetSelection sAsset = null;
    /** State of the download. */
    private static StreamingDownloaderState sCurrState = StreamingDownloaderState.IDLE;
    /** Dialog to show information to user. */
    private AlertDialog m_adlgDownload = null;
    /** Content url. */
    private static String sDownloadUrl = "";
    /** Local device directory to download content. */
    private static String sDestDirectory = "";
    /** Destination playlist. */
    private static String sDestDownloadedPlaylist = "";
    /** Time already downloaded. */
    private static int sDownloadCurrent = 0;
    /** Total time to download. */
    private static int sDownloadTotal = 0;

    /* View elements */
    private ProgressBar mPbDownloaded;
    private TextView mTvDownloadCurrent;
    private TextView mTvDownloadTotal;
    private EditText mEdURL;
    private Button mBtnDownload;
    private Button mBtnPause;
    private Button mBtnResume;
    private Button mBtnCancel;

    /**
     * Represents the three states that a StreamerDownload could be.
     */
    private enum StreamingDownloaderState {
        IDLE, DOWNLOADING, PAUSED
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.streaming_downloader);
        mEdURL = (EditText) findViewById(R.id.etDownloadUrl);

        // Get content url to configure the download url
        if (CspConstants.getActiveContent() != null
                && !CspConstants.getActiveContent().IsStreaming()) {

            sDownloadUrl = CspConstants.getActiveContent().getContentUrl();
        } else {

            sDownloadUrl = "";
        }

        mTvDownloadCurrent = (TextView) findViewById(R.id.tvDownloadCurrent);
        mTvDownloadTotal = (TextView) findViewById(R.id.tvDownloadTotal);
        mPbDownloaded = (ProgressBar) findViewById(R.id.progressBar1);

        // Configure Download button and its click listener
        mBtnDownload = (Button) findViewById(R.id.btnDownload);
        mBtnDownload.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                VO_OSMP_RETURN_CODE nRet;

                sDownloadUrl = mEdURL.getText().toString();
                // If there is not download url we do nothing
                if (!TextUtils.isEmpty(sDownloadUrl)) {
                    nRet = initStreamingDownloader();
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "StreamingDownloader: init failed!! with error = " + nRet);
                    }

                    nRet = openStreamingDownloader(sDownloadUrl);
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "StreamingDownloader: open failed!! with error = " + nRet);
                    }
                } else {
                    Toast.makeText(getBaseContext(), "You must specify an URL", Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // Configure Pause button and its listener
        mBtnPause = (Button) findViewById(R.id.btnPause);
        mBtnPause.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                VO_OSMP_RETURN_CODE nRet;
                if (sStreamingDownloader != null) {
                    nRet = sStreamingDownloader.pause();
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.e(CspConstants.TAG, "StreamingDownloader: pause failed!! with error = " + nRet);
                    } else {
                        setState(StreamingDownloaderState.PAUSED);
                    }
                }
            }
        });

        mBtnResume = (Button) findViewById(R.id.btnResume);
        mBtnResume.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

                VO_OSMP_RETURN_CODE nRet;
                if (sStreamingDownloader != null) {
                    nRet = sStreamingDownloader.resume();
                    if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                        Log.v(CspConstants.TAG, "StreamingDownloader: resume failed!! with error = " + nRet);
                    } else {
                        setState(StreamingDownloaderState.DOWNLOADING);
                    }
                }
            }
        });

        mBtnCancel = (Button) findViewById(R.id.btnCancel);
        mBtnCancel.setOnClickListener(new Button.OnClickListener() {

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

                        if (sStreamingDownloader != null) {
                            VOOSMPStreamingDownloaderProgressInfo info = sStreamingDownloader
                                    .getDuration();
                            if (info != null) {
                                sDownloadCurrent = info.getDownloadedStreamDuration();
                                sDownloadTotal = info.getTotalStreamDuration();
                            }
                        }
                        ((TextView) findViewById(R.id.tvDownloadCurrent))
                                .setText(Integer.toString(sDownloadCurrent));
                        ((TextView) findViewById(R.id.tvDownloadTotal))
                                .setText(Integer.toString(sDownloadTotal));
                        mPbDownloaded.setProgress(sDownloadTotal > 0
                                ? 100 * sDownloadCurrent / sDownloadTotal : 0);
                    }
                });
            }
        };
        timer.schedule(timerTask, 0, 200);

        // Load license file
        loadLicenseFile(CspConstants.LICENSE_FILE);
    }

    /**
     * Copies file from Assets directory to destination. Used for licenses and processor-specific
     * configurations.
     */
    private void loadLicenseFile(String fileName) {
        try {
            String filePath = getFilesDir().getParent() + "/" + fileName;
            InputStream InputStreamis = getAssets().open(fileName);
            File desFile = new File(filePath);
            desFile.createNewFile();
            FileOutputStream fos = new FileOutputStream(desFile);
            int bytesRead;
            byte[] buf = new byte[4 * 1024]; // 4K buffer
            while ((bytesRead = InputStreamis.read(buf)) != -1) {
                fos.write(buf, 0, bytesRead);
            }
            fos.flush();
            fos.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        setState(sCurrState);

        mEdURL.setText(sDownloadUrl);
        mTvDownloadCurrent.setText(Integer.toString(sDownloadCurrent));
        mTvDownloadTotal.setText(Integer.toString(sDownloadTotal));
        mPbDownloaded
                .setProgress(sDownloadTotal > 0 ? 100 * sDownloadCurrent / sDownloadTotal : 0);
    };

    /**
     * Initializes of the VisualOn download manager.
     * 
     * @return Error code.
     */
    private VO_OSMP_RETURN_CODE initStreamingDownloader() {
        VO_OSMP_RETURN_CODE nRet;

        String apkPath = getFilesDir().getParent() + "/lib/";
        sStreamingDownloader = new VOOSMPStreamingDownloaderImpl();
        VOOSMPStreamingDownloaderInitParam initParam = new VOOSMPStreamingDownloaderInitParam();
        initParam.setContext(this);
        initParam.setLibraryPath(apkPath);
        nRet = sStreamingDownloader.init(this, initParam);

        return nRet;
    }

    /**
     * Opens the configured download. It will start the download manager.
     * 
     * @param strPath Path to use for saving file.
     * @return Error code.
     */
    private VO_OSMP_RETURN_CODE openStreamingDownloader(String strPath) {
        VO_OSMP_RETURN_CODE nRet;
        String dirPath = Utils.generateDownloadFolderName(strPath);

        // creates dir if doesn't exists
        File dest = new File(dirPath);
        if (!dest.exists()) {
            if (!dest.mkdirs()) {
                Log.e(CspConstants.TAG, "StreamingDownloader: dest.mkdirs() FAILED.");
                AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle("Create destination folder failed")
                        .setNegativeButton("OK", new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create();
                dialog.show();
                // stop (reset) downloader and return general error.
                stopDownloader();
                return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_STATUS;
            }
        }

        sDestDirectory = dirPath;
        nRet = sStreamingDownloader.open(strPath, 0, dirPath);
        if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Download open failed").setMessage("Error = " + nRet)
                    .setNegativeButton("OK", new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    }).create();
            dialog.show();
            return nRet;
        }
        return nRet;
    }

    /**
     * Fills all fields for the Download view with the information of video/audio/sub assets.
     */
    private void fillDownloaderProgramInfo() {

        LayoutInflater inflater;
        View layout;
        inflater = LayoutInflater.from(this);
        layout = inflater.inflate(R.layout.asset_select, null);
        final Spinner sp_downloadSelectVideo = (Spinner) layout
                .findViewById(R.id.spDownloadSelectVideo);
        final Spinner sp_downloadSelectAudio = (Spinner) layout
                .findViewById(R.id.spDownloadSelectAudio);
        final Spinner sp_downloadSelectSubtitle = (Spinner) layout
                .findViewById(R.id.spDownloadSelectSubtitle);

        TextView tv_downloadVideo = (TextView) layout.findViewById(R.id.tvDownloadSelectVideo);
        TextView tv_downloadAudio = (TextView) layout.findViewById(R.id.tvDownloadSelectAudio);
        TextView tv_downloadSubtitle = (TextView) layout
                .findViewById(R.id.tvDownloadSelectSubtitle);

        if (sAsset.getVideoCount() == 0) {
            tv_downloadVideo.setVisibility(View.GONE);
            sp_downloadSelectVideo.setVisibility(View.GONE);
        }

        if (sAsset.getAudioCount() == 0) {
            tv_downloadAudio.setVisibility(View.GONE);
            sp_downloadSelectAudio.setVisibility(View.GONE);
        }

        if (sAsset.getSubtitleCount() == 0) {
            tv_downloadSubtitle.setVisibility(View.GONE);
            sp_downloadSelectSubtitle.setVisibility(View.GONE);
        }

        ArrayList<String> lstVideo = new ArrayList<String>();
        getVideoDescription(lstVideo);
        lstVideo.add(0, getResources().getString(R.string.BpsQuality_Auto));

        ArrayAdapter<String> adapterVideo = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                lstVideo);

        sp_downloadSelectVideo.setAdapter(adapterVideo);
        sp_downloadSelectVideo.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int index = sp_downloadSelectVideo.getSelectedItemPosition() - 1;
                VO_OSMP_RETURN_CODE nRet = sAsset.selectVideo(index);
                if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    Log.e(CspConstants.TAG,
                            "StreamingDownloader: Download module selectVideo FAILED with error = "
                                    + nRet);
                    return;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });

        ArrayList<String> lstAudio = new ArrayList<String>();
        getAudioDescription(lstAudio);

        ArrayAdapter<String> adapterAudio = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                lstAudio);

        sp_downloadSelectAudio.setAdapter(adapterAudio);
        sp_downloadSelectAudio.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

                int index = sp_downloadSelectAudio.getSelectedItemPosition();
                VO_OSMP_RETURN_CODE nRet = sAsset.selectAudio(index);
                if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    Log.e(CspConstants.TAG,
                            "StreamingDownloader: Download module selectAudio FAILED with error = "
                                    + nRet);
                    return;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        ArrayList<String> lstSubtitle = new ArrayList<String>();
        getSubtitleDescription(lstSubtitle);

        ArrayAdapter<String> adapterSubtitle = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                lstSubtitle);

        sp_downloadSelectSubtitle.setAdapter(adapterSubtitle);
        sp_downloadSelectSubtitle.setOnItemSelectedListener(new Spinner.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                int index = sp_downloadSelectSubtitle.getSelectedItemPosition();
                sAsset.selectSubtitle(index);
                VO_OSMP_RETURN_CODE nRet = sAsset.selectSubtitle(index);
                if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    Log.e(CspConstants.TAG,
                            "StreamingDownloader: Download module selectSubtitle FAILED with error = "
                                    + nRet);
                    return;
                }
            }

            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        m_adlgDownload = new AlertDialog.Builder(StreamingDownloaderActivity.this)
                .setTitle("Select Asset")
                .setView(layout).setNegativeButton("Cancel", new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).setPositiveButton("Start", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        VO_OSMP_RETURN_CODE nRet = sAsset.commitSelection();
                        if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                            Log.e(CspConstants.TAG,
                                    "StreamingDownloader: Download module commitSelection FAILED with error = "
                                            + nRet);
                            ;
                            return;
                        }
                        if (sStreamingDownloader != null) {
                            nRet = sStreamingDownloader.start();
                            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                                Log.v(CspConstants.TAG,
                                        "StreamingDownloader: Start FAILED with error = " + nRet);
                            } else {
                                setState(StreamingDownloaderState.DOWNLOADING);
                            }
                        }
                    }
                }).setOnKeyListener(new OnKeyListener() {

                    @Override
                    public boolean onKey(DialogInterface arg0, int arg1, KeyEvent arg2) {
                        if (arg1 == KeyEvent.KEYCODE_BACK) {
                            arg0.dismiss();
                            return true;
                        }
                        return false;
                    }
                }).create();
        m_adlgDownload.show();
    }

    /**
     * Returns into {@code lstString} all video description.
     * 
     * @param lstString Not null {@link ArrayList}.
     */
    private void getVideoDescription(ArrayList<String> lstString) {

        if (lstString == null || sAsset == null)
            return;

        int nAssetCount = sAsset.getVideoCount();
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = sAsset.getVideoProperty(nAssetIndex);
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

    /**
     * Returns into {@code lstString} all audio description.
     * 
     * @param lstString Not null {@link ArrayList}.
     */
    private void getAudioDescription(ArrayList<String> lstString) {

        if (lstString == null || sAsset == null)
            return;

        int nAssetCount = sAsset.getAudioCount();
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = sAsset.getAudioProperty(nAssetIndex);
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

    /**
     * Returns into {@code lstString} all subtitles description.
     * 
     * @param lstString Not null {@link ArrayList}.
     */
    private void getSubtitleDescription(ArrayList<String> lstString) {

        if (lstString == null || sAsset == null)
            return;

        int nAssetCount = sAsset.getSubtitleCount();
        if (nAssetCount == 0)
            return;

        int nDefaultIndex = 0;
        for (int nAssetIndex = 0; nAssetIndex < nAssetCount; nAssetIndex++) {
            VOOSMPAssetProperty propImpl = sAsset.getSubtitleProperty(nAssetIndex);
            String strDescription;
            int nPropertyCount = propImpl.getPropertyCount();
            if (nPropertyCount == 0) {
                strDescription = STRING_ASSETPROPERTYNAME_SUBTITLE
                        + Integer.toString(nDefaultIndex++);
            } else {
                final int KEY_DESCRIPTION_INDEX = 1;
                strDescription = (String) propImpl.getValue(KEY_DESCRIPTION_INDEX);
            }
            lstString.add(strDescription);
        }
    }

    /**
     * Sets the current state of the download activity.
     * 
     * @param state State {@link StreamingDownloaderState}.
     */
    private void setState(StreamingDownloaderState state) {
        sCurrState = state;
        switch (sCurrState) {
            case DOWNLOADING:
                mBtnCancel.setEnabled(true);
                mBtnDownload.setEnabled(false);
                mBtnPause.setEnabled(true);
                mBtnResume.setEnabled(false);
                break;
            case IDLE:
                mBtnCancel.setEnabled(false);
                mBtnDownload.setEnabled(true);
                mBtnPause.setEnabled(false);
                mBtnResume.setEnabled(false);
                break;
            case PAUSED:
                mBtnCancel.setEnabled(true);
                mBtnDownload.setEnabled(false);
                mBtnPause.setEnabled(false);
                mBtnResume.setEnabled(true);
                break;
        }
    }

    /**
     * Stops download task.
     */
    private void stopDownloader() {

        VO_OSMP_RETURN_CODE nRet;
        if (sStreamingDownloader != null) {
            if (sCurrState == StreamingDownloaderState.DOWNLOADING) {
                nRet = sStreamingDownloader.stop();
                if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                    Log.e(CspConstants.TAG, "StreamingDownloader: stop failed!! with error = " + nRet);
                }
            }
            nRet = sStreamingDownloader.close();
            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                Log.e(CspConstants.TAG, "StreamingDownloader: close failed!! with error = " + nRet);
            }
            nRet = sStreamingDownloader.destroy();
            if (nRet != VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE) {
                Log.e(CspConstants.TAG, "StreamingDownloader: destroy failed!! with error = " + nRet);
            }
        }

        setState(StreamingDownloaderState.IDLE);
        resetDownloadedParameters();
    }

    /**
     * Reset download parameters to defaults.
     */
    private void resetDownloadedParameters() {
        sStreamingDownloader = null;
        sDestDownloadedPlaylist = "";
        sDestDirectory = "";
        sDownloadCurrent = 0;
        sDownloadTotal = 0;
    }

    @Override
    public VO_OSMP_RETURN_CODE onVOStreamingDownloaderEvent(
            VO_OSMP_CB_STREAMING_DOWNLOADER_EVENT_ID id, int param1,
            int param2, Object param3) {
        Log.v(CspConstants.TAG, "onVOStreamingDownloaderEvent: id =  " + id + ", param1 = " + param1
                + ", param2 = " + param2
                + ", param3 = " + param3);

        switch (id) {
            case VO_OSMP_CB_STREAMING_DOWNLOADER_OPEN_COMPLETE: {
                sAsset = sStreamingDownloader;
                fillDownloaderProgramInfo();
                break;
            }
            case VO_OSMP_CB_STREAMING_DOWNLOADER_MANIFEST_OK: {
                sDestDownloadedPlaylist = (String) param3;

                File playlist = new File(sDestDirectory + File.separator + "MainPlaylist.txt");
                try {
                    playlist.createNewFile();
                    if (playlist.exists()) {
                        FileWriter fo = new FileWriter(playlist);
                        fo.write(sDestDownloadedPlaylist);
                        fo.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case VO_OSMP_CB_STREAMING_DOWNLOADER_END: {
                String toDisplay = sDownloadUrl;
                if (sDownloadUrl.length() > 40) {
                    toDisplay = sDownloadUrl.substring(0, 20) + "..."
                            + sDownloadUrl.substring(sDownloadUrl.length() - 20,
                                    sDownloadUrl.length());
                }

                Toast.makeText(this, "Download of \"" + toDisplay + "\" completed successfully",
                        Toast.LENGTH_LONG).show();

                stopDownloader();
                break;
            }

                // errors
            case VO_OSMP_CB_STREAMING_DOWNLOADER_DOWNLOAD_MANIFEST_FAIL:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_WRITE_MANIFEST_FAIL:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_DOWNLOAD_CHUNK_FAIL:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_WRITE_CHUNK_FAIL:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_DISK_FULL:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_LIVE_STREAM_NOT_SUPPORT:
            case VO_OSMP_CB_STREAMING_DOWNLOADER_LOCAL_STREAM_NOT_SUPPORT: {
                Log.e(CspConstants.TAG, "StreamingDownloader: Error = " + id + ", param1 = " + param1
                        + ", param2 = " + param2
                        + ", param3 = " + param3);

                String toDisplay = sDownloadUrl;
                if (sDownloadUrl.length() > 40) {
                    toDisplay = sDownloadUrl.substring(0, 20) + "..."
                            + sDownloadUrl.substring(sDownloadUrl.length() - 20,
                                    sDownloadUrl.length());
                }
                Toast.makeText(this,
                        "Download failed with error: " + id + " and will be stopped. Content: \""
                                + toDisplay + "\"",
                        Toast.LENGTH_LONG).show();

                stopDownloader();
            }
            default:
                break;
        }
        return VO_OSMP_RETURN_CODE.VO_OSMP_ERR_NONE;
    }

}
