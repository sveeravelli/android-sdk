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

package com.adobe.adobepass.apps.demo;

import android.app.Application;
import com.adobe.adobepass.accessenabler.api.AccessEnabler;
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;
import com.adobe.adobepass.accessenabler.utils.Log;
import com.adobe.adobepass.apps.demo.crypto.SignatureGenerator;
import com.adobe.adobepass.apps.demo.crypto.SigningCredential;

import java.io.InputStream;

public class AdobePassDemoApp extends Application {
    public static String STAGING_URL;
    public static String PRODUCTION_URL;

    public static final String ADOBE_PASS_PREFERENCES = "adobePassPreferences";
    public static final String ENVIRONMENT_URL = "environmentUrl";
    public static final String USE_HTTPS = "useHttps";

    private static final String LOG_TAG = "AdobePassDemoApp";
    private static AccessEnabler accessEnabler;
    private static SignatureGenerator signatureGenerator;

    @Override
    public void onCreate() {
        super.onCreate();

        STAGING_URL = getResources().getString(R.string.sp_url_staging);
        PRODUCTION_URL = getResources().getString(R.string.sp_url_production);

        String credentialStorePass = getResources().getString(R.string.credential_store_passwd);
        InputStream credentialStore = getResources().openRawResource(R.raw.adobepass);

        // load the signing credentials
        SigningCredential signingCredential = new SigningCredential(credentialStore, credentialStorePass);
        Log.d(LOG_TAG, "Credential file loaded.");

        // initialize the signature generator
        signatureGenerator = new SignatureGenerator(signingCredential);
        Log.d(LOG_TAG, "Signature generator initialized.");

        try { // get a reference to the AccessEnabler instance
            accessEnabler = AccessEnabler.Factory.getInstance(this);
        } catch (AccessEnablerException e) {
            Log.d(LOG_TAG, "Failed to initialize the AccessEnabler library. ");
        }
    }

    public static AccessEnabler getAccessEnablerInstance() { return AdobePassDemoApp.accessEnabler; }
    public static SignatureGenerator getSignatureGenerator() {return AdobePassDemoApp.signatureGenerator; }
}
