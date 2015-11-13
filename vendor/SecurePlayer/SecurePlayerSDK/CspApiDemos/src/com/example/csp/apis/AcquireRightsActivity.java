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

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmInvalidFormatException;
import com.discretix.drmdlc.api.exceptions.DrmNotProtectedException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;
import com.example.csp.CspContentItem.ECustomDataType;
import com.example.csp.Utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

/**
 * Represents the activity to perform the Content-Based Rights Acquisition. This use case is
 * suitable for local protected content and also applicable for protected streams. However this
 * right acquisition method is not suitable for HLS in Harmonic format.
 */
public class AcquireRightsActivity extends Activity {

    /**
     * Represents the asynchronous task to acquire content Rights.
     */
    private class DrmExecuter extends CspAsyncTaskBase {

        /** Basic constructor */
        public DrmExecuter() {
            super(AcquireRightsActivity.this, "Acquiring Rights");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            String userMessage;
            String fileName = CspConstants.getActiveContent().getTemplocalFile();
            Boolean isPassed = false;
            try {
                // First we need check if there is a local Content Path.
                if (!Utils.checkFileExists(fileName)) {
                    userMessage = "You need to download the file before you acquire Rights.";
                    return new CspResult(userMessage, false);
                }

                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(AcquireRightsActivity.this, null);

                String customData = getCustomData();
                String customUrl = getCustomUrl();
                cspApiSingleton.setCookies(getCookiesArry());
                cspApiSingleton.acquireRights(fileName, customData, customUrl);
                cspApiSingleton.setCookies(null);

                userMessage = "License acquisition successful";
                isPassed = true;
            } catch (DrmClientInitFailureException e) {
                userMessage = "Exception: DrmClientInitFailureException";
            } catch (DrmGeneralFailureException e) {
                userMessage = "Exception: DrmGeneralFailureException";
            } catch (FileNotFoundException e) {
                userMessage = "Exception: FileNotFoundException";
            } catch (DrmNotProtectedException e) {
                userMessage = "Exception: DrmNotProtectedException";
            } catch (DrmInvalidFormatException e) {
                userMessage = "Exception: DrmInvalidFormatException";
            } catch (DrmCommunicationFailureException e) {
                userMessage = "Exception: DrmCommunicationFailureException";
            } catch (DrmServerSoapErrorException e) {
                String customData = e.getCustomData();
                String redirectUrl = e.getRedirectUrl();
                userMessage = "Exception: DrmServerSoapErrorException: CustomData = " + customData
                        + " RedirectUrl = "
                        + redirectUrl;
            } catch (IOException e) {
                userMessage = "Exception: IOException, download failed";
            } catch (DrmAndroidPermissionMissingException e) {
            	String[] list = e.getMissingPermissions();
            	userMessage = "Exception: DrmAndroidPermissionMissingException ";
            	for (String permission : list)
            	{
            		userMessage+=permission + " ";
            	}
			}

            return new CspResult(userMessage, isPassed);
        }

        /**
         * Returns a string with the file content.
         * 
         * @param path File path.
         */
        private String readFile(String path) throws IOException {

            Reader reader = null;
            try {
                reader = new BufferedReader(new FileReader(path));
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
                return builder.toString();
            } finally {
                if (null != reader) {
                    reader.close();
                }
            }
        }

        /**
         * Returns a string with the content of an url.
         * 
         * @param url Url to read.
         * @throws IOException If any problem arise meanwhile reading.
         */
        private String readUrl(String url) throws IOException {
            StringBuilder customData = new StringBuilder();
            BufferedReader bufferedReader = null;
            try {
                URL theUrl = new URL(url);
                bufferedReader = new BufferedReader(
                        new InputStreamReader(theUrl.openConnection().getInputStream()));
                String inputLine;

                while ((inputLine = bufferedReader.readLine()) != null) {
                    customData.append(inputLine);
                }
            } finally {
                if (null != bufferedReader) {
                    bufferedReader.close();
                }
            }
            return customData.toString();

        }

        /**
         * Returns the content custom data depending on the data type.
         * 
         * @throws IOException If any problem happen while reading url content.
         */
        private String getCustomData() throws IOException {
            String customData = null;
            ECustomDataType customDataType = CspConstants.getActiveContent().getCustomDataType();

            switch (customDataType) {
                case CUSTOM_DATA_IS_FILE:
                    customData = readFile(CspConstants.getActiveContent().getCustomData());
                    break;
                case CUSTOM_DATA_IS_TEXT:
                    customData = CspConstants.getActiveContent().getCustomData();
                    break;
                case CUSTOM_DATA_IS_URL:
                    customData = readUrl(CspConstants.getActiveContent().getCustomData());
                    break;
            }

            if (null != customData && !customData.equals("")) {
                Log.w(CspConstants.TAG, "loaded custom data: " + customData);
                return customData;
            }
            return null;
        }

        /**
         * Returns the content custom url, it could be null.
         */
        private String getCustomUrl() {
            String customUrl = CspConstants.getActiveContent().getCustomUrl();

            if (null != customUrl && !customUrl.equals("")) {
                Log.i(CspConstants.TAG, "Loaded custom Url: " + customUrl);
                return customUrl;
            }

            return null;
        }

        /**
         * Returns content cookies string array.
         */
        private String[] getCookiesArry() {
            return CspConstants.getActiveContent().getCookiesArry();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new DrmExecuter().execute();
    }

}
