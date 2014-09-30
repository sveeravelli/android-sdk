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

import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class SigningCredential implements ICertificateInfo, IKeyInfo {
    private static final String LOG_TAG = "SigningCredential";

    protected KeyStore.PrivateKeyEntry mKeyEntry = null;

    public SigningCredential (InputStream inPKCSFile, String inPassword) {
        mKeyEntry = extractPrivateKeyEntry(inPKCSFile, inPassword);
    }

    private KeyStore.PrivateKeyEntry extractPrivateKeyEntry(InputStream inPKCSFile, String inPassword) {
        if (inPKCSFile == null)
            return null;

        try {
            KeyStore ks  = KeyStore.getInstance("PKCS12");
            Log.d(LOG_TAG, "KS provider : " + ks.getProvider());

            ks.load(inPKCSFile, inPassword.toCharArray());

            String keyAlias = null;
            Enumeration<String> aliases = ks.aliases();
            while(aliases.hasMoreElements()) {
                keyAlias = aliases.nextElement();
                if (ks.isKeyEntry(keyAlias))
                    break;
            }

            if (keyAlias != null) {
                KeyStore.PrivateKeyEntry keyEntry =
                    (KeyStore.PrivateKeyEntry) ks.getEntry
                        (keyAlias, new KeyStore.PasswordProtection(inPassword.toCharArray()));

                return keyEntry;
            }
        }
        catch(Exception e) {
            Log.e(LOG_TAG, e.getMessage());
        }


        return null;
    }

    public PrivateKey getPrivateKey() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getPrivateKey();
    }

    public Certificate getCertificate() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getCertificate();
    }

    public Certificate[] getCertificateChain() {
        if (mKeyEntry == null)
            return null;

        return mKeyEntry.getCertificateChain();

    }

    public boolean isValid() {
        return mKeyEntry != null;
    }
}
