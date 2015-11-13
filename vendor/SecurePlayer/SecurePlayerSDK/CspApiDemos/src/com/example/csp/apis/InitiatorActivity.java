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
import android.net.Uri;
import android.os.Bundle;

import com.discretix.drmdlc.api.DxDrmDlc;
import com.discretix.drmdlc.api.IDxDrmDlc;
import com.discretix.drmdlc.api.exceptions.DrmAndroidPermissionMissingException;
import com.discretix.drmdlc.api.exceptions.DrmClientInitFailureException;
import com.discretix.drmdlc.api.exceptions.DrmCommunicationFailureException;
import com.discretix.drmdlc.api.exceptions.DrmGeneralFailureException;
import com.discretix.drmdlc.api.exceptions.DrmServerSoapErrorException;
import com.example.csp.CspAsyncTaskBase;
import com.example.csp.CspConstants;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Represents the activity to perform the Initiator-Based Rights acquisition. This use case is
 * suitable for both local protected content and protected streams.
 */
public class InitiatorActivity extends Activity {

    /** Content Initiator url. */
    private String mInitiatorUrl = CspConstants.getActiveContent().getInitiatorUrl();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mInitiatorUrl = CspConstants.getActiveContent().getInitiatorUrl();
        new DrmExecuter().execute();
    }

    /**
     * Represents the asynchronous task to acquire content Rights.
     */
    private class DrmExecuter extends CspAsyncTaskBase {

        public DrmExecuter() {
            super(InitiatorActivity.this, "Executing Initiator");
        }

        @Override
        protected CspResult doInBackground(Void... arg0) {
            String userMessage;
            Boolean isPassed = false;

            if (mInitiatorUrl == null || mInitiatorUrl.equals("")) {
                userMessage = "No Initiator URL found.";
                return new CspResult(userMessage, false);
            }

            try {
                // Get the CSP singleton instance
                IDxDrmDlc cspApiSingleton = DxDrmDlc.getDxDrmDlc(InitiatorActivity.this, null);

                Uri initiatorUri = Uri.parse(mInitiatorUrl);

                // Import the rights in the initiator URL
                cspApiSingleton.setCookies(getCookiesArry());
                cspApiSingleton.executeInitiator(initiatorUri);
                cspApiSingleton.setCookies(null);
                userMessage = "License acquisition successful";
                isPassed = true;

            } catch (DrmClientInitFailureException e) {
                userMessage = "Exception: DrmClientInitFailureException";
            } catch (DrmGeneralFailureException e) {
                userMessage = "Exception: DrmGeneralFailureException";
            } catch (FileNotFoundException e) {
                userMessage = "Exception: FileNotFoundException";
            } catch (IOException e) {
                userMessage = "Exception: IOException";
            } catch (DrmCommunicationFailureException e) {
                userMessage = "Exception: DrmCommunicationFailureException";
            } catch (DrmServerSoapErrorException e) {
                String customData = e.getCustomData();
                String redirectUrl = e.getRedirectUrl();
                String soapError = e.getSoapMessage();
                userMessage = "Exception: DrmServerSoapErrorException: CustomData = " + customData
                        + " RedirectUrl = "
                        + redirectUrl + "\nSoap error:\n" + soapError;
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
         * Returns current {@link com.example.csp.CspContentItem content} cookies array.
         */
        private String[] getCookiesArry() {
            return CspConstants.getActiveContent().getCookiesArry();
        }
    }
}
