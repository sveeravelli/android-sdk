/*************************************************************************
 * ADOBE SYSTEMS INCORPORATED
 * Copyright 2013 Adobe Systems Incorporated
 * All Rights Reserved.
 *
 * NOTICE:  Adobe permits you to use, modify, and distribute this file in accordance with the
 * terms of the Adobe license agreement accompanying it.  If you have received this file from a
 * source other than Adobe, then your use, modification, or distribution of it requires the prior
 * written permission of Adobe.
 *
 * For the avoidance of doubt, this file is Documentation under the Agreement.
 ************************************************************************/

package com.adobe.adobepass.apps.demo.crypto;

import android.util.Base64;
import android.util.Log;

public class CryptoHelper {
    private static final String LOG_TAG = "CryptoHelper";

    public static String base64Encode(byte[] inData) {
        if (inData == null)
            return null;

        try {
            return new String(Base64.encode(inData, Base64.DEFAULT));
        }
        catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;
    }

    public static byte[] base64Decode(String inData) {
        if (inData == null)
            return null;

        try {
            return Base64.decode(inData.getBytes(), Base64.DEFAULT);
        }
        catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }

        return null;

    }

    public static String getSignatureAlgorithm() {
        return "SHA256WithRSA";
    }

    public static String getSymmetricEncryptionAlgorithm() {
        return "AES/CBC/PKCS5Padding";
    }
}
