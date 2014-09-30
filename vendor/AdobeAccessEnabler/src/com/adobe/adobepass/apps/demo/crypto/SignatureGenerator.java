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
import com.adobe.adobepass.accessenabler.api.AccessEnablerException;

import javax.crypto.Cipher;
import java.security.Signature;

public class SignatureGenerator {
    private static final String LOG_TAG = "SignatureGenerator";

	protected IKeyInfo mSignatureKey = null;

	public SignatureGenerator (SigningCredential inCreds) {
		mSignatureKey = inCreds;
	}

	public String generateSignature(String inData) throws AccessEnablerException {
		try {
			Signature rsaSigner = Signature.getInstance(CryptoHelper.getSignatureAlgorithm());

            rsaSigner.initSign(mSignatureKey.getPrivateKey());

			rsaSigner.update(inData.getBytes());
			byte[] signature = rsaSigner.sign();

			return CryptoHelper.base64Encode(signature);
		}
		catch(Exception e) {
			Log.e(LOG_TAG, e.toString());
            throw new AccessEnablerException();
		}
	}

    public String decryptCiphertext(String inData) throws AccessEnablerException {
        try {
            byte[] encryptedBytes = Base64.decode(inData, Base64.DEFAULT);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, mSignatureKey.getPrivateKey());

            int blockSize = 256; // 2048 key
            byte[] decryptedText = new byte[0];

            //if the input fits in one buffer the process it in one step
            if(encryptedBytes.length <= blockSize) {
                decryptedText = cipher.doFinal(encryptedBytes);
            } else {
                byte[] scrambled;
                byte[] buffer = new byte[blockSize];

                for(int i=0; i< encryptedBytes.length; i++) {
                    if( (i > 0) && (i%blockSize == 0)){
                        scrambled = cipher.doFinal(buffer);
                        decryptedText = append(decryptedText, scrambled);

                        //calculate the next block size
                        int newBlockSize = blockSize;
                        if(i + blockSize > encryptedBytes.length) {
                            newBlockSize = encryptedBytes.length - i;
                        }
                        //clean buffer
                        buffer = new byte[newBlockSize];
                    }
                    buffer[i%blockSize] = encryptedBytes[i];
                }
                scrambled = cipher.doFinal(buffer);
                decryptedText = append(decryptedText, scrambled);
            }

            return new String(decryptedText, "UTF-8");
        } catch (Exception e) {
            Log.e(LOG_TAG, e.toString());
            throw new AccessEnablerException();
        }
    }

    private byte[] append(byte[] someBytes, byte[] moreBytes) {
        byte[] bytes = new byte[someBytes.length+moreBytes.length];
        System.arraycopy(someBytes, 0, bytes, 0, someBytes.length);
        System.arraycopy(moreBytes, 0, bytes, someBytes.length, moreBytes.length);
        return bytes;
    }

}
