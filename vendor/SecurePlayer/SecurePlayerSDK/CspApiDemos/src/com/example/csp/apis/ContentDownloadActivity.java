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
import android.os.Bundle;
import android.util.Log;

import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.CspContentItem;
import com.example.csp.Utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;

/**
 * Represents the Activity to download a content to local device. That location of the content will
 * be defined by the {@link CspContentItem#getContentUrl()} of the currently active content, thus it
 * could be a description file (manifest.xml, m3u8, mpd...) or a complete multimedia content (mp4,
 * env,...). Also, the content could be configured for streaming or for local playback, therefore it
 * could take too long to complete the task.
 */
public class ContentDownloadActivity extends Activity {

    /** Content url of the currently active content. */
    private String mContentUrl = CspConstants.getActiveContent().getContentUrl();
    /** Local path where to save the content. */
    private String mLocalContentPath = CspConstants.getActiveContent().getTemplocalFile();
    /** Content is prepare for streaming. */
    private Boolean mIsContentStreaming = CspConstants.getActiveContent().IsStreaming();

    /**
     * Represents the asynchronous task to download the content.
     */
    private class ContentDownloader extends CspAsyncTaskBase {

        public ContentDownloader() {
            super(ContentDownloadActivity.this, "Downloading content");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            // Will be overwritten on any exception
            String userMessage;
            Boolean isPassed = false;

            // Reset internal file name state (refer at
            // HarmonicHlsDownloadActivity::doInBackground).
            CspConstants.getActiveContent().setmFileName(null);

            try {
                // If the file is for streaming the download will only download
                // the first 100k of the file.
                if (mIsContentStreaming && shouldDownloadPartially(mContentUrl)) {
                    Log.d(CspConstants.TAG,
                            "Downloading only first 1MB form " + mContentUrl);
                    Utils.downloadFile(mContentUrl, mLocalContentPath, 1024 * 1024);
                } else {
                    Utils.downloadFile(mContentUrl, mLocalContentPath);
                }

                userMessage = "download completed";
                isPassed = true;
            } catch (FileNotFoundException e) {
                userMessage = "Exception: FileNotFoundException";
            } catch (IOException e) {
                userMessage = "Exception: IOException, download failed";
            }

            return new CspResult(userMessage, isPassed);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContentUrl = CspConstants.getActiveContent().getContentUrl();
        mLocalContentPath = CspConstants.getActiveContent().getTemplocalFile();
        mIsContentStreaming = CspConstants.getActiveContent().IsStreaming();
        new ContentDownloader().execute();
    }

    /** Content file extensions that allow partially download. */
    private final String[] remoteFileExtensions = {
            "eny", "ismv"
    };

    /**
     * Test whether file denoted by the url argument should be fully download or not, according to
     * {@link #remoteFileExtensions}.
     * 
     * @param url - URL of the current file.
     * @return true in case current file should be partially downloaded.
     */
    private boolean shouldDownloadPartially(String url) {
        for (String fileEtension : remoteFileExtensions) {
            if (url.toLowerCase(Locale.getDefault()).endsWith(fileEtension)) {
                return true;
            }
        }

        return false;
    }
}
