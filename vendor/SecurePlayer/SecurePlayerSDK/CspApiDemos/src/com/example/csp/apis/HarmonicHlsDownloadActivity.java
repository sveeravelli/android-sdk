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

import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.CspContentItem;
import com.example.csp.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Locale;

/**
 * Represents the activity to download TS fragment for harmonic HLS stream. Maximum download size
 * will be 100 * 1024 bytes.
 */
public class HarmonicHlsDownloadActivity extends Activity {

    /**
     * Represents the asynchronous task to download TS fragment for harmonic HLS stream.
     */
    private class HlsDownloader extends CspAsyncTaskBase {

        public HlsDownloader() {
            super(HarmonicHlsDownloadActivity.this, "Downloading content");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            // Will be overwritten on any exception
            String userMessage;
            Boolean isPassed = false;

            // Reset internal file name state
            CspConstants.getActiveContent().setmFileName(null);

            try {
                if (!CspConstants.getActiveContent().getContentUrl()
                        .toLowerCase(Locale.getDefault())
                        .endsWith(".m3u8")) {
                    return new CspResult("Not applicable for current content", false);
                }

                // calculate .TS file URL
                String tsUrl = getTsUrl(CspConstants.getActiveContent().getContentUrl());

                if (tsUrl == null) {
                    throw new IOException("Failed to download .TS file");
                }
                // generate local file name for a .TS URL
                String tsFileName = CspContentItem.generateFileName(tsUrl);

                // set internal file name to .TS file - so all following file
                // based actions will work with it
                CspConstants.getActiveContent().setmFileName(tsFileName);

                // download .TS file
                Utils.downloadFile(tsUrl, tsFileName, 100 * 1024);

                userMessage = "download completed " + tsUrl;
                isPassed = true;
            } catch (FileNotFoundException e) {
                userMessage = "Exception: FileNotFoundException";
            } catch (IOException e) {
                userMessage = "Exception: IOException, download failed";
            }

            return new CspResult(userMessage, isPassed);
        }

        /**
         * Calculates TS fragment url from the Harmonic HLS url.
         * 
         * @param url Url from where to calculate TS url.
         * @return TS url calculated.
         * @throws IOException if any error while reading url.
         */
        private String getTsUrl(String url) throws IOException {

            URL playlistUrl = null;
            String baseUrl = url.substring(0, url.lastIndexOf('/') + 1);
            BufferedReader bufferedReader = null;
            try {
                playlistUrl = new URL(url);
                bufferedReader = new BufferedReader(
                        new InputStreamReader(playlistUrl.openConnection().getInputStream()));
                String inputLine;

                while ((inputLine = bufferedReader.readLine()) != null) {
                    String trimmedLine = inputLine.trim();
                    String trimmedLineWithoutQuery = trimmedLine;

                    // check if there is a query string
                    if (trimmedLine.lastIndexOf('?') > 0) {

                        // skip query string
                        trimmedLineWithoutQuery = trimmedLine.substring(0,
                                trimmedLine.lastIndexOf('?'));
                    }

                    if (trimmedLine.equals("") || trimmedLine.startsWith("#")) {
                        continue;
                    } else if (trimmedLineWithoutQuery.toLowerCase(Locale.getDefault())
                            .endsWith(".m3u8")) {
                        return getTsUrl(baseUrl + trimmedLine);
                    } else if (trimmedLineWithoutQuery.toLowerCase(Locale.getDefault())
                            .endsWith(".ts")) {
                        return baseUrl + trimmedLine;
                    }
                }
            } finally {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            }
            return null;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new HlsDownloader().execute();
    }

}
