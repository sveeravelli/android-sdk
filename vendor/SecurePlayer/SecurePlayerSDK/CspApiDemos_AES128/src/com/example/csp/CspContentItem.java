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

package com.example.csp;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Represents a Connected Sentinel Player multimedia content.
 */
public class CspContentItem {

    /**
     * Custom data types. It could be an URL, a TEXT or a FILE.
     */
    public enum ECustomDataType {
        CUSTOM_DATA_IS_TEXT {
            @Override
            public String toString() {
                return "TEXT";
            }
        },
        CUSTOM_DATA_IS_URL {
            @Override
            public String toString() {
                return "URL";
            }
        },
        CUSTOM_DATA_IS_FILE {
            @Override
            public String toString() {
                return "FILE";
            }
        }
    }

    /** Human readable content name. */
    private String mName;
    /** Content URL */
    private String mContentUrl;
    /** Initiator URL for this content. */
    private String mInitiatorUrl;
    /**
     * Indicates if content is for streaming or for playing locally. Content download behavior will
     * depending on the value of this field.
     */
    private Boolean mIsStreaming;
    /** Custom data string used on right acquisitions. */
    private String mCustomData;
    /** Custom Url. */
    private String mCustomUrl;
    /** Custom data type. */
    private ECustomDataType mCustomDataType;
    /** File to download or to play. */
    private String mFileName;
    private String[] mCookies;

    /**
     * {@link CspContentItem} builder.
     */
    public static class Builder {
        private String mName = null;
        private String mContentUrl = null;
        private String mInitiatorUrl = null;
        private Boolean mIsStreaming = true;
        private String mCustomData = null;
        private String mCustomUrl = null;
        private ECustomDataType mCustomDataType = ECustomDataType.CUSTOM_DATA_IS_TEXT;
        private String[] mCookies = null;

        /**
         * Creates {@link CspContentItem} builder instance.
         * 
         * @param name Content name.
         */
        public Builder(String name) {
            mName = name;
        }

        /**
         * Sets builder content url. {@link #build()} will use it to create the final
         * {@link CspContentItem} object.
         * 
         * @param url Content url.
         * @return itself.
         */
        public Builder setContentUrl(String url) {
            mContentUrl = url;
            return this;
        }

        /**
         * Sets builder streaming state. {@link #build()} will use it to create the final
         * {@link CspContentItem} object.
         * 
         * @param state Streaming state.
         * @return itself.
         */
        public Builder setIsStreaming(Boolean state) {
            mIsStreaming = state;
            return this;
        }

        /**
         * Sets builder initiator url. {@link #build()} will use it to create the final
         * {@link CspContentItem} object.
         * 
         * @param url Initiator url.
         * @return itself.
         */
        public Builder setInitiatorUrl(String url) {
            mInitiatorUrl = url;
            return this;
        }

        /**
         * Sets builder custom data string and type. {@link #build()} will use it to create the
         * final {@link CspContentItem} object.
         * 
         * @param customData String identifier.
         * @param customDataType {@link ECustomDataType} data type.
         * @return itself.
         */
        public Builder setCustomData(String customData, ECustomDataType customDataType) {
            mCustomData = customData;
            mCustomDataType = customDataType;
            return this;
        }

        /**
         * Sets builder custom url. {@link #build()} will use it to create the final
         * {@link CspContentItem} object.
         * 
         * @param url Custom url.
         * @return itself.
         */
        public Builder setCustomUrl(String url) {
            mCustomUrl = url;
            return this;
        }

        /**
         * Sets builder cookies. {@link #build()} will use it to create the final
         * {@link CspContentItem} object.
         * 
         * @param cookies Cookies string array.
         * @return itself.
         */
        public Builder setCookies(String[] cookies) {
            mCookies = cookies;
            return this;
        }

        /**
         * Build a {@link CspContentItem} object with values previously set into the {@link Builder}
         * .
         * 
         * @return A new {@link CspContentItem} instance.
         */
        public CspContentItem build() {
            return new CspContentItem(this);
        }
    }

    /**
     * {@link CspContentItem} constructor from a {@link Builder} object.
     * 
     * @param builder The builder object.
     */
    private CspContentItem(Builder builder) {
        mName = builder.mName;
        mContentUrl = builder.mContentUrl;
        mInitiatorUrl = builder.mInitiatorUrl;
        mIsStreaming = builder.mIsStreaming;
        mCustomData = builder.mCustomData;
        mCustomDataType = builder.mCustomDataType;
        mCustomUrl = builder.mCustomUrl;
        mCookies = builder.mCookies;
    }

    /**
     * Returns {@link #mCustomData} value.
     */
    public String getCustomData() {
        return mCustomData;
    }

    /**
     * Sets {@link #mCustomData} value.
     */
    public void setCustomData(String customData) {
        this.mCustomData = customData;
    }

    /**
     * Returns {@link #mCustomUrl} value.
     */
    public String getCustomUrl() {
        return mCustomUrl;
    }

    /**
     * Sets {@link #mCustomUrl} value.
     */
    public void setCustomUrl(String customUrl) {
        this.mCustomUrl = customUrl;
    }

    /**
     * Returns {@link #mCustomDataType} value.
     */
    public ECustomDataType getCustomDataType() {
        return mCustomDataType;
    }

    /**
     * Sets {@link #mCustomDataType} value.
     */
    public void setCustomDataType(ECustomDataType customDataType) {
        this.mCustomDataType = customDataType;
    }

    /**
     * Returns {@link #mCookies} String array.
     */
    public String[] getCookiesArry() {
        return mCookies;
    }

    /**
     * Sets {@link #mFileName} value.
     */
    public void setmFileName(String mFileName) {
        this.mFileName = mFileName;
    }

    /**
     * Returns {@link #mName} value.
     */
    public String getName() {
        return mName;
    }

    /**
     * Returns {@link #mContentUrl} value.
     */
    public String getContentUrl() {
        return mContentUrl;
    }

    /**
     * Returns {@link #mInitiatorUrl} value.
     */
    public String getInitiatorUrl() {
        return mInitiatorUrl;
    }

    /**
     * Generates the file name for a given url. It uses the url hashCode.
     * 
     * @param url Used to generate the file name.
     * @return Generated file name string.
     */
    public static String generateFileName(String url) {

        // Get last component of the url
        String lastComponent = url.substring(url.lastIndexOf('/') + 1);

        // check if there is a query string
        if (lastComponent.lastIndexOf('?') > 0) {
            // skip query string
            lastComponent = lastComponent.substring(0, lastComponent.lastIndexOf('?'));
        }

        if (lastComponent.lastIndexOf('.') > 0) {
            return String.format("%s/%08X%s", CspConstants.CONTENT_DIR, url.hashCode(),
                    lastComponent.substring(lastComponent.lastIndexOf('.')));
        }
        return String.format("%s/%08X_%s", CspConstants.CONTENT_DIR, url.hashCode(), lastComponent);
    }

    /**
     * Returns {@link #mFileName} or a temporal file name if {@link #mFileName} is null.
     */
    public String getTemplocalFile() {

        if (null == mFileName) {
            return generateFileName(mContentUrl);
        }

        return mFileName;
    }

    /**
     * Returns final content path from where to playback. If {@link #mIsStreaming} is true, the path
     * will be the same as returned by {@link #getContentUrl}, that means the content will be played
     * from its url. In other case the return value will be a path to a local file.
     */
    public String getPlayBackPath() {

        if (mIsStreaming) {
            return mContentUrl;
        } else {

            // HLS has a spetial treatment if it is configured for local playback
            // It is necessary to use Streaming Download action to correctly download
            // the content
            if (mContentUrl.toLowerCase().endsWith("m3u8") || mContentUrl.toLowerCase().endsWith("manifest")) {
                String localHLSPath = Utils.generateDownloadFolderName(mContentUrl) + File.separator
                        + "MainPlaylist.txt";
                InputStream fis;
                BufferedReader br;
                String line;

                Log.i(CspConstants.TAG, "Getting playback path " + localHLSPath);

                try {
                    // Open the local file
                    fis = new FileInputStream(localHLSPath);

                    br = new BufferedReader(new InputStreamReader(fis));
                    if ((line = br.readLine()) != null) {
                        Log.i(CspConstants.TAG, "Getting one line " + line);
                        br.close();
                        return line;
                    }
                    br.close();
                } catch (FileNotFoundException e) {
                    Log.e(CspConstants.TAG, "Error file not found " + localHLSPath);
                } catch (IOException e) {
                    Log.e(CspConstants.TAG, "Error with file " + localHLSPath);
                }
                br = null;
                fis = null;

            }

            return getTemplocalFile();
        }
    }

    /**
     * Sets content name.
     */
    public void setName(String name) {
        mName = name;
    }

    /**
     * Sets content url.
     */
    public void setContentUrl(String contentUrl) {
        mContentUrl = contentUrl;
    }

    /**
     * Sets content initiator url.
     * 
     * @param initiatorUrl
     */
    public void setInitiatorUrl(String initiatorUrl) {
        mInitiatorUrl = initiatorUrl;
    }

    /**
     * Sets streaming properties that determines the behavior of download action.
     */
    public void setmIsStreaming(Boolean mIsStreaming) {
        this.mIsStreaming = mIsStreaming;
    }

    /**
     * Returns {@link #mIsStreaming} value
     */
    public boolean IsStreaming() {
        return mIsStreaming;
    }

    /**
     * Sets cookies array from string.
     * 
     * @param cookies String with ; separated cookies.
     */
    public void setCookiesFromStr(String cookies) {
        if (cookies != null && cookies.length() > 0) {
            mCookies = cookies.split(";");
            for (int i = 0; i < mCookies.length; i++) {
                mCookies[i] = mCookies[i].trim();
            }
        } else {
            mCookies = null;
        }
    }

    /**
     * Returns a string with all content cookies appendes
     */
    public String getCookiesStr() {
        StringBuffer cookiesStr = new StringBuffer();
        if (mCookies != null && mCookies.length > 0) {
            for (int i = 0; i < mCookies.length; i++) {
                cookiesStr.append(mCookies[i]);
            }
            return cookiesStr.toString();
        }
        return "";
    }

}
